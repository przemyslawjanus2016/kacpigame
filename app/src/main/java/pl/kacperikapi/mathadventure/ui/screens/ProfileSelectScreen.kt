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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.kacperikapi.mathadventure.data.PlayerProfile
import pl.kacperikapi.mathadventure.ui.theme.*

@Composable
fun ProfileSelectScreen(
    profiles: List<PlayerProfile>,
    activeProfileId: String?,
    onSelect: (PlayerProfile) -> Unit,
    onAdd: (String) -> Unit,
    onDelete: (PlayerProfile) -> Unit,
    onBack: (() -> Unit)? = null
) {
    var adding by remember(profiles.size) { mutableStateOf(profiles.isEmpty()) }
    var name by remember { mutableStateOf("") }
    var deleteCandidate by remember { mutableStateOf<PlayerProfile?>(null) }

    deleteCandidate?.let { profile ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Usunąć profil?", fontWeight = FontWeight.Black) },
            text = { Text("Profil „${profile.name}” i jego postęp zostaną usunięte z tego urządzenia.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(profile)
                        deleteCandidate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Usuń")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) {
                    Text("Anuluj")
                }
            }
        )
    }

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
                    TextButton(onClick = onBack) { Text("← Wróć") }
                }
            } else {
                Spacer(Modifier.height(16.dp))
            }

            Text("👤🐾", fontSize = 58.sp)
            Text(
                "Kto dziś gra?",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = AdventureGreen,
                textAlign = TextAlign.Center
            )
            Text(
                "Wybierz zapisany profil albo dodaj nowego gracza.",
                color = WoodBrown,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            Column(
                Modifier.fillMaxWidth().widthIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                profiles.forEach { profile ->
                    val selected = profile.id == activeProfileId
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) BrightGreen.copy(alpha = .18f) else androidx.compose.ui.graphics.Color.White.copy(alpha = .92f),
                        shadowElevation = 3.dp
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                Modifier.weight(1f).clickable { onSelect(profile) }.padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = CircleShape, color = AdventureGreen.copy(alpha = .14f)) {
                                    Text(
                                        profile.name.take(1).uppercase(),
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = AdventureGreen
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(profile.name, fontSize = 21.sp, fontWeight = FontWeight.Black, color = Ink)
                                    Text(
                                        if (selected) "Aktualny profil" else "Graj jako ten gracz",
                                        color = WoodBrown,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Text(if (selected) "✓" else "›", fontSize = 25.sp, color = AdventureGreen)
                            }

                            IconButton(onClick = { deleteCandidate = profile }) {
                                Text("🗑️", fontSize = 20.sp)
                            }
                        }
                    }
                }

                if (adding) {
                    Surface(shape = RoundedCornerShape(20.dp), color = Parchment, shadowElevation = 3.dp) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Nowy gracz", fontWeight = FontWeight.Black, color = AdventureGreen)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it.take(24) },
                                singleLine = true,
                                label = { Text("Imię gracza") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (profiles.isNotEmpty()) {
                                    TextButton(onClick = { adding = false; name = "" }) {
                                        Text("Anuluj")
                                    }
                                }
                                Button(
                                    onClick = {
                                        val clean = name.trim()
                                        if (clean.isNotBlank()) {
                                            onAdd(clean)
                                            name = ""
                                            adding = false
                                        }
                                    },
                                    enabled = name.trim().isNotBlank(),
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrightGreen)
                                ) {
                                    Text("Dodaj i graj", fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                } else {
                    FilledTonalButton(
                        onClick = { adding = true },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("＋ Dodaj kolejnego gracza", fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                "🔒 Profile i postęp są zapisane tylko na tym urządzeniu.",
                color = WoodBrown,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}
