package pl.kacperikapi.mathadventure.data

import kotlin.math.max
import kotlin.random.Random

/**
 * Country- and language-aware question generator used by the multilingual game.
 * It deliberately keeps the same learning goals for every locale, while examples,
 * money, native-language exercises and local knowledge match the selected language.
 */
object LocalizedQuestionFactory {
    private data class NativeSeed(
        val minAge: Int,
        val prompt: String,
        val correct: String,
        val wrong: List<String>,
        val hint: String,
        val visual: String? = null
    )

    private data class WordSeed(val native: String, val english: String, val emoji: String)

    fun generate(
        category: LearningCategory,
        stage: Stage,
        age: Int,
        language: String,
        random: Random
    ): LearningQuestion {
        val lang = AppLanguages.normalize(language)
        return when (category) {
            LearningCategory.MATH -> math(stage, age, lang, random)
            LearningCategory.POLISH -> nativeLanguage(stage, age, lang, random)
            LearningCategory.ENGLISH -> english(stage, age, lang, random)
            LearningCategory.LOGIC -> logic(stage, age, lang, random)
            LearningCategory.NATURE -> nature(stage, age, lang, random)
            LearningCategory.WORLD -> world(stage, age, lang, random)
            LearningCategory.DAILY -> daily(stage, age, lang, random)
        }
    }

    private fun textQuestion(
        id: String,
        category: LearningCategory,
        prompt: String,
        correct: String,
        wrong: List<String>,
        hint: String,
        random: Random,
        visual: String? = null,
        type: QuestionType = QuestionType.CHOICE
    ): LearningQuestion {
        val options = (listOf(correct) + wrong).distinct().shuffled(random).take(4)
        val safeOptions = if (correct in options) options else (options.dropLast(1) + correct).shuffled(random)
        return LearningQuestion(
            id = id,
            category = category,
            type = type,
            promptPl = prompt,
            promptEn = prompt,
            optionsPl = safeOptions,
            optionsEn = safeOptions,
            correctIndex = safeOptions.indexOf(correct).coerceAtLeast(0),
            hintPl = hint,
            hintEn = hint,
            visual = visual
        )
    }

    private fun numberQuestion(
        id: String,
        prompt: String,
        answer: Int,
        maxOption: Int,
        hint: String,
        random: Random,
        visual: String? = null
    ): LearningQuestion {
        val distractors = buildSet {
            var delta = 1
            while (size < 5) {
                listOf(answer - delta, answer + delta, answer + delta + 1).forEach {
                    if (it >= 0 && it <= maxOption + 4 && it != answer) add(it)
                }
                delta++
            }
        }.shuffled(random).take(3).map(Int::toString)
        return textQuestion(id, LearningCategory.MATH, prompt, answer.toString(), distractors, hint, random, visual)
    }

    // ---------------- MATH ----------------

