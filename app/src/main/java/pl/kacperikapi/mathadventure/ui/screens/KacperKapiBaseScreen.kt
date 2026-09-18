package pl.kacperikapi.mathadventure.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.kacperikapi.mathadventure.data.GameProgress
import pl.kacperikapi.mathadventure.ui.components.ParchmentCard
import pl.kacperikapi.mathadventure.ui.theme.*

@Composable
fun KacperKapiBaseScreen(progress: GameProgress, onBack: () -> Unit) {
    val items = listOf(
        BaseItem("🛏️", "Legowisko Kapi", 100),
        BaseItem("🌍", "Globus", 150),
        BaseItem("🗺️", "Mapa przygód", 250),
        BaseItem("🏆", "Półka z pucharami", 400)
    )
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SkyBlue.copy(.25f), Cream, Parchment))).statusBarsPadding()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth()) { TextButton(onClick = onBack) { Text("← Wróć") } }
            Text("🏠 Baza Kacpra i Kapi", fontSize = 30.sp, fontWeight = FontWeight.Black, color = AdventureGreen)
            Text("Zdobywaj monety za naukę i przygody. Rozwijaj bazę i kolekcję Kapi!", color = WoodBrown, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            ParchmentCard(Modifier.fillMaxWidth().widthIn(max = 700.dp)) {
                Text("🪙 Twoje monety: ${progress.coins}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = ActionOrange)
                Spacer(Modifier.height(12.dp))
                items.forEach { item ->
                    Surface(Modifier.fillMaxWidth().padding(vertical = 5.dp), shape = RoundedCornerShape(16.dp), color = AdventureGreen.copy(alpha = .08f)) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(item.icon, fontSize = 30.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Black)
                                Text("Do odblokowania: ${item.cost} 🪙", color = WoodBrown, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(if (progress.coins >= item.cost) "🔓" else "🔒", fontSize = 24.sp)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Pierwsza wersja bazy pokazuje cele do zdobycia. W kolejnym etapie dodamy kupowanie i ustawianie przedmiotów w pokoju.", color = WoodBrown, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private data class BaseItem(val icon: String, val name: String, val cost: Int)
