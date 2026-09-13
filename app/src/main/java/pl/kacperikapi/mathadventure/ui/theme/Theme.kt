package pl.kacperikapi.mathadventure.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AdventureColors = lightColorScheme(
    primary = AdventureGreen,
    onPrimary = Color.White,
    secondary = StarYellow,
    onSecondary = Ink,
    tertiary = SkyBlue,
    background = Cream,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    error = HeartRed
)

@Composable
fun KacperKapiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AdventureColors,
        content = content
    )
}
