package pl.kacperikapi.mathadventure.data

import kotlin.math.max
import kotlin.random.Random

/**
 * Age-aware question engine for children aged 4–8.
 *
 * Stage 1 starts with preschool-level arithmetic, comparisons, picture vocabulary and very
 * simple logic. Difficulty rises gradually across ten stages, while the adaptive
 * offset can move at most one age step up or down based on the child's results.
 */
class QuestionEngine(
    private val history: QuestionHistoryStore,
    private val random: Random = Random.Default
) {
    private var adaptiveAgeOffset: Int = 0

    fun next(
        stage: Stage,
        fixedCategory: LearningCategory? = null,
        performance: Map<LearningCategory, CategoryStats> = emptyMap()
    ): LearningQuestion {
        val recent = history.recentIds()
        var fallback: LearningQuestion? = null

        repeat(60) {
            val category = fixedCategory ?: weightedCategory(stage, performance)
            adaptiveAgeOffset = difficultyOffset(performance[category])
            val age = effectiveAge(stage)
            val candidate = generate(category, stage, age)
            fallback = candidate
            if (candidate.id !in recent) return candidate
        }

        val category = fixedCategory ?: stage.categories.firstOrNull() ?: LearningCategory.MATH
        adaptiveAgeOffset = difficultyOffset(performance[category])
        return fallback ?: generate(category, stage, effectiveAge(stage))
    }

    fun markSeen(question: LearningQuestion) = history.remember(question.id)

    private fun effectiveAge(stage: Stage): Int =
        (stage.targetAge + adaptiveAgeOffset).coerceIn(GameRules.MIN_AGE, GameRules.MAX_AGE)

    private fun difficultyOffset(stats: CategoryStats?): Int = when {
        stats == null || stats.solved < 6 -> 0
        stats.accuracyPercent >= 92 -> 1
        stats.accuracyPercent <= 58 -> -1
        else -> 0
    }

    private fun weightedCategory(
        stage: Stage,
        performance: Map<LearningCategory, CategoryStats>
    ): LearningCategory {
        val pool = buildList {
            repeat(4) { addAll(stage.categories) }
            addAll(LearningCategory.entries)

            LearningCategory.entries.forEach { category ->
                val stats = performance[category] ?: CategoryStats()

                if (stats.solved < 10) repeat(2) { add(category) }

                when {
                    stats.solved >= 5 && stats.accuracyPercent < 55 -> repeat(6) { add(category) }
                    stats.solved >= 5 && stats.accuracyPercent < 70 -> repeat(4) { add(category) }
                    stats.solved >= 5 && stats.accuracyPercent < 80 -> repeat(2) { add(category) }
                }
            }
        }
        return pool.random(random)
    }

    private fun generate(category: LearningCategory, stage: Stage, age: Int): LearningQuestion = when (category) {
        LearningCategory.MATH -> math(stage, age)
        LearningCategory.POLISH -> polish(stage, age)
        LearningCategory.ENGLISH -> english(stage, age)
        LearningCategory.LOGIC -> logic(stage, age)
        LearningCategory.NATURE -> nature(stage, age)
        LearningCategory.WORLD -> worldKnowledge(stage, age)
        LearningCategory.DAILY -> daily(stage, age)
    }

    // -------------------- MATH --------------------

    private fun math(stage: Stage, age: Int): LearningQuestion {
        val kinds = when (age) {
            4 -> listOf("add5", "add5", "sub5", "compare10", "missing10")
            5 -> listOf("add10", "sub10", "missing10", "compare20", "story10", "add10")
            6 -> listOf("add20", "sub20", "missing20", "money20", "story20", "compare50")
            7 -> listOf("add100", "sub100", "mulEasy", "money50", "story50", "missing50")
            else -> listOf("add100", "sub100", "mul", "div", "money100", "story100", "missing100")
        }
        return when (val kind = kinds.random(random)) {
            "add5" -> operationQuestion(stage, "+", 5)
            "sub5" -> operationQuestion(stage, "−", 5)
            "add10" -> operationQuestion(stage, "+", 10)
            "sub10" -> operationQuestion(stage, "−", 10)
            "add20" -> operationQuestion(stage, "+", 20)
            "sub20" -> operationQuestion(stage, "−", 20)
            "add100" -> operationQuestion(stage, "+", 100)
            "sub100" -> operationQuestion(stage, "−", 100)
            "missing10" -> missingNumber(stage, 10)
            "missing20" -> missingNumber(stage, 20)
            "missing50" -> missingNumber(stage, 50)
            "missing100" -> missingNumber(stage, 100)
            "compare10" -> compareNumbers(stage, 10)
            "compare20" -> compareNumbers(stage, 20)
            "compare50" -> compareNumbers(stage, 50)
            "money20" -> money(stage, 20)
            "money50" -> money(stage, 50)
            "money100" -> money(stage, 100)
            "story10" -> storyMath(stage, 10)
            "story20" -> storyMath(stage, 20)
            "story50" -> storyMath(stage, 50)
            "story100" -> storyMath(stage, 100)
            "mulEasy" -> multiplication(stage, listOf(2, 5, 10))
            "mul" -> multiplication(stage, (2..10).toList())
            "div" -> division(stage)
            else -> operationQuestion(stage, if (kind.contains("sub")) "−" else "+", 20)
        }
    }

    private fun operationQuestion(stage: Stage, op: String, maxResult: Int): LearningQuestion {
        val a: Int
        val b: Int
        val answer: Int
        if (op == "+") {
            a = random.nextInt(0, maxResult + 1)
            b = random.nextInt(0, maxResult - a + 1)
            answer = a + b
        } else {
            a = random.nextInt(0, maxResult + 1)
            b = random.nextInt(0, a + 1)
            answer = a - b
        }
        return numericQuestion(
            id = "math:$op:$a:$b:$maxResult",
            category = LearningCategory.MATH,
            promptPl = "Oblicz: $a $op $b = ?",
            promptEn = "Calculate: $a $op $b = ?",
            answer = answer,
            maxOption = maxResult,
            hintPl = if (op == "+") "Dodaj elementy do siebie." else "Odejmij krok po kroku.",
            hintEn = if (op == "+") "Add the groups together." else "Subtract step by step."
        )
    }

    private fun missingNumber(stage: Stage, maxResult: Int): LearningQuestion {
        val a = random.nextInt(0, maxResult.coerceAtLeast(2))
        val answer = random.nextInt(0, (maxResult - a).coerceAtLeast(0) + 1)
        val result = a + answer
        return numericQuestion(
            id = "math:missing:$a:$result:$maxResult",
            category = LearningCategory.MATH,
            promptPl = "Jaka liczba pasuje?  $a + ? = $result",
            promptEn = "Which number fits?  $a + ? = $result",
            answer = answer,
            maxOption = maxResult,
            hintPl = "Policz od $a do $result.",
            hintEn = "Count from $a up to $result."
        )
    }

    private fun compareNumbers(stage: Stage, maxValue: Int): LearningQuestion {
        var a = random.nextInt(0, maxValue + 1)
        var b = random.nextInt(0, maxValue + 1)
        if (a == b) b = (b + 1).coerceAtMost(maxValue)
        if (a == b) a = (a - 1).coerceAtLeast(0)
        val correct = if (a > b) ">" else "<"
        return shuffledQuestion(
            id = "math:compare:$a:$b:$maxValue",
            category = LearningCategory.MATH,
            type = QuestionType.CHOICE,
            promptPl = "Który znak pasuje?  $a  ?  $b",
            promptEn = "Which sign fits?  $a  ?  $b",
            optionsPl = listOf(">", "<", "=", "?"),
            optionsEn = listOf(">", "<", "=", "?"),
            correctIndex = if (correct == ">") 0 else 1,
            hintPl = "Większa liczba jest po szerszej stronie znaku.",
            hintEn = "The larger number is on the wider side of the sign."
        )
    }

    private fun money(stage: Stage, maxTotal: Int): LearningQuestion {
        val values = listOf(1, 2, 3, 4, 5, 6, 8, 10, 12, 15, 20, 25, 30, 40, 50).filter { it <= maxTotal }
        val p1 = values.random(random)
        val p2 = values.filter { it + p1 <= maxTotal }.ifEmpty { listOf(1) }.random(random)
        return numericQuestion(
            id = "math:money:$p1:$p2",
            category = LearningCategory.MATH,
            promptPl = "Pamiątka kosztuje $p1 zł, a pocztówka $p2 zł. Ile razem?",
            promptEn = "A souvenir costs $p1 PLN and a postcard $p2 PLN. How much altogether?",
            answer = p1 + p2,
            maxOption = maxTotal,
            hintPl = "Dodaj obie ceny.",
            hintEn = "Add both prices.",
            visual = "🪙"
        )
    }

    private fun storyMath(stage: Stage, maxValue: Int): LearningQuestion {
        val a = random.nextInt(1, max(2, maxValue / 2) + 1)
        val b = random.nextInt(1, max(2, maxValue / 2) + 1)
        val addition = random.nextBoolean()
        val first = if (addition) a else max(a, b)
        val second = if (addition) b else minOf(a, b)
        val answer = if (addition) first + second else first - second
        return numericQuestion(
            id = "math:story:${if (addition) "plus" else "minus"}:$first:$second:$maxValue",
            category = LearningCategory.MATH,
            promptPl = if (addition)
                "Kapi znalazł $first naklejek, a Kacper $second. Ile mają razem?"
            else
                "Kacper miał $first naklejek i oddał $second. Ile zostało?",
            promptEn = if (addition)
                "Kapi found $first stickers and Kacper found $second. How many altogether?"
            else
                "Kacper had $first stickers and gave away $second. How many are left?",
            answer = answer,
            maxOption = maxValue,
            hintPl = if (addition) "Połącz obie grupy." else "Odejmij oddane naklejki.",
            hintEn = if (addition) "Join the two groups." else "Subtract the stickers that were given away.",
            visual = "🎒"
        )
    }

    private fun multiplication(stage: Stage, tables: List<Int>): LearningQuestion {
        val a = tables.random(random)
        val b = random.nextInt(1, 11)
        return numericQuestion(
            id = "math:mul:$a:$b",
            category = LearningCategory.MATH,
            promptPl = "Oblicz: $a × $b = ?",
            promptEn = "Calculate: $a × $b = ?",
            answer = a * b,
            maxOption = 100,
            hintPl = "Możesz dodać liczbę $a kilka razy.",
            hintEn = "You can add $a repeatedly."
        )
    }

    private fun division(stage: Stage): LearningQuestion {
        val divisor = random.nextInt(2, 11)
        val result = random.nextInt(1, 11)
        val value = divisor * result
        return numericQuestion(
            id = "math:div:$value:$divisor",
            category = LearningCategory.MATH,
            promptPl = "Oblicz: $value ÷ $divisor = ?",
            promptEn = "Calculate: $value ÷ $divisor = ?",
            answer = result,
            maxOption = 12,
            hintPl = "Pomyśl, ile razy $divisor mieści się w $value.",
            hintEn = "Think how many times $divisor fits into $value."
        )
    }

    // -------------------- POLISH --------------------

    private fun polish(stage: Stage, age: Int): LearningQuestion = when (age) {
        4 -> listOf(::firstLetterQuestion, ::simpleSyllableQuestion, ::pictureWordQuestion).random(random)(stage)
        5 -> listOf(::firstLetterQuestion, ::simpleSyllableQuestion, ::pictureWordQuestion, ::punctuationQuestion).random(random)(stage)
        6 -> listOf(::simpleSyllableQuestion, ::punctuationQuestion, { s -> grammarQuestion(s, "noun") }, { s -> grammarQuestion(s, "verb") }).random(random)(stage)
        7 -> listOf(::orthographyQuestion, ::punctuationQuestion, { s -> grammarQuestion(s, "noun") }, { s -> grammarQuestion(s, "verb") }).random(random)(stage)
        else -> listOf(::orthographyQuestion, ::punctuationQuestion, { s -> grammarQuestion(s, "noun") }, { s -> grammarQuestion(s, "verb") }, { s -> grammarQuestion(s, "adjective") }).random(random)(stage)
    }

    private fun firstLetterQuestion(stage: Stage): LearningQuestion {
        val words = listOf("dom" to "🏠", "kot" to "🐱", "pies" to "🐶", "las" to "🌲", "ryba" to "🐟", "słońce" to "☀️", "kwiat" to "🌼", "auto" to "🚗")
        val item = words.random(random)
        val correct = item.first.first().uppercase()
        val letters = (listOf(correct) + listOf("A", "K", "M", "P", "R", "S", "T").filter { it != correct }.shuffled(random).take(3))
        return shuffledQuestion(
            id = "pl:first:${item.first}",
            category = LearningCategory.POLISH,
            type = QuestionType.IMAGE_CHOICE,
            promptPl = "Na jaką literę zaczyna się słowo „${item.first}”?",
            promptEn = "Which letter does the Polish word “${item.first}” start with?",
            optionsPl = letters,
            optionsEn = letters,
            correctIndex = 0,
            hintPl = "Powiedz słowo powoli i posłuchaj pierwszego dźwięku.",
            hintEn = "Say the word slowly and listen to the first sound.",
            visual = item.second
        )
    }

    private fun simpleSyllableQuestion(stage: Stage): LearningQuestion {
        val maxSyllables = if (stage.targetAge <= 5) 2 else 4
        val pool = QuestionBank.syllables.filter { it.syllables <= maxSyllables }
        val item = pool.ifEmpty { QuestionBank.syllables }.random(random)
        return numericQuestion(
            id = "pl:syll:${item.word}",
            category = LearningCategory.POLISH,
            promptPl = "Ile sylab ma słowo „${item.word}”?",
            promptEn = "How many syllables are in the Polish word “${item.word}”?",
            answer = item.syllables,
            maxOption = 5,
            hintPl = "Powiedz słowo rytmicznie i zaklaszcz.",
            hintEn = "Say the word slowly and clap each syllable."
        )
    }

    private fun pictureWordQuestion(stage: Stage): LearningQuestion {
        val pool = QuestionBank.englishWords.filter { it.group in setOf("animals", "food", "nature", "places") }
        val item = pool.random(random)
        val wrong = pool.filter { it.pl != item.pl }.shuffled(random).take(3)
        return shuffledQuestion(
            id = "pl:picture:${item.pl}",
            category = LearningCategory.POLISH,
            type = QuestionType.IMAGE_CHOICE,
            promptPl = "Które słowo pasuje do obrazka?",
            promptEn = "Which Polish word matches the picture?",
            optionsPl = listOf(item.pl) + wrong.map { it.pl },
            optionsEn = listOf(item.pl) + wrong.map { it.pl },
            correctIndex = 0,
            hintPl = "Nazwij obrazek na głos.",
            hintEn = "Name the picture aloud.",
            visual = item.emoji
        )
    }

    private fun orthographyQuestion(stage: Stage): LearningQuestion {
        val o = QuestionBank.orthography.random(random)
        return shuffledQuestion(
            id = "pl:ortho:${o.correct}",
            category = LearningCategory.POLISH,
            type = QuestionType.CHOICE,
            promptPl = "Który wyraz jest zapisany poprawnie?",
            promptEn = "Which Polish word is spelled correctly?",
            optionsPl = listOf(o.correct) + o.wrong.take(3),
            optionsEn = listOf(o.correct) + o.wrong.take(3),
            correctIndex = 0,
            hintPl = "Przeczytaj każde słowo powoli.",
            hintEn = "Read each word slowly."
        )
    }

    private fun punctuationQuestion(stage: Stage): LearningQuestion {
        val variants = listOf(
            Triple("Gdzie jest Kapi", "?", "To jest pytanie."),
            Triple("Ale piękny widok", "!", "To zdanie wyraża emocje."),
            Triple("Kacper idzie do szkoły", ".", "To zwykłe zdanie."),
            Triple("Czy lubisz podróże", "?", "To jest pytanie.")
        )
        val v = variants.random(random)
        val options = listOf(".", "?", "!", ",")
        return shuffledQuestion(
            id = "pl:punct:${v.first}",
            category = LearningCategory.POLISH,
            type = QuestionType.CHOICE,
            promptPl = "Jaki znak kończy zdanie: „${v.first}”",
            promptEn = "Which mark ends the Polish sentence: “${v.first}”",
            optionsPl = options,
            optionsEn = options,
            correctIndex = options.indexOf(v.second),
            hintPl = v.third,
            hintEn = v.third
        )
    }

    private fun grammarQuestion(stage: Stage, kind: String): LearningQuestion {
        val nouns = listOf("pies", "zamek", "książka", "drzewo", "plecak", "rzeka")
        val verbs = listOf("biegnie", "czyta", "skacze", "śpi", "pisze", "słucha")
        val adjectives = listOf("zielony", "wesoły", "wysoki", "mały", "ciepły", "kolorowy")
        val correctPool = when (kind) { "noun" -> nouns; "verb" -> verbs; else -> adjectives }
        val labelPl = when (kind) { "noun" -> "rzeczownikiem"; "verb" -> "czasownikiem"; else -> "przymiotnikiem" }
        val labelEn = when (kind) { "noun" -> "a noun"; "verb" -> "a verb"; else -> "an adjective" }
        val correct = correctPool.random(random)
        val wrong = (nouns + verbs + adjectives).filter { it !in correctPool }.shuffled(random).take(3)
        return shuffledQuestion(
            id = "pl:grammar:$kind:$correct:${wrong.sorted().joinToString("-")}",
            category = LearningCategory.POLISH,
            type = QuestionType.CHOICE,
            promptPl = "Które słowo jest $labelPl?",
            promptEn = "Which Polish word is $labelEn?",
            optionsPl = listOf(correct) + wrong,
            optionsEn = listOf(correct) + wrong,
            correctIndex = 0,
            hintPl = "Pomyśl, czy słowo nazywa rzecz, czynność czy cechę.",
            hintEn = "Think whether the word names a thing, an action or a quality."
        )
    }

    // -------------------- ENGLISH --------------------

    private fun english(stage: Stage, age: Int): LearningQuestion {
        val allowedGroups = when (age) {
            4 -> setOf("animals", "colors", "numbers", "food")
            5 -> setOf("animals", "colors", "numbers", "food", "body")
            6 -> setOf("animals", "colors", "numbers", "food", "body", "places", "nature")
            else -> QuestionBank.englishWords.map { it.group }.toSet()
        }
        val pool = QuestionBank.englishWords.filter { it.group in allowedGroups }
        val word = pool.random(random)
        val sameGroup = pool.filter { it.group == word.group && it != word }.shuffled(random).take(3)
        val fallback = pool.filter { it != word && it !in sameGroup }.shuffled(random).take(3 - sameGroup.size)
        val wrong = sameGroup + fallback

        return if (age <= 5 || random.nextBoolean()) {
            shuffledQuestion(
                id = "en:picture:${word.en}",
                category = LearningCategory.ENGLISH,
                type = QuestionType.IMAGE_CHOICE,
                promptPl = "Jak jest po angielsku?",
                promptEn = "What is this in English?",
                optionsPl = listOf(word.en) + wrong.map { it.en },
                optionsEn = listOf(word.en) + wrong.map { it.en },
                correctIndex = 0,
                hintPl = "Spójrz na obrazek ${word.emoji}.",
                hintEn = "Look at the picture ${word.emoji}.",
                visual = word.emoji
            )
        } else {
            shuffledQuestion(
                id = "en:pl2en:${word.pl}",
                category = LearningCategory.ENGLISH,
                type = QuestionType.CHOICE,
                promptPl = "Jak jest po angielsku „${word.pl}”?",
                promptEn = "What is “${word.pl}” in English?",
                optionsPl = listOf(word.en) + wrong.map { it.en },
                optionsEn = listOf(word.en) + wrong.map { it.en },
                correctIndex = 0,
                hintPl = "Spróbuj skojarzyć słowo z obrazkiem ${word.emoji}.",
                hintEn = "Think of the picture ${word.emoji}.",
                visual = word.emoji
            )
        }
    }

    // -------------------- LOGIC --------------------

    private fun logic(stage: Stage, age: Int): LearningQuestion = when (age) {
        4 -> listOf(::simplePattern, ::oddEmoji, ::numberOrder).random(random)(stage)
        5 -> listOf(::simplePattern, ::oddEmoji, ::numberOrder, ::easySequence).random(random)(stage)
        6 -> listOf(::simplePattern, ::numberOrder, ::easySequence, ::memoryPattern, ::matchingMiniGame).random(random)(stage)
        else -> listOf(::simplePattern, ::numberOrder, ::easySequence, ::memoryPattern, ::analogyQuestion, ::matchingMiniGame).random(random)(stage)
    }

    private fun matchingMiniGame(stage: Stage): LearningQuestion {
        val sets = listOf(
            listOf(
                MatchPair("🐶", "🐶", "pies", "dog"),
                MatchPair("🐱", "🐱", "kot", "cat"),
                MatchPair("🐟", "🐟", "ryba", "fish")
            ),
            listOf(
                MatchPair("☀️", "☀️", "dzień", "day"),
                MatchPair("🌙", "🌙", "noc", "night"),
                MatchPair("🌧️", "🌧️", "deszcz", "rain")
            ),
            listOf(
                MatchPair("2 + 2", "2 + 2", "4", "4"),
                MatchPair("3 + 2", "3 + 2", "5", "5"),
                MatchPair("5 − 2", "5 − 2", "3", "3")
            ),
            listOf(
                MatchPair("🏔️", "🏔️", "góry", "mountains"),
                MatchPair("🌊", "🌊", "woda", "water"),
                MatchPair("🌳", "🌳", "las", "forest")
            )
        )
        val pairs = sets.random(random)
        return LearningQuestion(
            id = "logic:matching:${stage.id}:${pairs.joinToString("|") { it.leftPl + "-" + it.rightPl }}",
            category = LearningCategory.LOGIC,
            type = QuestionType.MATCHING,
            promptPl = "Połącz elementy w pasujące pary.",
            promptEn = "Match the items into correct pairs.",
            optionsPl = pairs.map { it.rightPl },
            optionsEn = pairs.map { it.rightEn },
            correctIndex = 0,
            hintPl = "Sprawdź znaczenie, obrazek albo wynik działania.",
            hintEn = "Look for the matching meaning, picture or result.",
            pairs = pairs
        )
    }

    private fun simplePattern(stage: Stage): LearningQuestion {
        val patterns = listOf(
            Triple("🔴 🔵 🔴 🔵 ?", "🔴", listOf("🔴", "🔵", "🟢", "🟡")),
            Triple("⭐ 🌙 ⭐ 🌙 ?", "⭐", listOf("⭐", "🌙", "☀️", "☁️")),
            Triple("🐶 🐱 🐶 🐱 ?", "🐶", listOf("🐶", "🐱", "🐭", "🐰")),
            Triple("1 2 1 2 ?", "1", listOf("1", "2", "3", "4"))
        )
        val p = patterns.random(random)
        return shuffledQuestion(
            id = "logic:pattern:${p.first}",
            category = LearningCategory.LOGIC,
            type = QuestionType.SEQUENCE,
            promptPl = "Co będzie dalej?",
            promptEn = "What comes next?",
            optionsPl = p.third,
            optionsEn = p.third,
            correctIndex = p.third.indexOf(p.second),
            hintPl = "Sprawdź, co powtarza się na zmianę.",
            hintEn = "Look for the repeating pattern.",
            visual = p.first
        )
    }

    private fun oddEmoji(stage: Stage): LearningQuestion {
        val sets = listOf(
            listOf("🍎", "🍌", "🍓", "🚗"),
            listOf("🐶", "🐱", "🐰", "🌼"),
            listOf("🔴", "🔵", "🟢", "🐟"),
            listOf("🏠", "🏫", "🏰", "🍎")
        )
        val set = sets.random(random)
        return shuffledQuestion(
            id = "logic:odd:${set.joinToString()}",
            category = LearningCategory.LOGIC,
            type = QuestionType.IMAGE_CHOICE,
            promptPl = "Co tutaj nie pasuje?",
            promptEn = "Which one does not belong?",
            optionsPl = set,
            optionsEn = set,
            correctIndex = 3,
            hintPl = "Trzy obrazki należą do tej samej grupy.",
            hintEn = "Three pictures belong to the same group."
        )
    }

    private fun numberOrder(stage: Stage): LearningQuestion {
        val start = random.nextInt(0, 8)
        val numbers = listOf(start, start + 1, start + 2, start + 3)
        val shuffled = numbers.shuffled(random).map { it.toString() }
        val correct = numbers.map { it.toString() }
        return LearningQuestion(
            id = "logic:order:${numbers.joinToString("-")}",
            category = LearningCategory.LOGIC,
            type = QuestionType.ORDERING,
            promptPl = "Ułóż liczby od najmniejszej do największej.",
            promptEn = "Put the numbers from smallest to largest.",
            optionsPl = shuffled,
            optionsEn = shuffled,
            correctIndex = 0,
            hintPl = "Zacznij od najmniejszej liczby.",
            hintEn = "Start with the smallest number.",
            correctOrderPl = correct,
            correctOrderEn = correct
        )
    }

    private fun easySequence(stage: Stage): LearningQuestion {
        val step = if (stage.targetAge <= 6) listOf(1, 2).random(random) else listOf(2, 3, 5, 10).random(random)
        val start = random.nextInt(0, 10)
        val values = List(4) { start + it * step }
        val answer = start + 4 * step
        return numericQuestion(
            id = "logic:seq:$start:$step",
            category = LearningCategory.LOGIC,
            promptPl = "Jaka liczba będzie następna? ${values.joinToString(", ")}, ?",
            promptEn = "Which number comes next? ${values.joinToString(", ")}, ?",
            answer = answer,
            maxOption = answer + step * 2,
            hintPl = "Sprawdź, o ile rośnie każda kolejna liczba.",
            hintEn = "Check how much each number increases."
        )
    }

    private fun memoryPattern(stage: Stage): LearningQuestion {
        val patterns = listOf("🔴 🔵 🟢", "⭐ 🌙 ☀️", "🐶 🐱 🐰", "1 3 2")
        val correct = patterns.random(random)
        val wrong = patterns.filter { it != correct }.shuffled(random).take(3)
        return shuffledQuestion(
            id = "logic:memory:$correct",
            category = LearningCategory.LOGIC,
            type = QuestionType.MEMORY,
            promptPl = "Zapamiętaj układ i wybierz taki sam.",
            promptEn = "Remember the pattern and choose the same one.",
            optionsPl = listOf(correct) + wrong,
            optionsEn = listOf(correct) + wrong,
            correctIndex = 0,
            hintPl = "Zwróć uwagę na kolejność.",
            hintEn = "Pay attention to the order.",
            visual = correct
        )
    }

    private fun analogyQuestion(stage: Stage): LearningQuestion {
        val items = listOf(
            Triple("ręka : rękawiczka = stopa : ?", "but", listOf("but", "czapka", "szalik", "plecak")),
            Triple("ptak : niebo = ryba : ?", "woda", listOf("woda", "drzewo", "droga", "dom")),
            Triple("dzień : słońce = noc : ?", "księżyc", listOf("księżyc", "rower", "kwiat", "zamek"))
        )
        val q = items.random(random)
        return shuffledQuestion(
            id = "logic:analogy:${q.first}",
            category = LearningCategory.LOGIC,
            type = QuestionType.CHOICE,
            promptPl = q.first,
            promptEn = q.first,
            optionsPl = q.third,
            optionsEn = q.third,
            correctIndex = q.third.indexOf(q.second),
            hintPl = "Szukaj podobnej relacji między parami.",
            hintEn = "Look for the same relationship between the pairs."
        )
    }

    // -------------------- NATURE / WORLD --------------------

    private fun nature(stage: Stage, age: Int): LearningQuestion {
        if (age <= 5) {
            val simple = listOf(
                SimpleFact("Które zwierzę mieszka w wodzie?", "Which animal lives in water?", listOf("🐟 ryba", "🐶 pies", "🐱 kot", "🐴 koń"), listOf("🐟 fish", "🐶 dog", "🐱 cat", "🐴 horse"), 0),
                SimpleFact("Co rośnie w ziemi?", "What grows in soil?", listOf("🌼 kwiat", "🚗 auto", "📘 książka", "⚽ piłka"), listOf("🌼 flower", "🚗 car", "📘 book", "⚽ ball"), 0),
                SimpleFact("Co świeci na niebie w dzień?", "What shines in the sky during the day?", listOf("☀️ Słońce", "🌙 Księżyc", "🐟 ryba", "🏠 dom"), listOf("☀️ Sun", "🌙 Moon", "🐟 fish", "🏠 house"), 0),
                SimpleFact("Która pora roku jest zwykle najzimniejsza?", "Which season is usually the coldest?", listOf("zima", "lato", "wiosna", "jesień"), listOf("winter", "summer", "spring", "autumn"), 0)
            ).random(random)
            return simpleFactQuestion("nature:simple", LearningCategory.NATURE, simple)
        }

        val facts = QuestionBank.facts.filter { it.category == LearningCategory.NATURE && (it.worldId == null || it.worldId == stage.worldId) }
        return facts.randomOrNull(random)?.toQuestion("nature") ?: nature(stage, 5)
    }

    private fun worldKnowledge(stage: Stage, age: Int): LearningQuestion {
        if (age <= 5) {
            val world = GameContent.world(stage.worldId)
            val otherNamesPl = GameContent.worlds.filter { it.id != stage.worldId }.shuffled(random).take(3).map { worldNamePl(it.id) }
            val otherNamesEn = GameContent.worlds.filter { it.id != stage.worldId }.shuffled(random).take(3).map { worldNameEn(it.id) }
            return shuffledQuestion(
                id = "world:where:${stage.worldId}:${stage.number}",
                category = LearningCategory.WORLD,
                type = QuestionType.CHOICE,
                promptPl = "W jakim miejscu jest teraz Kacper i Kapi?",
                promptEn = "Where are Kacper and Kapi now?",
                optionsPl = listOf(worldNamePl(world.id)) + otherNamesPl,
                optionsEn = listOf(worldNameEn(world.id)) + otherNamesEn,
                correctIndex = 0,
                hintPl = "Spójrz na nazwę świata na mapie.",
                hintEn = "Look at the world name on the map.",
                visual = world.icon
            )
        }

        val facts = QuestionBank.facts.filter { it.category == LearningCategory.WORLD && (it.worldId == null || it.worldId == stage.worldId) }
        return facts.randomOrNull(random)?.toQuestion("world") ?: attractionNameQuestion(stage)
    }

    private fun attractionNameQuestion(stage: Stage): LearningQuestion {
        val world = GameContent.world(stage.worldId)
        val wrong = world.stages.filter { it.id != stage.id }.shuffled(random).take(3)
        return shuffledQuestion(
            id = "world:attraction:${stage.id}",
            category = LearningCategory.WORLD,
            type = QuestionType.CHOICE,
            promptPl = "Jak nazywa się miejsce z tej misji?",
            promptEn = "What is the place in this mission called?",
            optionsPl = listOf(stage.namePl) + wrong.map { it.namePl },
            optionsEn = listOf(stage.nameEn) + wrong.map { it.nameEn },
            correctIndex = 0,
            hintPl = "Przypomnij sobie kartę miejsca przed rozpoczęciem gry.",
            hintEn = "Remember the place card you saw before the game."
        )
    }

    // -------------------- EVERYDAY SKILLS --------------------

    private fun daily(stage: Stage, age: Int): LearningQuestion = when (age) {
        4 -> simpleDaily(stage)
        5 -> listOf(::simpleDaily, ::daysQuestion).random(random)(stage)
        6 -> listOf(::daysQuestion, ::clockQuestion, { s -> money(s, 20) }).random(random)(stage)
        7 -> listOf(::daysQuestion, ::clockQuestion, { s -> money(s, 50) }).random(random)(stage)
        else -> listOf(::daysQuestion, ::clockQuestion, { s -> money(s, 100) }).random(random)(stage)
    }

    private fun simpleDaily(stage: Stage): LearningQuestion {
        val items = listOf(
            SimpleFact("Kiedy zwykle jemy śniadanie?", "When do we usually eat breakfast?", listOf("rano", "w nocy", "po północy", "podczas snu"), listOf("in the morning", "at night", "after midnight", "while sleeping"), 0),
            SimpleFact("Co zakładamy, gdy pada deszcz?", "What do we use when it rains?", listOf("☂️ parasol", "🥄 łyżkę", "⚽ piłkę", "📘 książkę"), listOf("☂️ umbrella", "🥄 spoon", "⚽ ball", "📘 book"), 0),
            SimpleFact("Który przedmiot służy do picia?", "Which item is used for drinking?", listOf("🥤 kubek", "👟 but", "✏️ ołówek", "🧢 czapka"), listOf("🥤 cup", "👟 shoe", "✏️ pencil", "🧢 cap"), 0)
        ).random(random)
        return simpleFactQuestion("daily:simple", LearningCategory.DAILY, items)
    }

    private fun daysQuestion(stage: Stage): LearningQuestion {
        val daysPlNom = listOf("poniedziałek", "wtorek", "środa", "czwartek", "piątek", "sobota", "niedziela")
        val daysPlAfter = listOf("poniedziałku", "wtorku", "środzie", "czwartku", "piątku", "sobocie", "niedzieli")
        val daysEn = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val index = random.nextInt(0, 6)
        val optionsPl = listOf(daysPlNom[index + 1], daysPlNom[index], daysPlNom[(index + 2) % 7], daysPlNom[(index + 3) % 7])
        val optionsEn = listOf(daysEn[index + 1], daysEn[index], daysEn[(index + 2) % 7], daysEn[(index + 3) % 7])
        return shuffledQuestion(
            id = "daily:day:${daysPlNom[index]}",
            category = LearningCategory.DAILY,
            type = QuestionType.CHOICE,
            promptPl = "Jaki dzień jest po ${daysPlAfter[index]}?",
            promptEn = "Which day comes after ${daysEn[index]}?",
            optionsPl = optionsPl,
            optionsEn = optionsEn,
            correctIndex = 0,
            hintPl = "Przypomnij sobie kolejność dni tygodnia.",
            hintEn = "Remember the order of the days of the week."
        )
    }

    private fun clockQuestion(stage: Stage): LearningQuestion {
        val hour = random.nextInt(1, 13)
        val half = stage.targetAge >= 7 && random.nextBoolean()
        val correctPl = if (half) "$hour:30" else "$hour:00"
        val correctEn = correctPl
        val wrongHours = listOf((hour % 12) + 1, ((hour + 1) % 12) + 1, ((hour + 5) % 12) + 1)
        val optionsPl = listOf(correctPl) + wrongHours.map { if (half) "$it:30" else "$it:00" }

        val fullClock = listOf("🕐", "🕑", "🕒", "🕓", "🕔", "🕕", "🕖", "🕗", "🕘", "🕙", "🕚", "🕛")
        val halfClock = listOf("🕜", "🕝", "🕞", "🕟", "🕠", "🕡", "🕢", "🕣", "🕤", "🕥", "🕦", "🕧")
        val nextHourNamePl = listOf(
            "pierwszej", "drugiej", "trzeciej", "czwartej", "piątej", "szóstej",
            "siódmej", "ósmej", "dziewiątej", "dziesiątej", "jedenastej", "dwunastej"
        )[(hour % 12)]

        return shuffledQuestion(
            id = "daily:clock:$correctPl",
            category = LearningCategory.DAILY,
            type = QuestionType.CHOICE,
            promptPl = if (half) {
                "Która godzina oznacza „wpół do $nextHourNamePl”?"
            } else {
                "Którą godzinę pokazuje zegar?"
            },
            promptEn = if (half) "Which answer shows half past $hour?" else "What time does the clock show?",
            optionsPl = optionsPl,
            optionsEn = optionsPl,
            correctIndex = 0,
            hintPl = if (half) {
                "„Wpół do $nextHourNamePl” oznacza 30 minut po poprzedniej pełnej godzinie."
            } else {
                "Dłuższa wskazówka na 12 oznacza pełną godzinę."
            },
            hintEn = "Look at the hour and minute hands.",
            visual = if (half) halfClock[hour - 1] else fullClock[hour - 1]
        )
    }

    // -------------------- HELPERS --------------------

    private data class SimpleFact(
        val promptPl: String,
        val promptEn: String,
        val optionsPl: List<String>,
        val optionsEn: List<String>,
        val correctIndex: Int
    )

    private fun simpleFactQuestion(prefix: String, category: LearningCategory, fact: SimpleFact): LearningQuestion =
        shuffledQuestion(
            id = "$prefix:${fact.promptPl.hashCode()}",
            category = category,
            type = QuestionType.CHOICE,
            promptPl = fact.promptPl,
            promptEn = fact.promptEn,
            optionsPl = fact.optionsPl,
            optionsEn = fact.optionsEn,
            correctIndex = fact.correctIndex,
            hintPl = "Pomyśl o tym, co widzisz na co dzień.",
            hintEn = "Think about what you see every day."
        )

    private fun QuestionBank.Fact.toQuestion(prefix: String): LearningQuestion =
        shuffledQuestion(
            id = "$prefix:${promptPl.hashCode()}",
            category = category,
            type = QuestionType.CHOICE,
            promptPl = promptPl,
            promptEn = promptEn,
            optionsPl = optionsPl,
            optionsEn = optionsEn,
            correctIndex = correctIndex,
            hintPl = hintPl,
            hintEn = hintEn
        )

    private fun <T> List<T>.randomOrNull(random: Random): T? = if (isEmpty()) null else random(random)

    private fun numericQuestion(
        id: String,
        category: LearningCategory,
        promptPl: String,
        promptEn: String,
        answer: Int,
        maxOption: Int,
        hintPl: String,
        hintEn: String,
        visual: String? = null
    ): LearningQuestion {
        val values = linkedSetOf(answer)
        val deltas = listOf(-2, -1, 1, 2, -3, 3).shuffled(random)
        deltas.forEach { delta ->
            if (values.size < 4) {
                val candidate = (answer + delta).coerceAtLeast(0)
                if (candidate <= max(maxOption, answer + 4)) values += candidate
            }
        }
        while (values.size < 4) {
            values += random.nextInt(0, max(2, max(maxOption, answer + 4)) + 1)
        }
        val options = values.map { it.toString() }
        return shuffledQuestion(
            id = id,
            category = category,
            type = QuestionType.CHOICE,
            promptPl = promptPl,
            promptEn = promptEn,
            optionsPl = options,
            optionsEn = options,
            correctIndex = options.indexOf(answer.toString()),
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
        correctIndex: Int,
        hintPl: String,
        hintEn: String,
        visual: String? = null
    ): LearningQuestion {
        require(optionsPl.size == optionsEn.size)
        require(correctIndex in optionsPl.indices)
        val order = optionsPl.indices.shuffled(random)
        val shuffledPl = order.map { optionsPl[it] }
        val shuffledEn = order.map { optionsEn[it] }
        val newCorrect = order.indexOf(correctIndex)
        return LearningQuestion(
            id = id,
            category = category,
            type = type,
            promptPl = promptPl,
            promptEn = promptEn,
            optionsPl = shuffledPl,
            optionsEn = shuffledEn,
            correctIndex = newCorrect,
            hintPl = hintPl,
            hintEn = hintEn,
            visual = visual
        )
    }

    private fun worldNamePl(id: Int): String = when (id) {
        1 -> "Wieliczka"
        2 -> "Kraków"
        3 -> "Tatry"
        4 -> "Rzym"
        5 -> "Londyn"
        6 -> "Mediolan"
        else -> "Malta"
    }

    private fun worldNameEn(id: Int): String = when (id) {
        1 -> "Wieliczka"
        2 -> "Krakow"
        3 -> "Tatras"
        4 -> "Rome"
        5 -> "London"
        6 -> "Milan"
        else -> "Malta"
    }
}
