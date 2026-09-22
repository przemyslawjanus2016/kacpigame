package pl.kacperikapi.mathadventure.ui.screens

import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import pl.kacperikapi.mathadventure.R
import pl.kacperikapi.mathadventure.data.*
import pl.kacperikapi.mathadventure.ui.components.ParchmentCard
import pl.kacperikapi.mathadventure.ui.theme.*

@Composable
fun LearningGameScreen(
    stage: Stage,
    fixedCategory: LearningCategory? = null,
    performance: Map<LearningCategory, CategoryStats> = emptyMap(),
    narratorEnabled: Boolean = true,
    soundEnabled: Boolean = true,
    onBack: () -> Unit,
    onRoundFinished: (RoundResult) -> Unit
) {
    val context = LocalContext.current
    val language = AppLanguages.normalize(LocalConfiguration.current.locales[0].language)
    val engine = remember(language) { LocalizedQuestionEngine(QuestionHistoryStore(context), language) }
    val tone = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 45) }
    var ttsReady by remember { mutableStateOf(false) }
    val tts = remember {
        TextToSpeech(context.applicationContext) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            runCatching { tone.release() }
            runCatching { tts.stop(); tts.shutdown() }
        }
    }

    var questionIndex by remember(stage.id, fixedCategory, language) { mutableIntStateOf(0) }
    var correctCount by remember(stage.id, fixedCategory, language) { mutableIntStateOf(0) }
    var hearts by remember(stage.id, fixedCategory, language) { mutableIntStateOf(3) }
    var question by remember(stage.id, fixedCategory, language) { mutableStateOf(engine.next(stage, fixedCategory, performance)) }
    var answered by remember(question.id) { mutableStateOf(false) }
    var wasCorrect by remember(question.id) { mutableStateOf(false) }
    var finished by remember(stage.id, fixedCategory, language) { mutableStateOf(false) }
    var localStats by remember(stage.id, fixedCategory, language) { mutableStateOf(emptyMap<LearningCategory, CategoryStats>()) }
    val total = GameRules.QUESTIONS_PER_ROUND

    fun speakPrompt() {
        if (!narratorEnabled || !ttsReady) return
        runCatching {
            tts.language = AppLanguages.profile(language).ttsLocale
            tts.speak(question.prompt(language), TextToSpeech.QUEUE_FLUSH, null, "question-${question.id}")
        }
    }

    LaunchedEffect(question.id, ttsReady, narratorEnabled, language) {
        if (ttsReady && narratorEnabled) speakPrompt()
    }

    fun resolve(ok: Boolean) {
        if (answered) return
        answered = true
        wasCorrect = ok
        if (ok) correctCount++ else hearts = (hearts - 1).coerceAtLeast(0)
        if (soundEnabled) {
            runCatching {
                tone.startTone(if (ok) ToneGenerator.TONE_PROP_ACK else ToneGenerator.TONE_PROP_NACK, 130)
            }
        }
        engine.markSeen(question)
        val old = localStats[question.category] ?: CategoryStats()
        localStats = localStats + (question.category to old.copy(
            solved = old.solved + 1,
            correct = old.correct + if (ok) 1 else 0
        ))
    }

    fun nextQuestion() {
        questionIndex++
        if (questionIndex >= total) {
            finished = true
        } else {
            val mergedPerformance = performance.toMutableMap().apply {
                localStats.forEach { (category, round) ->
                    val old = this[category] ?: CategoryStats()
                    this[category] = CategoryStats(old.solved + round.solved, old.correct + round.correct)
                }
            }
            question = engine.next(stage, fixedCategory, mergedPerformance)
            answered = false
            wasCorrect = false
        }
    }

    BoxWithConstraints(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyBlue.copy(.36f), Cream, Parchment)))
            .statusBarsPadding()
    ) {
        val tablet = maxWidth >= 700.dp
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("← ${stringResource(R.string.back)}") }
                Spacer(Modifier.weight(1f))
                if (narratorEnabled && !finished) {
                    TextButton(onClick = { speakPrompt() }) { Text("🔊", fontSize = 22.sp) }
                }
                Text("❤️ $hearts", fontWeight = FontWeight.Black, color = HeartRed, fontSize = 20.sp)
            }

            Text(
                stage.name(language),
                fontSize = if (tablet) 30.sp else 24.sp,
                fontWeight = FontWeight.Black,
                color = AdventureGreen,
                textAlign = TextAlign.Center
            )
            Text(
                fixedCategory?.let { categoryLabel(it) } ?: stringResource(R.string.mixed_mission),
                color = WoodBrown,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { (questionIndex.toFloat() / total).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(if (tablet) .65f else 1f).height(10.dp),
                color = BrightGreen,
                trackColor = Color.White.copy(.75f)
            )
            Text(stringResource(R.string.question_count, (questionIndex + 1).coerceAtMost(total), total), color = Ink)
            Spacer(Modifier.height(16.dp))

            if (finished) {
                val earned = starsFor(correctCount, total)
                ParchmentCard(Modifier.fillMaxWidth(if (tablet) .60f else 1f)) {
                    Text(stringResource(R.string.round_complete), fontSize = 29.sp, fontWeight = FontWeight.Black, color = AdventureGreen)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.round_result, correctCount, total), fontSize = 19.sp)
                    Text(stringResource(R.string.earned_stars, earned), fontSize = 19.sp)
                    Text(if (earned == 0) "🐾" else "⭐".repeat(earned), fontSize = 38.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { onRoundFinished(RoundResult(correctCount, total, earned, localStats)) },
                        colors = ButtonDefaults.buttonColors(containerColor = AdventureGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.continue_button), fontWeight = FontWeight.Black) }
                }
            } else if (tablet) {
                Row(
                    Modifier.fillMaxWidth().widthIn(max = 980.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CharacterCard(question.id, answered, wasCorrect, Modifier.weight(.55f))
                    QuestionCard(question, language, answered, wasCorrect, ::resolve, ::nextQuestion, Modifier.weight(1f))
                }
            } else {
                QuestionCard(question, language, answered, wasCorrect, ::resolve, ::nextQuestion)
                Spacer(Modifier.height(12.dp))
                CharacterCard(question.id, answered, wasCorrect, Modifier.fillMaxWidth())
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuestionCard(
    question: LearningQuestion,
    language: String,
    answered: Boolean,
    wasCorrect: Boolean,
    onResolved: (Boolean) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    ParchmentCard(modifier.fillMaxWidth()) {
        Text("${categoryIcon(question.category, language)} ${categoryLabel(question.category)}", color = AdventureGreen, fontWeight = FontWeight.Bold)
        if (question.type != QuestionType.MEMORY) {
            question.visual?.let {
                Text(it, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 45.sp)
                Spacer(Modifier.height(4.dp))
            }
        }
        Text(
            question.prompt(language),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 25.sp,
            fontWeight = FontWeight.Black,
            color = Ink
        )
        Spacer(Modifier.height(16.dp))

        when (question.type) {
            QuestionType.MATCHING -> MatchingTask(question, language, answered, onResolved)
            QuestionType.ORDERING -> OrderingTask(question, language, answered, onResolved)
            QuestionType.MEMORY -> MemoryTask(question, language, answered, onResolved)
            else -> ChoiceTask(question, language, answered, onResolved)
        }

        if (answered) {
            Spacer(Modifier.height(10.dp))
            Text(
                if (wasCorrect) stringResource(R.string.correct_generic)
                else if (question.type == QuestionType.MATCHING || question.type == QuestionType.ORDERING) stringResource(R.string.try_again_next)
                else stringResource(R.string.wrong_generic, question.correctAnswer(language)),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Black,
                color = if (wasCorrect) AdventureGreen else HeartRed
            )
            Text(question.hint(language), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = WoodBrown, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = AdventureGreen)) {
                Text(stringResource(R.string.continue_button), fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun ChoiceTask(question: LearningQuestion, language: String, answered: Boolean, onResolved: (Boolean) -> Unit) {
    val options = question.options(language)
    options.forEachIndexed { index, option ->
        val selectedCorrect = answered && index == question.correctIndex
        val buttonColor = if (selectedCorrect) BrightGreen else listOf(SkyBlue, ActionOrange, Color(0xFF7E57C2), AdventureGreen)[index % 4]
        Button(
            onClick = { onResolved(index == question.correctIndex) },
            enabled = !answered,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).heightIn(min = 54.dp),
            shape = RoundedCornerShape(17.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                disabledContainerColor = buttonColor.copy(alpha = .82f),
                disabledContentColor = Color.White
            )
        ) { Text(option, fontSize = 18.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center) }
    }
}

@Composable
private fun MemoryTask(question: LearningQuestion, language: String, answered: Boolean, onResolved: (Boolean) -> Unit) {
    var showPattern by remember(question.id) { mutableStateOf(true) }
    LaunchedEffect(question.id) {
        kotlinx.coroutines.delay(2400)
        showPattern = false
    }
    if (showPattern && !answered) {
        Surface(shape = RoundedCornerShape(20.dp), color = SkyBlue.copy(.15f), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("👀", fontSize = 34.sp)
                Text(question.visual.orEmpty(), fontSize = 32.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                Text(stringResource(R.string.remember_it), color = WoodBrown, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        ChoiceTask(question, language, answered, onResolved)
    }
}

@Composable
private fun MatchingTask(question: LearningQuestion, language: String, answered: Boolean, onResolved: (Boolean) -> Unit) {
    val pairs = question.pairs
    var current by remember(question.id) { mutableIntStateOf(0) }
    var correctMatches by remember(question.id) { mutableIntStateOf(0) }
    if (pairs.isEmpty()) {
        ChoiceTask(question, language, answered, onResolved)
        return
    }
    val rightOptions = remember(question.id, current, language) {
        pairs.map { it.right(language) }.distinct().shuffled()
    }
    if (!answered && current < pairs.size) {
        Text(pairs[current].left(language), fontSize = 28.sp, fontWeight = FontWeight.Black, color = Ink, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        rightOptions.forEach { right ->
            OutlinedButton(
                onClick = {
                    val ok = right == pairs[current].right(language)
                    val newCorrect = correctMatches + if (ok) 1 else 0
                    correctMatches = newCorrect
                    if (current == pairs.lastIndex) onResolved(newCorrect == pairs.size) else current++
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).heightIn(min = 52.dp)
            ) { Text(right, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
        }
        Text("${current + 1}/${pairs.size}", color = WoodBrown, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    } else if (answered) {
        Text("🔗 ${stringResource(R.string.pairs_checked)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

@Composable
private fun OrderingTask(question: LearningQuestion, language: String, answered: Boolean, onResolved: (Boolean) -> Unit) {
    val options = question.options(language)
    val correct = question.correctOrder(language)
    var selected by remember(question.id) { mutableStateOf(emptyList<String>()) }
    val remaining = options.filterNot { it in selected }

    if (!answered) {
        Text(selected.joinToString("  →  ").ifBlank { "…" }, fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = AdventureGreen)
        Spacer(Modifier.height(10.dp))
        remaining.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { item ->
                    Button(
                        onClick = {
                            val next = selected + item
                            selected = next
                            if (next.size == options.size) onResolved(next == correct)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                    ) { Text(item, fontWeight = FontWeight.Black) }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
        }
        TextButton(onClick = { selected = emptyList() }, enabled = selected.isNotEmpty()) { Text(stringResource(R.string.reset_order)) }
    } else {
        Text(correct.joinToString("  →  "), fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = AdventureGreen)
    }
}

@Composable
private fun CharacterCard(
    questionId: String,
    answered: Boolean,
    wasCorrect: Boolean,
    modifier: Modifier = Modifier
) {
    val messages = when {
        answered && wasCorrect -> listOf(
            "Kacper: Super! Kapi też wiedział, że dasz radę. 🐾",
            "Kapi: Hau! Kacper: Dokładnie tak — świetna odpowiedź!",
            "Kacper: Brawo! Kapi już wypatruje następnego zadania.",
            "Kapi merda ogonem. Kacper: Punkt dla naszej drużyny!",
            "Kacper: Świetnie policzone! Kapi daje łapę na zgodę.",
            "Kapi: Hau, hau! Kacper: Tak jest — lecimy dalej!"
        )
        answered -> listOf(
            "Kacper: Sprawdźmy to jeszcze raz. Kapi zostaje z nami do końca.",
            "Kapi przekrzywia głowę. Kacper: Już wiemy więcej — następne pójdzie lepiej.",
            "Kacper: Dobra próba. Zobacz poprawną odpowiedź i zapamiętajmy ją razem.",
            "Kapi siada obok. Kacper: Uczymy się właśnie na takich zadaniach.",
            "Kacper: Było blisko. Kapi mówi „hau”, czyli: próbujemy dalej!",
            "Kapi patrzy uważnie. Kacper: Zapamiętujemy wskazówkę i ruszamy dalej."
        )
        else -> listOf(
            "Kacper: Przeczytajmy uważnie. Kapi już węszy za odpowiedzią!",
            "Kapi: Hau! Kacper: Spokojnie — najpierw pomyślmy, potem wybieramy.",
            "Kacper: Dasz radę. Kapi pilnuje, żeby żadna wskazówka nam nie uciekła.",
            "Kapi nadstawia uszy. Kacper: Co tu będzie najważniejszą wskazówką?",
            "Kacper: Spróbujmy wykluczyć złe odpowiedzi. Kapi zaczyna od tej najbardziej podejrzanej!",
            "Kapi już gotowy. Kacper: Twoja kolej — pokaż, co potrafisz!"
        )
    }
    val message = remember(questionId, answered, wasCorrect) {
        messages[(questionId.hashCode() and Int.MAX_VALUE) % messages.size]
    }

    Surface(modifier, shape = RoundedCornerShape(22.dp), color = Color.White.copy(.72f)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.app_icon),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
            )
            Text(
                message,
                modifier = Modifier.padding(10.dp),
                color = WoodBrown,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun categoryLabel(category: LearningCategory): String = stringResource(
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

private fun categoryIcon(category: LearningCategory, language: String): String = when (category) {
    LearningCategory.MATH -> "➕"
    LearningCategory.POLISH -> AppLanguages.profile(language).countryFlag
    LearningCategory.ENGLISH -> "🇬🇧"
    LearningCategory.LOGIC -> "🧠"
    LearningCategory.NATURE -> "🌿"
    LearningCategory.WORLD -> "🌍"
    LearningCategory.DAILY -> "⏰"
}

private fun starsFor(correct: Int, total: Int): Int = when {
    correct == total -> 3
    correct >= total - 2 -> 2
    correct >= total / 2 -> 1
    else -> 0
}
