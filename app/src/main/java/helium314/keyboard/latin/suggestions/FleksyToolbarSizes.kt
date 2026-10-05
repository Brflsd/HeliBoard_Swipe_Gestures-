// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.suggestions

import android.content.Context
import androidx.core.content.edit
import helium314.keyboard.keyboard.KeyboardSwitcher
import helium314.keyboard.latin.utils.prefs

/** Taille de la barre d'outils et du texte des suggestions (en pourcentage de la taille d'origine). */
object FleksyToolbarSizes {
    const val PREF_HEIGHT_SCALE = "fleksy_toolbar_height_scale"
    const val PREF_TEXT_SCALE = "fleksy_suggestion_text_scale"
    const val MIN_SCALE = 0.7f
    const val MAX_SCALE = 1.8f

    @JvmStatic
    fun heightScale(context: Context) = context.prefs().getFloat(PREF_HEIGHT_SCALE, 1f).coerceIn(MIN_SCALE, MAX_SCALE)

    @JvmStatic
    fun textScale(context: Context) = context.prefs().getFloat(PREF_TEXT_SCALE, 1f).coerceIn(MIN_SCALE, MAX_SCALE)

    fun setHeightScale(context: Context, scale: Float) = save(context, PREF_HEIGHT_SCALE, scale)
    fun setTextScale(context: Context, scale: Float) = save(context, PREF_TEXT_SCALE, scale)

    private fun save(context: Context, key: String, scale: Float) {
        context.prefs().edit { putFloat(key, scale.coerceIn(MIN_SCALE, MAX_SCALE)) }
        // la vue du clavier est recréée à la prochaine ouverture avec les nouvelles tailles
        runCatching { KeyboardSwitcher.getInstance().setThemeNeedsReload() }
    }
}
