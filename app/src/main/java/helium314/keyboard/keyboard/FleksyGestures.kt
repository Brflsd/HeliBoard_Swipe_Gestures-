// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.keyboard

import android.content.Context
import androidx.core.content.edit
import helium314.keyboard.keyboard.internal.keyboard_parser.floris.KeyCode
import helium314.keyboard.latin.SuggestedWords
import helium314.keyboard.latin.common.Constants
import helium314.keyboard.latin.inputlogic.InputLogic
import helium314.keyboard.latin.utils.prefs
import java.text.BreakIterator

/**
 * Gestes façon Fleksy, déclenchés par un glissement qui part d'une touche de caractère :
 *  - droite : espace (deux fois de suite : point, grâce au « double espace = point » de HeliBoard)
 *  - gauche : effacer le mot précédent
 *  - bas / haut : suggestion suivante / précédente pour le dernier mot
 *  - deux pouces vers le bas / le haut : cacher / afficher la rangée de la barre d'espace
 */
class FleksyGestures(
    private val inputLogic: InputLogic,
    /** envoie un code de touche comme si elle avait été tapée (passe par toute la logique normale) */
    private val sendCode: (Int) -> Unit,
    private val haptic: () -> Unit,
    /** recharge le clavier après un changement de disposition */
    private val reloadKeyboard: () -> Unit,
    private val context: Context,
) {
    private val connection get() = inputLogic.mConnection

    // --- état du cycle de suggestions ---
    /** mots proposés pour le dernier mot tapé (le mot tapé lui-même en fait partie) */
    private var candidates: List<String> = emptyList()
    private var candidateIndex = 0
    /** texte exact que le cycle a laissé juste avant le curseur (mot, éventuellement suivi d'un espace) */
    private var insertedText: String? = null

    fun onSwipe(direction: Int) {
        when (direction) {
            KeyboardActionListener.FLEKSY_SWIPE_RIGHT -> sendCode(Constants.CODE_SPACE)
            KeyboardActionListener.FLEKSY_SWIPE_LEFT -> deleteWord()
            KeyboardActionListener.FLEKSY_SWIPE_DOWN -> cycleSuggestion(+1)
            KeyboardActionListener.FLEKSY_SWIPE_UP -> cycleSuggestion(-1)
            else -> return
        }
        haptic()
    }

    fun onTwoFingerSwipe(direction: Int) {
        val hide = direction == KeyboardActionListener.FLEKSY_SWIPE_DOWN
        if (isSpaceRowHidden(context) == hide) return
        setSpaceRowHidden(context, hide)
        haptic()
        reloadKeyboard()
    }

    // ------------------------------------------------------------------
    // Espace : on mémorise les suggestions du mot qui vient d'être validé
    // ------------------------------------------------------------------

    /** À appeler juste avant le traitement de chaque touche. */
    fun beforeCodeInput(code: Int): PendingCommit? {
        resetCycle()
        if (code != Constants.CODE_SPACE || !inputLogic.isComposingWordForFleksy) return null
        val typed = inputLogic.typedWordForFleksy
        if (typed.isEmpty()) return null
        return PendingCommit(typed, collectCandidates(typed))
    }

    /** À appeler juste après le traitement d'un espace qui a validé un mot. */
    fun afterCodeInput(pending: PendingCommit?) {
        if (pending == null) return
        // après la validation, la liste des suggestions est souvent plus à jour : on la préfère si elle correspond
        val fresh = collectCandidates(pending.typed)
        val list = if (fresh.size > 1) fresh else pending.candidates
        if (list.size < 2) return
        val maxLength = list.maxOf { it.length }
        val before = connection.getTextBeforeCursor(maxLength + 2, 0)?.toString() ?: return
        if (!before.endsWith(" ")) return
        val body = before.dropLast(1)
        // le mot réellement inséré (mot tapé ou correction automatique)
        val committed = list.filter { endsWithWord(body, it) }.maxByOrNull { it.length } ?: return
        candidates = list
        candidateIndex = list.indexOf(committed)
        insertedText = "$committed "
    }

    class PendingCommit(val typed: String, val candidates: List<String>)

    // ------------------------------------------------------------------
    // Haut / bas : faire défiler les suggestions
    // ------------------------------------------------------------------

    private fun cycleSuggestion(step: Int) {
        if (connection.hasSelection()) return
        var current = insertedText
        if (current != null && connection.getTextBeforeCursor(current.length, 0)?.toString() != current) {
            // le texte a changé depuis (curseur déplacé, autre saisie…) : le cycle n'est plus valable
            resetCycle()
            current = null
        }
        if (current == null) {
            // pas de mot validé récemment : on travaille sur le mot en cours de saisie
            if (!inputLogic.isComposingWordForFleksy) return
            val typed = inputLogic.typedWordForFleksy
            val list = collectCandidates(typed)
            if (list.size < 2) return
            inputLogic.finishInput() // fige le mot tel qu'il a été tapé
            if (connection.getTextBeforeCursor(typed.length, 0)?.toString() != typed) return
            candidates = list
            candidateIndex = list.indexOf(typed).coerceAtLeast(0)
            current = typed
        }
        if (candidates.size < 2) return

        candidateIndex = Math.floorMod(candidateIndex + step, candidates.size)
        val word = candidates[candidateIndex]
        val replacement = if (current.endsWith(" ")) "$word " else word
        inputLogic.finishInput()
        connection.beginBatchEdit()
        connection.deleteTextBeforeCursor(current.length)
        connection.commitText(replacement, 1)
        connection.endBatchEdit()
        insertedText = replacement
    }

    /** Liste des mots à proposer pour le mot tapé, dans l'ordre des suggestions de HeliBoard. */
    private fun collectCandidates(typed: String): List<String> {
        val suggestions = inputLogic.mSuggestedWords
        if (suggestions.isPunctuationSuggestions || suggestions.mTypedWordInfo?.mWord != typed)
            return listOf(typed) // suggestions périmées ou absentes
        val words = LinkedHashSet<String>()
        for (i in 0 until suggestions.size()) {
            val info = suggestions.getInfo(i)
            if (info.isKindOf(SuggestedWords.SuggestedWordInfo.KIND_PREDICTION)) continue
            if (info.mWord.isNotEmpty()) words.add(info.mWord)
            if (words.size >= MAX_CANDIDATES) break
        }
        if (typed !in words) return listOf(typed) + words.take(MAX_CANDIDATES - 1)
        return words.toList()
    }

    private fun resetCycle() {
        candidates = emptyList()
        candidateIndex = 0
        insertedText = null
    }

    // ------------------------------------------------------------------
    // Gauche : effacer le mot précédent
    // ------------------------------------------------------------------

    private fun deleteWord() {
        resetCycle()
        if (!connection.hasSelection()) {
            inputLogic.finishInput() // le mot en cours de saisie devient du texte normal, puis on l'efface
            val before = connection.getTextBeforeCursor(MAX_WORD_LOOKBACK, 0)?.toString() ?: return
            val count = charsToDelete(before)
            if (count <= 0) return
            val end = connection.expectedSelectionEnd
            val start = end - count
            if (start < 0) return
            // on sélectionne puis on efface : HeliBoard met ainsi à jour majuscules, suggestions et annulation
            connection.setSelection(start, end)
        }
        sendCode(KeyCode.DELETE)
    }

    companion object {
        private const val MAX_CANDIDATES = 8
        private const val MAX_WORD_LOOKBACK = 100
        private const val PREF_HIDE_SPACE_ROW = "fleksy_hide_space_row"

        @Volatile
        private var spaceRowHiddenCache: Boolean? = null

        /** true si la rangée de la barre d'espace est cachée (lu par le constructeur de clavier). */
        @JvmStatic
        fun isSpaceRowHidden(context: Context): Boolean =
            spaceRowHiddenCache ?: context.prefs().getBoolean(PREF_HIDE_SPACE_ROW, false).also { spaceRowHiddenCache = it }

        private fun setSpaceRowHidden(context: Context, hidden: Boolean) {
            spaceRowHiddenCache = hidden
            context.prefs().edit { putBoolean(PREF_HIDE_SPACE_ROW, hidden) }
        }

        /** Nombre de caractères à effacer : espaces de fin + mot ; sinon un seul symbole (ponctuation, emoji). */
        internal fun charsToDelete(text: String): Int {
            if (text.isEmpty()) return 0
            if (text.last() == '\n') return 1 // un retour à la ligne s'efface seul
            var i = text.length
            while (i > 0 && text[i - 1].isWhitespace() && text[i - 1] != '\n') i--
            val wordEnd = i
            while (i > 0) {
                val cp = text.codePointBefore(i)
                if (!isWordCodePoint(cp)) break
                i -= Character.charCount(cp)
            }
            if (i == wordEnd && i > 0) {
                // pas de lettre avant le curseur : on efface un seul caractère visible (emoji compris)
                val iterator = BreakIterator.getCharacterInstance()
                iterator.setText(text.substring(0, i))
                val previous = iterator.preceding(i)
                i = if (previous == BreakIterator.DONE) 0 else previous
            }
            return text.length - i
        }

        private fun isWordCodePoint(cp: Int): Boolean =
            Character.isLetterOrDigit(cp) || cp == '\''.code || cp == '’'.code
                || Character.getType(cp) == Character.NON_SPACING_MARK.toInt()

        /** true si [text] se termine par [word] précédé d'un début de texte ou d'un caractère hors mot. */
        private fun endsWithWord(text: String, word: String): Boolean {
            if (!text.endsWith(word)) return false
            val start = text.length - word.length
            return start == 0 || !isWordCodePoint(text.codePointBefore(start))
        }
    }
}
