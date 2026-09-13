package pl.kacperikapi.mathadventure.data

import kotlin.math.max
import kotlin.random.Random

/**
 * Hybrid question engine:
 * - large offline seed banks,
 * - procedural math / logic / time / money tasks,
 * - multiple language templates,
 * - repeat suppression through QuestionHistoryStore.
 *
 * This gives far more combinations than a fixed list of quiz questions.
 */
class QuestionEngine(
    private val history: QuestionHistoryStore,
    private val random: Random = Random.Default
) {
    private var adaptiveDifficultyOffset: Int = 0

    fun next(
        stage: Stage,
        fixedCategory: LearningCategory? = null,
        performance: Map<LearningCategory, CategoryStats> = emptyMap()
    ): LearningQuestion {
        val recent = history.recentIds()
        var fallback: LearningQuestion? = null
        repeat(40) {
            val category = fixedCategory ?: weightedCategory(stage, performance)
            adaptiveDifficultyOffset = difficultyOffset(performance[category])
            val candidate = generate(category, stage)
            fallback = candidate
            if (candidate.id !in recent) return candidate
        }
        val category = fixedCategory ?: LearningCategory.MATH
        adaptiveDifficultyOffset = difficultyOffset(performance[category])
        return fallback ?: generate(category, stage)
    }

    fun markSeen(question: LearningQuestion) = history.remember(question.id)

    private fun weightedCategory(
        stage: Stage,
        performance: Map<LearningCategory, CategoryStats>
    ): LearningCategory {
        val pool = buildList {
            addAll(stage.categories)
            addAll(stage.categories)
            addAll(LearningCategory.entries)
            // W mieszanych misjach częściej wracamy do tematów, które wymagają powtórki.
            performance.forEach { (category, stats) ->
                if (stats.solved >= 5 && stats.accuracyPercent < 70) {
                    repeat(3) { add(category) }
                } else if (stats.solved >= 5 && stats.accuracyPercent < 82) {
                    add(category)
                }
            }
        }
        return pool.random(random)
    }

    private fun difficultyOffset(stats: CategoryStats?): Int = when {
        stats == null || stats.solved < 8 -> 0
        stats.accuracyPercent >= 92 -> 2
        stats.accuracyPercent >= 82 -> 1
        stats.accuracyPercent < 58 -> -2
        stats.accuracyPercent < 70 -> -1
        else -> 0
    }

    private fun generate(category: LearningCategory, stage: Stage): LearningQuestion = when (category) {
        LearningCategory.MATH -> math(stage)
        LearningCategory.POLISH -> polish(stage)
        LearningCategory.ENGLISH -> english(stage)
        LearningCategory.LOGIC -> logic(stage)
        LearningCategory.NATURE -> nature(stage)
        LearningCategory.WORLD -> worldKnowledge(stage)
        LearningCategory.DAILY -> daily(stage)
    }

    private fun math(stage: Stage): LearningQuestion {
        val d = difficulty(stage)
        val kinds = when {
            d <= 2 -> listOf("add", "sub", "missing", "compare")
            d <= 5 -> listOf("add", "sub", "mul", "missing", "compare", "money", "story")
            else -> listOf("add", "sub", "mul", "div", "missing", "compare", "money", "story")
        }
        return when (kinds.random(random)) {
            "add" -> numberOperation(stage, "+")
            "sub" -> numberOperation(stage, "−")
            "mul" -> numberOperation(stage, "×")
            "div" -> numberOperation(stage, "÷")
            "missing" -> missingNumber(stage)
            "compare" -> compareNumbers(stage)
            "money" -> money(stage)
            else -> storyMath(stage)
        }
    }

    private fun numberOperation(stage: Stage, op: String): LearningQuestion {
        val d = difficulty(stage)
        val (a, b, answer) = when (op) {
            "+" -> {
                val top = 25 + d * 18
                val x = random.nextInt(3, top)
                val y = random.nextInt(2, max(4, top / 2))
                Triple(x, y, x + y)
            }
            "−" -> {
                val top = 35 + d * 18
                val x = random.nextInt(12, top)
                val y = random.nextInt(2, x)
                Triple(x, y, x - y)
            }
            "×" -> {
                val x = random.nextInt(2, (4 + d).coerceAtMost(11))
                val y = random.nextInt(2, 11)
                Triple(x, y, x * y)
            }
            else -> {
                val y = random.nextInt(2, 11)
                val result = random.nextInt(2, (4 + d).coerceAtMost(13))
                Triple(y * result, y, result)
            }
        }
        val prompt = "$a $op $b = ?"
        return numericQuestion(
            id = "math:${op}:${a}:${b}",
            stage = stage,
            promptPl = "Oblicz: $prompt",
            promptEn = "Calculate: $prompt",
            answer = answer,
            hintPl = "Policz spokojnie krok po kroku.",
            hintEn = "Work it out one step at a time."
        )
    }

    private fun missingNumber(stage: Stage): LearningQuestion {
        val d = difficulty(stage)
        val multiplication = d >= 3 && random.nextBoolean()
        return if (multiplication) {
            val a = random.nextInt(2, (4 + d).coerceAtMost(11))
            val answer = random.nextInt(2, 11)
            val result = a * answer
            numericQuestion(
                "math:missingMul:$a:$result",
                stage,
                "Znajdź brakującą liczbę: $a × ? = $result",
                "Find the missing number: $a × ? = $result",
                answer,
                "Pomyśl: przez co trzeba pomnożyć $a?",
                "What number times $a gives $result?"
            )
        } else {
            val answer = random.nextInt(2, 20 + d * 5)
            val add = random.nextInt(2, 15 + d * 3)
            val result = answer + add
            numericQuestion(
                "math:missingAdd:$add:$result",
                stage,
                "Znajdź brakującą liczbę: ? + $add = $result",
                "Find the missing number: ? + $add = $result",
                answer,
                "Od wyniku odejmij znaną liczbę.",
                "Subtract the known number from the result."
            )
        }
    }

    private fun compareNumbers(stage: Stage): LearningQuestion {
        var a = random.nextInt(5, 50 + difficulty(stage) * 20)
        var b = random.nextInt(5, 50 + difficulty(stage) * 20)
        if (a == b) b += 1
        val correct = if (a > b) ">" else "<"
        return shuffledQuestion(
            id = "math:compare:$a:$b",
            category = LearningCategory.MATH,
            type = QuestionType.CHOICE,
            promptPl = "Który znak pasuje?  $a  ?  $b",
            promptEn = "Which sign fits?  $a  ?  $b",
            optionsPl = listOf(">", "<", "=", "≠"),
            optionsEn = listOf(">", "<", "=", "≠"),
            correctPl = correct,
            hintPl = "Większa liczba powinna być po szerszej stronie znaku.",
            hintEn = "The larger number belongs on the wider side of the sign."
        )
    }

    private fun money(stage: Stage): LearningQuestion {
        val prices = listOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 18)
        val p1 = prices.random(random)
        val p2 = prices.random(random)
        val answer = p1 + p2
        return numericQuestion(
            "math:money:$p1:$p2",
            stage,
            "Kacper kupuje pamiątkę za $p1 zł i pocztówkę za $p2 zł. Ile płaci razem?",
            "Kacper buys a souvenir for $p1 PLN and a postcard for $p2 PLN. How much altogether?",
            answer,
            "Dodaj obie ceny.",
            "Add the two prices.",
            visual = "🪙"
        )
    }

    private fun storyMath(stage: Stage): LearningQuestion {
        val nouns = listOf("kryształów" to "crystals", "naklejek" to "stickers", "pocztówek" to "postcards", "muszelek" to "shells", "odznak" to "badges")
        val noun = nouns.random(random)
        val a = random.nextInt(4, 15 + difficulty(stage) * 2)
        val b = random.nextInt(2, 10 + difficulty(stage))
        val addition = random.nextBoolean()
        val answer = if (addition) a + b else max(0, a - b.coerceAtMost(a))
        val safeB = if (addition) b else b.coerceAtMost(a)
        val idOp = if (addition) "plus" else "minus"
        val pl = if (addition)
            "Kapi znalazł $a ${noun.first}, a Kacper jeszcze $safeB. Ile mają razem?"
        else
            "Kacper miał $a ${noun.first} i podarował $safeB. Ile mu zostało?"
        val en = if (addition)
            "Kapi found $a ${noun.second}, and Kacper found $safeB more. How many altogether?"
        else
            "Kacper had $a ${noun.second} and gave away $safeB. How many are left?"
        return numericQuestion(
            "math:story:$idOp:$a:$safeB:${noun.first}",
            stage, pl, en, answer,
            "Zastanów się, czy trzeba dodać, czy odjąć.",
            "Decide whether to add or subtract.",
            visual = "🎒"
        )
    }

    private fun polish(stage: Stage): LearningQuestion {
        return when (random.nextInt(6)) {
            0 -> {
                val o = QuestionBank.orthography.random(random)
                shuffledQuestion(
                    "pl:ortho:${o.correct}", LearningCategory.POLISH, QuestionType.CHOICE,
                    "Który wyraz jest zapisany poprawnie?", "Which Polish word is spelled correctly?",
                    listOf(o.correct) + o.wrong.take(3), listOf(o.correct) + o.wrong.take(3), o.correct,
                    "Przeczytaj każde słowo powoli.", "Read each Polish word slowly."
                )
            }
            1 -> {
                val s = QuestionBank.syllables.random(random)
                numericQuestion(
                    "pl:syll:${s.word}", stage,
                    "Ile sylab ma słowo „${s.word}”?", "How many syllables are in the Polish word “${s.word}”?",
                    s.syllables, "Wypowiedz słowo rytmicznie i zaklaszcz.", "Say the word slowly and clap each syllable.",
                    category = LearningCategory.POLISH
                )
            }
            2 -> grammarQuestion(stage, "noun")
            3 -> grammarQuestion(stage, "verb")
            4 -> grammarQuestion(stage, "adjective")
            else -> punctuationQuestion(stage)
        }
    }

    private fun grammarQuestion(stage: Stage, kind: String): LearningQuestion {
        val nouns = listOf("rower", "pies", "zamek", "książka", "miasto", "drzewo", "Kapi", "plecak", "rzeka", "kwiat")
        val verbs = listOf("biegnie", "czyta", "rysuje", "skacze", "śpi", "płynie", "odkrywa", "liczy", "pisze", "słucha")
        val adjectives = listOf("zielony", "wesoły", "wysoki", "mały", "szybki", "ciepły", "ciekawy", "solny", "górski", "kolorowy")
        val adverbs = listOf("szybko", "wesoło", "cicho", "uważnie", "daleko", "blisko", "dzisiaj", "wczoraj")
        val (plName, enName, correctPool, wrongPools) = when (kind) {
            "noun" -> Quad("rzeczownikiem", "a noun", nouns, listOf(verbs, adjectives, adverbs))
            "verb" -> Quad("czasownikiem", "a verb", verbs, listOf(nouns, adjectives, adverbs))
            else -> Quad("przymiotnikiem", "an adjective", adjectives, listOf(nouns, verbs, adverbs))
        }
        val correct = correctPool.random(random)
        val wrong = wrongPools.map { it.random(random) }
        val all = listOf(correct) + wrong
        return shuffledQuestion(
            "pl:grammar:$kind:${all.sorted().joinToString("-")}", LearningCategory.POLISH, QuestionType.CHOICE,
            "Które słowo jest $plName?", "Which Polish word is $enName?",
            all, all, correct,
            "Pomyśl, co nazywa rzecz, czynność albo cechę.", "Think about whether the word names a thing, action or quality."
        )
    }

    private data class Quad(
        val plName: String,
        val enName: String,
        val correctPool: List<String>,
        val wrongPools: List<List<String>>
    )

    private fun punctuationQuestion(stage: Stage): LearningQuestion {
        val variants = listOf(
            Triple("Gdzie jest Kapi", "?", "To jest pytanie."),
            Triple("Ale piękny widok", "!", "To zdanie wyraża emocje."),
            Triple("Kacper idzie do szkoły", ".", "To zwykłe zdanie oznajmujące."),
            Triple("Czy lubisz podróże", "?", "To jest pytanie."),
            Triple("Brawo Kapi", "!", "To okrzyk."))
        val v = variants.random(random)
        return shuffledQuestion(
            "pl:punct:${v.first}", LearningCategory.POLISH, QuestionType.CHOICE,
            "Jaki znak kończy zdanie: „${v.first}”", "Which mark ends the Polish sentence: “${v.first}”",
            listOf(".", "?", "!", ","), listOf(".", "?", "!", ","), v.second,
            v.third, v.third
        )
    }

    private fun english(stage: Stage): LearningQuestion {
        val word = QuestionBank.englishWords.random(random)
        val sameGroup = QuestionBank.englishWords.filter { it.group == word.group && it != word }.shuffled(random).take(3)
        return when (random.nextInt(10)) {
            0 -> shuffledQuestion(
                "en:pl2en:${word.pl}", LearningCategory.ENGLISH, QuestionType.CHOICE,
                "Jak jest po angielsku „${word.pl}”?", "What is “${word.pl}” in English?",
                listOf(word.en) + sameGroup.map { it.en }, listOf(word.en) + sameGroup.map { it.en }, word.en,
                "Spróbuj skojarzyć słowo z obrazkiem ${word.emoji}.", "Think of the picture ${word.emoji}.", word.emoji
            )
            1 -> shuffledQuestion(
                "en:en2pl:${word.en}", LearningCategory.ENGLISH, QuestionType.CHOICE,
                "Co znaczy „${word.en}”?", "Which Polish word means “${word.en}”?",
                listOf(word.pl) + sameGroup.map { it.pl }, listOf(word.pl) + sameGroup.map { it.pl }, word.pl,
                "Podpowiedź: ${word.emoji}", "Hint: ${word.emoji}", word.emoji
            )
            2 -> shuffledQuestion(
                "en:emoji:${word.en}", LearningCategory.ENGLISH, QuestionType.IMAGE_CHOICE,
                "Które angielskie słowo pasuje do ${word.emoji}?", "Which English word matches ${word.emoji}?",
                listOf(word.en) + sameGroup.map { it.en }, listOf(word.en) + sameGroup.map { it.en }, word.en,
                "Nazwij obrazek najpierw po polsku.", "Name the picture in your first language first.", word.emoji
            )
            3 -> {
                val groupNames = mapOf("animals" to "animals", "places" to "places", "nature" to "nature", "weather" to "weather", "colors" to "colours", "numbers" to "numbers", "food" to "food", "body" to "body parts", "time" to "time", "days" to "days", "school" to "school", "home" to "home")
                val target = groupNames[word.group] ?: word.group
                val distractors = groupNames.values.filter { it != target }.shuffled(random).take(3)
                shuffledQuestion(
                    "en:group:${word.en}", LearningCategory.ENGLISH, QuestionType.CHOICE,
                    "Do jakiej grupy należy angielskie słowo „${word.en}”?", "Which group does the word “${word.en}” belong to?",
                    listOf(target) + distractors, listOf(target) + distractors, target,
                    "Pomyśl, co oznacza to słowo.", "Think about what the word means.", word.emoji
                )
            }
            4 -> {
                val phrases = listOf(
                    Triple("Dzień dobry", "Good morning", "🌅"), Triple("Dziękuję", "Thank you", "🙏"),
                    Triple("Proszę", "Please", "🙂"), Triple("Do widzenia", "Goodbye", "👋"),
                    Triple("Jak się masz?", "How are you?", "😊"), Triple("Mam na imię Kacper", "My name is Kacper", "👦"),
                    Triple("Gdzie jest toaleta?", "Where is the toilet?", "🚻"), Triple("Poproszę wodę", "Water, please", "💧"),
                    Triple("Ile to kosztuje?", "How much is it?", "💰"), Triple("Pomocy!", "Help!", "🆘"),
                    Triple("Tak", "Yes", "✅"), Triple("Nie", "No", "❌"))
                val p = phrases.random(random)
                val others = phrases.filter { it != p }.shuffled(random).take(3)
                shuffledQuestion(
                    "en:phrase:${p.second}", LearningCategory.ENGLISH, QuestionType.CHOICE,
                    "Jak powiesz po angielsku: „${p.first}”?", "How do you say “${p.first}” in English?",
                    listOf(p.second) + others.map { it.second }, listOf(p.second) + others.map { it.second }, p.second,
                    "To przydatne zdanie w podróży.", "A useful phrase while travelling.", p.third
                )
            }
            5 -> englishSpelling(word)
            6 -> englishFirstLetter(word)
            7 -> englishWordLength()
            8 -> englishOddGroup(word)
            else -> englishMatching()
        }
    }


    private fun englishMatching(): LearningQuestion {
        val words = QuestionBank.englishWords
            .shuffled(random)
            .distinctBy { it.en }
            .take(3)
        val pairs = words.map { MatchPair(it.emoji + " " + it.pl, it.emoji + " " + it.pl, it.en, it.en) }
        return LearningQuestion(
            id = "en:match:" + words.map { it.en }.sorted().joinToString("-"),
            category = LearningCategory.ENGLISH,
            type = QuestionType.MATCHING,
            promptPl = "Połącz obrazek i polskie słowo z angielskim odpowiednikiem.",
            promptEn = "Match each picture and Polish word with its English word.",
            optionsPl = emptyList(),
            optionsEn = emptyList(),
            correctIndex = 0,
            hintPl = "Zacznij od słowa, które znasz najlepiej.",
            hintEn = "Start with the word you know best.",
            pairs = pairs
        )
    }

    private fun englishSpelling(word: QuestionBank.WordPair): LearningQuestion {
        val correct = word.en
        val wrong = linkedSetOf<String>()
        if (correct.length > 2) wrong += correct.dropLast(1)
        if (correct.length > 3) wrong += correct.substring(0, 1) + correct[2] + correct[1] + correct.substring(3)
        wrong += correct + correct.last()
        wrong += correct.first() + correct
        var n = 0
        while (wrong.size < 3) {
            wrong += correct + ('a'.code + (n++ % 26)).toChar()
        }
        return shuffledQuestion(
            "en:spelling:$correct", LearningCategory.ENGLISH, QuestionType.CHOICE,
            "Który zapis angielskiego słowa jest poprawny? ${word.emoji}", "Which spelling is correct? ${word.emoji}",
            listOf(correct) + wrong.take(3), listOf(correct) + wrong.take(3), correct,
            "Spójrz uważnie na kolejność liter.", "Look carefully at the order of the letters.", word.emoji
        )
    }

    private fun englishFirstLetter(word: QuestionBank.WordPair): LearningQuestion {
        val correct = word.en.first().uppercase()
        val letters = linkedSetOf(correct)
        while (letters.size < 4) letters += ('A'.code + random.nextInt(26)).toChar().toString()
        return shuffledQuestion(
            "en:first:${word.en}", LearningCategory.ENGLISH, QuestionType.CHOICE,
            "Jaką literą zaczyna się angielskie słowo „${word.en}”?", "Which letter does “${word.en}” start with?",
            letters.toList(), letters.toList(), correct,
            "Spójrz na pierwszą literę słowa.", "Look at the first letter of the word.", word.emoji
        )
    }

    private fun englishWordLength(): LearningQuestion {
        val four = QuestionBank.englishWords.shuffled(random).take(4)
        val maxLen = four.maxOf { it.en.length }
        val candidates = four.filter { it.en.length == maxLen }
        val correctWord = candidates.random(random)
        val idWords = four.map { it.en }.sorted().joinToString("-")
        return shuffledQuestion(
            "en:longest:$idWords", LearningCategory.ENGLISH, QuestionType.CHOICE,
            "Które angielskie słowo ma najwięcej liter?", "Which English word has the most letters?",
            four.map { it.en }, four.map { it.en }, correctWord.en,
            "Policz litery w każdym słowie.", "Count the letters in each word."
        )
    }

    private fun englishOddGroup(word: QuestionBank.WordPair): LearningQuestion {
        val same = QuestionBank.englishWords.filter { it.group == word.group }.shuffled(random).take(3)
        val odd = QuestionBank.englishWords.filter { it.group != word.group }.random(random)
        val options = (same + odd).distinctBy { it.en }
        if (options.size < 4) return englishFirstLetter(word)
        return shuffledQuestion(
            "en:odd:${options.map { it.en }.sorted().joinToString("-")}", LearningCategory.ENGLISH, QuestionType.CHOICE,
            "Które angielskie słowo nie pasuje do pozostałych?", "Which English word does not belong with the others?",
            options.map { it.en }, options.map { it.en }, odd.en,
            "Trzy słowa należą do jednej grupy znaczeniowej.", "Three words belong to the same meaning group."
        )
    }

    private fun logic(stage: Stage): LearningQuestion = when (random.nextInt(6)) {
        0 -> arithmeticSequence(stage)
        1 -> repeatingPattern(stage)
        2 -> oddOneOut(stage)
        3 -> ordering(stage)
        4 -> analogy(stage)
        else -> memoryPattern(stage)
    }

    private fun arithmeticSequence(stage: Stage): LearningQuestion {
        val step = random.nextInt(2, 3 + difficulty(stage).coerceAtMost(7))
        val start = random.nextInt(1, 12)
        val values = List(4) { start + it * step }
        val answer = start + 4 * step
        return numericQuestion(
            "logic:seq:$start:$step", stage,
            "Dokończ ciąg: ${values.joinToString(", ")}, ?",
            "Complete the sequence: ${values.joinToString(", ")}, ?",
            answer, "Każda liczba rośnie o tyle samo.", "Each number increases by the same amount.",
            category = LearningCategory.LOGIC, type = QuestionType.SEQUENCE
        )
    }

    private fun repeatingPattern(stage: Stage): LearningQuestion {
        val patterns = listOf(
            Pair(listOf("🔵", "🟡"), "🔵"), Pair(listOf("⭐", "🌙"), "⭐"),
            Pair(listOf("🌲", "🌲", "🌼"), "🌲"), Pair(listOf("🐾", "🧭", "🎒"), "🐾"),
            Pair(listOf("▲", "●", "■"), "▲"))
        val p = patterns.random(random)
        val shown = (p.first + p.first).take(5).joinToString("  ")
        val options = (p.first + listOf("❤️", "◆", "🟢")).distinct().take(4)
        return shuffledQuestion(
            "logic:pattern:${p.first.joinToString("")}", LearningCategory.LOGIC, QuestionType.SEQUENCE,
            "Co jest następne?  $shown  ?", "What comes next?  $shown  ?",
            options, options, p.second,
            "Poszukaj powtarzającego się rytmu.", "Look for the repeating rhythm."
        )
    }

    private fun oddOneOut(stage: Stage): LearningQuestion {
        val sets = listOf(
            Pair(listOf("pies", "kot", "koń", "krzesło"), "krzesło"),
            Pair(listOf("czerwony", "zielony", "niebieski", "rower"), "rower"),
            Pair(listOf("jabłko", "banan", "truskawka", "but"), "but"),
            Pair(listOf("Warszawa", "Kraków", "Londyn", "marchewka"), "marchewka"),
            Pair(listOf("2", "4", "6", "7"), "7"))
        val set = sets.random(random)
        return shuffledQuestion(
            "logic:odd:${set.second}", LearningCategory.LOGIC, QuestionType.CHOICE,
            "Co nie pasuje do pozostałych?", "Which one does not belong?",
            set.first, set.first, set.second,
            "Trzy elementy mają wspólną cechę.", "Three items share something in common."
        )
    }

    private fun ordering(stage: Stage): LearningQuestion {
        val values = linkedSetOf<Int>()
        while (values.size < 4) values += random.nextInt(2, 35 + difficulty(stage) * 8)
        val correct = values.sorted().map(Int::toString)
        val shuffled = correct.shuffled(random)
        return LearningQuestion(
            id = "logic:order:" + correct.joinToString("-"),
            category = LearningCategory.LOGIC,
            type = QuestionType.ORDERING,
            promptPl = "Ułóż liczby od najmniejszej do największej.",
            promptEn = "Put the numbers in order from smallest to largest.",
            optionsPl = shuffled,
            optionsEn = shuffled,
            correctIndex = 0,
            hintPl = "Najpierw znajdź najmniejszą liczbę.",
            hintEn = "Find the smallest number first.",
            correctOrderPl = correct,
            correctOrderEn = correct
        )
    }

    private fun memoryPattern(stage: Stage): LearningQuestion {
        val symbols = listOf("⭐", "🐾", "🧭", "🌲", "🌼", "💎", "🏰", "⛵")
        val length = if (difficulty(stage) >= 6) 4 else 3
        val sequence = List(length) { symbols.random(random) }
        val correct = sequence.joinToString(" ")
        val options = linkedSetOf(correct)
        var guard = 0
        while (options.size < 4 && guard++ < 30) {
            val candidate = if (guard < 12) sequence.shuffled(random).joinToString(" ")
            else List(length) { symbols.random(random) }.joinToString(" ")
            options += candidate
        }
        while (options.size < 4) {
            options += (symbols.take(length - 1) + symbols[(options.size + length) % symbols.size]).joinToString(" ")
        }
        return shuffledQuestion(
            "logic:memory:" + sequence.joinToString(""),
            LearningCategory.LOGIC,
            QuestionType.MEMORY,
            "Zapamiętaj kolejność symboli.",
            "Remember the order of the symbols.",
            options.toList(),
            options.toList(),
            correct,
            "Spójrz od lewej do prawej.",
            "Look from left to right.",
            correct
        )
    }

    private fun analogy(stage: Stage): LearningQuestion {
        val analogies = listOf(
            Triple("ptak : lata = ryba : ?", "pływa", listOf("pływa", "czyta", "rysuje", "śpi")),
            Triple("dzień : słońce = noc : ?", "księżyc", listOf("księżyc", "rower", "chleb", "zamek")),
            Triple("but : stopa = rękawiczka : ?", "dłoń", listOf("dłoń", "ucho", "nos", "kolano")),
            Triple("lato : ciepło = zima : ?", "zimno", listOf("zimno", "głośno", "okrągło", "słodko")))
        val a = analogies.random(random)
        return shuffledQuestion(
            "logic:analogy:${a.first}", LearningCategory.LOGIC, QuestionType.CHOICE,
            "Uzupełnij: ${a.first}", "Complete the analogy: ${a.first}",
            a.third, a.third, a.second,
            "Znajdź podobną relację między słowami.", "Find the same relationship between the words."
        )
    }

    private fun nature(stage: Stage): LearningQuestion {
        return when (random.nextInt(4)) {
            0 -> factQuestion(stage, LearningCategory.NATURE)
            1 -> {
                val animal = QuestionBank.englishWords.filter { it.group == "animals" }.random(random)
                val nonAnimals = QuestionBank.englishWords.filter { it.group != "animals" }.shuffled(random).take(3)
                val optionsPl = listOf(animal.pl) + nonAnimals.map { it.pl }
                val optionsEn = listOf(animal.en) + nonAnimals.map { it.en }
                shuffledQuestion(
                    "nature:animal:${animal.en}:${nonAnimals.joinToString { it.en }}",
                    LearningCategory.NATURE, QuestionType.IMAGE_CHOICE,
                    "Który z tych wyrazów oznacza zwierzę?", "Which of these words names an animal?",
                    optionsPl, optionsEn, animal.pl,
                    "Zwierzę może się samodzielnie poruszać i odżywia się gotowym pokarmem.",
                    "An animal can move on its own and eats food.", animal.emoji
                )
            }
            2 -> {
                val seasonsPl = listOf("wiosna", "lato", "jesień", "zima")
                val seasonsEn = listOf("spring", "summer", "autumn", "winter")
                val i = random.nextInt(4)
                val next = (i + 1) % 4
                val order = (0..3).shuffled(random)
                LearningQuestion(
                    "nature:season:$i", LearningCategory.NATURE, QuestionType.CHOICE,
                    "Jaka pora roku jest po: ${seasonsPl[i]}?", "Which season comes after ${seasonsEn[i]}?",
                    order.map { seasonsPl[it] }, order.map { seasonsEn[it] }, order.indexOf(next),
                    "Pory roku powtarzają się w stałej kolejności.", "The seasons repeat in a fixed order.", "🌱☀️🍂❄️"
                )
            }
            else -> {
                val habitats = listOf(
                    Triple("ryba", "w wodzie", "in water"), Triple("wiewiórka", "w lesie i parkach", "in forests and parks"),
                    Triple("kozica", "w górach", "in the mountains"), Triple("żaba", "blisko wody", "near water"),
                    Triple("pszczoła", "w pobliżu kwiatów", "near flowers"))
                val h = habitats.random(random)
                val optsPl = listOf(h.second, "na Księżycu", "w lodówce", "na środku autostrady")
                val optsEn = listOf(h.third, "on the Moon", "in a fridge", "in the middle of a motorway")
                shuffledQuestion(
                    "nature:habitat:${h.first}", LearningCategory.NATURE, QuestionType.CHOICE,
                    "Gdzie naturalnie możemy spotkać: ${h.first}?", "Where can we naturally find: ${h.first}?",
                    optsPl, optsEn, h.second,
                    "Pomyśl, czego to zwierzę potrzebuje do życia.", "Think about what this animal needs to live."
                )
            }
        }
    }

    private fun worldKnowledge(stage: Stage): LearningQuestion {
        return when (random.nextInt(4)) {
            0 -> factQuestion(stage, LearningCategory.WORLD)
            1 -> {
                val world = GameContent.world(stage.worldId)
                val correctStage = world.stages.random(random)
                val otherStages = GameContent.worlds.filter { it.id != world.id }
                    .flatMap { it.stages }.shuffled(random).take(3)
                val optionsPl = listOf(correctStage.namePl) + otherStages.map { it.namePl }
                val optionsEn = listOf(correctStage.nameEn) + otherStages.map { it.nameEn }
                shuffledQuestion(
                    "world:${stage.worldId}:landmark:${correctStage.id}:${otherStages.joinToString { it.id }}",
                    LearningCategory.WORLD, QuestionType.CHOICE,
                    "Które miejsce odwiedzamy w świecie ${worldNamePl(world.id)}?",
                    "Which place do we visit in ${worldNameEn(world.id)}?",
                    optionsPl, optionsEn, correctStage.namePl,
                    "Przypomnij sobie mapę tego świata.", "Remember the map of this world.", world.icon
                )
            }
            2 -> {
                val countries = mapOf(1 to ("Polska" to "Poland"), 2 to ("Polska" to "Poland"), 3 to ("Polska" to "Poland"), 4 to ("Włochy" to "Italy"), 5 to ("Wielka Brytania" to "United Kingdom"), 6 to ("Włochy" to "Italy"), 7 to ("Malta" to "Malta"))
                val correct = countries.getValue(stage.worldId)
                val pool = listOf("Polska" to "Poland", "Włochy" to "Italy", "Wielka Brytania" to "United Kingdom", "Malta" to "Malta", "Francja" to "France", "Hiszpania" to "Spain")
                val wrong = pool.filter { it != correct }.shuffled(random).take(3)
                val all = listOf(correct) + wrong
                val shuffled = all.shuffled(random)
                LearningQuestion(
                    "world:${stage.worldId}:country:${correct.second}", LearningCategory.WORLD, QuestionType.CHOICE,
                    "W jakim kraju leży ${worldNamePl(stage.worldId)}?", "Which country is ${worldNameEn(stage.worldId)} in?",
                    shuffled.map { it.first }, shuffled.map { it.second }, shuffled.indexOf(correct),
                    "Spójrz na podróż Kacpra i Kapi po Europie.", "Think about Kacper and Kapi's journey across Europe.", "🗺️"
                )
            }
            else -> {
                val order = GameContent.worlds.map { it.id }
                val current = stage.worldId
                val answer = if (current < order.last()) current + 1 else current
                val candidates = order.shuffled(random).take(4).toMutableList()
                if (answer !in candidates) candidates[0] = answer
                candidates.shuffle(random)
                LearningQuestion(
                    "world:${stage.worldId}:route:$current", LearningCategory.WORLD, QuestionType.CHOICE,
                    if (current < 7) "Jaki świat jest następny po ${worldNamePl(current)}?" else "Jaki świat kończy obecną podróż?",
                    if (current < 7) "Which world comes after ${worldNameEn(current)}?" else "Which world ends the current journey?",
                    candidates.map { worldNamePl(it) }, candidates.map { worldNameEn(it) }, candidates.indexOf(answer),
                    "Kolejność: Wieliczka, Kraków, Tatry, Rzym, Londyn, Mediolan, Malta.",
                    "Order: Wieliczka, Krakow, Tatras, Rome, London, Milan, Malta.", "🧭"
                )
            }
        }
    }

    private fun worldNamePl(id: Int): String = when (id) {
        1 -> "Wieliczka"; 2 -> "Kraków"; 3 -> "Tatry"; 4 -> "Rzym"; 5 -> "Londyn"; 6 -> "Mediolan"; else -> "Malta"
    }

    private fun worldNameEn(id: Int): String = when (id) {
        1 -> "Wieliczka"; 2 -> "Krakow"; 3 -> "the Tatras"; 4 -> "Rome"; 5 -> "London"; 6 -> "Milan"; else -> "Malta"
    }

    private fun factQuestion(stage: Stage, category: LearningCategory): LearningQuestion {
        val candidates = QuestionBank.facts.filter {
            it.category == category && (it.worldId == null || it.worldId == stage.worldId)
        }
        val fact = (candidates.ifEmpty { QuestionBank.facts.filter { it.category == category } }).random(random)
        val key = fact.promptEn.hashCode().toUInt().toString(16)
        val paired = fact.optionsPl.indices.map { i -> fact.optionsPl[i] to fact.optionsEn[i] }
        val correctPair = paired[fact.correctIndex]
        val shuffled = paired.shuffled(random)
        return LearningQuestion(
            id = "fact:${fact.worldId ?: 0}:${category.name}:$key",
            category = category,
            type = QuestionType.CHOICE,
            promptPl = fact.promptPl,
            promptEn = fact.promptEn,
            optionsPl = shuffled.map { it.first },
            optionsEn = shuffled.map { it.second },
            correctIndex = shuffled.indexOf(correctPair),
            hintPl = fact.hintPl,
            hintEn = fact.hintEn
        )
    }

    private fun daily(stage: Stage): LearningQuestion = when (random.nextInt(10)) {
        0 -> clockQuestion(stage)
        1 -> changeQuestion(stage)
        2 -> weekdayQuestion(stage)
        3 -> calendarQuestion(stage)
        4 -> measurementQuestion(stage)
        5 -> directionQuestion(stage)
        6 -> durationQuestion(stage)
        7 -> shoppingTotalQuestion(stage)
        8 -> temperatureQuestion(stage)
        else -> everydayUnitQuestion(stage)
    }

    private fun durationQuestion(stage: Stage): LearningQuestion {
        val hour = random.nextInt(7, 20)
        val minute = listOf(0, 15, 30, 45).random(random)
        val duration = listOf(15, 30, 45, 60, 90).random(random)
        val startTotal = hour * 60 + minute
        val endTotal = (startTotal + duration) % (24 * 60)
        val answer = "%02d:%02d".format(endTotal / 60, endTotal % 60)
        val options = linkedSetOf(answer)
        while (options.size < 4) {
            val delta = listOf(-30, -15, 15, 30, 45, 60).random(random)
            val t = (endTotal + delta).coerceIn(0, 23 * 60 + 59)
            options += "%02d:%02d".format(t / 60, t % 60)
        }
        val start = "%02d:%02d".format(hour, minute)
        return shuffledQuestion(
            "daily:duration:$start:$duration", LearningCategory.DAILY, QuestionType.CHOICE,
            "Misja zaczyna się o $start i trwa $duration minut. O której się skończy?",
            "The mission starts at $start and lasts $duration minutes. When does it end?",
            options.toList(), options.toList(), answer,
            "Dodaj czas trwania do godziny rozpoczęcia.", "Add the duration to the start time.", "⏱️"
        )
    }

    private fun shoppingTotalQuestion(stage: Stage): LearningQuestion {
        val a = random.nextInt(1, 31)
        val b = random.nextInt(1, 31)
        val c = if (difficulty(stage) >= 5 && random.nextBoolean()) random.nextInt(1, 21) else 0
        val answer = a + b + c
        val pl = if (c == 0) "Kupujesz dwie rzeczy za $a zł i $b zł. Ile płacisz razem?" else "Kupujesz trzy rzeczy za $a zł, $b zł i $c zł. Ile płacisz razem?"
        val en = if (c == 0) "You buy two things for $a PLN and $b PLN. How much altogether?" else "You buy three things for $a PLN, $b PLN and $c PLN. How much altogether?"
        return numericQuestion(
            "daily:shopping:$a:$b:$c", stage, pl, en, answer,
            "Dodaj wszystkie ceny.", "Add all the prices.", "🛍️", LearningCategory.DAILY
        )
    }

    private fun temperatureQuestion(stage: Stage): LearningQuestion {
        var a = random.nextInt(-10, 36)
        var b = random.nextInt(-10, 36)
        if (a == b) b = (b + 1).coerceAtMost(36)
        val answer = if (a > b) "$a°C" else "$b°C"
        return shuffledQuestion(
            "daily:temp:$a:$b", LearningCategory.DAILY, QuestionType.CHOICE,
            "Która temperatura jest wyższa?", "Which temperature is higher?",
            listOf("$a°C", "$b°C", "są równe", "nie da się porównać"),
            listOf("$a°C", "$b°C", "they are equal", "cannot compare"),
            answer,
            "Na osi liczb większa liczba leży bardziej na prawo.", "On a number line, the larger number is farther to the right.", "🌡️"
        )
    }

    private fun everydayUnitQuestion(stage: Stage): LearningQuestion {
        val items = listOf(
            Triple("długość ołówka", "centymetry", "centimetres"),
            Triple("długość pokoju", "metry", "metres"),
            Triple("masę plecaka", "kilogramy", "kilograms"),
            Triple("ilość wody w butelce", "litry", "litres"),
            Triple("czas lekcji", "minuty", "minutes"),
            Triple("odległość między miastami", "kilometry", "kilometres"),
            Triple("temperaturę powietrza", "stopnie Celsjusza", "degrees Celsius"),
            Triple("masę jabłka", "gramy", "grams"))
        val i = items.indices.random(random)
        val item = items[i]
        val other = items.filterIndexed { index, _ -> index != i }.shuffled(random).take(3)
        val plOptions = listOf(item.second) + other.map { it.second }
        val enOptions = listOf(item.third) + other.map { it.third }
        return shuffledQuestion(
            "daily:unit:${item.first}", LearningCategory.DAILY, QuestionType.CHOICE,
            "W jakich jednostkach najwygodniej mierzyć ${item.first}?",
            "Which unit is best for measuring ${item.first}?",
            plOptions, enOptions, item.second,
            "Dopasuj jednostkę do wielkości, którą mierzymy.", "Match the unit to what is being measured.", "📏"
        )
    }

    private fun clockQuestion(stage: Stage): LearningQuestion {
        val hour = random.nextInt(7, 20)
        val minute = listOf(0, 15, 30, 45).random(random)
        val correct = "%02d:%02d".format(hour, minute)
        val options = linkedSetOf(correct)
        while (options.size < 4) {
            options += "%02d:%02d".format((hour + random.nextInt(-2, 3)).coerceIn(0, 23), listOf(0, 15, 30, 45).random(random))
        }
        return shuffledQuestion(
            "daily:clock:$correct", LearningCategory.DAILY, QuestionType.CHOICE,
            "Kacper ma spotkanie o $correct. Wskaż tę godzinę.", "Kacper has a meeting at $correct. Choose that time.",
            options.toList(), options.toList(), correct,
            "Najpierw odczytaj godzinę, potem minuty.", "Read the hour first, then the minutes.", "🕒"
        )
    }

    private fun changeQuestion(stage: Stage): LearningQuestion {
        val price = random.nextInt(2, 16)
        val paid = listOf(20, 50).first { it > price }
        return numericQuestion(
            "daily:change:$paid:$price", stage,
            "Pamiątka kosztuje $price zł. Płacisz $paid zł. Ile otrzymasz reszty?",
            "A souvenir costs $price PLN. You pay $paid PLN. How much change do you get?",
            paid - price, "Od kwoty zapłaconej odejmij cenę.", "Subtract the price from the amount paid.",
            "💰", LearningCategory.DAILY
        )
    }

    private fun weekdayQuestion(stage: Stage): LearningQuestion {
        val daysPl = listOf("poniedziałek", "wtorek", "środa", "czwartek", "piątek", "sobota", "niedziela")
        val daysEn = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val i = random.nextInt(0, 7)
        val next = (i + 1) % 7
        val pairs = daysPl.indices.map { daysPl[it] to daysEn[it] }
        val options = (listOf(pairs[next]) + pairs.filterIndexed { index, _ -> index != next }.shuffled(random).take(3)).shuffled(random)
        return LearningQuestion(
            "daily:weekday:$i", LearningCategory.DAILY, QuestionType.CHOICE,
            "Jaki dzień jest po: ${daysPl[i]}?", "Which day comes after ${daysEn[i]}?",
            options.map { it.first }, options.map { it.second }, options.indexOf(pairs[next]),
            "Przypomnij sobie kolejność dni tygodnia.", "Remember the order of the days of the week.", "📅"
        )
    }

    private fun calendarQuestion(stage: Stage): LearningQuestion {
        val monthsPl = listOf("styczeń", "luty", "marzec", "kwiecień", "maj", "czerwiec", "lipiec", "sierpień", "wrzesień", "październik", "listopad", "grudzień")
        val monthsEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val i = random.nextInt(0, 11)
        val target = i + 1
        val indices = (0..11).filter { it != target }.shuffled(random).take(3) + target
        val shuffled = indices.shuffled(random)
        return LearningQuestion(
            "daily:month:$i", LearningCategory.DAILY, QuestionType.CHOICE,
            "Jaki miesiąc jest po: ${monthsPl[i]}?", "Which month comes after ${monthsEn[i]}?",
            shuffled.map { monthsPl[it] }, shuffled.map { monthsEn[it] }, shuffled.indexOf(target),
            "Przypomnij sobie kolejność miesięcy.", "Remember the order of the months.", "🗓️"
        )
    }

    private fun measurementQuestion(stage: Stage): LearningQuestion {
        val kind = random.nextBoolean()
        return if (kind) {
            shuffledQuestion(
                "daily:measure:length", LearningCategory.DAILY, QuestionType.CHOICE,
                "Czym najwygodniej zmierzyć długość biurka?", "What is best for measuring the length of a desk?",
                listOf("miarką", "zegarem", "termometrem", "wagą"), listOf("measuring tape", "clock", "thermometer", "scale"), "miarką",
                "Szukamy narzędzia do mierzenia długości.", "We need a tool for measuring length.", "📏"
            )
        } else {
            shuffledQuestion(
                "daily:measure:temp", LearningCategory.DAILY, QuestionType.CHOICE,
                "Czym mierzymy temperaturę?", "What do we use to measure temperature?",
                listOf("termometrem", "linijką", "kompasem", "zegarkiem"), listOf("thermometer", "ruler", "compass", "watch"), "termometrem",
                "To narzędzie pokazuje stopnie.", "This tool shows degrees.", "🌡️"
            )
        }
    }

    private fun directionQuestion(stage: Stage): LearningQuestion {
        val optionsPl = listOf("północ", "południe", "wschód", "zachód")
        val optionsEn = listOf("north", "south", "east", "west")
        val i = random.nextInt(4)
        return LearningQuestion(
            "daily:direction:$i", LearningCategory.DAILY, QuestionType.CHOICE,
            "Kompas wskazuje ${listOf("N", "S", "E", "W")[i]}. Jaki to kierunek?",
            "The compass points to ${listOf("N", "S", "E", "W")[i]}. Which direction is it?",
            optionsPl, optionsEn, i,
            "N = północ, S = południe, E = wschód, W = zachód.", "N = north, S = south, E = east, W = west.", "🧭"
        )
    }

    private fun numericQuestion(
        id: String,
        stage: Stage,
        promptPl: String,
        promptEn: String,
        answer: Int,
        hintPl: String,
        hintEn: String,
        visual: String? = null,
        category: LearningCategory = LearningCategory.MATH,
        type: QuestionType = QuestionType.CHOICE
    ): LearningQuestion {
        val values = linkedSetOf(answer)
        val span = max(3, max(1, answer / 5))
        var guard = 0
        while (values.size < 4 && guard < 100) {
            guard++
            val delta = random.nextInt(-span, span + 1)
            values += (answer + if (delta == 0) span else delta).coerceAtLeast(0)
        }
        var filler = answer + span + 1
        while (values.size < 4) values += filler++
        val shuffled = values.map(Int::toString).shuffled(random)
        return LearningQuestion(
            id = id,
            category = category,
            type = type,
            promptPl = promptPl,
            promptEn = promptEn,
            optionsPl = shuffled,
            optionsEn = shuffled,
            correctIndex = shuffled.indexOf(answer.toString()),
            hintPl = hintPl,
            hintEn = hintEn,
            visual = visual
        )
    }

    private fun shuffledQuestion(
        id: String,
        category: LearningCategory,
        type: QuestionType,
        promptPl: String,
        promptEn: String,
        optionsPl: List<String>,
        optionsEn: List<String>,
        correctPl: String,
        hintPl: String,
        hintEn: String,
        visual: String? = null
    ): LearningQuestion {
        require(optionsPl.size == optionsEn.size) { "PL/EN options must have same size" }
        val pairs = optionsPl.indices.map { optionsPl[it] to optionsEn[it] }
        val correctIndexOriginal = optionsPl.indexOf(correctPl).coerceAtLeast(0)
        val correctPair = pairs[correctIndexOriginal]
        val shuffled = pairs.distinct().shuffled(random).toMutableList()
        while (shuffled.size < minOf(4, pairs.size)) shuffled += pairs[shuffled.size]
        return LearningQuestion(
            id = id,
            category = category,
            type = type,
            promptPl = promptPl,
            promptEn = promptEn,
            optionsPl = shuffled.map { it.first },
            optionsEn = shuffled.map { it.second },
            correctIndex = shuffled.indexOf(correctPair).coerceAtLeast(0),
            hintPl = hintPl,
            hintEn = hintEn,
            visual = visual
        )
    }

    private fun difficulty(stage: Stage): Int =
        (((stage.worldId - 1) * 2 + stage.number / 2 + 1) + adaptiveDifficultyOffset).coerceIn(1, 10)
}