    private fun math(stage: Stage, age: Int, lang: String, random: Random): LearningQuestion {
        val profile = AppLanguages.profile(lang)
        val kind = when (age) {
            4 -> listOf("add", "add", "sub", "compare", "missing")
            5 -> listOf("add", "sub", "missing", "money", "compare", "add")
            6 -> listOf("add20", "sub20", "missing", "money", "story", "compare")
            7 -> listOf("add100", "sub100", "mul", "money", "story", "missing")
            else -> listOf("add100", "sub100", "mul", "div", "money", "story", "missing")
        }.random(random)

        val maxValue = when (age) { 4 -> 5; 5 -> 10; 6 -> 20; 7 -> 100; else -> 100 }
        return when (kind) {
            "compare" -> {
                var a = random.nextInt(0, maxValue + 1)
                var b = random.nextInt(0, maxValue + 1)
                if (a == b) b = (b + 1).coerceAtMost(maxValue)
                if (a == b) a = (a - 1).coerceAtLeast(0)
                val correct = if (a > b) ">" else "<"
                textQuestion(
                    "loc:$lang:math:compare:$a:$b:$maxValue", LearningCategory.MATH,
                    tr(lang,
                        "Który znak pasuje? $a ? $b", "Which sign fits? $a ? $b",
                        "Welches Zeichen passt? $a ? $b", "¿Qué signo corresponde? $a ? $b",
                        "Quale segno va bene? $a ? $b", "Ktoré znamienko patrí? $a ? $b"),
                    correct, listOf(if (correct == ">") "<" else ">", "=", "?"),
                    tr(lang,
                        "Porównaj obie liczby.", "Compare the two numbers.", "Vergleiche beide Zahlen.",
                        "Compara los dos números.", "Confronta i due numeri.", "Porovnaj obe čísla."),
                    random
                )
            }
            "money" -> {
                val limit = when (age) { 4,5 -> 10; 6 -> 20; 7 -> 50; else -> 100 }
                val a = random.nextInt(1, max(2, limit / 2) + 1)
                val b = random.nextInt(1, max(2, limit - a) + 1)
                numberQuestion(
                    "loc:$lang:math:money:$a:$b:${profile.currencyCode}",
                    tr(lang,
                        "Pamiątka kosztuje $a ${profile.currencySymbol}, a pocztówka $b ${profile.currencySymbol}. Ile razem?",
                        "A souvenir costs ${profile.currencySymbol}$a and a postcard ${profile.currencySymbol}$b. How much altogether?",
                        "Ein Andenken kostet $a ${profile.currencySymbol}, eine Postkarte $b ${profile.currencySymbol}. Wie viel zusammen?",
                        "Un recuerdo cuesta $a ${profile.currencySymbol} y una postal $b ${profile.currencySymbol}. ¿Cuánto es en total?",
                        "Un souvenir costa $a ${profile.currencySymbol} e una cartolina $b ${profile.currencySymbol}. Quanto in tutto?",
                        "Suvenír stojí $a ${profile.currencySymbol} a pohľadnica $b ${profile.currencySymbol}. Koľko spolu?"),
                    a + b, limit + 5,
                    tr(lang,
                        "Dodaj obie ceny.", "Add the two prices.", "Addiere die beiden Preise.",
                        "Suma los dos precios.", "Somma i due prezzi.", "Sčítaj obe ceny."),
                    random, "🪙 ${profile.currencyCode}"
                )
            }
            "mul" -> {
                val tables = if (age == 7) listOf(2, 5, 10) else (2..10).toList()
                val a = tables.random(random); val b = random.nextInt(1, 11)
                numberQuestion(
                    "loc:$lang:math:mul:$a:$b", "$a × $b = ?", a * b, 100,
                    tr(lang,
                        "Możesz dodawać $a kilka razy.", "You can add $a repeatedly.",
                        "Du kannst $a mehrmals addieren.", "Puedes sumar $a varias veces.",
                        "Puoi sommare $a più volte.", "Môžeš číslo $a viackrát sčítať."), random
                )
            }
            "div" -> {
                val divisor = random.nextInt(2, 11); val result = random.nextInt(1, 11); val value = divisor * result
                numberQuestion(
                    "loc:$lang:math:div:$value:$divisor", "$value ÷ $divisor = ?", result, 12,
                    tr(lang,
                        "Pomyśl, ile razy dzielnik mieści się w liczbie.", "Think how many times the divisor fits.",
                        "Überlege, wie oft der Teiler hineinpasst.", "Piensa cuántas veces cabe el divisor.",
                        "Pensa quante volte entra il divisore.", "Premysli, koľkokrát sa deliteľ zmestí."), random
                )
            }
            "missing" -> {
                val a = random.nextInt(0, maxValue.coerceAtLeast(2)); val answer = random.nextInt(0, (maxValue - a) + 1); val result = a + answer
                numberQuestion(
                    "loc:$lang:math:missing:$a:$result:$maxValue",
                    tr(lang,
                        "Jaka liczba pasuje? $a + ? = $result", "Which number fits? $a + ? = $result",
                        "Welche Zahl passt? $a + ? = $result", "¿Qué número falta? $a + ? = $result",
                        "Quale numero manca? $a + ? = $result", "Ktoré číslo chýba? $a + ? = $result"),
                    answer, maxValue,
                    tr(lang,
                        "Policz od pierwszej liczby do wyniku.", "Count from the first number to the result.",
                        "Zähle von der ersten Zahl bis zum Ergebnis.", "Cuenta desde el primer número hasta el resultado.",
                        "Conta dal primo numero fino al risultato.", "Počítaj od prvého čísla po výsledok."), random
                )
            }
            "story" -> {
                val a = random.nextInt(1, max(3, maxValue / 2)); val b = random.nextInt(1, max(3, maxValue / 2)); val sum = a + b
                numberQuestion(
                    "loc:$lang:math:story:$a:$b",
                    tr(lang,
                        "Kacper ma $a naklejek, a Kapi znalazł $b. Ile mają razem?",
                        "Kacper has $a stickers and Kapi found $b. How many altogether?",
                        "Kacper hat $a Sticker und Kapi findet $b. Wie viele sind es zusammen?",
                        "Kacper tiene $a pegatinas y Kapi encuentra $b. ¿Cuántas tienen en total?",
                        "Kacper ha $a adesivi e Kapi ne trova $b. Quanti sono in tutto?",
                        "Kacper má $a nálepiek a Kapi nájde $b. Koľko ich majú spolu?"),
                    sum, maxValue + 5,
                    tr(lang,
                        "Połącz obie grupy.", "Join the two groups.", "Verbinde beide Gruppen.",
                        "Junta los dos grupos.", "Unisci i due gruppi.", "Spoj obe skupiny."), random, "🎒"
                )
            }
            else -> {
                val limit = when (kind) { "add20", "sub20" -> 20; "add100", "sub100" -> 100; else -> maxValue }
                val subtract = kind.startsWith("sub")
                val a: Int; val b: Int; val answer: Int
                if (subtract) {
                    a = random.nextInt(0, limit + 1); b = random.nextInt(0, a + 1); answer = a - b
                } else {
                    a = random.nextInt(0, limit + 1); b = random.nextInt(0, limit - a + 1); answer = a + b
                }
                val op = if (subtract) "−" else "+"
                numberQuestion(
                    "loc:$lang:math:op:$op:$a:$b:$limit", "$a $op $b = ?", answer, limit,
                    tr(lang,
                        if (subtract) "Odejmuj krok po kroku." else "Dodaj obie liczby.",
                        if (subtract) "Subtract step by step." else "Add the two numbers.",
                        if (subtract) "Ziehe Schritt für Schritt ab." else "Addiere die beiden Zahlen.",
                        if (subtract) "Resta paso a paso." else "Suma los dos números.",
                        if (subtract) "Sottrai passo dopo passo." else "Somma i due numeri.",
                        if (subtract) "Odčítavaj krok po kroku." else "Sčítaj obe čísla."), random
                )
            }
        }
    }

    // ---------------- NATIVE LANGUAGE ----------------

    private fun nativeLanguage(stage: Stage, age: Int, lang: String, random: Random): LearningQuestion {
        val seeds = nativeSeeds(lang).filter { it.minAge <= age }
        val seed = seeds.ifEmpty { nativeSeeds(lang) }.random(random)
        return textQuestion(
            "loc:$lang:native:${nativeSeeds(lang).indexOf(seed)}:${stage.number}",
            LearningCategory.POLISH,
            seed.prompt,
            seed.correct,
            seed.wrong,
            seed.hint,
            random,
            seed.visual,
            if (seed.visual != null) QuestionType.IMAGE_CHOICE else QuestionType.CHOICE
        )
    }

