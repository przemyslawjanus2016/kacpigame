package pl.kacperikapi.mathadventure.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.kacperikapi.mathadventure.R
import pl.kacperikapi.mathadventure.data.GameContent
import pl.kacperikapi.mathadventure.data.GameProgress
import pl.kacperikapi.mathadventure.data.GameRules
import pl.kacperikapi.mathadventure.data.Stage
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
    onPractice: () -> Unit,
    notice: String? = null,
    onNoticeDismiss: () -> Unit = {}
) {
    val language = LocalConfiguration.current.locales[0].language
    val stage = world.stages.first { it.number == selectedStage }
    var lockedStage by remember { mutableStateOf<Stage?>(null) }

    fun lockedMessage(item: Stage): String = when {
        !progress.isWorldUnlocked(item.worldId) -> {
            if (language == "en")
                "Finish the previous world first."
            else
                "Najpierw ukończ poprzedni świat."
        }
        item.number > 1 -> {
            if (language == "en")
                "Stage ${item.number} is locked. Finish stage ${item.number - 1}. ${GameRules.requirementEn()}"
            else
                "Etap ${item.number} jest zablokowany. Ukończ etap ${item.number - 1}. ${GameRules.requirementPl()}"
        }
        else -> if (language == "en") GameRules.requirementEn() else GameRules.requirementPl()
    }

    lockedStage?.let { item ->
        AlertDialog(
            onDismissRequest = { lockedStage = null },
            title = { Text(if (language == "en") "🔒 Stage locked" else "🔒 Etap zablokowany", fontWeight = FontWeight.Black) },
            text = { Text(lockedMessage(item)) },
            confirmButton = {
                Button(onClick = { lockedStage = null }) {
                    Text(if (language == "en") "OK" else "Rozumiem")
                }
            }
        )
    }

    if (notice != null) {
        AlertDialog(
            onDismissRequest = onNoticeDismiss,
            title = { Text(if (language == "en") "One more try" else "Jeszcze jedna próba", fontWeight = FontWeight.Black) },
            text = { Text(notice) },
            confirmButton = { Button(onClick = onNoticeDismiss) { Text("OK") } }
        )
    }

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
                    StageRouteMap(
                        world = world,
                        progress = progress,
                        selectedStage = selectedStage,
                        onSelect = onSelectStage,
                        onLocked = { lockedStage = it },
                        modifier = Modifier.weight(1.45f),
                        height = 650.dp
                    )
                    MissionPanel(stage, world.stages.size, language, onPlay, onPractice, Modifier.weight(.7f))
                }
            } else {
                StageRouteMap(
                    world = world,
                    progress = progress,
                    selectedStage = selectedStage,
                    onSelect = onSelectStage,
                    onLocked = { lockedStage = it },
                    height = 620.dp
                )
                Spacer(Modifier.height(10.dp))
                MissionPanel(stage, world.stages.size, language, onPlay, onPractice)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MissionPanel(stage: Stage, totalStages: Int, language: String, onPlay: () -> Unit, onPractice: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = Parchment, shadowElevation = 4.dp) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.current_mission), color = AdventureGreen, fontWeight = FontWeight.Bold)
            Text(stage.name(language), fontSize = 23.sp, fontWeight = FontWeight.Black, color = Ink)
            Text(
                if (language == "en")
                    "Stage ${stage.number}/$totalStages • difficulty around age ${stage.targetAge}"
                else
                    "Etap ${stage.number}/$totalStages • poziom ok. ${stage.targetAge} lat",
                style = MaterialTheme.typography.bodySmall,
                color = WoodBrown
            )
            Text(stage.categories.joinToString(" • ") { it.name.lowercase() }, style = MaterialTheme.typography.bodySmall, color = WoodBrown)
            Spacer(Modifier.height(12.dp))
            PrimaryGameButton(if (language == "en") "Discover place" else "Poznaj miejsce", onPlay, Modifier.fillMaxWidth())
            TextButton(onClick = onPractice) { Text("📚 ${stringResource(R.string.choose_category)}") }
        }
    }
}
