// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.suggestions

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import helium314.keyboard.keyboard.KeyboardSwitcher
import helium314.keyboard.latin.R
import helium314.keyboard.latin.RichInputMethodManager
import helium314.keyboard.latin.common.ColorType
import helium314.keyboard.latin.common.Colors
import helium314.keyboard.latin.common.Constants
import helium314.keyboard.settings.SettingsDestination
import helium314.keyboard.latin.settings.SettingsSubtype.Companion.toSettingsSubtype
import helium314.keyboard.latin.utils.FleksyShortcuts
import helium314.keyboard.latin.utils.LayoutType
import helium314.keyboard.latin.utils.SubtypeSettings
import helium314.keyboard.latin.utils.SubtypeUtilsAdditional
import helium314.keyboard.latin.utils.prefs

/** Pages de la barre d'outils, parcourues en glissant horizontalement sur la barre (façon Fleksy). */
enum class FleksyToolbarPage { SUGGESTIONS, EDIT, NUMBERS, SHORTCUTS, LAYOUTS }

/** Construit le contenu des pages « Chiffres », « Raccourcis » et « Disposition » et exécute leurs actions. */
class FleksyToolbarPages(
    private val context: Context,
    private val container: ViewGroup,
    private val colors: Colors,
    private val listener: () -> SuggestionStripView.Listener,
    private val haptic: () -> Unit,
    private val selectedBackground: () -> Drawable?,
) {
    fun show(page: FleksyToolbarPage) {
        container.removeAllViews()
        when (page) {
            FleksyToolbarPage.NUMBERS -> showNumbers()
            FleksyToolbarPage.SHORTCUTS -> showShortcuts()
            FleksyToolbarPage.LAYOUTS -> showLayouts()
            else -> {}
        }
    }

    // --- Chiffres ---
    private fun showNumbers() {
        "1234567890".forEach { digit ->
            container.addView(button(digit.toString(), weight = 1f) {
                listener().onCodeInput(digit.code, Constants.SUGGESTION_STRIP_COORDINATE, Constants.SUGGESTION_STRIP_COORDINATE, false)
            })
        }
    }

    // --- Raccourcis texte ---
    private fun showShortcuts() {
        val shortcuts = FleksyShortcuts.load(context)
        if (shortcuts.isEmpty()) {
            container.addView(button("＋ Ajouter un raccourci (adresse e-mail…)", weight = 1f) {
                listener().openSettingsAt(SettingsDestination.FleksyShortcuts)
            })
            return
        }
        val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        shortcuts.forEach { shortcut ->
            row.addView(button(shortcut.buttonLabel(), minWidthDp = 48) { listener().onTextInput(shortcut.text) })
        }
        val scroll = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            addView(row, ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
        container.addView(scroll, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
        container.addView(button("✎", minWidthDp = 44) { listener().openSettingsAt(SettingsDestination.FleksyShortcuts) })
    }

    // --- Disposition (QWERTY / QWERTZ / AZERTY) ---
    private fun showLayouts() {
        val current = currentMainLayout()?.removeSuffix("+")
        LAYOUTS.forEach { (name, label) ->
            val b = button(label, weight = 1f) {
                if (switchMainLayout(name)) show(FleksyToolbarPage.LAYOUTS)
            }
            if (name == current) b.background = selectedBackground()
            container.addView(b)
        }
        container.addView(button("⚙", minWidthDp = 44) {
            val subtype = runCatching { RichInputMethodManager.getInstance().currentSubtype.rawSubtype.toSettingsSubtype() }.getOrNull()
            listener().openSettingsAt(
                if (subtype != null) SettingsDestination.Subtype + subtype.toPref() else SettingsDestination.Languages
            )
        })
    }

    private fun currentMainLayout(): String? = runCatching {
        RichInputMethodManager.getInstance().currentSubtype.rawSubtype.toSettingsSubtype().mainLayoutName()
    }.getOrNull()

    /** Change la disposition principale de la langue en cours, comme le ferait l'écran de réglages. */
    private fun switchMainLayout(layout: String): Boolean {
        val raw = runCatching { RichInputMethodManager.getInstance().currentSubtype.rawSubtype }.getOrNull() ?: return false
        val from = raw.toSettingsSubtype()
        if (from.mainLayoutName()?.removeSuffix("+") == layout) return false
        val to = from.withLayout(LayoutType.MAIN, layout)
        SubtypeUtilsAdditional.changeAdditionalSubtype(from, to, context)
        val newSubtype = to.toEnabledSubtype() ?: to.toAdditionalSubtype().also {
            // la langue n'était activée qu'implicitement (langue du système) : on active la nouvelle variante
            SubtypeSettings.addEnabledSubtype(context.prefs(), it)
        }
        KeyboardSwitcher.getInstance().switchToSubtype(newSubtype)
        return true
    }

    private fun button(text: String, weight: Float = 0f, minWidthDp: Int = 0, onClick: () -> Unit): TextView {
        val view = TextView(context, null, R.attr.suggestionWordStyle)
        view.text = text
        view.gravity = Gravity.CENTER
        view.maxLines = 1
        view.setTextColor(colors.get(ColorType.SUGGESTED_WORD))
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (text.length <= 2) 20f else 15f)
        colors.setBackground(view, ColorType.STRIP_BACKGROUND)
        val density = context.resources.displayMetrics.density
        view.minWidth = (minWidthDp * density).toInt()
        view.setPadding((8 * density).toInt(), 0, (8 * density).toInt(), 0)
        view.layoutParams = LinearLayout.LayoutParams(
            if (weight > 0f) 0 else ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            weight,
        )
        view.setOnClickListener {
            haptic()
            onClick()
        }
        view.isSoundEffectsEnabled = false
        view.isHapticFeedbackEnabled = false
        view.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        return view
    }

    companion object {
        private val LAYOUTS = listOf("qwerty" to "QWERTY", "qwertz" to "QWERTZ", "azerty" to "AZERTY")
    }
}
