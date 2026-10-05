// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.keyboard

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import androidx.core.content.edit
import androidx.core.graphics.ColorUtils
import helium314.keyboard.latin.common.ColorType
import helium314.keyboard.latin.common.Colors
import helium314.keyboard.latin.utils.prefs

/**
 * Couleurs façon Fleksy : tout le clavier (barre d'outils, fond, rangées) prend une teinte unie,
 * seule la rangée du milieu a sa propre teinte ; pas de fond individuel sur les touches.
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

    fun setBaseColor(context: Context, color: Int?) {
        context.prefs().edit { if (color == null) remove(PREF_BASE_COLOR) else putInt(PREF_BASE_COLOR, color) }
        runCatching { KeyboardSwitcher.getInstance().setThemeNeedsReload() }
    }

    fun setMiddleColor(context: Context, color: Int?) {
        context.prefs().edit { if (color == null) remove(PREF_MIDDLE_COLOR) else putInt(PREF_MIDDLE_COLOR, color) }
        runCatching { KeyboardSwitcher.getInstance().setThemeNeedsReload() }
    }

    fun automaticMiddle(base: Int) = ColorUtils.blendARGB(base, Color.WHITE, 0.07f)

    /** [haut, milieu, bas] ou null si le style Fleksy n'est pas activé (couleurs du thème). */
    @JvmStatic
    fun rowColors(context: Context): IntArray? {
        val base = baseColor(context) ?: return null
        val middle = middleColorOverride(context) ?: automaticMiddle(base)
        return intArrayOf(base, middle, base)
    }

    /** Remplace les couleurs de fond du thème (barre d'outils, fond, barre de navigation) par la teinte unie. */
    @JvmStatic
    fun wrap(context: Context, themeColors: Colors): Colors {
        val base = baseColor(context) ?: return themeColors
        return UniformColors(themeColors, base)
    }

    private class UniformColors(private val theme: Colors, private val base: Int) : Colors by theme {
        private val pressed = ColorUtils.blendARGB(base, Color.WHITE, 0.15f)

        override fun get(color: ColorType): Int = when (color) {
            ColorType.MAIN_BACKGROUND, ColorType.STRIP_BACKGROUND, ColorType.NAVIGATION_BAR,
            ColorType.MORE_SUGGESTIONS_BACKGROUND, ColorType.CLIPBOARD_SUGGESTION_BACKGROUND -> base
            else -> theme.get(color)
        }

        override fun setBackground(view: View, color: ColorType) {
            when (color) {
                ColorType.MAIN_BACKGROUND -> view.background = ColorDrawable(base)
                ColorType.STRIP_BACKGROUND -> view.background = StateListDrawable().apply {
                    addState(intArrayOf(android.R.attr.state_pressed), ColorDrawable(pressed))
                    addState(intArrayOf(), ColorDrawable(base))
                }
                else -> theme.setBackground(view, color)
            }
        }
    }
}
