package pl.kacperikapi.mathadventure.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import pl.kacperikapi.mathadventure.data.GameProgress
import pl.kacperikapi.mathadventure.data.GameRules
import pl.kacperikapi.mathadventure.data.Stage
import pl.kacperikapi.mathadventure.data.WorldDefinition
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
    var lockedStage by remember { mutableStateOf<Stage?>(null) }

    fun lockedMessage(item: Stage): String = when {
        item.number > 1 -> if (language == "en")
            "Stage ${item.number} is locked. Finish stage ${item.number - 1}. ${GameRules.requirementEn()}"
        else
            "Etap ${item.number} jest zablokowany. Ukończ etap ${item.number - 1}. ${GameRules.requirementPl()}"
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

            // Atrakcja otwiera się wyłącznie po dotknięciu punktu na mapie.
            // Nie pokazujemy już osobnego panelu atrakcji pod mapą ani obok niej.
            StageRouteMap(
                world = world,
                progress = progress,
                selectedStage = selectedStage,
                onSelect = onSelectStage,
                onLocked = { lockedStage = it },
                height = if (tablet) 650.dp else 620.dp
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}
