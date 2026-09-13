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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.kacperikapi.mathadventure.R
import pl.kacperikapi.mathadventure.data.GameProgress
import pl.kacperikapi.mathadventure.data.WorldDefinition
import pl.kacperikapi.mathadventure.ui.components.PrimaryGameButton
import pl.kacperikapi.mathadventure.ui.components.StageRouteMap
import pl.kacperikapi.mathadventure.ui.theme.*

@Composable
fun AdventureMapScreen(
    world: WorldDefinition,
    progress: GameProgress,
    selectedStage: Int,
    onSelectStage: (Int) -> Unit,
    onPlay: () -> Unit,
    onBack: () -> Unit,
    onPractice: () -> Unit
) {
    val language = LocalConfiguration.current.locales[0].language
    val stage = world.stages.first { it.number == selectedStage }
    BoxWithConstraints(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SkyBlue.copy(.28f), Cream, Parchment))).statusBarsPadding()
    ) {
        val tablet = maxWidth >= 700.dp
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("← ${stringResource(R.string.worlds)}") }
                Spacer(Modifier.weight(1f))
                Text("🪙 ${progress.coins}   ⭐ ${progress.stars}", fontWeight = FontWeight.Black)
            }
            Text("${world.icon} ${stringResource(world.nameRes)}", fontSize = if (tablet) 34.sp else 28.sp, fontWeight = FontWeight.Black, color = AdventureGreen)
            Text(world.subtitle(language), color = WoodBrown, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            if (tablet) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    StageRouteMap(world, progress, selectedStage, onSelectStage, Modifier.weight(1.45f), height = 650.dp)
                    MissionPanel(stage.name(language), stage.categories.joinToString(" • ") { it.name.lowercase() }, onPlay, onPractice, Modifier.weight(.7f))
                }
            } else {
                StageRouteMap(world, progress, selectedStage, onSelectStage, height = 620.dp)
                Spacer(Modifier.height(10.dp))
                MissionPanel(stage.name(language), stage.categories.joinToString(" • ") { it.name.lowercase() }, onPlay, onPractice)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MissionPanel(stageName: String, categories: String, onPlay: () -> Unit, onPractice: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = Parchment, shadowElevation = 4.dp) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.current_mission), color = AdventureGreen, fontWeight = FontWeight.Bold)
            Text(stageName, fontSize = 23.sp, fontWeight = FontWeight.Black, color = Ink)
            Text(categories, style = MaterialTheme.typography.bodySmall, color = WoodBrown)
            Spacer(Modifier.height(12.dp))
            PrimaryGameButton(stringResource(R.string.play), onPlay, Modifier.fillMaxWidth())
            TextButton(onClick = onPractice) { Text("📚 ${stringResource(R.string.choose_category)}") }
        }
    }
}
