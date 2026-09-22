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
                "Skarb Wieliczki" to "Wieliczka Treasure"
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
                "Mistrz Krakowa" to "Krakow Master"
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
                "Górski Odkrywca" to "Mountain Explorer"
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
                "Mistrz Rzymu" to "Rome Master"
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
                "London Explorer" to "London Explorer"
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
                "Mistrz Mediolanu" to "Milan Master"
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
                "Mistrz Przygody" to "Adventure Master"
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
        val worldGenPl = listOf("Wieliczki", "Krakowa", "Tatr", "Rzymu", "Londynu", "Mediolanu", "Malty")[stage.worldId - 1]
        val worldLocPl = listOf("Wieliczce", "Krakowie", "Tatrach", "Rzymie", "Londynie", "Mediolanie", "Malcie")[stage.worldId - 1]
        val worldAccPl = listOf("Wieliczkę", "Kraków", "Tatry", "Rzym", "Londyn", "Mediolan", "Maltę")[stage.worldId - 1]
        val worldNameEn = listOf("Wieliczka", "Krakow", "Tatras", "Rome", "London", "Milan", "Malta")[stage.worldId - 1]

        val worldGoalPl = when (stage.worldId) {
            1 -> "Złoty Kryształ Soli"
            2 -> "Smoczy Medalion"
            3 -> "Odznaka Górskiego Odkrywcy"
            4 -> "Mapa Wiecznego Miasta"
            5 -> "Znaki Londyńskiego Podróżnika"
            6 -> "Szkic Mistrza Mediolanu"
            else -> "Kompas Siedmiu Wysp"
        }
        val worldGoalEn = when (stage.worldId) {
            1 -> "Golden Salt Crystal"
            2 -> "Dragon Medallion"
            3 -> "Mountain Explorer Badge"
            4 -> "Map of the Eternal City"
            5 -> "London Traveller Signs"
            6 -> "Milan Master's Sketch"
            else -> "Compass of the Seven Islands"
        }

        val chapterPl = listOf(
            "Pierwszy trop",
            "Znak na mapie",
            "Ukryta wskazówka",
            "Tajemniczy symbol",
            "Połowa drogi",
            "Nowy kierunek",
            "Elementy układanki",
            "Już blisko",
            "Ostatnia wskazówka",
            "Finał wyprawy"
        )
        val chapterEn = listOf(
            "First clue",
            "Mark on the map",
            "Hidden hint",
            "Mysterious symbol",
            "Halfway there",
            "New direction",
            "Pieces fit together",
            "Almost there",
            "Final clue",
            "Adventure finale"
        )

        val facts = when (stage.worldId) {
            1 -> listOf(
                "Wieliczka od setek lat jest związana z wydobyciem soli.",
                "Pod ziemią znajdują się komory, jeziora i kaplice wykute w soli.",
                "Kaplica św. Kingi należy do najbardziej znanych miejsc kopalni."
            )
            2 -> listOf(
                "Krakowski Rynek Główny należy do największych średniowiecznych rynków Europy.",
                "Legenda o Smoku Wawelskim jest jedną z najbardziej znanych polskich legend.",
                "Wawel przez wieki był siedzibą polskich królów."
            )
            3 -> listOf(
                "Tatry są najwyższymi górami w Polsce.",
                "Morskie Oko jest jednym z najbardziej znanych tatrzańskich jezior.",
                "Kozica i świstak to zwierzęta kojarzone z Tatrami."
            )
            4 -> listOf(
                "Rzym nazywany jest Wiecznym Miastem.",
                "Koloseum było ogromnym amfiteatrem starożytnego Rzymu.",
                "Fontanna di Trevi jest jedną z najsłynniejszych fontann świata."
            )
            5 -> listOf(
                "Przez Londyn przepływa Tamiza.",
                "Tower Bridge to jeden z najbardziej rozpoznawalnych mostów Londynu.",
                "Big Ben to potoczna nazwa wielkiego dzwonu przy Pałacu Westminsterskim."
            )
            6 -> listOf(
                "Mediolan jest jednym z najważniejszych miast północnych Włoch.",
                "Duomo di Milano to ogromna gotycka katedra w centrum miasta.",
                "La Scala jest jednym z najsłynniejszych teatrów operowych świata."
            )
            else -> listOf(
                "Malta leży na Morzu Śródziemnym.",
                "Valletta jest stolicą Malty.",
                "Mdina nazywana jest Cichym Miastem."
            )
        }
        val factsEn = when (stage.worldId) {
            1 -> listOf("Wieliczka has been connected with salt mining for centuries.", "Underground there are chambers, lakes and chapels carved in salt.", "St. Kinga's Chapel is one of the mine's most famous places.")
            2 -> listOf("Krakow's Main Market Square is one of Europe's largest medieval squares.", "The Wawel Dragon is one of Poland's best-known legends.", "Wawel Castle was home to Polish kings for centuries.")
            3 -> listOf("The Tatras are the highest mountains in Poland.", "Morskie Oko is one of the best-known Tatra lakes.", "Chamois and marmots are strongly associated with the Tatras.")
            4 -> listOf("Rome is known as the Eternal City.", "The Colosseum was a huge amphitheatre in ancient Rome.", "Trevi Fountain is one of the world's most famous fountains.")
            5 -> listOf("The River Thames flows through London.", "Tower Bridge is one of London's most recognisable bridges.", "Big Ben is the nickname of the great bell at the Palace of Westminster.")
            6 -> listOf("Milan is one of northern Italy's most important cities.", "Milan Cathedral is a huge Gothic cathedral in the city centre.", "La Scala is one of the world's most famous opera houses.")
            else -> listOf("Malta lies in the Mediterranean Sea.", "Valletta is the capital of Malta.", "Mdina is known as the Silent City.")
        }

        val interactionsPl = listOf(
            "Kacper pokazuje Kapiemu pierwszą kartę wyprawy. — Zaczynamy! — mówi. Kapi łapie trop, a oni ruszają przez $worldAccPl. Cel całej wyprawy: $worldGoalPl. Pierwszy punkt to: ${stage.namePl}.",
            "Kapi znajduje znak, którego wcześniej nie było na mapie. Kacper porównuje go z planem $worldGenPl. — To druga wskazówka! — mówi. Prowadzi ich do miejsca: ${stage.namePl}.",
            "W $worldLocPl Kapi zatrzymuje się nagle i zaczyna węszyć. Kacper odkrywa kolejny fragment zagadki. — Dobrze idziemy, partnerze. Następny punkt: ${stage.namePl}.",
            "Kacper obraca znaleziony symbol i pokazuje go Kapiemu. — Ten znak wskazuje dalej przez $worldAccPl. — Kapi merda ogonem, a na mapie pojawia się: ${stage.namePl}.",
            "Kacper liczy zebrane wskazówki, a Kapi pilnuje plecaka. — Jesteśmy w połowie drogi do celu: $worldGoalPl. — Kolejny etap wyprawy to: ${stage.namePl}.",
            "Kapi pierwszy zauważa, że trop zmienia kierunek. Kacper sprawdza mapę $worldGenPl. — Dobra robota! Nowa trasa prowadzi do miejsca: ${stage.namePl}.",
            "Kacper układa obok siebie dotychczasowe wskazówki, a Kapi dotyka łapą pasującego fragmentu. — Układanka zaczyna mieć sens. W $worldLocPl musimy teraz sprawdzić: ${stage.namePl}.",
            "Kapi biegnie kilka kroków do przodu i wraca podekscytowany. Kacper uśmiecha się: — Jesteśmy już blisko celu: $worldGoalPl. Następny punkt to: ${stage.namePl}.",
            "Kacper znajduje ostatni brakujący znak, a Kapi szczeka jakby chciał powiedzieć „mamy to!”. — Został już tylko finał. Najpierw sprawdzamy: ${stage.namePl}.",
            "Wszystkie wskazówki łączą się w całość. Kacper i Kapi kończą wyprawę po $worldLocPl. — Udało się! — mówi Kacper. Finałowy punkt: ${stage.namePl}. Cel odkryty: $worldGoalPl."
        )
        val interactionsEn = listOf(
            "Kacper shows Kapi the first expedition card. “We're starting!” Kapi catches the trail and they set off through $worldNameEn. Their goal: $worldGoalEn. First stop: ${stage.nameEn}.",
            "Kapi finds a mark that was not on the map before. Kacper checks it carefully. “That's our second clue!” It leads them to ${stage.nameEn}.",
            "Kapi suddenly stops and sniffs the ground. Kacper discovers another piece of the mystery. “We're on the right track, partner.” Next stop: ${stage.nameEn}.",
            "Kacper turns the strange symbol around and shows it to Kapi. “This points further ahead.” Kapi wags his tail. The next place is ${stage.nameEn}.",
            "Kacper counts the clues while Kapi guards the backpack. “We're halfway to $worldGoalEn.” The next stage is ${stage.nameEn}.",
            "Kapi notices first that the trail changes direction. Kacper checks the map. “Good job!” The new route leads to ${stage.nameEn}.",
            "Kacper lays out all the clues and Kapi taps the matching piece with his paw. “The puzzle is starting to make sense.” Now they need to check ${stage.nameEn}.",
            "Kapi runs a few steps ahead and comes back excited. Kacper smiles. “We're very close to $worldGoalEn.” Next stop: ${stage.nameEn}.",
            "Kacper finds the final missing mark and Kapi barks as if to say, “We've got it!” Only the finale remains. First: ${stage.nameEn}.",
            "All the clues finally connect. Kacper and Kapi finish their journey through $worldNameEn. “We did it!” says Kacper. Final stop: ${stage.nameEn}. Goal discovered: $worldGoalEn."
        )

        val factIndex = (stage.number - 1) % facts.size
        val interactionIndex = (stage.number - 1).coerceIn(0, interactionsPl.lastIndex)
        return StoryBeat(
            titlePl = "Rozdział ${stage.number}/${GameRules.STAGES_PER_WORLD} • ${chapterPl[interactionIndex]}",
            titleEn = "Chapter ${stage.number}/${GameRules.STAGES_PER_WORLD} • ${chapterEn[interactionIndex]}",
            textPl = interactionsPl[interactionIndex],
            textEn = interactionsEn[interactionIndex],
            factPl = facts[factIndex],
            factEn = factsEn[factIndex],
            emoji = when (stage.worldId) { 1 -> "🧂"; 2 -> "🐉"; 3 -> "🏔️"; 4 -> "🏛️"; 5 -> "🚌"; 6 -> "🎨"; else -> "⛵" }
        )
    }

    fun world(id: Int): WorldDefinition = worlds.first { it.id == id }
    fun stage(worldId: Int, stageNumber: Int): Stage = world(worldId).stages.first { it.number == stageNumber }
    fun categoryInfo(id: LearningCategory): CategoryInfo = categories.first { it.id == id }
}
