package pl.kacperikapi.mathadventure

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import pl.kacperikapi.mathadventure.billing.PremiumBillingManager
import pl.kacperikapi.mathadventure.data.*
import pl.kacperikapi.mathadventure.ui.screens.*
import pl.kacperikapi.mathadventure.ui.theme.*
import java.time.LocalDate
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var store: ProgressStore
    private lateinit var billingManager: PremiumBillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val crashPrefs = getSharedPreferences(KacperKapiApplication.CRASH_PREFS, MODE_PRIVATE)
        val previousCrash = crashPrefs.getString(KacperKapiApplication.LAST_CRASH, null)
        val crashVersion = crashPrefs.getInt(KacperKapiApplication.LAST_CRASH_VERSION, -1)
        if (!previousCrash.isNullOrBlank() && crashVersion == BuildConfig.VERSION_CODE) {
            showCrashDiagnostic(previousCrash)
            return
        } else if (!previousCrash.isNullOrBlank()) {
            crashPrefs.edit()
                .remove(KacperKapiApplication.LAST_CRASH)
                .remove(KacperKapiApplication.LAST_CRASH_VERSION)
                .apply()
        }

        store = ProgressStore(this)
        billingManager = PremiumBillingManager(this)
        applyLanguageSafely(store.loadLanguage())

        setContent {
            KacperKapiTheme {
                GameApp(
                    store = store,
                    billingManager = billingManager,
                    activity = this@MainActivity,
                    onLanguageChanged = { recreate() }
                )
            }
        }
    }

    override fun onDestroy() {
        if (::billingManager.isInitialized) billingManager.close()
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    private fun applyLanguageSafely(tag: String) {
        runCatching {
            val safeTag = AppLanguages.normalize(tag)
            val locale = AppLanguages.profile(safeTag).ttsLocale
            Locale.setDefault(locale)
            val configuration = resources.configuration
            configuration.setLocale(locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }

    private fun showCrashDiagnostic(stackTrace: String) {
        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }
        root.addView(TextView(this).apply {
            text = "Kacper i Kapi – diagnostyka\n\nAplikacja zapisała błąd z poprzedniego uruchomienia. Zrób zdjęcie tego ekranu i wyślij mi je."
            textSize = 20f
            setTypeface(typeface, Typeface.BOLD)
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(TextView(this).apply {
            text = stackTrace
            textSize = 12f
            setTextIsSelectable(true)
            setPadding(0, pad, 0, pad)
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(Button(this).apply {
            text = "Wyczyść błąd i spróbuj ponownie"
            setOnClickListener {
                getSharedPreferences(KacperKapiApplication.CRASH_PREFS, MODE_PRIVATE)
                    .edit()
                    .remove(KacperKapiApplication.LAST_CRASH)
                    .remove(KacperKapiApplication.LAST_CRASH_VERSION)
                    .apply()
                recreate()
            }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        setContentView(ScrollView(this).apply { addView(root) })
    }
}

private sealed interface Screen {
    data object Splash : Screen
    data object Worlds : Screen
    data class Map(val worldId: Int) : Screen
    data class Story(val worldId: Int, val stageNumber: Int) : Screen
    data class Categories(val worldId: Int, val stageNumber: Int) : Screen
    data class Game(val worldId: Int, val stageNumber: Int, val category: LearningCategory?, val daily: Boolean = false) : Screen
    data object DailyIntro : Screen
    data object Passport : Screen
    data object Rewards : Screen
    data object Parent : Screen
    data object Settings : Screen
    data object Premium : Screen
}

@Composable
private fun GameApp(
    store: ProgressStore,
    billingManager: PremiumBillingManager,
    activity: Activity,
    onLanguageChanged: () -> Unit
) {
    var screen by remember { mutableStateOf<Screen>(Screen.Splash) }
    var progress by remember { mutableStateOf(store.load()) }
    var narratorEnabled by remember { mutableStateOf(store.loadNarratorEnabled()) }
    var soundEnabled by remember { mutableStateOf(store.loadSoundEnabled()) }
    var progressionNotice by remember { mutableStateOf<String?>(null) }
    var selectedStages by remember {
        mutableStateOf(GameContent.worlds.associate { it.id to progress.availableMaxStage(it.id).coerceAtLeast(1) })
    }
    val billingState by billingManager.state.collectAsState()

    fun persist(newProgress: GameProgress) {
        progress = newProgress
        store.save(newProgress)
    }

    fun switchLanguage() {
        val next = AppLanguages.next(store.loadLanguage())
        store.saveLanguage(next)
        onLanguageChanged()
    }

    BackHandler(enabled = screen != Screen.Worlds && screen != Screen.Splash) {
        screen = when (val current = screen) {
            Screen.Splash -> Screen.Worlds
            Screen.Worlds -> Screen.Worlds
            is Screen.Map -> Screen.Worlds
            is Screen.Story -> Screen.Map(current.worldId)
            is Screen.Categories -> Screen.Map(current.worldId)
            is Screen.Game -> if (current.daily) Screen.DailyIntro else Screen.Map(current.worldId)
            Screen.DailyIntro -> Screen.Worlds
            Screen.Passport -> Screen.Worlds
            Screen.Rewards -> Screen.Worlds
            Screen.Parent -> Screen.Worlds
            Screen.Settings -> Screen.Worlds
            Screen.Premium -> Screen.Worlds
        }
    }

    val blockedWorldId = when (val current = screen) {
        is Screen.Map -> current.worldId
        is Screen.Story -> current.worldId
        is Screen.Categories -> current.worldId
        is Screen.Game -> if (current.daily) null else current.worldId
        else -> null
    }
    if (blockedWorldId != null && PremiumAccess.shouldShowPaywall(blockedWorldId, billingState.premiumUnlocked)) {
        PremiumUnlockScreen(
            state = billingState,
            onBuy = { billingManager.launchPurchase(activity) },
            onRestore = { billingManager.restorePurchases() },
            onBack = { screen = Screen.Worlds }
        )
        return
    }

    when (val current = screen) {
        Screen.Splash -> SplashScreen { screen = Screen.Worlds }
        Screen.Worlds -> WorldSelectScreen(
            progress = progress,
            premiumUnlocked = billingState.premiumUnlocked,
            onWorld = { worldId ->
                when {
                    PremiumAccess.shouldShowPaywall(worldId, billingState.premiumUnlocked) -> screen = Screen.Premium
                    PremiumAccess.canOpenWorld(worldId, progress.isWorldUnlocked(worldId), billingState.premiumUnlocked) -> screen = Screen.Map(worldId)
                }
            },
            onPremium = { screen = Screen.Premium },
            onPractice = {
                val worldId = if (billingState.premiumUnlocked || DevOptions.UNLOCK_ALL_CONTENT) {
                    progress.unlockedWorldId.coerceIn(1, GameContent.worlds.size)
                } else {
                    PremiumAccess.FREE_WORLD_ID
                }
                val stage = progress.maxStage(worldId).coerceIn(1, GameRules.STAGES_PER_WORLD)
                screen = Screen.Categories(worldId, stage)
            },
            onDaily = { screen = Screen.DailyIntro },
            onPassport = { screen = Screen.Passport },
            onRewards = { screen = Screen.Rewards },
            onParent = { screen = Screen.Parent },
            onSettings = { screen = Screen.Settings },
            onLanguage = ::switchLanguage
        )
        is Screen.Map -> {
            val safeWorldId = current.worldId.coerceIn(1, GameContent.worlds.size)
            val world = GameContent.world(safeWorldId)
            val maxStage = progress.availableMaxStage(safeWorldId).coerceIn(1, GameRules.STAGES_PER_WORLD)
            val selected = selectedStages[safeWorldId]?.coerceIn(1, maxStage) ?: 1
            AdventureMapScreen(
                world = world,
                progress = progress,
                selectedStage = selected,
                onSelectStage = { number ->
                    val safeStage = number.coerceIn(1, maxStage)
                    selectedStages = selectedStages + (safeWorldId to safeStage)
                    screen = Screen.Story(safeWorldId, safeStage)
                },
                onPlay = { screen = Screen.Story(safeWorldId, selected) },
                onBack = { screen = Screen.Worlds },
                onPractice = { screen = Screen.Categories(safeWorldId, selected) },
                notice = progressionNotice,
                onNoticeDismiss = { progressionNotice = null }
            )
        }
        is Screen.Story -> {
            val stage = GameContent.stage(current.worldId, current.stageNumber)
            StoryScreen(
                stage = stage,
                onStart = { screen = Screen.Game(current.worldId, current.stageNumber, null) },
                onBack = { screen = Screen.Map(current.worldId) }
            )
        }
        is Screen.Categories -> CategorySelectScreen(
            onCategory = { screen = Screen.Game(current.worldId, current.stageNumber, it) },
            onBack = { screen = Screen.Map(current.worldId) }
        )
        is Screen.Game -> {
            val stage = if (current.daily) dailyStage(billingState.premiumUnlocked) else GameContent.stage(
                current.worldId.coerceIn(1, GameContent.worlds.size),
                current.stageNumber.coerceIn(1, GameRules.STAGES_PER_WORLD)
            )
            LearningGameScreen(
                stage = stage,
                fixedCategory = current.category,
                performance = progress.categoryStats,
                narratorEnabled = narratorEnabled,
                soundEnabled = soundEnabled,
                onBack = { screen = if (current.daily) Screen.DailyIntro else Screen.Map(stage.worldId) },
                onRoundFinished = { result ->
                    val mergedStats = progress.categoryStats.toMutableMap()
                    result.categoryStats.forEach { (category, roundStats) ->
                        val old = mergedStats[category] ?: CategoryStats()
                        mergedStats[category] = CategoryStats(
                            solved = old.solved + roundStats.solved,
                            correct = old.correct + roundStats.correct
                        )
                    }

                    if (current.daily) {
                        val today = LocalDate.now()
                        val previous = runCatching { LocalDate.parse(progress.dailyLastCompletedDate) }.getOrNull()
                        val alreadyDoneToday = previous == today
                        val nextStreak = when {
                            alreadyDoneToday -> progress.dailyStreak
                            previous == today.minusDays(1) -> progress.dailyStreak + 1
                            else -> 1
                        }
                        persist(
                            progress.copy(
                                coins = progress.coins + result.correct * 10 + if (alreadyDoneToday) 0 else 50,
                                solvedTasks = progress.solvedTasks + result.total,
                                correctTasks = progress.correctTasks + result.correct,
                                categoryStats = mergedStats,
                                dailyStreak = nextStreak,
                                dailyLastCompletedDate = today.toString(),
                                totalDailyMissions = progress.totalDailyMissions + if (alreadyDoneToday) 0 else 1
                            )
                        )
                        screen = Screen.Worlds
                    } else {
                        val isAdventure = current.category == null
                        val completedWell = result.correct >= GameRules.PASSING_CORRECT
                        var unlockedWorld = progress.unlockedWorldId
                        val maxByWorld = progress.maxStageByWorld.toMutableMap()
                        val completed = progress.completedStageIds.toMutableSet()
                        val stageStars = progress.starsByStage.toMutableMap()
                        val postcards = progress.collectedPostcards.toMutableSet()
                        var starGain = 0

                        if (isAdventure && completedWell) {
                            completed += stage.id
                            val previousStars = stageStars[stage.id] ?: 0
                            if (result.earnedStars > previousStars) {
                                starGain = result.earnedStars - previousStars
                                stageStars[stage.id] = result.earnedStars
                            }
                            if (stage.number < GameRules.STAGES_PER_WORLD) {
                                val nextStage = stage.number + 1
                                maxByWorld[stage.worldId] = maxOf(progress.maxStage(stage.worldId), nextStage)
                                selectedStages = selectedStages + (stage.worldId to nextStage)
                            } else {
                                postcards += stage.worldId
                                if (stage.worldId < GameContent.worlds.size) {
                                    unlockedWorld = maxOf(unlockedWorld, stage.worldId + 1)
                                    maxByWorld[stage.worldId + 1] = maxOf(maxByWorld[stage.worldId + 1] ?: 0, 1)
                                }
                            }
                        } else if (isAdventure) {
                            progressionNotice = GameRules.failedNotice(store.loadLanguage(), result.correct, result.total)
                        }

                        persist(
                            progress.copy(
                                coins = progress.coins + result.correct * 10,
                                stars = progress.stars + starGain,
                                unlockedWorldId = unlockedWorld.coerceIn(1, GameContent.worlds.size),
                                maxStageByWorld = maxByWorld,
                                completedStageIds = completed,
                                starsByStage = stageStars,
                                solvedTasks = progress.solvedTasks + result.total,
                                correctTasks = progress.correctTasks + result.correct,
                                categoryStats = mergedStats,
                                collectedPostcards = postcards
                            )
                        )
                        screen = Screen.Map(stage.worldId)
                    }
                }
            )
        }
        Screen.DailyIntro -> DailyMissionIntroScreen(
            progress = progress,
            onStart = {
                val stage = dailyStage(billingState.premiumUnlocked)
                screen = Screen.Game(stage.worldId, stage.number, null, daily = true)
            },
            onBack = { screen = Screen.Worlds }
        )
        Screen.Passport -> PassportScreen(progress) { screen = Screen.Worlds }
        Screen.Rewards -> RewardsScreen(progress) { screen = Screen.Worlds }
        Screen.Parent -> ParentScreen(
            progress = progress,
            onResetAll = {
                progress = store.resetAllProgress()
                selectedStages = GameContent.worlds.associate { it.id to 1 }
                progressionNotice = null
                screen = Screen.Worlds
            },
            onBack = { screen = Screen.Worlds }
        )
        Screen.Settings -> SettingsScreen(
            narratorEnabled = narratorEnabled,
            soundEnabled = soundEnabled,
            onNarratorChanged = { narratorEnabled = it; store.saveNarratorEnabled(it) },
            onSoundChanged = { soundEnabled = it; store.saveSoundEnabled(it) },
            onLanguage = ::switchLanguage,
            onResetAll = {
                progress = store.resetAllProgress()
                selectedStages = GameContent.worlds.associate { it.id to 1 }
                progressionNotice = null
            },
            onBack = { screen = Screen.Worlds }
        )
        Screen.Premium -> PremiumUnlockScreen(
            state = billingState,
            onBuy = { billingManager.launchPurchase(activity) },
            onRestore = { billingManager.restorePurchases() },
            onBack = { screen = Screen.Worlds }
        )
    }
}

private fun dailyStage(premiumUnlocked: Boolean): Stage {
    val today = LocalDate.now()
    val worldId = if (premiumUnlocked || DevOptions.UNLOCK_ALL_CONTENT) {
        ((today.dayOfYear - 1) % GameContent.worlds.size) + 1
    } else {
        PremiumAccess.FREE_WORLD_ID
    }
    val stageNumber = ((today.dayOfMonth - 1) % GameRules.STAGES_PER_WORLD) + 1
    return Stage(
        id = "daily-${today}",
        worldId = worldId,
        number = stageNumber,
        namePl = "Misja dnia",
        nameEn = "Daily Mission",
        categories = LearningCategory.entries.toList(),
        x = .5f,
        y = .5f,
        targetAge = GameRules.targetAge(stageNumber)
    )
}

@Composable
private fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2200)
        onDone()
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.adventure_splash),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    listOf(
                        androidx.compose.ui.graphics.Color.Black.copy(alpha = .08f),
                        androidx.compose.ui.graphics.Color.Transparent,
                        androidx.compose.ui.graphics.Color.Black.copy(alpha = .62f)
                    )
                )
            )
        )
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("KACPER i KAPI", fontSize = 34.sp, fontWeight = FontWeight.Black, color = androidx.compose.ui.graphics.Color.White, textAlign = TextAlign.Center)
            Text(stringResource(R.string.game_subtitle_v2), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StarYellow, textAlign = TextAlign.Center)
            Text(stringResource(R.string.seven_worlds_one_adventure), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = androidx.compose.ui.graphics.Color.White, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(.72f).height(7.dp).clip(RoundedCornerShape(99.dp)),
                color = BrightGreen,
                trackColor = androidx.compose.ui.graphics.Color.White.copy(alpha = .35f)
            )
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.loading_adventure), color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}