    private fun nativeSeeds(lang: String): List<NativeSeed> = when (lang) {
        "en" -> listOf(
            NativeSeed(4, "Which letter does CAT start with?", "C", listOf("B", "M", "T"), "Say CAT slowly.", "🐱"),
            NativeSeed(4, "Which word matches the picture?", "dog", listOf("sun", "car", "fish"), "Look at the picture.", "🐶"),
            NativeSeed(4, "Which word rhymes with CAT?", "hat", listOf("dog", "sun", "tree"), "Listen to the ending sound."),
            NativeSeed(5, "Choose the plural of DOG.", "dogs", listOf("doges", "dog", "dog's"), "More than one dog needs -s."),
            NativeSeed(5, "Which sentence starts correctly?", "Kapi runs.", listOf("kapi runs.", "Kapi Runs.", "kapi Runs."), "A sentence starts with a capital letter."),
            NativeSeed(6, "Which word is a noun?", "castle", listOf("run", "quickly", "green"), "A noun names a person, place or thing."),
            NativeSeed(6, "Which word is a verb?", "jump", listOf("blue", "table", "quiet"), "A verb tells what someone does."),
            NativeSeed(7, "Choose the correctly spelled word.", "because", listOf("becaus", "beacause", "becose"), "Look for the familiar spelling."),
            NativeSeed(7, "Which word is an adjective?", "bright", listOf("walk", "school", "slowly"), "An adjective describes a noun."),
            NativeSeed(8, "Choose the sentence with correct punctuation.", "Where is Kapi?", listOf("Where is Kapi.", "where is Kapi?", "Where is Kapi!"), "A question ends with a question mark."),
            NativeSeed(8, "Which word is a synonym of HAPPY?", "glad", listOf("sad", "tiny", "late"), "A synonym has a similar meaning."),
            NativeSeed(8, "Which word is the opposite of EARLY?", "late", listOf("fast", "first", "near"), "Think of the opposite meaning.")
        )
        "de" -> listOf(
            NativeSeed(4, "Mit welchem Buchstaben beginnt HUND?", "H", listOf("B", "K", "M"), "Sprich HUND langsam.", "🐶"),
            NativeSeed(4, "Welches Wort passt zum Bild?", "Katze", listOf("Sonne", "Auto", "Fisch"), "Schau genau auf das Bild.", "🐱"),
            NativeSeed(4, "Welches Wort beginnt mit M?", "Maus", listOf("Hund", "Sonne", "Ball"), "Höre auf den ersten Laut.", "🐭"),
            NativeSeed(5, "Welcher Artikel passt zu HAUS?", "das", listOf("der", "die", "den"), "Man sagt: das Haus."),
            NativeSeed(5, "Welcher Artikel passt zu SONNE?", "die", listOf("der", "das", "dem"), "Man sagt: die Sonne."),
            NativeSeed(6, "Welches Wort ist ein Nomen?", "Schule", listOf("laufen", "schnell", "grün"), "Nomen benennen Personen, Tiere, Dinge oder Orte."),
            NativeSeed(6, "Welches Wort ist ein Verb?", "spielen", listOf("blau", "Tisch", "leise"), "Verben sagen, was jemand tut."),
            NativeSeed(7, "Welche Schreibweise ist richtig?", "Straße", listOf("Strase", "Strahse", "strasse"), "Achte auf ß und den Großbuchstaben."),
            NativeSeed(7, "Welches Wort ist ein Adjektiv?", "fröhlich", listOf("laufen", "Haus", "gestern"), "Adjektive beschreiben Eigenschaften."),
            NativeSeed(8, "Welcher Satz ist richtig geschrieben?", "Kapi spielt im Park.", listOf("kapi spielt im Park.", "Kapi Spielt im park.", "Kapi spielt im park"), "Namen und Nomen schreibt man groß."),
            NativeSeed(8, "Was ist das Gegenteil von GROSS?", "klein", listOf("breit", "laut", "hell"), "Gesucht ist das Gegenwort."),
            NativeSeed(8, "Welches Wort passt: Ich ___ ein Buch.", "lese", listOf("liest", "lesen", "lest"), "Das Subjekt ist ich.")
        )
        "es" -> listOf(
            NativeSeed(4, "¿Con qué letra empieza GATO?", "G", listOf("C", "P", "S"), "Di GATO despacio.", "🐱"),
            NativeSeed(4, "¿Qué palabra corresponde al dibujo?", "perro", listOf("sol", "coche", "pez"), "Mira el dibujo.", "🐶"),
            NativeSeed(4, "¿Qué palabra empieza por M?", "mesa", listOf("gato", "sol", "pan"), "Escucha el primer sonido."),
            NativeSeed(5, "¿Qué artículo va con CASA?", "la", listOf("el", "los", "un"), "Decimos: la casa."),
            NativeSeed(5, "¿Qué artículo va con SOL?", "el", listOf("la", "las", "una"), "Decimos: el sol."),
            NativeSeed(6, "¿Cuál es un sustantivo?", "escuela", listOf("correr", "rápido", "verde"), "Un sustantivo nombra personas, lugares o cosas."),
            NativeSeed(6, "¿Cuál es un verbo?", "jugar", listOf("azul", "mesa", "tranquilo"), "Un verbo expresa una acción."),
            NativeSeed(7, "Elige la palabra bien escrita.", "árbol", listOf("arbol", "árvol", "arvól"), "Fíjate en la tilde."),
            NativeSeed(7, "¿Cuál es un adjetivo?", "feliz", listOf("correr", "casa", "ayer"), "Un adjetivo describe cómo es algo."),
            NativeSeed(8, "¿Qué frase está bien puntuada?", "¿Dónde está Kapi?", listOf("Dónde está Kapi?", "¿Dónde está Kapi.", "dónde está Kapi?"), "Las preguntas llevan signos de apertura y cierre."),
            NativeSeed(8, "¿Cuál es lo contrario de GRANDE?", "pequeño", listOf("ancho", "alto", "rápido"), "Busca el significado opuesto."),
            NativeSeed(8, "Completa: Yo ___ un libro.", "leo", listOf("lees", "leen", "leer"), "El sujeto es yo.")
        )
        "it" -> listOf(
            NativeSeed(4, "Con quale lettera inizia GATTO?", "G", listOf("C", "P", "S"), "Pronuncia GATTO lentamente.", "🐱"),
            NativeSeed(4, "Quale parola corrisponde all'immagine?", "cane", listOf("sole", "auto", "pesce"), "Guarda bene l'immagine.", "🐶"),
            NativeSeed(4, "Quale parola inizia con M?", "mela", listOf("gatto", "sole", "pane"), "Ascolta il primo suono."),
            NativeSeed(5, "Quale articolo va con CASA?", "la", listOf("il", "lo", "i"), "Diciamo: la casa."),
            NativeSeed(5, "Quale articolo va con SOLE?", "il", listOf("la", "le", "una"), "Diciamo: il sole."),
            NativeSeed(6, "Quale parola è un nome?", "scuola", listOf("correre", "veloce", "verde"), "Un nome indica persone, luoghi o cose."),
            NativeSeed(6, "Quale parola è un verbo?", "giocare", listOf("blu", "tavolo", "calmo"), "Un verbo indica un'azione."),
            NativeSeed(7, "Scegli la parola scritta correttamente.", "acqua", listOf("aqua", "acua", "aqqua"), "Ricorda la doppia c."),
            NativeSeed(7, "Quale parola è un aggettivo?", "felice", listOf("correre", "casa", "ieri"), "Un aggettivo descrive una qualità."),
            NativeSeed(8, "Quale frase ha la punteggiatura corretta?", "Dov'è Kapi?", listOf("Dov'è Kapi.", "dov'è Kapi?", "Dov'è Kapi!"), "Una domanda termina con il punto interrogativo."),
            NativeSeed(8, "Qual è il contrario di GRANDE?", "piccolo", listOf("largo", "alto", "veloce"), "Cerca il significato opposto."),
            NativeSeed(8, "Completa: Io ___ un libro.", "leggo", listOf("leggi", "leggono", "leggere"), "Il soggetto è io.")
        )
        "sk" -> listOf(
            NativeSeed(4, "Na ktoré písmeno sa začína MAČKA?", "M", listOf("P", "S", "K"), "Povedz MAČKA pomaly.", "🐱"),
            NativeSeed(4, "Ktoré slovo patrí k obrázku?", "pes", listOf("slnko", "auto", "ryba"), "Pozri sa na obrázok.", "🐶"),
            NativeSeed(4, "Ktoré slovo sa začína na L?", "lopta", listOf("pes", "slnko", "dom"), "Počúvaj prvú hlásku."),
            NativeSeed(5, "Ktoré slovo má dve slabiky?", "mačka", listOf("dom", "pes", "strom"), "Vyslov slovo po slabikách."),
            NativeSeed(5, "Ktoré slovo označuje farbu?", "modrá", listOf("bežať", "dom", "pes"), "Farba opisuje, ako niečo vyzerá."),
            NativeSeed(6, "Ktoré slovo je podstatné meno?", "škola", listOf("bežať", "rýchlo", "zelený"), "Podstatné meno pomenúva osobu, zviera, vec alebo miesto."),
            NativeSeed(6, "Ktoré slovo je sloveso?", "hrať", listOf("modrý", "stôl", "tichý"), "Sloveso pomenúva činnosť."),
            NativeSeed(7, "Vyber správne napísané slovo.", "kôň", listOf("kon", "koň", "kôn"), "Všimni si mäkčeň a vokáň."),
            NativeSeed(7, "Ktoré slovo je prídavné meno?", "veselý", listOf("bežať", "dom", "včera"), "Prídavné meno opisuje vlastnosť."),
            NativeSeed(8, "Ktorá veta je napísaná správne?", "Kapi sa hrá v parku.", listOf("kapi sa hrá v parku.", "Kapi sa Hrá v parku.", "Kapi sa hrá v Parku"), "Veta sa začína veľkým písmenom."),
            NativeSeed(8, "Čo je opakom slova VEĽKÝ?", "malý", listOf("široký", "hlasný", "svetlý"), "Hľadaj opačný význam."),
            NativeSeed(8, "Doplň: Ja ___ knihu.", "čítam", listOf("čítaš", "čítajú", "čítať"), "Podmet je ja.")
        )
        else -> listOf(
            NativeSeed(4, "Na jaką literę zaczyna się KOT?", "K", listOf("P", "S", "M"), "Powiedz KOT powoli.", "🐱"),
            NativeSeed(4, "Które słowo pasuje do obrazka?", "pies", listOf("słońce", "auto", "ryba"), "Spójrz na obrazek.", "🐶"),
            NativeSeed(4, "Które słowo zaczyna się na M?", "mysz", listOf("kot", "słońce", "dom"), "Posłuchaj pierwszej głoski."),
            NativeSeed(5, "Które słowo ma dwie sylaby?", "mama", listOf("dom", "kot", "las"), "Podziel słowo na sylaby."),
            NativeSeed(5, "Który znak kończy pytanie?", "?", listOf(".", "!", ","), "Pytanie kończymy znakiem zapytania."),
            NativeSeed(6, "Które słowo jest rzeczownikiem?", "szkoła", listOf("biegać", "szybko", "zielony"), "Rzeczownik nazywa osoby, miejsca i rzeczy."),
            NativeSeed(6, "Które słowo jest czasownikiem?", "skakać", listOf("niebieski", "stół", "cichy"), "Czasownik mówi, co ktoś robi."),
            NativeSeed(7, "Wybierz poprawnie zapisane słowo.", "rzeka", listOf("żeka", "rzega", "żega"), "Przypomnij sobie pisownię rz."),
            NativeSeed(7, "Które słowo jest przymiotnikiem?", "wesoły", listOf("biegać", "dom", "wczoraj"), "Przymiotnik opisuje cechę."),
            NativeSeed(8, "Które zdanie ma poprawną interpunkcję?", "Gdzie jest Kapi?", listOf("Gdzie jest Kapi.", "gdzie jest Kapi?", "Gdzie jest Kapi!"), "Pytanie kończy znak zapytania."),
            NativeSeed(8, "Co jest przeciwieństwem słowa DUŻY?", "mały", listOf("szeroki", "głośny", "jasny"), "Szukamy znaczenia przeciwnego."),
            NativeSeed(8, "Uzupełnij: Ja ___ książkę.", "czytam", listOf("czytasz", "czytają", "czytać"), "Podmiotem jest ja.")
        )
    }

