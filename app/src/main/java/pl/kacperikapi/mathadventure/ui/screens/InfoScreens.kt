package pl.kacperikapi.mathadventure.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.kacperikapi.mathadventure.R
import pl.kacperikapi.mathadventure.data.*
import pl.kacperikapi.mathadventure.ui.components.ParchmentCard
import pl.kacperikapi.mathadventure.ui.theme.*

@Composable
fun RewardsScreen(progress: GameProgress, onBack: () -> Unit) {
    InfoScaffold(stringResource(R.string.reward_room), onBack) {
        Text(stringResource(R.string.reward_room_description), color = Ink)
        Spacer(Modifier.height(14.dp))
        RewardBadge("⭐", stringResource(R.string.badge_first_star), progress.stars >= 1)
        RewardBadge("🧠", stringResource(R.string.badge_five_tasks), progress.correctTasks >= 5)
        RewardBadge("🧂", stringResource(R.string.badge_wieliczka_explorer), progress.unlockedWorldId >= 2)
        RewardBadge("🐉", stringResource(R.string.badge_krakow_explorer), progress.unlockedWorldId >= 3)
        RewardBadge("🏔️", stringResource(R.string.badge_tatry_explorer), progress.unlockedWorldId >= 4)
        RewardBadge("🌍", stringResource(R.string.badge_world_traveler), progress.unlockedWorldId >= 7)
        Spacer(Modifier.height(14.dp))
        Text(stringResource(R.string.visual_rewards), fontSize = 20.sp, fontWeight = FontWeight.Black, color = AdventureGreen)
        RewardBadge("🧭", stringResource(R.string.reward_compass), progress.stars >= 3)
        RewardBadge("🎒", stringResource(R.string.reward_backpack), progress.stars >= 8)
        RewardBadge("🧣", stringResource(R.string.reward_kapi_scarf), progress.stars >= 15)
        RewardBadge("🤠", stringResource(R.string.reward_explorer_hat), progress.stars >= 25)
        RewardBadge("🗺️", stringResource(R.string.reward_postcard_album), progress.totalDailyMissions >= 3)
    }
}

