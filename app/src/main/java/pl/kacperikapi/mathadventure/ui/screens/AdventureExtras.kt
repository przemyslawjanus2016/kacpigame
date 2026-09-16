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
import java.time.LocalDate

@Composable
fun StoryScreen(stage: Stage, onStart: () -> Unit, onBack: () -> Unit) {
    val language = AppLanguages.normalize(LocalConfiguration.current.locales[0].language)
    val context = LocalContext.current
    val world = GameContent.world(stage.worldId)
    val story = GameContent.story(stage)
    val attraction = AttractionContent.forStage(stage)
    var resolvedAttribution by remember(stage.id) { mutableStateOf<CommonsPhotoAttribution?>(null) }
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
                        "${world.icon} ${stage.number}/${world.stages.size}",
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
                    WikimediaPhoto(
                        stageId = stage.id,
                        fileName = attraction.photoFileName,
                        searchQuery = attraction.photoSearchQuery,
                        contentDescription = stage.name(language),
                        fallbackRes = world.heroArtRes,
                        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                        onAttribution = { resolvedAttribution = it }
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        resolvedAttribution?.credit(language) ?: attraction.credit(language),
                        style = MaterialTheme.typography.labelSmall,
                        color = WoodBrown.copy(alpha = .82f)
                    )
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
                    Spacer(Modifier.height(8.dp))
                    Text(
                        storyLabel(language, "photo_offline"),
                        style = MaterialTheme.typography.labelSmall,
                        color = WoodBrown
                    )
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
                        PrimaryGameButton(stringResource(R.string.start_mission), onStart, Modifier.fillMaxWidth())
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
        "photo_offline" -> when (l) {
            "en" -> "Attraction photo is included in the app and works offline."
            "de" -> "Das Foto der Attraktion ist in der App enthalten und funktioniert offline."
            "es" -> "La foto de la atracción está incluida en la app y funciona sin conexión."
            "it" -> "La foto dell’attrazione è inclusa nell’app e funziona offline."
            "sk" -> "Fotografia atrakcie je súčasťou aplikácie a funguje offline."
            else -> "Zdjęcie atrakcji jest wbudowane w aplikację i działa offline."
        }
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
                } else {
                    PrimaryGameButton(stringResource(R.string.start_mission), onStart, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
fun RewardsScreen(progress: GameProgress, onBack: () -> Unit) {
    val language = AppLanguages.normalize(LocalConfiguration.current.locales[0].language)
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SkyBlue.copy(.25f), Cream, Parchment))).statusBarsPadding()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth()) { TextButton(onClick = onBack) { Text("← ${stringResource(R.string.back)}") } }
            Text("🏆 ${stringResource(R.string.rewards)}", fontSize = 32.sp, fontWeight = FontWeight.Black, color = AdventureGreen)
            Spacer(Modifier.height(12.dp))
            ParchmentCard(Modifier.fillMaxWidth().widthIn(max = 760.dp)) {
                Text("⭐ ${progress.stars}   🪙 ${progress.coins}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = WoodBrown)
                Spacer(Modifier.height(12.dp))
                GameContent.worlds.forEach { world ->
                    val done = progress.isWorldCompleted(world.id)
                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (done) "🏅" else "▫️", fontSize = 25.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(world.nameRes), fontWeight = FontWeight.Bold)
                            Text(world.subtitle(language), style = MaterialTheme.typography.bodySmall, color = WoodBrown)
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun ParentScreen(progress: GameProgress, onBack: () -> Unit) {
    val language = AppLanguages.normalize(LocalConfiguration.current.locales[0].language)
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SkyBlue.copy(.25f), Cream, Parchment))).statusBarsPadding()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth()) { TextButton(onClick = onBack) { Text("← ${stringResource(R.string.back)}") } }
            Text("👨‍👩‍👦 ${stringResource(R.string.parent)}", fontSize = 30.sp, fontWeight = FontWeight.Black, color = AdventureGreen)
            Spacer(Modifier.height(14.dp))
            ParchmentCard(Modifier.fillMaxWidth().widthIn(max = 780.dp)) {
                Text(stringResource(R.string.parent_progress_title), fontWeight = FontWeight.Black, fontSize = 21.sp, color = WoodBrown)
                Text("⭐ ${progress.stars}   🪙 ${progress.coins}", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                GameContent.worlds.forEach { world ->
                    val completed = progress.completedStages.count { it.startsWith("w${world.id}s") }
                    Text("${world.icon} ${stringResource(world.nameRes)}: $completed/${world.stages.size}", modifier = Modifier.padding(vertical = 3.dp))
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    when (language) {
                        "en" -> "The game, exercises and attraction photos are available offline after installation."
                        "de" -> "Das Spiel, die Aufgaben und die Fotos der Attraktionen sind nach der Installation offline verfügbar."
                        "es" -> "El juego, los ejercicios y las fotos de las atracciones están disponibles sin conexión después de instalar la app."
                        "it" -> "Il gioco, gli esercizi e le foto delle attrazioni sono disponibili offline dopo l’installazione."
                        "sk" -> "Hra, úlohy a fotografie atrakcií sú po inštalácii dostupné offline."
                        else -> "Gra, zadania i zdjęcia atrakcji są dostępne offline po instalacji."
                    },
                    color = WoodBrown
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    narratorEnabled: Boolean,
    soundEnabled: Boolean,
    onNarrator: (Boolean) -> Unit,
    onSound: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SkyBlue.copy(.25f), Cream, Parchment))).statusBarsPadding()) {
        Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth()) { TextButton(onClick = onBack) { Text("← ${stringResource(R.string.back)}") } }
            Text("⚙️ ${stringResource(R.string.settings)}", fontSize = 30.sp, fontWeight = FontWeight.Black, color = AdventureGreen)
            Spacer(Modifier.height(14.dp))
            ParchmentCard(Modifier.fillMaxWidth().widthIn(max = 660.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.narrator), fontWeight = FontWeight.Black)
                        Text(stringResource(R.string.narrator_desc), style = MaterialTheme.typography.bodySmall, color = WoodBrown)
                    }
                    Switch(checked = narratorEnabled, onCheckedChange = onNarrator)
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.sound), fontWeight = FontWeight.Black)
                        Text(stringResource(R.string.sound_desc), style = MaterialTheme.typography.bodySmall, color = WoodBrown)
                    }
                    Switch(checked = soundEnabled, onCheckedChange = onSound)
                }
                Spacer(Modifier.height(16.dp))
                Text("${stringResource(R.string.version)} ${BuildConfig.VERSION_NAME}", color = WoodBrown)
            }
        }
    }
}
