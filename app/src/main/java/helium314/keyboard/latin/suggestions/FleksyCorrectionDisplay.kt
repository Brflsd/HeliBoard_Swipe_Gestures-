// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.suggestions

/**
 * Affichage façon Fleksy de la correction en cours dans la barre de suggestions :
 * à gauche ce que donnera un glissement vers le haut (ou le livre ouvert : ajout au dictionnaire),
 * au centre le mot actuel, à droite ce que donnera un glissement vers le bas.
 */
object FleksyCorrectionDisplay {
    data class State(
        /** mot obtenu en glissant vers le haut, ou null */
        val previous: String?,
        /** true : glisser vers le haut ajoutera le mot actuel au dictionnaire personnel (livre ouvert) */
        val upAddsToDictionary: Boolean,
        /** true : le mot actuel vient d'être ajouté au dictionnaire */
        val justAdded: Boolean,
        val current: String,
        /** mot obtenu en glissant vers le bas, ou null */
        val next: String?,
    )

    var state: State? = null
        private set

    /** appelé quand l'affichage doit être redessiné (enregistré par la barre de suggestions) */
    var onChange: (() -> Unit)? = null
    /** actions des mots de gauche et de droite quand on les touche (enregistrées par les gestes) */
    var onTapPrevious: (() -> Unit)? = null
    var onTapNext: (() -> Unit)? = null

    fun show(newState: State) {
        state = newState
        onChange?.invoke()
    }

    /** le cycle de correction est terminé : la barre reprendra les suggestions normales à la prochaine mise à jour */
    fun clear() {
        state = null
    }
}
