// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import helium314.keyboard.keyboard.FleksyColors
import helium314.keyboard.latin.utils.BackButton
import helium314.keyboard.settings.dialogs.ColorPickerDialog

/** Couleurs façon Fleksy : teinte du clavier et teinte de la rangée du milieu, avec aperçu. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleksyColorsScreen(onClickBack: () -> Unit) {
    val ctx = LocalContext.current
    var base by remember { mutableStateOf(FleksyColors.baseColor(ctx)) }
    var middle by remember { mutableStateOf(FleksyColors.middleColorOverride(ctx)) }
    var pickBase by remember { mutableStateOf(false) }
    var pickMiddle by remember { mutableStateOf(false) }

    fun setBase(color: Int?) { base = color; FleksyColors.setBaseColor(ctx, color) }
    fun setMiddle(color: Int?) { middle = color; FleksyColors.setMiddleColor(ctx, color) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(title = { Text("Couleurs du clavier") }, navigationIcon = { BackButton(onClickBack) })
        }
    ) { innerPadding ->
        Column(
            Modifier.padding(innerPadding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
        ) {
            // --- aperçu ---
            val b = base
            if (b != null) {
                val rows = intArrayOf(b, middle ?: FleksyColors.automaticMiddle(b), FleksyColors.rowColors(ctx)?.get(2) ?: b)
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))) {
                    Text(
                        "1  2  3  4  5  6  7  8  9  0",
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().background(Color(b)).padding(vertical = 10.dp),
                    )
                    listOf("q w e r t z u i o p", "a s d f g h j k l", "⇧  y x c v b n m  ⌫").forEachIndexed { i, letters ->
                        Text(
                            letters,
                            color = Color.White,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().background(Color(rows[i])).padding(vertical = 12.dp),
                        )
                    }
                }
            } else {
                Text(
                    "Le clavier utilise actuellement les couleurs du thème choisi dans Apparence. " +
                        "Choisissez une teinte ci-dessous : tout le clavier (barre d'outils comprise) prend " +
                        "cette couleur unie, et seule la rangée du milieu peut avoir sa propre teinte.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            // --- teinte du clavier ---
            Spacer(Modifier.height(24.dp))
            Text("Teinte du clavier", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FleksyColors.PRESETS.forEach { (name, color) ->
                    ColorChoice(name, color, selected = base == color) { setBase(color) }
                }
                ColorChoice("Autre…", null, selected = b != null && FleksyColors.PRESETS.none { it.second == b }) { pickBase = true }
            }

            // --- rangée du milieu ---
            Spacer(Modifier.height(24.dp))
            Text("Rangée du milieu", style = MaterialTheme.typography.titleMedium)
            Text(
                if (middle == null) "Automatique : un peu plus claire que la teinte du clavier" else "Teinte personnalisée",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(enabled = b != null, onClick = { pickMiddle = true }) { Text("Choisir une teinte") }
                OutlinedButton(enabled = middle != null, onClick = { setMiddle(null) }) { Text("Automatique") }
            }

            // --- retour au thème ---
            Spacer(Modifier.height(24.dp))
            OutlinedButton(enabled = b != null, onClick = { setBase(null); setMiddle(null) }) {
                Text("Revenir aux couleurs du thème")
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Les nouvelles couleurs s'appliquent la prochaine fois que le clavier s'affiche. " +
                    "Pour la barre du haut et la couleur du texte, utilisez Apparence → Couleurs.",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(32.dp))
        }
    }

    if (pickBase) {
        ColorPickerDialog(
            onDismissRequest = { pickBase = false },
            initialColor = base ?: FleksyColors.PRESETS.first().second,
            title = "Teinte du clavier",
            showDefault = true,
            onDefault = { setBase(null); setMiddle(null) },
            onConfirmed = { setBase(it) },
        )
    }
    if (pickMiddle) {
        ColorPickerDialog(
            onDismissRequest = { pickMiddle = false },
            initialColor = middle ?: base?.let { FleksyColors.automaticMiddle(it) } ?: FleksyColors.PRESETS.first().second,
            title = "Rangée du milieu",
            showDefault = true,
            onDefault = { setMiddle(null) },
            onConfirmed = { setMiddle(it) },
        )
    }
}

@Composable
private fun ColorChoice(name: String, color: Int?, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp).clickable(onClick = onClick),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (color != null) Color(color) else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = CircleShape,
                ),
        ) {
            if (color == null) Text("🎨", fontSize = 18.sp)
            else if (selected) Text("✓", color = Color.White, fontSize = 18.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(name, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
    }
}
