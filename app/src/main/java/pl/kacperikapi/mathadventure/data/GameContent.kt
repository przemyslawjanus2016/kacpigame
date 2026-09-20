package pl.kacperikapi.mathadventure.data

import pl.kacperikapi.mathadventure.R

object GameContent {
    val categories = listOf(
        CategoryInfo(LearningCategory.MATH, R.string.category_math, R.string.category_math_desc, "➕"),
        CategoryInfo(LearningCategory.POLISH, R.string.category_polish, R.string.category_polish_desc, "🇵🇱"),
        CategoryInfo(LearningCategory.ENGLISH, R.string.category_english, R.string.category_english_desc, "🇬🇧"),
        CategoryInfo(LearningCategory.LOGIC, R.string.category_logic, R.string.category_logic_desc, "🧠"),
        CategoryInfo(LearningCategory.NATURE, R.string.category_nature, R.string.category_nature_desc, "🌿"),
        CategoryInfo(LearningCategory.WORLD, R.string.category_world, R.string.category_world_desc, "🌍"),
        CategoryInfo(LearningCategory.DAILY, R.string.category_daily, R.string.category_daily_desc, "⏰")
    )

    private val route = listOf(
        .14f to .17f,
        .38f to .12f,
        .68f to .20f,
        .78f to .37f,
        .59f to .48f,
        .76f to .63f,
        .50f to .71f,
        .24f to .67f,
        .31f to .84f,
        .67f to .87f
    )

    private val categoryRotation = listOf(
        listOf(LearningCategory.MATH, LearningCategory.LOGIC),
        listOf(LearningCategory.POLISH, LearningCategory.MATH),
        listOf(LearningCategory.NATURE, LearningCategory.LOGIC),
        listOf(LearningCategory.ENGLISH, LearningCategory.MATH),
        listOf(LearningCategory.WORLD, LearningCategory.MATH),
        listOf(LearningCategory.POLISH, LearningCategory.WORLD),
        listOf(LearningCategory.DAILY, LearningCategory.LOGIC),
        listOf(LearningCategory.NATURE, LearningCategory.ENGLISH),
        listOf(LearningCategory.MATH, LearningCategory.POLISH, LearningCategory.LOGIC),
        LearningCategory.entries.toList()
    )