    // ---------------- ENGLISH AS A SECOND LANGUAGE ----------------

    private fun english(stage: Stage, age: Int, lang: String, random: Random): LearningQuestion {
        if (lang == "en") {
            val seed = nativeSeeds("en").filter { it.minAge <= age }.random(random)
            return textQuestion("loc:en:english:${nativeSeeds("en").indexOf(seed)}:${stage.number}", LearningCategory.ENGLISH,
                seed.prompt, seed.correct, seed.wrong, seed.hint, random, seed.visual)
        }
        val words = englishWords(lang)
        val item = words.random(random)
        val distractors = words.filter { it.english != item.english }.shuffled(random).take(3).map { it.english }
        val prompt = when (age) {
            4,5 -> tr(lang,
                "Jak jest po angielsku?", "How do you say it in English?", "Wie heißt das auf Englisch?",
                "¿Cómo se dice en inglés?", "Come si dice in inglese?", "Ako sa to povie po anglicky?")
            else -> tr(lang,
                "Wybierz angielskie tłumaczenie słowa „${item.native}”.", "Choose the English translation of “${item.native}”.",
                "Wähle die englische Übersetzung von „${item.native}“.", "Elige la traducción inglesa de «${item.native}».",
                "Scegli la traduzione inglese di «${item.native}».", "Vyber anglický preklad slova „${item.native}“." )
        }
        return textQuestion(
            "loc:$lang:english:${item.english}:${stage.number}", LearningCategory.ENGLISH,
            prompt, item.english, distractors,
            tr(lang,
                "Spójrz na obrazek i przypomnij sobie angielskie słowo.", "Look at the picture and recall the English word.",
                "Schau auf das Bild und erinnere dich an das englische Wort.", "Mira el dibujo y recuerda la palabra inglesa.",
                "Guarda l'immagine e ricorda la parola inglese.", "Pozri na obrázok a spomeň si na anglické slovo."),
            random, item.emoji, QuestionType.IMAGE_CHOICE
        )
    }

