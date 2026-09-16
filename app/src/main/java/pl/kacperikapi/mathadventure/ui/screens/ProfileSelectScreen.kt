package pl.kacperikapi.mathadventure.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.kacperikapi.mathadventure.data.AppLanguages
import pl.kacperikapi.mathadventure.data.PlayerProfile
import pl.kacperikapi.mathadventure.ui.theme.*

@Composable
fun ProfileSelectScreen(
    profiles: List<PlayerProfile>,
    activeProfileId: String?,
    onSelect: (PlayerProfile) -> Unit,
    onAdd: (String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val language = AppLanguages.normalize(LocalConfiguration.current.locales[0].language)
    var adding by remember(profiles.size) { mutableStateOf(profiles.isEmpty()) }
    var name by remember { mutableStateOf("") }

    fun text(key: String): String = profileText(language, key)

    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyBlue.copy(.30f), Cream, Parchment)))
            .statusBarsPadding()
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (onBack != null) {
                Row(Modifier.fillMaxWidth()) {
                    TextButton(onClick = onBack) { Text("← ${text("back")}") }
                }
            } else {
                Spacer(Modifier.height(16.dp))
            }

            Text("👤🐾", fontSize = 58.sp)
            Text(text("who"), fontSize = 32.sp, fontWeight = FontWeight.Black, color = AdventureGreen, textAlign = TextAlign.Center)
            Text(text("local"), color = WoodBrown, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))

            Column(
                Modifier.fillMaxWidth().widthIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                profiles.forEach { profile ->
                    val selected = profile.id == activeProfileId
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(profile) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) BrightGreen.copy(alpha = .18f) else androidx.compose.ui.graphics.Color.White.copy(alpha = .92f),
                        shadowElevation = 3.dp
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = AdventureGreen.copy(alpha = .14f)) {
                                Text(profile.name.take(1).uppercase(), modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), fontSize = 24.sp, fontWeight = FontWeight.Black, color = AdventureGreen)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(profile.name, fontSize = 21.sp, fontWeight = FontWeight.Black, color = Ink)
                                Text(if (selected) text("current") else text("play"), color = WoodBrown, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(if (selected) "✓" else "›", fontSize = 25.sp, color = AdventureGreen)
                        }
                    }
                }

                if (adding) {
                    Surface(shape = RoundedCornerShape(20.dp), color = Parchment, shadowElevation = 3.dp) {
                        Column(Modifier.padding(16.dp)) {
                            Text(text("new"), fontWeight = FontWeight.Black, color = AdventureGreen)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it.take(24) },
                                singleLine = true,
                                label = { Text(text("name")) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (profiles.isNotEmpty()) {
                                    TextButton(onClick = { adding = false; name = "" }) { Text(text("cancel")) }
                                }
                                Button(
                                    onClick = { val clean = name.trim(); if (clean.isNotBlank()) { onAdd(clean); name = ""; adding = false } },
                                    enabled = name.trim().isNotBlank(),
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrightGreen)
                                ) { Text(text("add"), fontWeight = FontWeight.Black) }
                            }
                        }
                    }
                } else {
                    FilledTonalButton(
                        onClick = { adding = true },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("＋ ${text("add_player")}", fontWeight = FontWeight.Black)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("🔒 ${text("privacy")}", color = WoodBrown, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
    }
}

private fun profileText(language: String, key: String): String = when (AppLanguages.normalize(language)) {
    "en" -> when (key) {
        "who" -> "Who is playing today?"; "local" -> "Choose a local player profile"; "current" -> "Current profile"; "play" -> "Play as this player";
        "new" -> "New player"; "name" -> "Player name"; "add" -> "Add and play"; "cancel" -> "Cancel"; "add_player" -> "Add player";
        "privacy" -> "Profiles and progress stay only on this device."; "back" -> "Back"; else -> key
    }
    "de" -> when (key) {
        "who" -> "Wer spielt heute?"; "local" -> "Lokales Spielerprofil wählen"; "current" -> "Aktuelles Profil"; "play" -> "Mit diesem Profil spielen";
        "new" -> "Neuer Spieler"; "name" -> "Name"; "add" -> "Hinzufügen und spielen"; "cancel" -> "Abbrechen"; "add_player" -> "Spieler hinzufügen";
        "privacy" -> "Profile und Fortschritt bleiben nur auf diesem Gerät."; "back" -> "Zurück"; else -> key
    }
    "es" -> when (key) {
        "who" -> "¿Quién juega hoy?"; "local" -> "Elige un perfil local"; "current" -> "Perfil actual"; "play" -> "Jugar con este perfil";
        "new" -> "Nuevo jugador"; "name" -> "Nombre"; "add" -> "Añadir y jugar"; "cancel" -> "Cancelar"; "add_player" -> "Añadir jugador";
        "privacy" -> "Los perfiles y el progreso se guardan solo en este dispositivo."; "back" -> "Volver"; else -> key
    }
    "it" -> when (key) {
        "who" -> "Chi gioca oggi?"; "local" -> "Scegli un profilo locale"; "current" -> "Profilo attuale"; "play" -> "Gioca con questo profilo";
        "new" -> "Nuovo giocatore"; "name" -> "Nome"; "add" -> "Aggiungi e gioca"; "cancel" -> "Annulla"; "add_player" -> "Aggiungi giocatore";
        "privacy" -> "Profili e progressi restano solo su questo dispositivo."; "back" -> "Indietro"; else -> key
    }
    "sk" -> when (key) {
        "who" -> "Kto dnes hrá?"; "local" -> "Vyber miestny profil hráča"; "current" -> "Aktuálny profil"; "play" -> "Hrať s týmto profilom";
        "new" -> "Nový hráč"; "name" -> "Meno"; "add" -> "Pridať a hrať"; "cancel" -> "Zrušiť"; "add_player" -> "Pridať hráča";
        "privacy" -> "Profily a postup zostávajú iba v tomto zariadení."; "back" -> "Späť"; else -> key
    }
    else -> when (key) {
        "who" -> "Kto dziś gra?"; "local" -> "Wybierz lokalny profil gracza"; "current" -> "Aktualny profil"; "play" -> "Graj jako ten gracz";
        "new" -> "Nowy gracz"; "name" -> "Imię gracza"; "add" -> "Dodaj i graj"; "cancel" -> "Anuluj"; "add_player" -> "Dodaj gracza";
        "privacy" -> "Profile i postęp są zapisane tylko na tym urządzeniu."; "back" -> "Wróć"; else -> key
    }
}