    val worlds: List<WorldDefinition> = listOf(
        WorldDefinition(
            id = 1,
            nameRes = R.string.world_wieliczka,
            icon = "🧂",
            subtitlePl = "Solna przygoda i rodzinne miasto Kacpra i Kapi",
            subtitleEn = "A salt adventure in Kacper and Kapi's hometown",
            heroArtRes = R.drawable.wieliczka_world_art,
            thumbnailRes = R.drawable.world_thumb_wieliczka,
            stages = stages(1, listOf(
                "Rynek Górny" to "Upper Market Square",
                "Zamek Żupny" to "Saltworks Castle",
                "Tężnia Solankowa" to "Brine Graduation Tower",
                "Szyb Daniłowicza" to "Daniłowicz Shaft",
                "Komora Mikołaja Kopernika" to "Nicolaus Copernicus Chamber",
                "Kaplica św. Kingi" to "St. Kinga's Chapel",
                "Komora Weimar" to "Weimar Chamber",
                "Podziemne jezioro" to "Underground Lake",
                "Komora Staszica" to "Staszic Chamber",
                "Skarbnik" to "Treasurer Spirit"
            ))
        ),
        WorldDefinition(
            id = 2,
            nameRes = R.string.world_krakow,
            icon = "🐉",
            subtitlePl = "Smok, Wawel i zagadki królewskiego miasta",
            subtitleEn = "The dragon, Wawel Castle and royal-city puzzles",
            heroArtRes = R.drawable.krakow_world_art,
            thumbnailRes = R.drawable.world_thumb_krakow,
            stages = stages(2, listOf(
                "Planty" to "Planty Park",
                "Rynek Główny" to "Main Market Square",
                "Sukiennice" to "Cloth Hall",
                "Kościół Mariacki" to "St. Mary's Basilica",
                "Brama Floriańska" to "St. Florian's Gate",
                "Barbakan" to "Barbican",
                "Wawel" to "Wawel Castle",
                "Smok Wawelski" to "Wawel Dragon",
                "Bulwary Wiślane" to "Vistula Boulevards",
                "Kopiec Krakusa" to "Krakus Mound"
            ))
        ),
        WorldDefinition(
            id = 3,
            nameRes = R.string.world_tatry,
            icon = "🏔️",
            subtitlePl = "Góry, przyroda i wyzwania prawdziwego odkrywcy",
            subtitleEn = "Mountains, nature and true explorer challenges",
            heroArtRes = R.drawable.tatry_world_art,
            thumbnailRes = R.drawable.world_thumb_tatry,
            stages = stages(3, listOf(
                "Kuźnice" to "Kuźnice",
                "Dolina Strążyska" to "Strążyska Valley",
                "Rusinowa Polana" to "Rusinowa Glade",
                "Morskie Oko" to "Morskie Oko",
                "Gęsia Szyja" to "Gęsia Szyja",
                "Hala Gąsienicowa" to "Gąsienicowa Meadow",
                "Kasprowy Wierch" to "Kasprowy Wierch",
                "Giewont" to "Giewont",
                "Tatrzańska fauna" to "Tatra Wildlife",
                "Dolina Pięciu Stawów" to "Five Polish Ponds Valley"
            ))
        ),
        WorldDefinition(
            id = 4,
            nameRes = R.string.world_rome,
            icon = "🏛️",
            subtitlePl = "Historia, zabytki i włoskie odkrycia",
            subtitleEn = "History, landmarks and Italian discoveries",
            heroArtRes = R.drawable.rome_world_art,
            thumbnailRes = R.drawable.world_thumb_rome,
            stages = stages(4, listOf(
                "Piazza Navona" to "Piazza Navona",
                "Panteon" to "Pantheon",
                "Fontanna di Trevi" to "Trevi Fountain",
                "Schody Hiszpańskie" to "Spanish Steps",
                "Koloseum" to "Colosseum",
                "Forum Romanum" to "Roman Forum",
                "Kapitol" to "Capitoline Hill",
                "Zamek Świętego Anioła" to "Castel Sant'Angelo",
                "Villa Borghese" to "Villa Borghese",
                "Circus Maximus" to "Circus Maximus"
            ))
        ),
        WorldDefinition(
            id = 5,
            nameRes = R.string.world_london,
            icon = "🚌",
            subtitlePl = "Angielski, ciekawostki i wielkomiejska przygoda",
            subtitleEn = "English, curiosities and a big-city adventure",
            heroArtRes = R.drawable.london_world_art,
            thumbnailRes = R.drawable.world_thumb_london,
            stages = stages(5, listOf(
                "Big Ben" to "Big Ben",
                "Westminster" to "Westminster",
                "London Eye" to "London Eye",
                "Tower Bridge" to "Tower Bridge",
                "Tower of London" to "Tower of London",
                "British Museum" to "British Museum",
                "Hyde Park" to "Hyde Park",
                "Buckingham Palace" to "Buckingham Palace",
                "Natural History Museum" to "Natural History Museum",
                "Katedra św. Pawła" to "St Paul’s Cathedral"
            ))
        ),
        WorldDefinition(
            id = 6,
            nameRes = R.string.world_milan,
            icon = "⛪",
            subtitlePl = "Sztuka, wzory, design i kreatywne łamigłówki",
            subtitleEn = "Art, patterns, design and creative puzzles",
            heroArtRes = R.drawable.milan_world_art,
            thumbnailRes = R.drawable.world_thumb_milan,
            stages = stages(6, listOf(
                "Duomo" to "Duomo",
                "Galleria Vittorio Emanuele II" to "Galleria Vittorio Emanuele II",
                "Zamek Sforzów" to "Sforza Castle",
                "Brera" to "Brera",
                "Navigli" to "Navigli",
                "La Scala" to "La Scala",
                "Muzeum Nauki i Techniki" to "Science and Technology Museum",
                "Porta Nuova" to "Porta Nuova",
                "San Siro" to "San Siro",
                "Arco della Pace" to "Arco della Pace"
            ))
        ),
        WorldDefinition(
            id = 7,
            nameRes = R.string.world_malta,
            icon = "⛵",
            subtitlePl = "Wyspy, morze i wielki finał edukacyjnej podróży",
            subtitleEn = "Islands, sea and the grand finale of the learning journey",
            heroArtRes = R.drawable.malta_world_art,
            thumbnailRes = R.drawable.world_thumb_malta,
            stages = stages(7, listOf(
                "Valletta" to "Valletta",
                "Upper Barrakka Gardens" to "Upper Barrakka Gardens",
                "Trzy Miasta" to "Three Cities",
                "Mdina" to "Mdina",
                "Rabat" to "Rabat",
                "Blue Grotto" to "Blue Grotto",
                "Marsaxlokk" to "Marsaxlokk",
                "Gozo" to "Gozo",
                "Comino" to "Comino",
                "Świątynie Ħaġar Qim" to "Ħaġar Qim Temples"
            ))
        )
    )