@Composable
fun ParentScreen(progress: GameProgress, onResetAll: () -> Unit, onBack: () -> Unit) {
    var confirmReset by remember { mutableStateOf(false) }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(stringResource(R.string.reset_all_progress_title), fontWeight = FontWeight.Black) },
            text = { Text(stringResource(R.string.reset_all_progress_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        confirmReset = false
                        onResetAll()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.reset_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    InfoScaffold(stringResource(R.string.parent_panel), onBack) {
        Text(stringResource(R.string.parent_panel_description_v2), color = Ink)
        if (DevOptions.UNLOCK_ALL_CONTENT) {
            Spacer(Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(16.dp), color = StarYellow.copy(.22f)) {
                Text(
                    stringResource(R.string.preview_mode_notice),
                    modifier = Modifier.padding(12.dp),
                    color = Ink,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        StatRow("🪙 ${stringResource(R.string.coins)}", progress.coins.toString())
        StatRow("⭐ ${stringResource(R.string.stars)}", progress.stars.toString())
        StatRow("✅ ${stringResource(R.string.solved_tasks)}", progress.solvedTasks.toString())
        StatRow("🎯 ${stringResource(R.string.accuracy)}", "${progress.accuracyPercent}%")
        StatRow("🗺️ ${stringResource(R.string.unlocked_worlds)}", "${progress.unlockedWorldId}/${GameContent.worlds.size}")
        StatRow("🔥 ${stringResource(R.string.current_streak)}", progress.dailyStreak.toString())
        Spacer(Modifier.height(18.dp))
        val measured = progress.categoryStats.filterValues { it.solved >= 3 }
        val strongest = measured.maxByOrNull { it.value.accuracyPercent }
        val weakest = measured.minByOrNull { it.value.accuracyPercent }
        if (strongest != null || weakest != null) {
            Surface(shape = RoundedCornerShape(16.dp), color = BrightGreen.copy(.10f)) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    strongest?.let { (category, stats) ->
                        Text("⭐ ${stringResource(R.string.strong_side)}: ${categoryDisplay(category)} (${stats.accuracyPercent}%)", fontWeight = FontWeight.Bold, color = AdventureGreen)
                    }
                    weakest?.let { (category, stats) ->
                        Text("🎯 ${stringResource(R.string.needs_practice)}: ${categoryDisplay(category)} (${stats.accuracyPercent}%)", fontWeight = FontWeight.Bold, color = ActionOrange)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        Text(stringResource(R.string.progress_by_category), fontSize = 20.sp, fontWeight = FontWeight.Black, color = AdventureGreen)
        Spacer(Modifier.height(8.dp))
        GameContent.categories.forEach { info ->
            val stats = progress.categoryStats[info.id] ?: CategoryStats()
            CategoryProgressRow(info.icon, stringResource(info.nameRes), stats)
        }
        Spacer(Modifier.height(18.dp))
        OutlinedButton(
            onClick = { confirmReset = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
        ) {
            Text("↺ ${stringResource(R.string.reset_all_progress)}", fontWeight = FontWeight.Black)
        }
        Text(
            stringResource(R.string.reset_keeps_language),
            modifier = Modifier.padding(top = 7.dp),
            style = MaterialTheme.typography.bodySmall,
            color = WoodBrown
        )
        Spacer(Modifier.height(14.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = SkyBlue.copy(.13f)) {
            Text(stringResource(R.string.parent_privacy_note), modifier = Modifier.padding(12.dp), color = Ink)
        }
    }
}

@Composable
private fun CategoryProgressRow(icon: String, label: String, stats: CategoryStats) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon)
            Spacer(Modifier.width(7.dp))
            Text(label, Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text("${stats.accuracyPercent}%", fontWeight = FontWeight.Black, color = AdventureGreen)
        }
        LinearProgressIndicator(
            progress = { stats.accuracyPercent / 100f },
            modifier = Modifier.fillMaxWidth().height(7.dp),
            color = AdventureGreen,
            trackColor = LockedGrey.copy(.18f)
        )
        Text(stringResource(R.string.category_attempts, stats.correct, stats.solved), style = MaterialTheme.typography.labelSmall, color = WoodBrown)
    }
}

@Composable
private fun categoryDisplay(category: LearningCategory): String = stringResource(
    when (category) {
        LearningCategory.MATH -> R.string.category_math
        LearningCategory.POLISH -> R.string.category_polish
        LearningCategory.ENGLISH -> R.string.category_english
        LearningCategory.LOGIC -> R.string.category_logic
        LearningCategory.NATURE -> R.string.category_nature
        LearningCategory.WORLD -> R.string.category_world
        LearningCategory.DAILY -> R.string.category_daily
    }
)

@Composable
private fun InfoScaffold(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SkyBlue.copy(.35f), Cream, Parchment))).statusBarsPadding()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth()) { TextButton(onClick = onBack) { Text("← ${stringResource(R.string.back)}") } }
            Text(title, fontSize = 30.sp, fontWeight = FontWeight.Black, color = AdventureGreen)
            Spacer(Modifier.height(18.dp))
            ParchmentCard(Modifier.fillMaxWidth().widthIn(max = 760.dp), content = content)
        }
    }
}

@Composable
private fun RewardBadge(icon: String, name: String, unlocked: Boolean) {
    Surface(Modifier.fillMaxWidth().padding(vertical = 5.dp), shape = RoundedCornerShape(16.dp), color = if (unlocked) BrightGreen.copy(.16f) else LockedGrey.copy(.12f)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 30.sp)
            Spacer(Modifier.width(12.dp))
            Text(name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text(if (unlocked) "✓" else "🔒", fontSize = 22.sp)
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = AdventureGreen)
    }
    HorizontalDivider(color = WoodBrown.copy(.15f))
}
