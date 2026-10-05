// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import helium314.keyboard.latin.utils.BackButton
import helium314.keyboard.latin.utils.FleksyShortcuts
import helium314.keyboard.latin.utils.TextShortcut

/** Écran de réglage des raccourcis texte affichés dans la page « Raccourcis » de la barre d'outils. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleksyShortcutsScreen(onClickBack: () -> Unit) {
    val ctx = LocalContext.current
    var shortcuts by remember { mutableStateOf(FleksyShortcuts.load(ctx)) }
    var newLabel by remember { mutableStateOf("") }
    var newText by remember { mutableStateOf("") }

    fun update(list: List<TextShortcut>) {
        shortcuts = list
        FleksyShortcuts.save(ctx, list)
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Raccourcis texte") },
                navigationIcon = { BackButton(onClickBack) },
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "Ces textes s'insèrent d'une touche depuis la barre d'outils du clavier : " +
                    "glissez horizontalement sur la barre jusqu'à la page des raccourcis.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(16.dp))

            if (shortcuts.isEmpty()) {
                Text("Aucun raccourci pour l'instant.", style = MaterialTheme.typography.bodyMedium)
            }
            shortcuts.forEachIndexed { index, shortcut ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(shortcut.buttonLabel(), style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(56.dp))
                    Text(shortcut.text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    if (index > 0)
                        TextButton(onClick = {
                            update(shortcuts.toMutableList().apply { add(index - 1, removeAt(index)) })
                        }) { Text("↑") }
                    TextButton(onClick = {
                        update(shortcuts.toMutableList().apply { removeAt(index) })
                    }) { Text("Supprimer") }
                }
                HorizontalDivider()
            }

            Spacer(Modifier.height(24.dp))
            Text("Ajouter un raccourci", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = newText,
                onValueChange = { newText = it },
                label = { Text("Texte à insérer (ex. votre adresse e-mail)") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = newLabel,
                onValueChange = { newLabel = it },
                label = { Text("Libellé du bouton (facultatif)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "Sans libellé : ✉ pour une adresse e-mail, ☎ pour un numéro de téléphone, 🌐 pour un lien.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    enabled = newText.isNotBlank(),
                    onClick = {
                        update(shortcuts + TextShortcut(newLabel.trim(), newText))
                        newLabel = ""
                        newText = ""
                    }
                ) { Text("Ajouter") }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
