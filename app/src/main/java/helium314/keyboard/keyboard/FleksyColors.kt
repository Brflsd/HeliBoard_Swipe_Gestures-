// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.keyboard

import android.content.Context
import android.graphics.Color
import androidx.core.content.edit
import androidx.core.graphics.ColorUtils
import helium314.keyboard.latin.utils.prefs

/**
 * Couleurs façon Fleksy : le clavier de lettres est dessiné en bandes horizontales
 * (une teinte de base, une rangée du milieu un peu plus claire, une rangée du bas plus sombre),
 * sans fond individuel sur les touches.
 */
object FleksyColors {
    const val PREF_BASE_COLOR = "fleksy_base_color"
    const val PREF_MIDDLE_COLOR = "fleksy_middle_color"

    /** Teintes proposées (toutes sombres, pour garder le texte blanc lisible). */
    val PRESETS: List<Pair<String, Int>> = listOf(
        "Fleksy" to 0xFF2B2B2B.toInt(),
        "Ardoise" to 0xFF263238.toInt(),
        "Bleu nuit" to 0xFF1B2A41.toInt(),
        "Forêt" to 0xFF1E3326.toInt(),
        "Bordeaux" to 0xFF3B1C26.toInt(),
        "Prune" to 0xFF2E2140.toInt(),
        "Noir" to 0xFF121212.toInt(),
    )

    fun baseColor(context: Context): Int? {
        val prefs = context.prefs()
        return if (prefs.contains(PREF_BASE_COLOR)) prefs.getInt(PREF_BASE_COLOR, 0) else null
    }

    fun middleColorOverride(context: Context): Int? {
        val prefs = context.prefs()
        return if (prefs.contains(PREF_MIDDLE_COLOR)) prefs.getInt(PREF_MIDDLE_COLOR, 0) else null
    }

    fun setBaseColor(context: Context, color: Int?) = context.prefs().edit {
        if (color == null) remove(PREF_BASE_COLOR) else putInt(PREF_BASE_COLOR, color)
    }

    fun setMiddleColor(context: Context, color: Int?) = context.prefs().edit {
        if (color == null) remove(PREF_MIDDLE_COLOR) else putInt(PREF_MIDDLE_COLOR, color)
    }

    fun automaticMiddle(base: Int) = ColorUtils.blendARGB(base, Color.WHITE, 0.07f)

    /** [haut, milieu, bas] ou null si le style en bandes n'est pas activé (couleurs du thème). */
    @JvmStatic
    fun rowColors(context: Context): IntArray? {
        val base = baseColor(context) ?: return null
        val middle = middleColorOverride(context) ?: automaticMiddle(base)
        val bottom = ColorUtils.blendARGB(base, Color.BLACK, 0.18f)
        return intArrayOf(base, middle, bottom)
    }
}
