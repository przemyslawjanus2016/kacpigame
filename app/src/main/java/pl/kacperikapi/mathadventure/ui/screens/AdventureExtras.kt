package pl.kacperikapi.mathadventure.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.kacperikapi.mathadventure.BuildConfig
import pl.kacperikapi.mathadventure.R
import pl.kacperikapi.mathadventure.data.*
import pl.kacperikapi.mathadventure.ui.components.ParchmentCard
import pl.kacperikapi.mathadventure.ui.components.PrimaryGameButton
import pl.kacperikapi.mathadventure.ui.components.WikimediaPhoto
import pl.kacperikapi.mathadventure.ui.theme.*
import pl.kacperikapi.mathadventure.update.PlayUpdateChecker
import java.time.LocalDate

@Composable
fun StoryScreen(stage: Stage, onStart: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val language = "pl"
    val world = GameContent.world(stage.worldId)
    val story = GameContent.story(stage)
    val attraction = AttractionContent.forStage(stage)
    val stagePhotoRes = remember(stage.id) {
        context.resources.getIdentifier(stage.id, "drawable", context.packageName).takeIf { it != 0 }
    }
    var ttsReady by remember { mutableStateOf(false) }
    val ttsHolder = remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(language) {
        val engine = TextToSpeech(context.applicationContext) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
        ttsHolder.value = engine
        onDispose {
            engine.stop()
            engine.shutdown()
            if (ttsHolder.value === engine) ttsHolder.value = null
        }
    }

    val readAloud: () -> Unit = {
        val spoken = if (attraction != null) {
            "${stage.name(language)}. ${attraction.description(language)}. ${attraction.fact(language)}"
        } else {
            "${story.title(language)}. ${story.text(language)}. ${story.fact(language)}"
        }
        ttsHolder.value?.let { engine ->
            engine.language = AppLanguages.profile(language).ttsLocale
            engine.setSpeechRate(0.92f)
            engine.speak(spoken, TextToSpeech.QUEUE_FLUSH, null, "attraction-${stage.id}")
        }
    }

    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyBlue.copy(.22f), Cream, Parchment)))
            .statusBarsPadding()
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth().widthIn(max = 900.dp), verticalAlignment = Alignment.CenterVertically) {
                FilledTonalButton(onClick = onBack) { Text("← ${stringResource(R.string.back)}") }
                Spacer(Modifier.weight(1f))
                Surface(shape = CircleShape, color = Parchment) {
                    Text(
                        "${world.icon} ${stage.number}/${GameRules.STAGES_PER_WORLD}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "📍 ${storyLabel(language, "discover")}",
                fontSize = 25.sp,
                fontWeight = FontWeight.Black,
                color = AdventureGreen,
                textAlign = TextAlign.Center
            )
            Text(stage.name(language), fontSize = 31.sp, fontWeight = FontWeight.Black, color = Ink, textAlign = TextAlign.Center)
            Text(
                storyLabel(language, "age").replace("%d", stage.targetAge.toString()),
                color = WoodBrown,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))

            ParchmentCard(Modifier.fillMaxWidth().widthIn(max = 900.dp)) {
                if (attraction != null) {
                    Surface(shape = RoundedCornerShape(14.dp), color = AdventureGreen.copy(alpha = .10f)) {
                        Text(
                            "${story.emoji} ${story.title(language)}",
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
                            color = AdventureGreen,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    if (stagePhotoRes != null) {
                        Image(
                            painter = painterResource(stagePhotoRes),
                            contentDescription = stage.name(language),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(20.dp))
                        )
                    } else {
                        WikimediaPhoto(
                            fileName = attraction.photoFileName,
                            searchQuery = attraction.photoSearchQuery,
                            contentDescription = stage.name(language),
                            fallbackRes = world.heroArtRes,
                            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                            onAttribution = { }
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(attraction.description(language), color = Ink, fontSize = 17.sp, lineHeight = 24.sp)
                    Spacer(Modifier.height(12.dp))
                    Surface(shape = RoundedCornerShape(16.dp), color = StarYellow.copy(.20f)) {
                        Column(Modifier.padding(13.dp)) {
                            Text("💡 ${storyLabel(language, "did_you_know")}", fontWeight = FontWeight.Black, color = WoodBrown)
                            Spacer(Modifier.height(4.dp))
                            Text(attraction.fact(language), color = WoodBrown, fontWeight = FontWeight.SemiBold, lineHeight = 21.sp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Surface(shape = RoundedCornerShape(16.dp), color = AdventureGreen.copy(alpha = .10f)) {
                        Text("🐾 ${story.text(language)}", modifier = Modifier.padding(12.dp), color = Ink)
                    }
                } else {
                    Text("${story.emoji} ${story.title(language)}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = AdventureGreen)
                    Spacer(Modifier.height(8.dp))
                    Text(story.text(language), color = Ink, fontSize = 17.sp)
                    Spacer(Modifier.height(12.dp))
                    Surface(shape = RoundedCornerShape(16.dp), color = StarYellow.copy(.18f)) {
                        Text("💡 ${story.fact(language)}", modifier = Modifier.padding(12.dp), color = WoodBrown, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = readAloud, enabled = ttsReady, modifier = Modifier.weight(1f)) {
                        Text("🔊 ${storyLabel(language, "listen")}")
                    }
                    Box(Modifier.weight(1.25f)) {
                        PrimaryGameButton(
                            stringResource(R.string.start_mission),
                            onStart,
                            Modifier.fillMaxWidth(),
                            textSizeSp = 20
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun storyLabel(language: String, key: String): String {
    val l = AppLanguages.normalize(language)
    return when (key) {
        "discover" -> when (l) { "en" -> "Discover this place"; "de" -> "Entdecke diesen Ort"; "es" -> "Descubre este lugar"; "it" -> "Scopri questo luogo"; "sk" -> "Spoznaj toto miesto"; else -> "Poznaj to miejsce" }
        "age" -> when (l) { "en" -> "Difficulty: around age %d"; "de" -> "Schwierigkeit: etwa %d Jahre"; "es" -> "Nivel: alrededor de %d años"; "it" -> "Livello: circa %d anni"; "sk" -> "Úroveň: približne %d rokov"; else -> "Poziom: około %d lat" }
        "did_you_know" -> when (l) { "en" -> "Did you know?"; "de" -> "Wusstest du?"; "es" -> "¿Sabías que…?"; "it" -> "Lo sapevi?"; "sk" -> "Vedeli ste, že?"; else -> "Czy wiesz, że?" }
        "listen" -> when (l) { "en" -> "Listen"; "de" -> "Anhören"; "es" -> "Escuchar"; "it" -> "Ascolta"; "sk" -> "Vypočuť"; else -> "Posłuchaj" }
        else -> key
    }
}

@Composable
fun PassportScreen(progress: GameProgress, onBack: () -> Unit) {
    val language = AppLanguages.normalize(LocalConfiguration.current.locales[0].language)
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SkyBlue.copy(.25f), Cream, Parchment))).statusBarsPadding()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth()) { TextButton(onClick = onBack) { Text("← ${stringResource(R.string.back)}") } }
            Text("🛂 ${stringResource(R.string.traveler_passport)}", fontSize = 30.sp, fontWeight = FontWeight.Black, color = AdventureGreen)
            Text(stringResource(R.string.passport_description), color = WoodBrown, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            ParchmentCard(Modifier.fillMaxWidth().widthIn(max = 760.dp)) {
                GameContent.worlds.forEach { world ->
                    val completed = progress.isWorldCompleted(world.id)
                    Surface(
                        Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = if (completed) BrightGreen.copy(.14f) else LockedGrey.copy(.10f)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (world.thumbnailRes != null) {
                                Image(
                                    painter = painterResource(world.thumbnailRes), contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(62.dp).clip(RoundedCornerShape(13.dp))
                                )
                            } else Text(world.icon, fontSize = 32.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(world.nameRes), fontWeight = FontWeight.Black, fontSize = 18.sp)
                                Text(world.subtitle(language), color = WoodBrown, style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (completed) "✅" else "◯", fontSize = 26.sp)
                                Text(if (completed) stringResource(R.string.stamp_earned) else stringResource(R.string.stamp_waiting), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun ExplorerAlbumScreen(progress: GameProgress, onBack: () -> Unit) {
    val context = LocalContext.current
    val language = "pl"
    val discovered = GameContent.worlds.sumOf { world ->
        world.stages.count { progress.isStageCompleted(it) }
    }

    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyBlue.copy(.24f), Cream, Parchment)))
            .statusBarsPadding()
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth()) {
                TextButton(onClick = onBack) { Text("← ${stringResource(R.string.back)}") }
            }
            Text("📸 ${stringResource(R.string.explorer_album)}", fontSize = 30.sp, fontWeight = FontWeight.Black, color = AdventureGreen)
            Text(
                stringResource(R.string.explorer_album_desc),
                color = WoodBrown,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Surface(shape = RoundedCornerShape(18.dp), color = BrightGreen.copy(.14f)) {
                Text(
                    "🗺️ Odkryte miejsca: $discovered / ${GameContent.worlds.size * GameRules.STAGES_PER_WORLD}",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = AdventureGreen,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.height(18.dp))

            GameContent.worlds.forEach { world ->
                val worldDiscovered = world.stages.count { progress.isStageCompleted(it) }
                ParchmentCard(Modifier.fillMaxWidth().widthIn(max = 900.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(world.icon, fontSize = 28.sp)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(world.nameRes), fontSize = 21.sp, fontWeight = FontWeight.Black, color = Ink)
                            Text("$worldDiscovered/${GameRules.STAGES_PER_WORLD} odkrytych miejsc", color = WoodBrown, fontSize = 13.sp)
                        }
                    }
                    Spacer(Modifier.height(10.dp))

                    world.stages.chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { stage ->
                                val unlocked = progress.isStageCompleted(stage)
                                val attraction = AttractionContent.forStage(stage)
                                val photoRes = context.resources.getIdentifier(stage.id, "drawable", context.packageName)
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(18.dp),
                                    color = if (unlocked) Color.White.copy(alpha = .88f) else LockedGrey.copy(alpha = .10f)
                                ) {
                                    Column {
                                        if (unlocked && photoRes != 0) {
                                            Image(
                                                painter = painterResource(photoRes),
                                                contentDescription = stage.name(language),
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 10f)
                                            )
                                        } else {
                                            Box(
                                                Modifier.fillMaxWidth().aspectRatio(16f / 10f).background(LockedGrey.copy(alpha = .10f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(if (unlocked) world.icon else "🔒", fontSize = 36.sp)
                                            }
                                        }
                                        Column(Modifier.padding(10.dp)) {
                                            Text(
                                                if (unlocked) stage.name(language) else "Etap ${stage.number} • Nieodkryte miejsce",
                                                fontWeight = FontWeight.Black,
                                                color = if (unlocked) Ink else LockedGrey,
                                                fontSize = 14.sp
                                            )
                                            if (unlocked && attraction != null) {
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    attraction.fact(language),
                                                    color = WoodBrown,
                                                    fontSize = 11.sp,
                                                    maxLines = 3
                                                )
                                                Spacer(Modifier.height(5.dp))
                                                Text(
                                                    "⭐ ${progress.stageStars(stage)}/3",
                                                    color = ActionOrange,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun DailyMissionIntroScreen(progress: GameProgress, onStart: () -> Unit, onBack: () -> Unit) {
    val today = LocalDate.now().toString()
    val completedToday = progress.dailyLastCompletedDate == today
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SkyBlue.copy(.25f), Cream, Parchment))).statusBarsPadding()) {
        Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth()) { TextButton(onClick = onBack) { Text("← ${stringResource(R.string.back)}") } }
            Spacer(Modifier.height(20.dp))
            Text("🎁", fontSize = 70.sp)
            Text(stringResource(R.string.daily_mission), fontSize = 34.sp, fontWeight = FontWeight.Black, color = AdventureGreen)
            Text(stringResource(R.string.daily_mission_desc), color = WoodBrown, textAlign = TextAlign.Center)
            Spacer(Modifier.height(18.dp))
            ParchmentCard(Modifier.fillMaxWidth().widthIn(max = 620.dp)) {
                Text("🔥 ${stringResource(R.string.current_streak)}: ${progress.dailyStreak}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = ActionOrange)
                Text("🏆 ${stringResource(R.string.daily_completed_total)}: ${progress.totalDailyMissions}", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                listOf(
                    "➕ ${stringResource(R.string.category_math)}", "🧠 ${stringResource(R.string.category_logic)}",
                    "🇬🇧 ${stringResource(R.string.category_english)}", "🌍 ${stringResource(R.string.category_world)}",
                    "⏰ ${stringResource(R.string.category_daily)}"
                ).forEach { Text("✓ $it", modifier = Modifier.padding(vertical = 3.dp)) }
                Spacer(Modifier.height(14.dp))
                if (completedToday) {
                    Surface(shape = RoundedCornerShape(15.dp), color = BrightGreen.copy(.17f)) {
                        Text("✅ ${stringResource(R.string.daily_done_today)}", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = AdventureGreen)
                    }
                    Spacer(Modifier.height(10.dp))
                }
                PrimaryGameButton(if (completedToday) stringResource(R.string.play_again) else stringResource(R.string.start_daily), onStart, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun SettingsScreen(
    narratorEnabled: Boolean,
    soundEnabled: Boolean,
    updateStatus: PlayUpdateChecker.Status,
    onCheckUpdate: () -> Unit,
    onOpenPlayStore: () -> Unit,
    onNarratorChanged: (Boolean) -> Unit,
    onSoundChanged: (Boolean) -> Unit,
    onResetAll: () -> Unit,
    onBack: () -> Unit
) {
    val language = AppLanguages.normalize(LocalConfiguration.current.locales[0].language)
    var confirmReset by remember { mutableStateOf(false) }
    var resetDone by remember { mutableStateOf(false) }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(stringResource(R.string.reset_all_data_title), fontWeight = FontWeight.Black) },
            text = { Text(stringResource(R.string.reset_all_data_message)) },
            confirmButton = {
                Button(
                    onClick = { confirmReset = false; onResetAll(); resetDone = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.reset_confirm)) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SkyBlue.copy(.25f), Cream, Parchment))).statusBarsPadding()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth()) { TextButton(onClick = onBack) { Text("← ${stringResource(R.string.back)}") } }
            Text("⚙️ ${stringResource(R.string.settings)}", fontSize = 30.sp, fontWeight = FontWeight.Black, color = AdventureGreen)
            Spacer(Modifier.height(18.dp))

            ParchmentCard(Modifier.fillMaxWidth().widthIn(max = 650.dp)) {
                SettingToggle("🔊", stringResource(R.string.narrator), stringResource(R.string.narrator_desc), narratorEnabled, onNarratorChanged)
                HorizontalDivider(color = WoodBrown.copy(.15f))
                SettingToggle("🎵", stringResource(R.string.sound_effects), stringResource(R.string.sound_effects_desc), soundEnabled, onSoundChanged)
                Spacer(Modifier.height(8.dp))
                Surface(shape = RoundedCornerShape(16.dp), color = BrightGreen.copy(.11f)) {
                    Text(stringResource(R.string.offline_safe_note), modifier = Modifier.padding(12.dp), color = Ink)
                }
            }

            Spacer(Modifier.height(14.dp))
            ParchmentCard(Modifier.fillMaxWidth().widthIn(max = 650.dp)) {
                Text("⬆️ ${stringResource(R.string.updates)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = WoodBrown)
                Text(stringResource(R.string.current_version, BuildConfig.VERSION_NAME), style = MaterialTheme.typography.bodySmall, color = WoodBrown)
                Spacer(Modifier.height(8.dp))
                when (updateStatus) {
                    PlayUpdateChecker.Status.Checking -> Text("Sprawdzanie aktualizacji…", color = Ink)
                    is PlayUpdateChecker.Status.Available -> {
                        Surface(shape = RoundedCornerShape(14.dp), color = BrightGreen.copy(.16f)) {
                            Text(
                                "✅ Dostępna jest nowsza wersja w Google Play.",
                                modifier = Modifier.fillMaxWidth().padding(11.dp),
                                color = AdventureGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = onOpenPlayStore,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Aktualizuj w Google Play", fontWeight = FontWeight.Black)
                        }
                    }
                    PlayUpdateChecker.Status.UpToDate -> Text("Masz najnowszą wersję.", color = AdventureGreen, fontWeight = FontWeight.Bold)
                    PlayUpdateChecker.Status.Unavailable -> Text(
                        "Nie udało się sprawdzić aktualizacji. Sprawdź połączenie z internetem i czy aplikacja została zainstalowana z Google Play.",
                        color = WoodBrown
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onCheckUpdate,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Sprawdź aktualizację", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(14.dp))
            ParchmentCard(Modifier.fillMaxWidth().widthIn(max = 650.dp)) {
                Text("🗄️ ${stringResource(R.string.data_and_progress)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = WoodBrown)
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = .72f)
                ) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Text(stringResource(R.string.reset_all_data), fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
                        Text(stringResource(R.string.reset_all_data_desc), style = MaterialTheme.typography.bodySmall, color = Ink)
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("🗑️  ${stringResource(R.string.reset_all_data)}", fontWeight = FontWeight.Black) }
                    }
                }
                if (resetDone) {
                    Spacer(Modifier.height(10.dp))
                    Surface(shape = RoundedCornerShape(14.dp), color = BrightGreen.copy(.16f)) {
                        Text("✅ ${stringResource(R.string.reset_done)}", modifier = Modifier.fillMaxWidth().padding(11.dp), color = AdventureGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingToggle(icon: String, title: String, description: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 25.sp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Black)
            Text(description, style = MaterialTheme.typography.bodySmall, color = WoodBrown)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