    private fun englishWords(lang: String): List<WordSeed> {
        val native = when (lang) {
            "de" -> listOf("Katze","Hund","Haus","Sonne","Wasser","Apfel","Buch","Baum","Auto","Fisch","Vogel","Blume","rot","blau","grün","eins","zwei","Mutter","Vater","Freund")
            "es" -> listOf("gato","perro","casa","sol","agua","manzana","libro","árbol","coche","pez","pájaro","flor","rojo","azul","verde","uno","dos","madre","padre","amigo")
            "it" -> listOf("gatto","cane","casa","sole","acqua","mela","libro","albero","auto","pesce","uccello","fiore","rosso","blu","verde","uno","due","mamma","papà","amico")
            "sk" -> listOf("mačka","pes","dom","slnko","voda","jablko","kniha","strom","auto","ryba","vták","kvet","červená","modrá","zelená","jeden","dva","mama","otec","priateľ")
            else -> listOf("kot","pies","dom","słońce","woda","jabłko","książka","drzewo","auto","ryba","ptak","kwiat","czerwony","niebieski","zielony","jeden","dwa","mama","tata","przyjaciel")
        }
        val en = listOf("cat","dog","house","sun","water","apple","book","tree","car","fish","bird","flower","red","blue","green","one","two","mother","father","friend")
        val emoji = listOf("🐱","🐶","🏠","☀️","💧","🍎","📘","🌳","🚗","🐟","🐦","🌼","🔴","🔵","🟢","1️⃣","2️⃣","👩","👨","🤝")
        return en.indices.map { WordSeed(native[it], en[it], emoji[it]) }
    }

    // ---------------- LOGIC ----------------

    private fun logic(stage: Stage, age: Int, lang: String, random: Random): LearningQuestion {
        return if (age <= 5 || random.nextBoolean()) {
            val patterns = listOf(
                Triple(listOf("🔴","🔵","🔴","🔵"), "🔴", listOf("🔵","🟢","⭐")),
                Triple(listOf("⭐","⭐","🌙","⭐","⭐","🌙"), "⭐", listOf("🌙","☀️","🔵")),
                Triple(listOf("🐾","🌼","🐾","🌼"), "🐾", listOf("🌼","🐶","🍎")),
                Triple(listOf("1","2","1","2"), "1", listOf("2","3","4"))
            )
            val p = patterns.random(random)
            textQuestion(
                "loc:$lang:logic:pattern:${p.first.joinToString("")}:${stage.number}", LearningCategory.LOGIC,
                tr(lang,
                    "Co będzie dalej?", "What comes next?", "Was kommt als Nächstes?",
                    "¿Qué viene después?", "Cosa viene dopo?", "Čo bude nasledovať?"),
                p.second, p.third,
                tr(lang,
                    "Znajdź powtarzający się wzór.", "Find the repeating pattern.", "Finde das sich wiederholende Muster.",
                    "Busca el patrón que se repite.", "Trova lo schema che si ripete.", "Nájdi opakujúci sa vzor."),
                random, p.first.joinToString(" "), QuestionType.SEQUENCE
            )
        } else {
            val a = random.nextInt(1, if (age >= 8) 10 else 6)
            val step = random.nextInt(2, if (age >= 8) 6 else 4)
            val seq = listOf(a, a + step, a + 2 * step, a + 3 * step)
            val answer = a + 4 * step
            numberQuestion(
                "loc:$lang:logic:num:$a:$step", tr(lang,
                    "Jaka liczba będzie następna? ${seq.joinToString(", ")}, ...",
                    "Which number comes next? ${seq.joinToString(", ")}, ...",
                    "Welche Zahl kommt als Nächstes? ${seq.joinToString(", ")}, ...",
                    "¿Qué número viene después? ${seq.joinToString(", ")}, ...",
                    "Quale numero viene dopo? ${seq.joinToString(", ")}, ...",
                    "Ktoré číslo bude nasledovať? ${seq.joinToString(", ")}, ..."),
                answer, answer + step * 2,
                tr(lang,
                    "Sprawdź, o ile rośnie każda kolejna liczba.", "Check how much each number increases.",
                    "Prüfe, um wie viel jede Zahl wächst.", "Mira cuánto aumenta cada número.",
                    "Controlla di quanto aumenta ogni numero.", "Pozri, o koľko sa každé číslo zväčšuje."),
                random
            ).copy(category = LearningCategory.LOGIC)
        }
    }

