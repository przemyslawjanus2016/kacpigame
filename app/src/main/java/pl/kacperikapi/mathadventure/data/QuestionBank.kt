package pl.kacperikapi.mathadventure.data

/**
 * Offline content bank. Nothing here requires network access or a child account.
 * The procedural QuestionEngine combines these seeds into many more variants.
 */
object QuestionBank {
    data class WordPair(val pl: String, val en: String, val emoji: String, val group: String)
    data class Orthography(val correct: String, val wrong: List<String>)
    data class SyllableWord(val word: String, val syllables: Int)
    data class Fact(
        val worldId: Int?,
        val category: LearningCategory,
        val promptPl: String,
        val promptEn: String,
        val optionsPl: List<String>,
        val optionsEn: List<String>,
        val correctIndex: Int,
        val hintPl: String,
        val hintEn: String
    )

    val englishWords = listOf(
        WordPair("pies", "dog", "🐶", "animals"), WordPair("kot", "cat", "🐱", "animals"),
        WordPair("koń", "horse", "🐴", "animals"), WordPair("krowa", "cow", "🐄", "animals"),
        WordPair("owca", "sheep", "🐑", "animals"), WordPair("świnia", "pig", "🐷", "animals"),
        WordPair("królik", "rabbit", "🐰", "animals"), WordPair("lis", "fox", "🦊", "animals"),
        WordPair("niedźwiedź", "bear", "🐻", "animals"), WordPair("wilk", "wolf", "🐺", "animals"),
        WordPair("jeleń", "deer", "🦌", "animals"), WordPair("wiewiórka", "squirrel", "🐿️", "animals"),
        WordPair("ptak", "bird", "🐦", "animals"), WordPair("ryba", "fish", "🐟", "animals"),
        WordPair("żaba", "frog", "🐸", "animals"), WordPair("motyl", "butterfly", "🦋", "animals"),
        WordPair("pszczoła", "bee", "🐝", "animals"), WordPair("lew", "lion", "🦁", "animals"),
        WordPair("słoń", "elephant", "🐘", "animals"), WordPair("małpa", "monkey", "🐒", "animals"),
        WordPair("dom", "house", "🏠", "places"), WordPair("szkoła", "school", "🏫", "places"),
        WordPair("sklep", "shop", "🏪", "places"), WordPair("park", "park", "🌳", "places"),
        WordPair("zamek", "castle", "🏰", "places"), WordPair("muzeum", "museum", "🏛️", "places"),
        WordPair("kościół", "church", "⛪", "places"), WordPair("most", "bridge", "🌉", "places"),
        WordPair("ulica", "street", "🛣️", "places"), WordPair("rynek", "market square", "🏘️", "places"),
        WordPair("góra", "mountain", "🏔️", "nature"), WordPair("rzeka", "river", "🏞️", "nature"),
        WordPair("jezioro", "lake", "🌊", "nature"), WordPair("morze", "sea", "🌊", "nature"),
        WordPair("drzewo", "tree", "🌳", "nature"), WordPair("kwiat", "flower", "🌼", "nature"),
        WordPair("las", "forest", "🌲", "nature"), WordPair("kamień", "stone", "🪨", "nature"),
        WordPair("słońce", "sun", "☀️", "nature"), WordPair("księżyc", "moon", "🌙", "nature"),
        WordPair("chmura", "cloud", "☁️", "weather"), WordPair("deszcz", "rain", "🌧️", "weather"),
        WordPair("śnieg", "snow", "❄️", "weather"), WordPair("wiatr", "wind", "💨", "weather"),
        WordPair("gorący", "hot", "🔥", "weather"), WordPair("zimny", "cold", "🧊", "weather"),
        WordPair("czerwony", "red", "🔴", "colors"), WordPair("niebieski", "blue", "🔵", "colors"),
        WordPair("zielony", "green", "🟢", "colors"), WordPair("żółty", "yellow", "🟡", "colors"),
        WordPair("pomarańczowy", "orange", "🟠", "colors"), WordPair("fioletowy", "purple", "🟣", "colors"),
        WordPair("biały", "white", "⚪", "colors"), WordPair("czarny", "black", "⚫", "colors"),
        WordPair("jeden", "one", "1️⃣", "numbers"), WordPair("dwa", "two", "2️⃣", "numbers"),
        WordPair("trzy", "three", "3️⃣", "numbers"), WordPair("cztery", "four", "4️⃣", "numbers"),
        WordPair("pięć", "five", "5️⃣", "numbers"), WordPair("sześć", "six", "6️⃣", "numbers"),
        WordPair("siedem", "seven", "7️⃣", "numbers"), WordPair("osiem", "eight", "8️⃣", "numbers"),
        WordPair("dziewięć", "nine", "9️⃣", "numbers"), WordPair("dziesięć", "ten", "🔟", "numbers"),
        WordPair("jabłko", "apple", "🍎", "food"), WordPair("banan", "banana", "🍌", "food"),
        WordPair("chleb", "bread", "🍞", "food"), WordPair("ser", "cheese", "🧀", "food"),
        WordPair("mleko", "milk", "🥛", "food"), WordPair("woda", "water", "💧", "food"),
        WordPair("jajko", "egg", "🥚", "food"), WordPair("marchewka", "carrot", "🥕", "food"),
        WordPair("pomidor", "tomato", "🍅", "food"), WordPair("truskawka", "strawberry", "🍓", "food"),
        WordPair("głowa", "head", "🙂", "body"), WordPair("ręka", "hand", "✋", "body"),
        WordPair("noga", "leg", "🦵", "body"), WordPair("oko", "eye", "👁️", "body"),
        WordPair("ucho", "ear", "👂", "body"), WordPair("nos", "nose", "👃", "body"),
        WordPair("rano", "morning", "🌅", "time"), WordPair("wieczór", "evening", "🌆", "time"),
        WordPair("dzisiaj", "today", "📅", "time"), WordPair("jutro", "tomorrow", "➡️", "time"),
        WordPair("poniedziałek", "Monday", "📅", "days"), WordPair("wtorek", "Tuesday", "📅", "days"),
        WordPair("środa", "Wednesday", "📅", "days"), WordPair("czwartek", "Thursday", "📅", "days"),
        WordPair("piątek", "Friday", "📅", "days"), WordPair("sobota", "Saturday", "📅", "days"),
        WordPair("niedziela", "Sunday", "📅", "days"), WordPair("książka", "book", "📘", "school"),
        WordPair("ołówek", "pencil", "✏️", "school"), WordPair("plecak", "backpack", "🎒", "school"),
        WordPair("stół", "table", "🪑", "home"), WordPair("krzesło", "chair", "🪑", "home")
    )

