package pl.kacperikapi.mathadventure.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import pl.kacperikapi.mathadventure.data.AppLanguages

@Composable
fun LanguagePickerDialog(
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🌍 Language / Język", fontWeight = FontWeight.Black) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                AppLanguages.supported.forEach { option ->
                    TextButton(
                        onClick = { onSelect(option.tag) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${option.countryFlag} ${option.nativeName}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {}
    )
}