    // ---------------- NATURE ----------------

    private fun nature(stage: Stage, age: Int, lang: String, random: Random): LearningQuestion {
        val local = localNatureSeeds(lang)
        val universal = listOf(
            NativeSeed(4, tr(lang,"Które zwierzę potrafi latać?","Which animal can fly?","Welches Tier kann fliegen?","¿Qué animal puede volar?","Quale animale sa volare?","Ktoré zviera vie lietať?"), "🐦", listOf("🐟","🐶","🐢"), tr(lang,"Ptaki mają skrzydła.","Birds have wings.","Vögel haben Flügel.","Las aves tienen alas.","Gli uccelli hanno le ali.","Vtáky majú krídla.")),
            NativeSeed(4, tr(lang,"Czego potrzebuje roślina do wzrostu?","What does a plant need to grow?","Was braucht eine Pflanze zum Wachsen?","¿Qué necesita una planta para crecer?","Di cosa ha bisogno una pianta per crescere?","Čo potrebuje rastlina na rast?"), tr(lang,"wody","water","Wasser","agua","acqua","vodu"), listOf("🧸","📺","🚗"), tr(lang,"Rośliny potrzebują wody i światła.","Plants need water and light.","Pflanzen brauchen Wasser und Licht.","Las plantas necesitan agua y luz.","Le piante hanno bisogno di acqua e luce.","Rastliny potrebujú vodu a svetlo."), "🌱"),
            NativeSeed(5, tr(lang,"Która pora roku jest zwykle najcieplejsza?","Which season is usually the warmest?","Welche Jahreszeit ist meist am wärmsten?","¿Qué estación suele ser la más cálida?","Quale stagione è di solito la più calda?","Ktoré ročné obdobie býva najteplejšie?"), tr(lang,"lato","summer","Sommer","verano","estate","leto"), listOf(tr(lang,"zima","winter","Winter","invierno","inverno","zima"),tr(lang,"jesień","autumn","Herbst","otoño","autunno","jeseň"),tr(lang,"wiosna","spring","Frühling","primavera","primavera","jar")), tr(lang,"Latem dni są długie i zwykle cieplejsze.","Summer days are long and usually warmer.","Im Sommer sind die Tage lang und meist wärmer.","En verano los días son largos y suelen ser más cálidos.","In estate le giornate sono lunghe e di solito più calde.","V lete sú dni dlhé a zvyčajne teplejšie.")),
            NativeSeed(6, tr(lang,"Co zmienia się z lodu w wodę?","What changes from ice into water?","Was wird aus Eis zu Wasser?","¿Qué pasa de hielo a agua?","Cosa passa da ghiaccio ad acqua?","Čo sa mení z ľadu na vodu?"), tr(lang,"topnienie","melting","Schmelzen","fusión","scioglimento","topenie"), listOf(tr(lang,"zamarzanie","freezing","Gefrieren","congelación","congelamento","mrznutie"),tr(lang,"parowanie","evaporation","Verdunstung","evaporación","evaporazione","vyparovanie"),tr(lang,"opad","rainfall","Niederschlag","precipitación","precipitazione","zrážky")), tr(lang,"Gdy lód się ogrzewa, topnieje.","When ice warms up, it melts.","Wenn Eis wärmer wird, schmilzt es.","Cuando el hielo se calienta, se derrite.","Quando il ghiaccio si scalda, si scioglie.","Keď sa ľad zohrieva, topí sa."))
        )
        val pool = (universal + local).filter { it.minAge <= age }
        val seed = pool.random(random)
        return textQuestion("loc:$lang:nature:${pool.indexOf(seed)}:${stage.number}", LearningCategory.NATURE, seed.prompt, seed.correct, seed.wrong, seed.hint, random, seed.visual)
    }

    private fun localNatureSeeds(lang: String): List<NativeSeed> = when (lang) {
        "de" -> listOf(
            NativeSeed(6,"Welches Gebirge liegt im Süden Deutschlands?","Alpen",listOf("Anden","Himalaya","Rocky Mountains"),"Die Alpen reichen bis in den Süden Deutschlands.","🏔️"),
            NativeSeed(7,"Welcher Baum ist in deutschen Wäldern häufig?","Buche",listOf("Palme","Baobab","Mango"),"Buchen gehören zu den typischen Laubbäumen.","🌳")
        )
        "es" -> listOf(
            NativeSeed(6,"¿Qué mar baña gran parte de la costa este de España?","Mediterráneo",listOf("Báltico","Negro","Rojo"),"El Mediterráneo está al este de la península.","🌊"),
            NativeSeed(7,"¿Qué árbol es típico de muchos paisajes mediterráneos españoles?","olivo",listOf("abeto polar","baobab","secuoya gigante"),"El olivo está muy ligado al paisaje mediterráneo.","🫒")
        )
        "it" -> listOf(
            NativeSeed(6,"Quale mare bagna molte coste italiane?","Mar Mediterraneo",listOf("Mar Baltico","Mar Rosso","Mar Nero"),"L'Italia si trova nel Mediterraneo.","🌊"),
            NativeSeed(7,"Quale vulcano si trova vicino a Napoli?","Vesuvio",listOf("Etna in Sicilia","Fuji","Kilimangiaro"),"Il Vesuvio domina il Golfo di Napoli.","🌋")
        )
        "sk" -> listOf(
            NativeSeed(6,"Ktoré pohorie je najvyššie na Slovensku?","Vysoké Tatry",listOf("Malé Karpaty","Šumava","Alpy"),"Najvyššie slovenské vrcholy sú vo Vysokých Tatrách.","🏔️"),
            NativeSeed(7,"Ktoré zviera je typické pre Tatry?","kamzík",listOf("ťava","koala","tučniak"),"Kamzík tatranský žije vo vysokých horách.","🐐")
        )
        "en" -> listOf(
            NativeSeed(6,"Which sea lies between Great Britain and mainland Europe?","North Sea",listOf("Red Sea","Black Sea","Caribbean Sea"),"The North Sea is east of Great Britain.","🌊"),
            NativeSeed(7,"Which animal is native to Britain?","red fox",listOf("kangaroo","polar bear","koala"),"Red foxes live across Britain.","🦊")
        )
        else -> listOf(
            NativeSeed(6,"Które góry są najwyższe w Polsce?","Tatry",listOf("Bieszczady","Sudety","Góry Świętokrzyskie"),"Najwyższe polskie szczyty leżą w Tatrach.","🏔️"),
            NativeSeed(7,"Które zwierzę jest symbolem tatrzańskiej przyrody?","kozica",listOf("wielbłąd","koala","pingwin"),"Kozice żyją wysoko w Tatrach.","🐐")
        )
    }