    val orthography = listOf(
        Orthography("góra", listOf("gura", "góóra", "góraa")), Orthography("król", listOf("krul", "krról", "króll")),
        Orthography("wróbel", listOf("wrubel", "wróbell", "wróbelh")), Orthography("żółw", listOf("rzółw", "żulw", "żółf")),
        Orthography("rzeka", listOf("żeka", "rzega", "rzekaa")), Orthography("drzewo", listOf("dżewo", "drzewó", "dżewko")),
        Orthography("chmura", listOf("hmura", "chmóra", "chmóraa")), Orthography("bohater", listOf("bochater", "bohaterz", "bohatr")),
        Orthography("herbata", listOf("cherbata", "herbadda", "herbataa")), Orthography("hokej", listOf("chokej", "hokeii", "hokejj")),
        Orthography("pszczoła", listOf("pszcoła", "pszczołaa", "pszczóla")), Orthography("książka", listOf("ksionżka", "ksiąrzka", "ksirążka")),
        Orthography("przyjaciel", listOf("pszyjaciel", "przyjacjel", "przyjacielj")), Orthography("morze", listOf("może", "morzee", "możeż")),
        Orthography("burza", listOf("bórza", "buża", "burzza")), Orthography("chleb", listOf("hleb", "chlep", "chlebb")),
        Orthography("słońce", listOf("słonce", "słońceh", "słońcee")), Orthography("źródło", listOf("zródło", "źrudło", "źródłoo")),
        Orthography("podróż", listOf("podrurz", "podróżż", "podróz")), Orthography("wiewiórka", listOf("wiewiurka", "wiewiórkaa", "wiewjórka")),
        Orthography("piórnik", listOf("piurnik", "pjórnik", "piórnikk")), Orthography("ogórek", listOf("ogurek", "ogórekk", "ogurekk")),
        Orthography("róża", listOf("ruża", "rórza", "róza")), Orthography("drużyna", listOf("drurzyna", "drużinaa", "dróżyna")),
        Orthography("schody", listOf("shody", "schodii", "schoddy")), Orthography("wschód", listOf("wshód", "wschud", "wschódd")),
        Orthography("chrząszcz", listOf("hrząszcz", "chrząszc", "chrząszczh")), Orthography("księżyc", listOf("ksienżyc", "księrzyc", "księzyc")),
        Orthography("pieniądze", listOf("pieniondze", "pieniądzeh", "pieniądzee")), Orthography("zwierzę", listOf("zwieże", "zwierze", "zwierzęę"))
    )

