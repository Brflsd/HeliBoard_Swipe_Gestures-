// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import helium314.keyboard.keyboard.FleksyColors
import helium314.keyboard.latin.suggestions.FleksyToolbarSizes
import helium314.keyboard.latin.utils.BackButton
import kotlin.math.roundToInt

/** Taille de la barre d'outils et du texte des suggestions, avec aperçu. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleksyToolbarScreen(onClickBack: () -> Unit) {
    val ctx = LocalContext.current
    var height by remember { mutableFloatStateOf(FleksyToolbarSizes.heightScale(ctx)) }
    var text by remember { mutableFloatStateOf(FleksyToolbarSizes.textScale(ctx)) }
    val barColor = Color(FleksyColors.baseColor(ctx) ?: 0xFF2B2B2B.toInt())

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { TopAppBar(title = { Text("Barre d'outils") }, navigationIcon = { BackButton(onClickBack) }) }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            // aperçu : la barre des chiffres
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(barColor).height((40 * height).dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                "1234567890".forEach { Text(it.toString(), color = Color.White, fontSize = (20 * text).sp) }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(barColor).height((40 * height).dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("salur", color = Color.LightGray, fontSize = (16 * text).sp)
                Text("salut", color = Color.White, fontSize = (16 * text).sp)
                Text("salir", color = Color.LightGray, fontSize = (16 * text).sp)
            }

            Spacer(Modifier.height(24.dp))
            Text("Hauteur de la barre : ${(height * 100).roundToInt()} %", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = height,
                onValueChange = { height = it },
                onValueChangeFinished = { FleksyToolbarSizes.setHeightScale(ctx, height) },
                valueRange = FleksyToolbarSizes.MIN_SCALE..FleksyToolbarSizes.MAX_SCALE,
            )

            Spacer(Modifier.height(16.dp))
            Text("Taille du texte : ${(text * 100).roundToInt()} %", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = text,
                onValueChange = { text = it },
                onValueChangeFinished = { FleksyToolbarSizes.setTextScale(ctx, text) },
                valueRange = FleksyToolbarSizes.MIN_SCALE..FleksyToolbarSizes.MAX_SCALE,
            )

            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = {
                height = 1f; text = 1f
                FleksyToolbarSizes.setHeightScale(ctx, 1f)
                FleksyToolbarSizes.setTextScale(ctx, 1f)
            }) { Text("Tailles d'origine") }
            Spacer(Modifier.height(8.dp))
            Text(
                "Les nouvelles tailles s'appliquent la prochaine fois que le clavier s'affiche.",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}