    // ---------------- WORLD ----------------

    private fun world(stage: Stage, age: Int, lang: String, random: Random): LearningQuestion {
        val p = AppLanguages.profile(lang)
        val questions = mutableListOf<NativeSeed>()
        questions += NativeSeed(4,
            tr(lang,"Która flaga jest flagą Twojego kraju?","Which is your country's flag?","Welche Flagge gehört zu deinem Land?","¿Cuál es la bandera de tu país?","Qual è la bandiera del tuo paese?","Ktorá vlajka patrí tvojej krajine?"),
            p.countryFlag, listOf("🇬🇧", "🇩🇪", "🇪🇸", "🇮🇹", "🇸🇰").filter { it != p.countryFlag }.shuffled(random).take(3),
            tr(lang,"Rozpoznaj kolory i układ flagi.","Recognise the colours and layout of the flag.","Erkenne Farben und Anordnung der Flagge.","Reconoce los colores y la forma de la bandera.","Riconosci i colori e la disposizione della bandiera.","Spoznaj farby a usporiadanie vlajky."))
        questions += NativeSeed(5,
            tr(lang,"Jaka jest stolica: ${p.countryName}?","What is the capital of ${p.countryName}?","Wie heißt die Hauptstadt von ${p.countryName}?","¿Cuál es la capital de ${p.countryName}?","Qual è la capitale di ${p.countryName}?","Aké je hlavné mesto krajiny ${p.countryName}?"),
            p.capital, listOf("Londyn", "Berlin", "Madryt", "Rzym", "Bratysława").filter { it != p.capital }.shuffled(random).take(3),
            tr(lang,"To najważniejsze miasto państwa.","It is the country's capital city.","Es ist die Hauptstadt des Landes.","Es la ciudad capital del país.","È la capitale del paese.","Je to hlavné mesto krajiny."))
        questions += NativeSeed(6,
            tr(lang,"Jakiej waluty używa się w Twoim kraju?","Which currency is used in your country?","Welche Währung wird in deinem Land verwendet?","¿Qué moneda se usa en tu país?","Quale valuta si usa nel tuo paese?","Aká mena sa používa v tvojej krajine?"),
            p.currencyCode, listOf("USD", "JPY", "CHF", "CAD").filter { it != p.currencyCode }.take(3),
            "${p.currencySymbol} = ${p.currencyCode}")
        questions += NativeSeed(6,
            tr(lang,"Który zabytek kojarzy się z Twoim krajem?","Which landmark belongs to your country?","Welche Sehenswürdigkeit gehört zu deinem Land?","¿Qué monumento pertenece a tu país?","Quale monumento appartiene al tuo paese?","Ktorá pamiatka patrí k tvojej krajine?"),
            p.landmark, listOf("Big Ben", "Brama Brandenburska", "Sagrada Família", "Koloseum", "Zamek Bratysławski").filter { it != p.landmark }.shuffled(random).take(3),
            tr(lang,"Pomyśl o znanych miejscach swojego kraju.","Think about famous places in your country.","Denke an berühmte Orte deines Landes.","Piensa en lugares famosos de tu país.","Pensa ai luoghi famosi del tuo paese.","Mysli na známe miesta svojej krajiny."))
        questions += NativeSeed(7,
            tr(lang,"Która stolica leży we Włoszech?","Which capital city is in Italy?","Welche Hauptstadt liegt in Italien?","¿Qué capital está en Italia?","Quale capitale si trova in Italia?","Ktoré hlavné mesto je v Taliansku?"),
            tr(lang,"Rzym","Rome","Rom","Roma","Roma","Rím"), listOf("Berlin","Madrid","London"), "🇮🇹")
        questions += NativeSeed(7,
            tr(lang,"Które miasto jest stolicą Malty?","Which city is the capital of Malta?","Welche Stadt ist die Hauptstadt von Malta?","¿Qué ciudad es la capital de Malta?","Quale città è la capitale di Malta?","Ktoré mesto je hlavným mestom Malty?"),
            "Valletta", listOf("Mdina","Sliema","Gozo"), "🇲🇹")
        questions += NativeSeed(8,
            tr(lang,"Która rzeka przepływa przez Londyn?","Which river flows through London?","Welcher Fluss fließt durch London?","¿Qué río pasa por Londres?","Quale fiume attraversa Londra?","Ktorá rieka preteká Londýnom?"),
            tr(lang,"Tamiza","Thames","Themse","Támesis","Tamigi","Temža"), listOf("Danube","Tiber","Vistula"), "🇬🇧")
        val seed = questions.filter { it.minAge <= age }.random(random)
        return textQuestion("loc:$lang:world:${questions.indexOf(seed)}:${stage.number}", LearningCategory.WORLD, seed.prompt, seed.correct, seed.wrong, seed.hint, random, seed.visual)
    }

    // ---------------- DAILY LIFE ----------------