    private fun stages(worldId: Int, names: List<Pair<String, String>>): List<Stage> =
        names.take(GameRules.STAGES_PER_WORLD).mapIndexed { index, pair ->
            val p = route[index]
            Stage(
                id = "w${worldId}s${index + 1}",
                worldId = worldId,
                number = index + 1,
                namePl = pair.first,
                nameEn = pair.second,
                categories = categoryRotation[index],
                x = p.first,
                y = p.second,
                targetAge = GameRules.targetAge(index + 1)
            )
        }

    fun story(stage: Stage): StoryBeat {
        val adventures = mapOf(
            1 to listOf(
                "Kapi wyczuł zapach soli i pociągnął Kacpra w stronę Rynku Górnego. To tutaj zaczyna się ich wielicka wyprawa!",
                "Kacper zauważył mury Zamku Żupnego, a Kapi już krąży przy wejściu i sprawdza każdy zakamarek.",
                "Przy tężni Kapi nadstawił uszu, a Kacper poczuł w powietrzu słoną mgiełkę. Czas sprawdzić, skąd się bierze!",
                "Przed Szybem Daniłowicza Kapi zatrzymał się i spojrzał w dół. Kacper wie, że prawdziwa przygoda prowadzi pod ziemię.",
                "W komorze Kopernika Kacper spogląda w górę, jakby szukał gwiazd, a Kapi cierpliwie czeka na kolejną wskazówkę.",
                "W Kaplicy św. Kingi Kapi cichnie, a Kacper z zachwytem ogląda solne rzeźby i niezwykłe wnętrze.",
                "Echo w Komorze Weimar przyciąga uwagę Kapi. Kacper rusza za nim, by sprawdzić, dokąd prowadzi dźwięk.",
                "Kapi usłyszał cichy plusk. Razem z Kacprem docierają do podziemnego jeziora ukrytego głęboko w kopalni.",
                "Ogromna Komora Staszica robi wrażenie nawet na Kapi. Kacper rozgląda się i szuka ostatniego śladu.",
                "Kapi złapał tajemniczy trop, a Kacper już wie, kto może czekać w kopalni. Pora spotkać legendarnego Skarbnika!"
            ),
            2 to listOf(
                "Kapi wbiega na Planty, a Kacper rusza za nim zieloną trasą otaczającą stare miasto.",
                "Na Rynku Głównym Kapi obserwuje gołębie, a Kacper wypatruje kolejnego punktu krakowskiej wyprawy.",
                "Kacper zauważa Sukiennice, a Kapi prowadzi go między gwar i stragany w samym sercu Krakowa.",
                "Rozlega się hejnał. Kapi nadstawia uszu, a Kacper od razu spogląda w stronę wież Kościoła Mariackiego.",
                "Kapi przechodzi pod Bramą Floriańską jak prawdziwy odkrywca, a Kacper szuka śladów dawnego Krakowa.",
                "Przed Barbakanem Kacper ogląda potężne mury, a Kapi sprawdza, czy za nimi nie kryje się kolejna zagadka.",
                "Wzgórze Wawelskie jest coraz bliżej. Kapi przyspiesza, a Kacper wie, że dotarli do miejsca polskich królów.",
                "Kapi nagle zatrzymuje się przy smoczej jamie. Czyżby wyczuł Smoka Wawelskiego? Kacper postanawia to sprawdzić.",
                "Nad Wisłą Kapi łapie wiatr w uszy, a Kacper obserwuje Wawel z Bulwarów Wiślanych.",
                "Kacper i Kapi wspinają się na Kopiec Krakusa. Na szczycie czeka na nich szeroki widok na Kraków."
            ),
            3 to listOf(
                "W Kuźnicach Kacper poprawia plecak, a Kapi z energią rusza na pierwszy tatrzański szlak.",
                "Kapi wyczuwa leśne zapachy w Dolinie Strążyskiej, a Kacper wypatruje górskich szczytów.",
                "Na Rusinowej Polanie Kapi zatrzymuje się na chwilę, a Kacper podziwia panoramę Tatr.",
                "Kacper dostrzega taflę Morskiego Oka. Kapi podbiega bliżej, ale wie, że górskie jezioro oglądamy z brzegu.",
                "Szlak prowadzi coraz wyżej. Kapi dzielnie towarzyszy Kacprowi w wyprawie w stronę Gęsiej Szyi.",
                "Na Hali Gąsienicowej Kacper rozgląda się po górach, a Kapi próbuje wychwycić każdy nowy zapach.",
                "Kasprowy Wierch jest wysoko nad nimi. Kacper wskazuje szczyt, a Kapi jest gotowy na kolejne wyzwanie.",
                "Kacper rozpoznaje charakterystyczny Giewont, a Kapi spogląda w tę samą stronę. Czas na górską zagadkę!",
                "Kapi zauważa ruch wśród skał. Kacper przypomina, że tatrzańskie zwierzęta obserwujemy spokojnie i z daleka.",
                "Przed nimi Dolina Pięciu Stawów. Kacper liczy jeziora, a Kapi z ciekawością poznaje finał tatrzańskiej wyprawy."
            ),
            4 to listOf(
                "Na Piazza Navona Kapi rozgląda się między fontannami, a Kacper rozpoczyna rzymską wyprawę.",
                "Kacper patrzy na ogromną kopułę Panteonu, a Kapi cierpliwie czeka, aż odkryją jego tajemnicę.",
                "Szum wody prowadzi ich do Fontanny di Trevi. Kapi nadstawia uszu, a Kacper podziwia niezwykłe rzeźby.",
                "Na Schodach Hiszpańskich Kacper wypatruje miasta z góry, a Kapi robi krótką przerwę przed dalszą drogą.",
                "Koloseum pojawia się przed nimi. Kacper wyobraża sobie starożytny Rzym, a Kapi bada otoczenie.",
                "W Forum Romanum Kacper szuka śladów dawnego miasta, a Kapi prowadzi go wśród historii sprzed wielu wieków.",
                "Na Kapitolu Kapi zwalnia, a Kacper rozgląda się po jednym z najsłynniejszych rzymskich wzgórz.",
                "Kacper dostrzega Zamek Świętego Anioła, a Kapi prowadzi go w stronę potężnej budowli nad Tybrem.",
                "W Villa Borghese Kapi cieszy się zielenią, a Kacper odpoczywa przed ostatnim etapem rzymskiej wyprawy.",
                "Na Circus Maximus Kacper wyobraża sobie pędzące rydwany. Kapi rusza wzdłuż dawnej areny po finałową wskazówkę."
            ),
            5 to listOf(
                "Kacper spogląda na zegar przy Big Benie, a Kapi rozpoczyna swoją londyńską przygodę.",
                "W Westminster Kapi maszeruje obok Kacpra, który wypatruje najważniejszych budynków brytyjskiej stolicy.",
                "London Eye góruje nad Tamizą. Kacper patrzy w górę, a Kapi zastanawia się, dokąd prowadzi kolejny trop.",
                "Przed Tower Bridge Kapi zatrzymuje się przy rzece, a Kacper podziwia charakterystyczne wieże mostu.",
                "W Tower of London Kacper szuka królewskich historii, a Kapi sprawdza, czy nie czeka tu kolejna tajemnica.",
                "British Museum jest pełne historii z całego świata. Kacper wybiera kierunek, a Kapi rusza tuż obok.",
                "W Hyde Parku Kapi wreszcie może nacieszyć się zielenią, a Kacper przygotowuje się do dalszego zwiedzania.",
                "Przed Buckingham Palace Kapi zatrzymuje się grzecznie, a Kacper wypatruje królewskich strażników.",
                "W Natural History Museum Kacper chce zobaczyć wszystko, a Kapi szczególnie ciekawi się śladami dawnych zwierząt.",
                "Nad Londynem widać kopułę Katedry św. Pawła. Kacper i Kapi ruszają tam po finał londyńskiej przygody."
            ),
            6 to listOf(
                "Kacper staje przed ogromnym Duomo, a Kapi zadziera głowę, jakby też próbował zobaczyć szczyt katedry.",
                "W Galleria Vittorio Emanuele II Kapi idzie przy nodze Kacpra, a wokół nich błyszczą eleganckie witryny.",
                "Przy Zamku Sforzów Kacper wypatruje dawnych murów, a Kapi sprawdza drogę do następnej misji.",
                "W dzielnicy Brera Kacper szuka sztuki i kolorów, a Kapi prowadzi go wąskimi uliczkami.",
                "Nad kanałami Navigli Kapi obserwuje wodę, a Kacper odkrywa zupełnie inną stronę Mediolanu.",
                "Przed La Scalą Kapi nadstawia uszu, jakby czekał na muzykę, a Kacper poznaje słynną operę.",
                "W Muzeum Nauki i Techniki Kacper ma mnóstwo pytań, a Kapi pomaga mu odnaleźć kolejny punkt wyprawy.",
                "W Porta Nuova nowoczesne budynki rosną wokół nich. Kacper patrzy w górę, a Kapi pewnie idzie dalej.",
                "Przy San Siro Kacper myśli o wielkich meczach, a Kapi z energią rusza w stronę ostatniego celu.",
                "Arco della Pace zamyka mediolańską trasę. Kacper i Kapi docierają pod monumentalny łuk po finałową wskazówkę."
            ),
            7 to listOf(
                "W Valletcie Kacper czuje morską bryzę, a Kapi rozpoczyna ostatnią wyspiarską przygodę.",
                "Z Upper Barrakka Gardens Kacper ogląda port, a Kapi wypatruje ruchu po drugiej stronie wody.",
                "Kacper wskazuje Trzy Miasta, a Kapi rusza poznawać kolejne maltańskie uliczki.",
                "W cichej Mdinie Kapi zwalnia kroku, a Kacper odkrywa wąskie uliczki dawnej stolicy.",
                "W Rabacie Kacper szuka śladów historii, a Kapi prowadzi go do kolejnego punktu wyprawy.",
                "Przy Blue Grotto Kacper zachwyca się kolorem morza, a Kapi obserwuje fale z bezpiecznego miejsca.",
                "W Marsaxlokk Kapi węszy morskie zapachy, a Kacper ogląda kolorowe łodzie w porcie.",
                "Na Gozo Kacper i Kapi ruszają przed siebie, gotowi odkrywać spokojniejszą stronę Malty.",
                "Błękitna woda wokół Comino przyciąga wzrok Kacpra. Kapi wie, że do finału został już tylko jeden etap.",
                "Przed świątyniami Ħaġar Qim Kacper patrzy na ogromne kamienie, a Kapi kończy z nim wielką podróż przez siedem światów."
            )
        )
        val text = adventures[stage.worldId]?.getOrNull(stage.number - 1)
            ?: "Kacper i Kapi ruszają do kolejnego miejsca."
        return StoryBeat(
            titlePl = "Przygoda: \${stage.namePl}",
            titleEn = "Adventure: \${stage.nameEn}",
            textPl = "$text Rozwiąż zadania i zdobądź kolejny punkt wyprawy.",
            textEn = "Kacper and Kapi continue their adventure at \${stage.nameEn}. Solve the tasks to discover what comes next.",
            factPl = AttractionContent.forStage(stage)?.factPl ?: "Każde miejsce na trasie kryje ciekawą historię.",
            factEn = AttractionContent.forStage(stage)?.factEn ?: "Every place on the route has an interesting story.",
            emoji = when (stage.worldId) { 1 -> "🧂"; 2 -> "🐉"; 3 -> "🏔️"; 4 -> "🏛️"; 5 -> "🚌"; 6 -> "🎨"; else -> "⛵" }
        )
    }

    fun world(id: Int): WorldDefinition = worlds.first { it.id == id }
    fun stage(worldId: Int, stageNumber: Int): Stage = world(worldId).stages.first { it.number == stageNumber }
    fun categoryInfo(id: LearningCategory): CategoryInfo = categories.first { it.id == id }
}
