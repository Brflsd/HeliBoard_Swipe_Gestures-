// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.utils

import android.content.Context
import android.util.Patterns
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

/** Un raccourci texte de la barre d'outils : un bouton qui insère un texte (adresse e-mail, téléphone…). */
data class TextShortcut(val label: String, val text: String) {

    /** Ce qui s'affiche sur le bouton : le libellé choisi, sinon une icône selon le type de texte. */
    fun buttonLabel(): String {
        if (label.isNotBlank()) return label.trim()
        val t = text.trim()
        return when {
            Patterns.EMAIL_ADDRESS.matcher(t).matches() -> "✉"
            Patterns.PHONE.matcher(t).matches() && t.count { it.isDigit() } >= 6 -> "☎"
            Patterns.WEB_URL.matcher(t).matches() -> "🌐"
            t.length <= 8 -> t
            else -> t.take(7) + "…"
        }
    }
}

/** Enregistrement des raccourcis texte dans les préférences (format JSON). */
object FleksyShortcuts {
    const val PREF_TEXT_SHORTCUTS = "fleksy_text_shortcuts"

    fun load(context: Context): List<TextShortcut> {
        val json = context.prefs().getString(PREF_TEXT_SHORTCUTS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { i ->
                val o = array.optJSONObject(i) ?: return@mapNotNull null
                val text = o.optString("text")
                if (text.isEmpty()) null else TextShortcut(o.optString("label"), text)
            }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, shortcuts: List<TextShortcut>) {
        val array = JSONArray()
        shortcuts.forEach { array.put(JSONObject().put("label", it.label).put("text", it.text)) }
        context.prefs().edit { putString(PREF_TEXT_SHORTCUTS, array.toString()) }
    }
}