    private fun daily(stage: Stage, age: Int, lang: String, random: Random): LearningQuestion {
        val p = AppLanguages.profile(lang)
        return when {
            age <= 4 -> {
                val prompt = tr(lang,"Która czynność robimy przed przejściem przez ulicę?","What should you do before crossing a street?","Was sollst du vor dem Überqueren einer Straße tun?","¿Qué debes hacer antes de cruzar una calle?","Cosa devi fare prima di attraversare la strada?","Čo máš urobiť pred prechodom cez cestu?")
                val correct = tr(lang,"rozejrzeć się","look both ways","nach beiden Seiten schauen","mirar a ambos lados","guardare da entrambi i lati","pozrieť sa na obe strany")
                textQuestion("loc:$lang:daily:safety:${stage.number}", LearningCategory.DAILY, prompt, correct,
                    listOf("🎮", "😴", "🏃"),
                    tr(lang,"Najpierw zatrzymaj się i sprawdź, czy jest bezpiecznie.","Stop first and check that it is safe.","Bleib zuerst stehen und prüfe, ob es sicher ist.","Primero párate y comprueba que sea seguro.","Prima fermati e controlla che sia sicuro.","Najprv zastav a skontroluj, či je bezpečne."), random, "🚦")
            }
            age <= 6 && random.nextBoolean() -> {
                val hour = listOf(7,8,9,12,15,18).random(random)
                textQuestion("loc:$lang:daily:clock:$hour:${stage.number}", LearningCategory.DAILY,
                    tr(lang,"Która godzina jest zapisana jako $hour:00?","Which time is $hour:00?","Welche Uhrzeit ist $hour:00?","¿Qué hora es $hour:00?","Che ora è $hour:00?","Koľko je hodín, keď je $hour:00?"),
                    "$hour:00", listOf("${(hour+1)%24}:00","$hour:30","${(hour+2)%24}:30"),
                    tr(lang,"Pełna godzina ma 00 minut.","A full hour has 00 minutes.","Eine volle Stunde hat 00 Minuten.","Una hora en punto tiene 00 minutos.","Un'ora esatta ha 00 minuti.","Celá hodina má 00 minút."), random, "🕒")
            }
            age <= 7 -> {
                val prices = listOf(2,3,4,5,6,8,10); val price = prices.random(random); val paid = (price + listOf(1,2,5,10).random(random)).coerceAtMost(20)
                numberQuestion("loc:$lang:daily:change:$price:$paid:${p.currencyCode}",
                    tr(lang,
                        "Kupujesz sok za $price ${p.currencySymbol} i płacisz $paid ${p.currencySymbol}. Ile reszty?",
                        "A drink costs ${p.currencySymbol}$price and you pay ${p.currencySymbol}$paid. How much change?",
                        "Ein Getränk kostet $price ${p.currencySymbol}; du zahlst $paid ${p.currencySymbol}. Wie viel Rückgeld?",
                        "Una bebida cuesta $price ${p.currencySymbol} y pagas $paid ${p.currencySymbol}. ¿Cuánto cambio recibes?",
                        "Una bibita costa $price ${p.currencySymbol} e paghi $paid ${p.currencySymbol}. Quanto resto ricevi?",
                        "Nápoj stojí $price ${p.currencySymbol} a zaplatíš $paid ${p.currencySymbol}. Koľko dostaneš späť?"),
                    paid-price, 20,
                    tr(lang,"Odejmij cenę od zapłaconej kwoty.","Subtract the price from the amount paid.","Ziehe den Preis vom bezahlten Betrag ab.","Resta el precio de la cantidad pagada.","Sottrai il prezzo dalla somma pagata.","Odčítaj cenu od zaplatenej sumy."), random, "💶 ${p.currencyCode}").copy(category = LearningCategory.DAILY)
            }
            else -> {
                val days = dayNames(lang)
                val i = random.nextInt(0, days.lastIndex)
                val dayForQuestionPl = if (AppLanguages.normalize(lang) == "pl") dayNamesAfterPl()[i] else days[i]
                textQuestion("loc:$lang:daily:day:$i:${stage.number}", LearningCategory.DAILY,
                    tr(lang,"Jaki dzień jest po $dayForQuestionPl?","Which day comes after ${days[i]}?","Welcher Tag kommt nach ${days[i]}?","¿Qué día viene después de ${days[i]}?","Quale giorno viene dopo ${days[i]}?","Ktorý deň nasleduje po ${days[i]}?"),
                    days[i+1], days.filterIndexed { index, _ -> index != i+1 }.shuffled(random).take(3),
                    tr(lang,"Przypomnij sobie kolejność dni tygodnia.","Recall the order of the days of the week.","Erinnere dich an die Reihenfolge der Wochentage.","Recuerda el orden de los días de la semana.","Ricorda l'ordine dei giorni della settimana.","Spomeň si na poradie dní v týždni."), random, "📅")
            }
        }
    }

    private fun dayNames(lang: String): List<String> = when (lang) {
        "en" -> listOf("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday")
        "de" -> listOf("Montag","Dienstag","Mittwoch","Donnerstag","Freitag","Samstag","Sonntag")
        "es" -> listOf("lunes","martes","miércoles","jueves","viernes","sábado","domingo")
        "it" -> listOf("lunedì","martedì","mercoledì","giovedì","venerdì","sabato","domenica")
        "sk" -> listOf("pondelok","utorok","streda","štvrtok","piatok","sobota","nedeľa")
        else -> listOf("poniedziałek","wtorek","środa","czwartek","piątek","sobota","niedziela")
    }

    private fun dayNamesAfterPl(): List<String> =
        listOf("poniedziałku","wtorku","środzie","czwartku","piątku","sobocie","niedzieli")

    private fun tr(lang: String, pl: String, en: String, de: String, es: String, it: String, sk: String): String = when (AppLanguages.normalize(lang)) {
        "en" -> en
        "de" -> de
        "es" -> es
        "it" -> it
        "sk" -> sk
        else -> pl
    }
}