    val syllables = listOf(
        SyllableWord("kot", 1), SyllableWord("dom", 1), SyllableWord("pies", 1), SyllableWord("las", 1),
        SyllableWord("rower", 2), SyllableWord("Kapi", 2), SyllableWord("zamek", 2), SyllableWord("góra", 2),
        SyllableWord("morze", 2), SyllableWord("rynek", 2), SyllableWord("Wieliczka", 3), SyllableWord("Kacper", 2),
        SyllableWord("kaplica", 3), SyllableWord("fontanna", 3), SyllableWord("Mediolan", 3), SyllableWord("kolorowy", 4),
        SyllableWord("przygoda", 3), SyllableWord("matematyka", 5), SyllableWord("edukacja", 4), SyllableWord("wiewiórka", 3),
        SyllableWord("samochód", 3), SyllableWord("telefon", 3), SyllableWord("komputer", 3), SyllableWord("wakacje", 3)
    )

    val facts: List<Fact> = buildList {
        // Nature and science
        add(f(null, LearningCategory.NATURE, "Które zwierzę jest ssakiem?", "Which animal is a mammal?", listOf("delfin", "pstrąg", "żaba", "jaszczurka"), listOf("dolphin", "trout", "frog", "lizard"), 0, "Ssaki karmią młode mlekiem.", "Mammals feed their young with milk."))
        add(f(null, LearningCategory.NATURE, "Czego roślina potrzebuje do wzrostu?", "What does a plant need to grow?", listOf("światła i wody", "plastiku", "samego piasku", "ciemności"), listOf("light and water", "plastic", "dry sand only", "darkness"), 0, "Rośliny korzystają ze światła i pobierają wodę.", "Plants use light and water."))
        add(f(null, LearningCategory.NATURE, "Która pora roku następuje po wiośnie?", "Which season comes after spring?", listOf("lato", "zima", "jesień", "wiosna"), listOf("summer", "winter", "autumn", "spring"), 0, "Wiosna, lato, jesień, zima.", "Spring, summer, autumn, winter."))
        add(f(null, LearningCategory.NATURE, "Co jest głównym źródłem światła dla Ziemi?", "What is Earth's main source of light?", listOf("Słońce", "Księżyc", "Mars", "chmura"), listOf("Sun", "Moon", "Mars", "cloud"), 0, "Słońce oświetla i ogrzewa Ziemię.", "The Sun lights and warms Earth."))
        add(f(null, LearningCategory.NATURE, "Które zwierzę może zapadać w sen zimowy?", "Which animal can hibernate?", listOf("jeż", "bocian", "rekin", "papuga"), listOf("hedgehog", "stork", "shark", "parrot"), 0, "Jeże ograniczają aktywność zimą.", "Hedgehogs can hibernate in winter."))
        add(f(null, LearningCategory.NATURE, "Która część rośliny pobiera wodę z gleby?", "Which plant part takes up water from soil?", listOf("korzenie", "kwiaty", "owoce", "nasiona"), listOf("roots", "flowers", "fruit", "seeds"), 0, "Korzenie są pod ziemią.", "Roots are underground."))
        add(f(null, LearningCategory.NATURE, "Który stan skupienia ma lód?", "What state of matter is ice?", listOf("stały", "ciekły", "gazowy", "świetlny"), listOf("solid", "liquid", "gas", "light"), 0, "Lód zachowuje swój kształt.", "Ice keeps its shape."))
        add(f(null, LearningCategory.NATURE, "Co robią pszczoły, odwiedzając kwiaty?", "What do bees do when visiting flowers?", listOf("pomagają w zapylaniu", "gaszą światło", "zamrażają wodę", "budują drogi"), listOf("help pollinate", "turn off lights", "freeze water", "build roads"), 0, "Przenoszą pyłek między kwiatami.", "They move pollen between flowers."))
        add(f(null, LearningCategory.NATURE, "Jak nazywa się woda spadająca z chmur?", "What is water falling from clouds called?", listOf("deszcz", "cień", "piasek", "dym"), listOf("rain", "shadow", "sand", "smoke"), 0, "To opad atmosferyczny.", "It is precipitation."))
        add(f(null, LearningCategory.NATURE, "Które zwierzę jest ptakiem?", "Which animal is a bird?", listOf("bocian", "lis", "żaba", "delfin"), listOf("stork", "fox", "frog", "dolphin"), 0, "Ptaki mają pióra.", "Birds have feathers."))

        // General world knowledge
        add(f(null, LearningCategory.WORLD, "Na jakim kontynencie leży Polska?", "Which continent is Poland in?", listOf("Europa", "Azja", "Afryka", "Australia"), listOf("Europe", "Asia", "Africa", "Australia"), 0, "Polska leży w Europie.", "Poland is in Europe."))
        add(f(null, LearningCategory.WORLD, "Które morze leży na północy Polski?", "Which sea lies north of Poland?", listOf("Bałtyckie", "Śródziemne", "Czerwone", "Karaibskie"), listOf("Baltic Sea", "Mediterranean Sea", "Red Sea", "Caribbean Sea"), 0, "Polskie wybrzeże leży nad Bałtykiem.", "Poland's coast is on the Baltic Sea."))
        add(f(null, LearningCategory.WORLD, "Jaka jest stolica Polski?", "What is the capital of Poland?", listOf("Warszawa", "Kraków", "Gdańsk", "Wrocław"), listOf("Warsaw", "Krakow", "Gdansk", "Wroclaw"), 0, "Stolicą Polski jest Warszawa.", "Warsaw is the capital of Poland."))
        add(f(null, LearningCategory.WORLD, "Która planeta jest naszym domem?", "Which planet is our home?", listOf("Ziemia", "Mars", "Wenus", "Jowisz"), listOf("Earth", "Mars", "Venus", "Jupiter"), 0, "Mieszkamy na Ziemi.", "We live on Earth."))
        add(f(null, LearningCategory.WORLD, "Który kierunek na mapie zwykle jest u góry?", "Which direction is usually at the top of a map?", listOf("północ", "południe", "wschód", "zachód"), listOf("north", "south", "east", "west"), 0, "Na większości map północ jest u góry.", "North is at the top of most maps."))

        // Wieliczka
        add(f(1, LearningCategory.WORLD, "Z czego słynie Wieliczka?", "What is Wieliczka famous for?", listOf("kopalni soli", "wielkiej pustyni", "wulkanu", "oceanu"), listOf("salt mine", "huge desert", "volcano", "ocean"), 0, "Wieliczka od wieków jest związana z solą.", "Wieliczka has been connected with salt for centuries."))
        add(f(1, LearningCategory.WORLD, "Jak nazywa się słynna podziemna kaplica w kopalni?", "What is the famous underground chapel in the mine called?", listOf("Kaplica św. Kingi", "Kaplica św. Pawła", "Kaplica Smoka", "Kaplica Morska"), listOf("St. Kinga's Chapel", "St. Paul's Chapel", "Dragon Chapel", "Sea Chapel"), 0, "Jej patronką jest św. Kinga.", "It is dedicated to St. Kinga."))
        add(f(1, LearningCategory.WORLD, "Do czego służy tężnia solankowa?", "What is a brine graduation tower used for?", listOf("do tworzenia solankowego mikroklimatu", "do startu samolotów", "do pieczenia chleba", "do hodowli ryb"), listOf("creating a brine microclimate", "launching planes", "baking bread", "raising fish"), 0, "Solanka spływa po gałązkach i tworzy aerozol.", "Brine flows over branches and creates salty aerosol."))
        add(f(1, LearningCategory.WORLD, "Który zabytek jest dawną siedzibą zarządu żup?", "Which landmark was the historic seat of the saltworks administration?", listOf("Zamek Żupny", "Koloseum", "Tower Bridge", "Duomo"), listOf("Saltworks Castle", "Colosseum", "Tower Bridge", "Duomo"), 0, "To Zamek Żupny w centrum Wieliczki.", "It is the Saltworks Castle in central Wieliczka."))

        // Krakow
        add(f(2, LearningCategory.WORLD, "Jaki legendarny stwór mieszkał pod Wawelem?", "Which legendary creature lived beneath Wawel?", listOf("smok", "jednorożec", "syrena", "olbrzym"), listOf("dragon", "unicorn", "mermaid", "giant"), 0, "Legenda opowiada o Smoku Wawelskim.", "The legend tells of the Wawel Dragon."))
        add(f(2, LearningCategory.WORLD, "Jak nazywa się duży plac w centrum Krakowa?", "What is the large square in central Krakow called?", listOf("Rynek Główny", "Plac Solny", "Piazza Navona", "Trafalgar Square"), listOf("Main Market Square", "Salt Square", "Piazza Navona", "Trafalgar Square"), 0, "To jeden z najbardziej znanych rynków w Polsce.", "It is one of Poland's best-known squares."))
        add(f(2, LearningCategory.WORLD, "Co znajduje się na wzgórzu wawelskim?", "What is on Wawel Hill?", listOf("zamek i katedra", "lotnisko", "kopalnia", "latarnia morska"), listOf("castle and cathedral", "airport", "mine", "lighthouse"), 0, "Wawel jest ważnym miejscem polskiej historii.", "Wawel is an important place in Polish history."))

        // Tatras
        add(f(3, LearningCategory.WORLD, "Jak nazywa się najwyższy szczyt Polski?", "What is the highest peak in Poland?", listOf("Rysy", "Giewont", "Kasprowy Wierch", "Śnieżka"), listOf("Rysy", "Giewont", "Kasprowy Wierch", "Sniezka"), 0, "Najwyższy punkt Rysów po polskiej stronie ma 2499 m.", "Rysy is Poland's highest peak."))
        add(f(3, LearningCategory.NATURE, "Które zwierzę kojarzy się z Tatrami?", "Which animal is strongly associated with the Tatras?", listOf("kozica", "żyrafa", "kangur", "pingwin"), listOf("chamois", "giraffe", "kangaroo", "penguin"), 0, "Kozice świetnie poruszają się po stromych skałach.", "Chamois move well on steep rocks."))
        add(f(3, LearningCategory.WORLD, "Morskie Oko to…", "Morskie Oko is a…", listOf("górskie jezioro", "morze", "zamek", "jaskinia solna"), listOf("mountain lake", "sea", "castle", "salt cave"), 0, "To duże jezioro w Tatrach.", "It is a large mountain lake in the Tatras."))

        // Rome
        add(f(4, LearningCategory.WORLD, "W jakim kraju leży Rzym?", "Which country is Rome in?", listOf("Włochy", "Hiszpania", "Francja", "Grecja"), listOf("Italy", "Spain", "France", "Greece"), 0, "Rzym jest stolicą Włoch.", "Rome is the capital of Italy."))
        add(f(4, LearningCategory.WORLD, "Który zabytek Rzymu był wielką areną?", "Which Rome landmark was a huge arena?", listOf("Koloseum", "Big Ben", "Sukiennice", "Duomo"), listOf("Colosseum", "Big Ben", "Cloth Hall", "Duomo"), 0, "Koloseum było amfiteatrem.", "The Colosseum was an amphitheatre."))
        add(f(4, LearningCategory.WORLD, "Do której fontanny turyści często wrzucają monety?", "Into which fountain do visitors often toss coins?", listOf("Fontanna di Trevi", "Fontanna Neptuna w Gdańsku", "Tężnia", "Blue Grotto"), listOf("Trevi Fountain", "Neptune Fountain", "Graduation Tower", "Blue Grotto"), 0, "To słynna Fontanna di Trevi.", "It is the famous Trevi Fountain."))

        // London
        add(f(5, LearningCategory.WORLD, "Nad jaką rzeką leży Londyn?", "Which river runs through London?", listOf("Tamiza", "Wisła", "Tyber", "Sekwana"), listOf("Thames", "Vistula", "Tiber", "Seine"), 0, "Przez Londyn płynie Tamiza.", "The Thames runs through London."))
        add(f(5, LearningCategory.WORLD, "Który most jest jednym z symboli Londynu?", "Which bridge is one of London's symbols?", listOf("Tower Bridge", "Most Grunwaldzki", "Ponte Vecchio", "Golden Gate"), listOf("Tower Bridge", "Grunwald Bridge", "Ponte Vecchio", "Golden Gate"), 0, "Ma dwie charakterystyczne wieże.", "It has two distinctive towers."))
        add(f(5, LearningCategory.ENGLISH, "Jak po angielsku powiesz 'Dzień dobry'?", "How do you say 'Dzień dobry' in English?", listOf("Good morning", "Good night", "Thank you", "Please"), listOf("Good morning", "Good night", "Thank you", "Please"), 0, "To powitanie używane rano.", "It is a morning greeting."))

        // Milan
        add(f(6, LearningCategory.WORLD, "W jakim kraju leży Mediolan?", "Which country is Milan in?", listOf("Włochy", "Malta", "Polska", "Wielka Brytania"), listOf("Italy", "Malta", "Poland", "United Kingdom"), 0, "Mediolan leży w północnych Włoszech.", "Milan is in northern Italy."))
        add(f(6, LearningCategory.WORLD, "Jak nazywa się słynna katedra w Mediolanie?", "What is Milan's famous cathedral called?", listOf("Duomo", "Big Ben", "Wawel", "Koloseum"), listOf("Duomo", "Big Ben", "Wawel", "Colosseum"), 0, "Duomo di Milano stoi w centrum miasta.", "Milan Cathedral, the Duomo, stands in the city centre."))
        add(f(6, LearningCategory.WORLD, "La Scala to słynny…", "La Scala is a famous…", listOf("teatr operowy", "most", "stadion narciarski", "zamek solny"), listOf("opera house", "bridge", "ski stadium", "salt castle"), 0, "La Scala jest związana z operą i muzyką.", "La Scala is famous for opera and music."))

        // Malta
        add(f(7, LearningCategory.WORLD, "Jaka jest stolica Malty?", "What is the capital of Malta?", listOf("Valletta", "Rzym", "Londyn", "Mdina"), listOf("Valletta", "Rome", "London", "Mdina"), 0, "Stolicą Malty jest Valletta.", "Valletta is the capital of Malta."))
        add(f(7, LearningCategory.WORLD, "Malta jest państwem…", "Malta is a…", listOf("wyspiarskim", "bez dostępu do morza", "arktycznym", "pustynnym"), listOf("island country", "landlocked country", "Arctic country", "desert country"), 0, "Malta składa się z wysp na Morzu Śródziemnym.", "Malta is made up of islands in the Mediterranean."))
        add(f(7, LearningCategory.WORLD, "Która miejscowość Malty nazywana jest Cichym Miastem?", "Which Maltese city is known as the Silent City?", listOf("Mdina", "Valletta", "Marsaxlokk", "Comino"), listOf("Mdina", "Valletta", "Marsaxlokk", "Comino"), 0, "Mdina słynie z cichych, historycznych uliczek.", "Mdina is known for its quiet historic streets."))
    }

    private fun f(
        worldId: Int?, category: LearningCategory,
        promptPl: String, promptEn: String,
        optionsPl: List<String>, optionsEn: List<String>, correctIndex: Int,
        hintPl: String, hintEn: String
    ) = Fact(worldId, category, promptPl, promptEn, optionsPl, optionsEn, correctIndex, hintPl, hintEn)
}
