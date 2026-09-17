package pl.kacperikapi.mathadventure.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.kacperikapi.mathadventure.data.AppLanguages
import pl.kacperikapi.mathadventure.ui.theme.*

/**
 * Shown only during the first-run onboarding, before creating/selecting a player.
 * The phone language is used as the initial suggestion, but the player confirms
 * one of the languages supported by the game.
 */
@Composable
fun LanguageSelectScreen(
    suggestedLanguage: String,
    onSelect: (String) -> Unit
) {
    val suggested = AppLanguages.normalize(suggestedLanguage)

    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyBlue.copy(.30f), Cream, Parchment)))
            .statusBarsPadding()
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(18.dp))
            Text("🌍🐾", fontSize = 64.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                onboardingTitle(suggested),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = AdventureGreen,
                textAlign = TextAlign.Center
            )
            Text(
                onboardingSubtitle(suggested),
                modifier = Modifier.padding(top = 7.dp),
                color = WoodBrown,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(22.dp))

            Column(
                Modifier.fillMaxWidth().widthIn(max = 650.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppLanguages.supported.forEach { language ->
                    val isSuggested = language.tag == suggested
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(language.tag) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSuggested) BrightGreen.copy(alpha = .17f) else androidx.compose.ui.graphics.Color.White.copy(alpha = .94f),
                        shadowElevation = 3.dp
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(language.countryFlag, fontSize = 32.sp)
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(language.nativeName, fontSize = 21.sp, fontWeight = FontWeight.Black, color = Ink)
                                if (isSuggested) {
                                    Text(
                                        suggestedLabel(suggested),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AdventureGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text("›", fontSize = 28.sp, color = AdventureGreen)
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                onboardingFooter(suggested),
                color = WoodBrown,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(18.dp))
        }
    }
}

private fun onboardingTitle(language: String): String = when (AppLanguages.normalize(language)) {
    "pl" -> "Wybierz język gry"
    "de" -> "Wähle die Sprache"
    "es" -> "Elige el idioma"
    "it" -> "Scegli la lingua"
    "sk" -> "Vyber jazyk"
    else -> "Choose game language"
}

private fun onboardingSubtitle(language: String): String = when (AppLanguages.normalize(language)) {
    "pl" -> "Najpierw wybierz język. Następnie wpiszesz imię gracza albo wybierzesz zapisany profil."
    "de" -> "Wähle zuerst die Sprache. Danach gibst du den Namen ein oder wählst ein gespeichertes Profil."
    "es" -> "Primero elige el idioma. Después escribe el nombre o elige un perfil guardado."
    "it" -> "Prima scegli la lingua. Poi inserisci il nome del giocatore o scegli un profilo salvato."
    "sk" -> "Najprv vyber jazyk. Potom zadaj meno hráča alebo vyber uložený profil."
    else -> "First choose the language. Then enter a player name or choose a saved profile."
}

private fun suggestedLabel(language: String): String = when (AppLanguages.normalize(language)) {
    "pl" -> "Sugerowany język telefonu"
    "de" -> "Vom Telefon vorgeschlagen"
    "es" -> "Sugerido por el teléfono"
    "it" -> "Suggerito dal telefono"
    "sk" -> "Navrhnuté podľa telefónu"
    else -> "Suggested from phone language"
}

private fun onboardingFooter(language: String): String = when (AppLanguages.normalize(language)) {
    "pl" -> "Język możesz później zmienić w ustawieniach."
    "de" -> "Die Sprache kannst du später in den Einstellungen ändern."
    "es" -> "Puedes cambiar el idioma más tarde en Ajustes."
    "it" -> "Potrai cambiare la lingua più tardi nelle Impostazioni."
    "sk" -> "Jazyk môžeš neskôr zmeniť v nastaveniach."
    else -> "You can change the language later in Settings."
}
