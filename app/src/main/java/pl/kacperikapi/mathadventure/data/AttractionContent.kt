package pl.kacperikapi.mathadventure.data

/**
 * Curated real-world attraction cards for all seven worlds.
 *
 * Photos are loaded from Wikimedia Commons on first view and cached on-device.
 * Full photo credits and source-page links are documented in docs/PHOTO_CREDITS.md.
 */
object AttractionContent {
    private fun item(
        stageId: String,
        descriptionPl: String,
        descriptionEn: String,
        factPl: String,
        factEn: String,
        photoFileName: String,
        photoAuthor: String,
        photoLicense: String
    ) = AttractionInfo(
        stageId = stageId,
        descriptionPl = descriptionPl,
        descriptionEn = descriptionEn,
        factPl = factPl,
        factEn = factEn,
        photoFileName = photoFileName,
        photoAuthor = photoAuthor,
        photoLicense = photoLicense,
        sourcePage = "https://commons.wikimedia.org/wiki/File:" + photoFileName.replace(" ", "_")
    )

    private fun searchItem(
        stageId: String,
        descriptionPl: String,
        descriptionEn: String,
        factPl: String,
        factEn: String,
        photoSearchQuery: String
    ) = AttractionInfo(
        stageId = stageId,
        descriptionPl = descriptionPl,
        descriptionEn = descriptionEn,
        factPl = factPl,
        factEn = factEn,
        photoFileName = "",
        photoAuthor = "Wikimedia Commons",
        photoLicense = "autor i licencja pobierane ze źródła",
        sourcePage = "https://commons.wikimedia.org/wiki/Special:MediaSearch?type=image&search=" + photoSearchQuery.replace(" ", "%20"),
        photoSearchQuery = photoSearchQuery
    )

    private val cards: Map<String, AttractionInfo> = listOf(
        // WIELICZKA
        item(
            "w1s1",
            "Rynek Górny to historyczne serce Wieliczki. Otaczają go kamienice, a nad placem widać wieżę kościoła św. Klemensa.",
            "Upper Market Square is the historic heart of Wieliczka. It is surrounded by old townhouses, with the tower of St. Clement's Church rising nearby.",
            "Wieliczka rozwijała się dzięki soli. Przez setki lat handel solą wpływał na życie całego miasta.",
            "Wieliczka grew thanks to salt. For centuries, the salt trade shaped life throughout the town.",
            "RynekGórny-POL, Wieliczka.JPG",
            "Mach240390",
            "CC BY 3.0"
        ),
        item(
            "w1s2",
            "Zamek Żupny był przez wieki centrum zarządzania wielickimi żupami, czyli kopalnią i przedsiębiorstwem solnym.",
            "The Saltworks Castle was for centuries the management centre of the Wieliczka saltworks and its mining enterprise.",
            "To właśnie tutaj urzędnicy pilnowali wydobycia i sprzedaży jednego z najcenniejszych dawniej produktów — soli.",
            "Officials here supervised the mining and sale of one of the most valuable products of the past — salt.",
            "Zamek Zupny.jpg",
            "Wojtas250",
            "CC BY-SA 3.0"
        ),
        item(
            "w1s3",
            "Tężnia solankowa działa dzięki solance spływającej po gałązkach tarniny. Wokół tworzy się delikatna, słona mgiełka.",
            "The brine graduation tower works by letting salty water flow over blackthorn branches, creating a fine salty mist around it.",
            "Spacer obok tężni przypomina pobyt nad morzem, bo w powietrzu unoszą się drobinki solanki.",
            "Walking near the tower can feel a little like being by the sea because tiny droplets of brine float in the air.",
            "Wieliczka tężnia solankowa 04.16 065.JPG",
            "Marek Mróz",
            "CC BY-SA 4.0"
        ),
        item(
            "w1s4",
            "Szyb Daniłowicza to jedno z najbardziej rozpoznawalnych wejść do podziemi kopalni. Zdjęcie pokazuje jego historyczny wygląd.",
            "The Daniłowicz Shaft is one of the best-known entrances to the mine's underground world. This photograph shows its historic appearance.",
            "Szyb służył do transportu ludzi między powierzchnią a podziemnymi poziomami kopalni.",
            "The shaft was used to move people between the surface and the underground levels of the mine.",
            "Wieliczka - szyb zjazdowy Danilowicza - The shaft Danilowicz 1916-1939 (73474617).jpg",
            "autor nieznany / Polona",
            "Public Domain"
        ),
        item(
            "w1s5",
            "Komora Mikołaja Kopernika przypomina o wizycie słynnego astronoma w wielickiej kopalni. Znajduje się tu rzeźba Kopernika wykonana w bryle soli.",
            "The Nicolaus Copernicus Chamber remembers the famous astronomer's visit to the Wieliczka mine. It contains a sculpture of Copernicus carved from a block of salt.",
            "Kopernik zwiedził kopalnię w 1493 roku, gdy studiował w Krakowie.",
            "Copernicus visited the mine in 1493 while he was studying in Krakow.",
            "Wieliczka, Komora Mikołaja Kopernika - fotopolska.eu (335513).jpg",
            "Andrzej G / fotopolska.eu",
            "CC BY-SA 3.0"
        ),
        item(
            "w1s6",
            "Kaplica św. Kingi to niezwykła podziemna świątynia wykuta w soli. Ściany, rzeźby i wiele dekoracji powstało z solnej skały.",
            "St. Kinga's Chapel is an extraordinary underground church carved in salt. Its walls, sculptures and many decorations were made from rock salt.",
            "Nawet część ozdobnych żyrandoli wykonano z kryształków soli.",
            "Even parts of the decorative chandeliers were made using salt crystals.",
            "Inside Wieliczka.jpg",
            "Martyna Zambrzycka",
            "Public Domain"
        ),
        item(
            "w1s7",
            "Komora Weimar pokazuje, jak ogromne przestrzenie mogły powstać po wydobyciu soli. Oświetlenie wydobywa kształty i kolory solnych ścian.",
            "The Weimar Chamber shows how huge spaces could remain after salt was mined. Lighting brings out the shapes and colours of the salt walls.",
            "Pod ziemią temperatura jest znacznie bardziej stała niż na powierzchni, dlatego latem w kopalni jest przyjemnie chłodno.",
            "Underground temperatures change much less than on the surface, so the mine feels pleasantly cool in summer.",
            "Weimar Chamber in Wieliczka Salt Mine, Poland.jpg",
            "Diego Delso",
            "CC BY-SA 3.0"
        ),
        item(
            "w1s8",
            "W wielickiej kopalni są także podziemne jeziora wypełnione bardzo słoną wodą — solanką.",
            "The Wieliczka mine also contains underground lakes filled with very salty water called brine.",
            "W jeziorze w komorze Erazma Barącza znajduje się około 320 gramów soli w jednym litrze solanki.",
            "The lake in the Erazm Barącz Chamber contains about 320 grams of salt in one litre of brine.",
            "Wieliczka mine 08.jpg",
            "ErwinMeier",
            "CC BY-SA 4.0"
        ),
        item(
            "w1s9",
            "Komora Stanisława Staszica zachwyca rozmiarem. To jedna z najwyższych komór na trasie podziemnej.",
            "The Stanisław Staszic Chamber is impressive because of its size. It is one of the tallest chambers on the underground route.",
            "Komora ma około 50 metrów wysokości — to mniej więcej tyle, co kilkunastopiętrowy budynek.",
            "The chamber is about 50 metres high — roughly the height of a multi-storey building.",
            "Wieliczka mine 14.jpg",
            "ErwinMeier",
            "CC BY-SA 4.0"
        ),
        item(
            "w1s10",
            "Skarbnik to legendarny duch kopalni i opiekun górników. Jego postać pojawia się w wielu opowieściach związanych z Wieliczką.",
            "Skarbnik is the legendary spirit of the mine and guardian of miners. He appears in many stories connected with Wieliczka.",
            "Według legend Skarbnik miał ostrzegać górników przed niebezpieczeństwem i pomagać tym, którzy szanowali kopalnię.",
            "According to legend, Skarbnik warned miners about danger and helped those who respected the mine.",
            "Poland Wieliczka - Skarbnik in a mine.JPG",
            "Merlin",
            "GFDL / CC"
        ),

        // KRAKÓW
        item(
            "w2s1",
            "Planty to zielony park otaczający krakowskie Stare Miasto. Tworzą wygodny pierścień spacerowy wokół historycznego centrum.",
            "Planty is a green park surrounding Krakow's Old Town, forming a pleasant walking ring around the historic centre.",
            "Planty powstały w miejscu większości dawnych murów miejskich, które z czasem rozebrano.",
            "Planty was created on the site of most of the old city walls after they were gradually removed.",
            "Planty Park, Old Town, Krakow, Poland.jpg",
            "Zygmunt Put (Zetpe0202)",
            "CC BY-SA 4.0"
        ),
        item(
            "w2s2",
            "Rynek Główny jest sercem Krakowa. Od wieków spotykają się tu mieszkańcy, kupcy, artyści i podróżnicy.",
            "Main Market Square is the heart of Krakow. For centuries it has been a meeting place for residents, merchants, artists and travellers.",
            "Plac wytyczono po lokacji Krakowa w 1257 roku. Każdy bok ma nieco ponad 200 metrów.",
            "The square was laid out after Krakow's city charter in 1257. Each side is a little over 200 metres long.",
            "Rynek Główny, Kraków.jpg",
            "Minderbinder",
            "CC BY-SA 4.0"
        ),
        item(
            "w2s3",
            "Sukiennice stoją pośrodku Rynku Głównego. Dawniej były jednym z najważniejszych miejsc handlu w mieście.",
            "The Cloth Hall stands in the middle of Main Market Square. In the past it was one of the city's most important trading places.",
            "Nazwa Sukiennice pochodzi od sukna — tkaniny, którą kupcy sprzedawali tu przed wiekami.",
            "The Polish name Sukiennice comes from cloth, one of the goods merchants sold here centuries ago.",
            "Kraków, Sukiennice w Krakowie.jpg",
            "Michu17",
            "CC BY 3.0"
        ),
        item(
            "w2s4",
            "Kościół Mariacki z dwiema nierównymi wieżami jest jednym z symboli Krakowa i stoi tuż przy Rynku Głównym.",
            "St. Mary's Basilica, with its two unequal towers, is one of Krakow's symbols and stands right beside Main Market Square.",
            "Z wyższej wieży co godzinę rozbrzmiewa hejnał mariacki — melodia urywa się nagle w trakcie grania.",
            "Every hour the famous trumpet call is played from the taller tower — and the melody ends suddenly.",
            "Kosciol mariacki krakow.jpg",
            "Pgkos",
            "CC BY-SA 4.0"
        ),
        item(
            "w2s5",
            "Brama Floriańska była jednym z najważniejszych wejść do średniowiecznego Krakowa i częścią miejskich fortyfikacji.",
            "St. Florian's Gate was one of the most important entrances to medieval Krakow and part of the city's fortifications.",
            "To przez tę bramę prowadziła historyczna Droga Królewska w stronę Rynku i Wawelu.",
            "The historic Royal Route passed through this gate towards Main Market Square and Wawel.",
            "Brama Floriańska w Krakowie 19.jpg",
            "Аимаина хикари",
            "CC0 1.0"
        ),
        item(
            "w2s6",
            "Barbakan to potężna, okrągła budowla obronna stojąca przed dawnymi murami miasta.",
            "The Barbican is a powerful round defensive structure built in front of the old city walls.",
            "Powstał pod koniec XV wieku i miał chronić drogę prowadzącą do Bramy Floriańskiej.",
            "It was built at the end of the 15th century to defend the approach to St. Florian's Gate.",
            "Barbakan w Krakowie (1907).jpg",
            "autor nieznany",
            "Public Domain"
        ),
        item(
            "w2s7",
            "Wawel był przez wieki ważnym miejscem władzy. Na wzgórzu stoją Zamek Królewski i katedra.",
            "Wawel was an important centre of power for centuries. The hill is home to the Royal Castle and the cathedral.",
            "Wawelska katedra była miejscem koronacji i pochówku wielu polskich królów.",
            "Wawel Cathedral was the coronation and burial place of many Polish kings.",
            "Wawel Castle.jpg",
            "Diddaw",
            "CC BY-SA 3.0"
        ),
        item(
            "w2s8",
            "Rzeźba Smoka Wawelskiego stoi u stóp Wawelu, obok wyjścia ze Smoczej Jamy.",
            "The Wawel Dragon statue stands at the foot of Wawel Hill beside the exit from the Dragon's Den.",
            "Smok z rzeźby naprawdę zieje ogniem w krótkich odstępach czasu — to jedna z ulubionych atrakcji dzieci.",
            "The statue really breathes fire at intervals, making it one of children's favourite Krakow attractions.",
            "SmokWawelski-Rzeźba-POL, Kraków.jpg",
            "Mach240390",
            "CC BY 4.0"
        ),
        item(
            "w2s9",
            "Bulwary Wiślane biegną wzdłuż rzeki i są popularnym miejscem spacerów, jazdy na rowerze i odpoczynku.",
            "The Vistula Boulevards run along the river and are popular for walking, cycling and relaxing.",
            "Z bulwarów pod Wawelem można jednocześnie zobaczyć Wisłę, zamek i wzgórze wawelskie.",
            "From the boulevards below Wawel you can see the Vistula, the castle and Wawel Hill at the same time.",
            "Kraków - Bulwar Czerwieński i Wzgórze Wawelskie - panorama.jpg",
            "Pece / Przemysław Czopor",
            "CC BY 3.0"
        ),
        item(
            "w2s10",
            "Kraków łączy średniowieczne ulice, królewskie zabytki, parki i nowoczesne życie dużego miasta.",
            "Krakow combines medieval streets, royal landmarks, parks and the everyday life of a modern city.",
            "Historyczne centrum Krakowa znalazło się na pierwszej Liście Światowego Dziedzictwa UNESCO w 1978 roku.",
            "Krakow's historic centre was included on UNESCO's first World Heritage List in 1978.",
            "Panorama KRK.jpg",
            "Kudak",
            "CC BY-SA 4.0"
        ),

        // TATRY
        item(
            "w3s1",
            "Kuźnice są jednym z najważniejszych punktów startowych górskich wycieczek z Zakopanego.",
            "Kuźnice is one of the most important starting points for mountain trips from Zakopane.",
            "W Kuźnicach znajduje się dolna stacja kolejki linowej na Kasprowy Wierch.",
            "Kuźnice is home to the lower station of the cable car to Kasprowy Wierch.",
            "Zakopane Kuznice.jpg",
            "Andrzej Otrębski",
            "CC BY-SA 4.0"
        ),
        item(
            "w3s2",
            "Dolina Strążyska to krótka i bardzo popularna dolina po północnej stronie Tatr, tuż pod masywem Giewontu.",
            "Strążyska Valley is a short and very popular valley on the northern side of the Tatras, directly below the Giewont massif.",
            "Z Polany Strążyskiej można podejść do wodospadu Siklawica spadającego ze skalnego progu.",
            "From Strążyska Glade you can walk towards Siklawica waterfall, which drops over a rocky step.",
            "Dolina Strążyska a3.jpg",
            "Jerzy Opioła",
            "CC BY-SA 4.0"
        ),
        item(
            "w3s3",
            "Rusinowa Polana to rozległa tatrzańska polana znana z pięknej panoramy wysokich szczytów.",
            "Rusinowa Polana is a wide Tatra glade famous for its beautiful panorama of the high peaks.",
            "Na polanie przetrwała tradycja wypasu owiec, dlatego w sezonie można spotkać tu pasterskie szałasy.",
            "The tradition of sheep grazing has survived here, so in season you may see shepherd huts on the glade.",
            "Rusinowa Polana.jpg",
            "Aneta Pawska",
            "CC BY 3.0"
        ),
        item(
            "w3s4",
            "Morskie Oko leży w otoczeniu wysokich szczytów Tatr i jest największym jeziorem w polskiej części Tatr.",
            "Morskie Oko lies among high Tatra peaks and is the largest lake in the Polish Tatras.",
            "Nazwa Morskie Oko wiąże się z dawnymi opowieściami, według których jezioro miało podziemne połączenie z morzem.",
            "The name Morskie Oko is linked to old stories claiming the lake had an underground connection to the sea.",
            "Morskie oko.jpg",
            "Krzysztof T. miw",
            "CC BY-SA 3.0"
        ),
        item(
            "w3s5",
            "Gęsia Szyja to widokowy szczyt w Tatrach, na który można dojść między innymi od Rusinowej Polany.",
            "Gęsia Szyja is a scenic Tatra peak that can be reached, among other routes, from Rusinowa Polana.",
            "Ze szczytu przy dobrej pogodzie rozciąga się szeroka panorama Tatr Wysokich.",
            "In good weather the summit offers a wide panorama of the High Tatras.",
            "Gęsia Szyja.jpg",
            "Krzysztof Dudzik-Górnicki (ToSter)",
            "CC BY 3.0"
        ),
        item(
            "w3s6",
            "Hala Gąsienicowa to jedna z najbardziej rozpoznawalnych wysokogórskich polan i dolinnych przestrzeni w polskich Tatrach.",
            "Hala Gąsienicowa is one of the most recognisable high-mountain meadow and valley areas in the Polish Tatras.",
            "W pobliżu znajduje się schronisko Murowaniec, ważny punkt dla wielu tatrzańskich szlaków.",
            "Nearby stands Murowaniec mountain hut, an important stop for many Tatra trails.",
            "Hala Gąsienicowa T12.jpg",
            "Jerzy Opioła",
            "CC BY-SA 4.0"
        ),
        item(
            "w3s7",
            "Kasprowy Wierch to jeden z najbardziej znanych tatrzańskich szczytów. Można na niego wejść szlakiem albo wjechać kolejką linową.",
            "Kasprowy Wierch is one of the best-known Tatra peaks. You can hike up or travel by cable car.",
            "W wysokich górach pogoda może zmienić się bardzo szybko, dlatego przed wyjściem zawsze warto sprawdzić prognozę.",
            "Mountain weather can change very quickly, so it is always worth checking the forecast before setting out.",
            "Kasprowy Wierch.jpg",
            "Tomekkucharczyk1994",
            "CC BY-SA 4.0"
        ),
        item(
            "w3s8",
            "Giewont to charakterystyczny masyw górujący nad Zakopanem. Jego sylwetkę łatwo rozpoznać z wielu miejsc na Podhalu.",
            "Giewont is the distinctive massif rising above Zakopane. Its outline is easy to recognise from many places in Podhale.",
            "Wielki Giewont ma 1895 metrów wysokości, a na jego szczycie stoi duży metalowy krzyż.",
            "Wielki Giewont is 1,895 metres high and a large metal cross stands on its summit.",
            "Giewont strona południowa, Tatry, Giewont, south side Tatry Mountains, Poland.jpg",
            "Jarosław Pocztarski",
            "CC BY 2.0"
        ),
        item(
            "w3s9",
            "Kozica tatrzańska świetnie radzi sobie na stromych skałach. Jest jednym z najbardziej charakterystycznych dzikich zwierząt Tatr.",
            "The Tatra chamois is superbly adapted to steep rocky terrain and is one of the mountains' most characteristic wild animals.",
            "Dzikich zwierząt w parku narodowym nie wolno dokarmiać — najlepiej obserwować je z bezpiecznej odległości.",
            "Wild animals in the national park should never be fed — the best choice is to watch them from a safe distance.",
            "Kozica Tatrzańska w Koziej Dolince.jpg",
            "Aneta Pawska",
            "CC BY 3.0"
        ),
        item(
            "w3s10",
            "Tatry tworzą niezwykły krajobraz wysokich szczytów, dolin, jezior i lasów. Ich przyroda jest chroniona w Tatrzańskim Parku Narodowym.",
            "The Tatras form a remarkable landscape of high peaks, valleys, lakes and forests. Their nature is protected by Tatra National Park.",
            "Najlepszym sposobem pomagania górom jest chodzenie po wyznaczonych szlakach i zabieranie wszystkich śmieci ze sobą.",
            "One of the best ways to help the mountains is to stay on marked trails and take all your rubbish with you.",
            "Tatry Panorama.jpg",
            "Leszek Leszczynski",
            "CC BY 2.0"
        ),

        // RZYM
        searchItem(
            "w4s1",
            "Piazza Navona to jeden z najsłynniejszych placów Rzymu. Jego wydłużony kształt przypomina o starożytnym stadionie, który znajdował się tu prawie dwa tysiące lat temu.",
            "Piazza Navona is one of Rome's most famous squares. Its long shape recalls the ancient stadium that stood here almost two thousand years ago.",
            "Plac zachował zarys Stadionu Domicjana, a dziś słynie z barokowych fontann, w tym Fontanny Czterech Rzek.",
            "The square follows the outline of Domitian's Stadium and is now famous for Baroque fountains, including the Fountain of the Four Rivers.",
            "Piazza Navona Rome Fountain of the Four Rivers"
        ),
        searchItem(
            "w4s2",
            "Panteon jest jednym z najlepiej zachowanych budynków starożytnego Rzymu. Dziś pełni funkcję kościoła, ale jego ogromna kopuła nadal pokazuje kunszt rzymskich budowniczych.",
            "The Pantheon is one of the best-preserved buildings of ancient Rome. Today it is a church, while its huge dome still shows the skill of Roman builders.",
            "W środku kopuły znajduje się okrągły otwór zwany okulusem. To przez niego do wnętrza wpada światło, a czasem także deszcz.",
            "At the centre of the dome is a round opening called the oculus. Light, and sometimes rain, enters through it.",
            "Pantheon Rome exterior oculus"
        ),
        searchItem(
            "w4s3",
            "Fontanna di Trevi to ogromna barokowa fontanna ukryta pomiędzy rzymskimi ulicami. Woda spływa wokół rzeźb przedstawiających postacie związane z morzem.",
            "Trevi Fountain is a huge Baroque fountain hidden among Rome's streets. Water flows around sculptures linked with the sea.",
            "Popularny zwyczaj mówi, że wrzucenie monety do fontanny ma zapewnić powrót do Rzymu. Monety są później zbierane i przeznaczane na cele społeczne.",
            "A popular tradition says that tossing a coin into the fountain will bring you back to Rome. The coins are later collected for charitable purposes.",
            "Trevi Fountain Rome"
        ),
        searchItem(
            "w4s4",
            "Schody Hiszpańskie łączą Piazza di Spagna z kościołem Trinità dei Monti. To jedno z najbardziej rozpoznawalnych miejsc spacerowych w centrum Rzymu.",
            "The Spanish Steps connect Piazza di Spagna with the church of Trinità dei Monti. They are one of central Rome's best-known walking landmarks.",
            "Monumentalne schody powstały w XVIII wieku i mają 135 stopni.",
            "The monumental staircase was built in the 18th century and has 135 steps.",
            "Spanish Steps Rome Trinita dei Monti"
        ),
        searchItem(
            "w4s5",
            "Koloseum było wielkim amfiteatrem starożytnego Rzymu. Odbywały się tu widowiska oglądane przez dziesiątki tysięcy widzów.",
            "The Colosseum was the great amphitheatre of ancient Rome. Spectacles here were watched by tens of thousands of people.",
            "Budowlę nazywano pierwotnie Amfiteatrem Flawiuszów. Jej budowę rozpoczęto w I wieku naszej ery.",
            "The building was originally known as the Flavian Amphitheatre. Construction began in the 1st century AD.",
            "Colosseum Rome exterior"
        ),
        searchItem(
            "w4s6",
            "Forum Romanum było centrum życia publicznego starożytnego Rzymu. Stały tu świątynie, bazyliki i budynki związane z polityką oraz handlem.",
            "The Roman Forum was the centre of public life in ancient Rome, filled with temples, basilicas and buildings linked to politics and trade.",
            "Spacerując po Forum można zobaczyć pozostałości budowli powstających przez wiele stuleci historii Rzymu.",
            "A walk through the Forum reveals remains of buildings created across many centuries of Roman history.",
            "Roman Forum Rome panorama"
        ),
        searchItem(
            "w4s7",
            "Kapitol należy do siedmiu historycznych wzgórz Rzymu. Dziś znajduje się tu reprezentacyjny plac Piazza del Campidoglio i Muzea Kapitolińskie.",
            "Capitoline Hill is one of Rome's seven historic hills. Today it contains Piazza del Campidoglio and the Capitoline Museums.",
            "Układ placu na Kapitolu został zaprojektowany w XVI wieku przez Michała Anioła.",
            "The layout of the Capitoline square was designed in the 16th century by Michelangelo.",
            "Capitoline Hill Piazza del Campidoglio Rome"
        ),
        searchItem(
            "w4s8",
            "Zamek Świętego Anioła stoi nad Tybrem niedaleko Watykanu. Początkowo był mauzoleum cesarza Hadriana, a później stał się twierdzą.",
            "Castel Sant'Angelo stands beside the Tiber near the Vatican. It began as Emperor Hadrian's mausoleum and later became a fortress.",
            "Zamek łączy z Watykanem ufortyfikowany korytarz Passetto di Borgo, którym papieże mogli uciekać w razie zagrożenia.",
            "A fortified passage called the Passetto di Borgo links the castle with the Vatican and once offered popes an escape route in times of danger.",
            "Castel Sant Angelo Rome Tiber"
        ),
        searchItem(
            "w4s9",
            "Villa Borghese to rozległy park w centrum Rzymu. Wśród zieleni znajdują się alejki, ogrody, muzea i niewielkie jezioro.",
            "Villa Borghese is a large park in central Rome, with paths, gardens, museums and a small lake among its greenery.",
            "Na terenie parku działa Galleria Borghese, w której można zobaczyć dzieła słynnych włoskich artystów.",
            "The park is home to the Borghese Gallery, which displays works by famous Italian artists.",
            "Villa Borghese Rome park lake"
        ),
        searchItem(
            "w4s10",
            "Circus Maximus był ogromnym starożytnym stadionem Rzymu, na którym organizowano między innymi wyścigi rydwanów.",
            "Circus Maximus was a huge ancient Roman stadium used among other things for chariot races.",
            "Obiekt leży w dolinie między wzgórzami Palatyn i Awentyn i mógł pomieścić dziesiątki tysięcy widzów.",
            "It lies in the valley between the Palatine and Aventine hills and could hold tens of thousands of spectators.",
            "Circus Maximus Rome"
        ),

        // LONDYN
        searchItem(
            "w5s1",
            "Big Ben to popularna nazwa kojarzona z wieżą zegarową przy Pałacu Westminsterskim. Sama wieża od 2012 roku nosi nazwę Elizabeth Tower.",
            "Big Ben is the popular name associated with the clock tower beside the Palace of Westminster. Since 2012 the tower has been called Elizabeth Tower.",
            "Ściśle mówiąc, Big Ben jest nazwą wielkiego dzwonu znajdującego się wewnątrz wieży, a nie całego zegara.",
            "Strictly speaking, Big Ben is the name of the great bell inside the tower, not the whole clock.",
            "Big Ben Elizabeth Tower London"
        ),
        searchItem(
            "w5s2",
            "Westminster to część Londynu pełna ważnych budynków państwowych. Nad Tamizą stoi Pałac Westminsterski, w którym obraduje brytyjski parlament.",
            "Westminster is an area of London filled with important government buildings. The Palace of Westminster beside the Thames is home to the UK Parliament.",
            "Obecny neogotycki pałac powstał głównie w XIX wieku po wielkim pożarze wcześniejszych zabudowań.",
            "The present Gothic Revival palace was built mainly in the 19th century after a major fire destroyed much of the earlier complex.",
            "Palace of Westminster London Thames"
        ),
        searchItem(
            "w5s3",
            "London Eye to wielkie koło obserwacyjne nad Tamizą. Z kapsuł rozciąga się szeroki widok na centrum Londynu.",
            "The London Eye is a giant observation wheel beside the Thames, with capsules offering broad views across central London.",
            "Koło ma około 135 metrów wysokości i zostało otwarte dla pasażerów w 2000 roku.",
            "The wheel is about 135 metres tall and opened to passengers in 2000.",
            "London Eye Thames"
        ),
        searchItem(
            "w5s4",
            "Tower Bridge to charakterystyczny most z dwiema wieżami. Jego środkowe części mogą się unosić, aby przepuszczać wysokie statki.",
            "Tower Bridge is a distinctive bridge with two towers. Its central sections can lift to let tall ships pass.",
            "Most otwarto w 1894 roku. Mechanizm podnoszenia przęseł działa do dziś.",
            "The bridge opened in 1894 and its lifting mechanism still operates today.",
            "Tower Bridge London raised"
        ),
        searchItem(
            "w5s5",
            "Tower of London to historyczna twierdza nad Tamizą. Jej najstarsza część, White Tower, powstała po podboju Anglii przez Normanów.",
            "The Tower of London is a historic fortress beside the Thames. Its oldest part, the White Tower, was built after the Norman conquest of England.",
            "Dziś w kompleksie przechowywane są brytyjskie klejnoty koronne.",
            "Today the complex houses the British Crown Jewels.",
            "Tower of London White Tower"
        ),
        searchItem(
            "w5s6",
            "British Museum gromadzi zabytki z wielu części świata i różnych okresów historii. Jest jednym z najstarszych publicznych muzeów tego typu.",
            "The British Museum holds objects from many parts of the world and many periods of history. It is one of the oldest public museums of its kind.",
            "Muzeum założono w 1753 roku, a dla publiczności otwarto w 1759 roku.",
            "The museum was founded in 1753 and opened to the public in 1759.",
            "British Museum London Great Court"
        ),
        searchItem(
            "w5s7",
            "Hyde Park to jeden z wielkich królewskich parków Londynu. Można tu spacerować, jeździć na rowerze i odpoczywać nad jeziorem Serpentine.",
            "Hyde Park is one of London's great Royal Parks, where visitors can walk, cycle and relax beside the Serpentine lake.",
            "W parku znajduje się Speakers' Corner, miejsce znane z tradycji publicznych przemówień i debat.",
            "The park contains Speakers' Corner, famous for its tradition of public speeches and debate.",
            "Hyde Park London Serpentine"
        ),
        searchItem(
            "w5s8",
            "Buckingham Palace jest londyńską rezydencją brytyjskiego monarchy i miejscem wielu oficjalnych uroczystości.",
            "Buckingham Palace is the London residence of the British monarch and the setting for many official ceremonies.",
            "Przed pałacem odbywa się słynna ceremonia zmiany warty, która przyciąga wielu widzów.",
            "The famous Changing of the Guard ceremony takes place at the palace and attracts many visitors.",
            "Buckingham Palace London Changing Guard"
        ),
        searchItem(
            "w5s9",
            "Natural History Museum zachwyca zarówno zbiorami przyrodniczymi, jak i monumentalnym budynkiem z XIX wieku.",
            "The Natural History Museum is impressive both for its natural science collections and its monumental 19th-century building.",
            "W głównym holu zawieszony jest szkielet płetwala błękitnego nazwany Hope.",
            "A blue whale skeleton named Hope hangs in the museum's main hall.",
            "Natural History Museum London Hintze Hall whale"
        ),
        searchItem(
            "w5s10",
            "Katedra św. Pawła z charakterystyczną kopułą jest jednym z najbardziej rozpoznawalnych zabytków Londynu.",
            "St Paul's Cathedral, with its distinctive dome, is one of London's most recognisable landmarks.",
            "Obecny budynek zaprojektował Christopher Wren po wielkim pożarze Londynu w 1666 roku.",
            "The present building was designed by Christopher Wren after the Great Fire of London in 1666.",
            "St Paul Cathedral London dome"
        ),

        // MEDIOLAN
        searchItem(
            "w6s1",
            "Duomo di Milano to ogromna gotycka katedra stojąca w samym sercu Mediolanu. Jej fasadę zdobią setki rzeźb i smukłych iglic.",
            "Milan Cathedral is a huge Gothic cathedral in the heart of the city, decorated with hundreds of sculptures and slender spires.",
            "Budowę katedry rozpoczęto w 1386 roku, a prace i zmiany trwały przez wiele kolejnych stuleci.",
            "Construction began in 1386, and work and alterations continued for many centuries.",
            "Duomo di Milano cathedral facade"
        ),
        searchItem(
            "w6s2",
            "Galleria Vittorio Emanuele II to elegancki pasaż handlowy przykryty szklanym dachem. Łączy okolice Duomo z placem przy teatrze La Scala.",
            "Galleria Vittorio Emanuele II is an elegant shopping arcade beneath a glass roof, linking the Duomo area with the square by La Scala.",
            "Pasaż otwarto w XIX wieku i nazwano na cześć pierwszego króla zjednoczonych Włoch.",
            "The arcade opened in the 19th century and was named after the first king of unified Italy.",
            "Galleria Vittorio Emanuele II Milan interior"
        ),
        searchItem(
            "w6s3",
            "Zamek Sforzów to potężna ceglana budowla związana z władcami Mediolanu. Dziś mieści kilka muzeów i kolekcji sztuki.",
            "Sforza Castle is a large brick fortress associated with Milan's rulers. Today it houses several museums and art collections.",
            "W XV wieku zamek rozbudowała rodzina Sforzów, od której pochodzi jego dzisiejsza nazwa.",
            "In the 15th century the castle was expanded by the Sforza family, whose name it still carries.",
            "Sforza Castle Milan Castello Sforzesco"
        ),
        searchItem(
            "w6s4",
            "Brera to dzielnica znana z wąskich ulic, galerii i artystycznej atmosfery. Znajduje się tu słynna Pinacoteca di Brera.",
            "Brera is a district known for narrow streets, galleries and an artistic atmosphere. It is home to the famous Pinacoteca di Brera.",
            "Pinakoteka posiada ważną kolekcję włoskiego malarstwa, obejmującą dzieła powstające przez wiele stuleci.",
            "The gallery holds an important collection of Italian paintings created across many centuries.",
            "Brera Milan Pinacoteca street"
        ),
        searchItem(
            "w6s5",
            "Navigli to część Mediolanu znana z kanałów i nadbrzeżnych uliczek. Dawniej drogi wodne pomagały przewozić towary do miasta.",
            "Navigli is the area of Milan known for canals and waterside streets. In the past the waterways helped transport goods into the city.",
            "System mediolańskich kanałów był przez wieki rozbudowywany i ulepszany, między innymi dzięki pomysłom dotyczącym śluz wodnych.",
            "Milan's canal system was expanded and improved over centuries, including through advances in water-lock design.",
            "Navigli Milan canal"
        ),
        searchItem(
            "w6s6",
            "Teatro alla Scala, zwykle nazywany po prostu La Scala, jest jednym z najsłynniejszych teatrów operowych świata.",
            "Teatro alla Scala, usually called simply La Scala, is one of the world's most famous opera houses.",
            "Teatr otwarto w 1778 roku. Występowało tu wielu znanych śpiewaków, muzyków i dyrygentów.",
            "The theatre opened in 1778 and has hosted many famous singers, musicians and conductors.",
            "Teatro alla Scala Milan exterior"
        ),
        searchItem(
            "w6s7",
            "Muzeum Nauki i Techniki im. Leonarda da Vinci pokazuje rozwój transportu, energii, komunikacji i różnych dziedzin inżynierii.",
            "The Leonardo da Vinci Museum of Science and Technology explores transport, energy, communication and many branches of engineering.",
            "W muzeum można zobaczyć między innymi prawdziwe lokomotywy, samoloty, statki i okręt podwodny.",
            "The museum includes real locomotives, aircraft, ships and even a submarine.",
            "Museo Nazionale Scienza Tecnologia Leonardo da Vinci Milan"
        ),
        searchItem(
            "w6s8",
            "Porta Nuova to nowoczesna część Mediolanu pełna nowych placów i wieżowców. Pokazuje zupełnie inne oblicze miasta niż historyczne centrum.",
            "Porta Nuova is a modern part of Milan filled with new squares and towers, showing a very different side of the city from its historic centre.",
            "Jednym z symboli dzielnicy jest Bosco Verticale — para wieżowców z tysiącami drzew i krzewów na balkonach.",
            "One symbol of the district is Bosco Verticale, a pair of towers with thousands of trees and shrubs growing on their balconies.",
            "Porta Nuova Milan Bosco Verticale skyline"
        ),
        searchItem(
            "w6s9",
            "San Siro, oficjalnie Stadio Giuseppe Meazza, to ogromny stadion piłkarski w Mediolanie.",
            "San Siro, officially Stadio Giuseppe Meazza, is a huge football stadium in Milan.",
            "Stadion otwarto w 1926 roku i przez wiele lat był domowym obiektem dwóch wielkich mediolańskich klubów: AC Milan i Interu.",
            "The stadium opened in 1926 and for many years has been home to Milan's two major clubs, AC Milan and Inter.",
            "San Siro Stadium Milan exterior"
        ),
        searchItem(
            "w6s10",
            "Arco della Pace to monumentalny łuk stojący przy Parco Sempione, niedaleko Zamku Sforzów.",
            "Arco della Pace is a monumental arch beside Parco Sempione, near Sforza Castle.",
            "Łuk powstał w XIX wieku i jest jednym z najbardziej charakterystycznych neoklasycznych zabytków Mediolanu.",
            "The arch was completed in the 19th century and is one of Milan's most distinctive Neoclassical monuments.",
            "Arco della Pace Milan"
        ),

        // MALTA
        searchItem(
            "w7s1",
            "Valletta jest niewielką, silnie ufortyfikowaną stolicą Malty. Powstała po Wielkim Oblężeniu Malty w XVI wieku.",
            "Valletta is Malta's compact, heavily fortified capital, founded after the Great Siege of Malta in the 16th century.",
            "Całe historyczne miasto Valletta zostało wpisane na Listę Światowego Dziedzictwa UNESCO.",
            "The historic city of Valletta is listed as a UNESCO World Heritage Site.",
            "Valletta Malta skyline harbour"
        ),
        searchItem(
            "w7s2",
            "Upper Barrakka Gardens to ogrody położone wysoko nad Grand Harbour. Z tarasu widać port i Trzy Miasta po przeciwnej stronie wody.",
            "Upper Barrakka Gardens sit high above the Grand Harbour, with views across the port towards the Three Cities.",
            "Poniżej ogrodów znajduje się Saluting Battery, gdzie w określonych godzinach odbywa się ceremonialny wystrzał armatni.",
            "Below the gardens is the Saluting Battery, where ceremonial cannon fire takes place at set times.",
            "Upper Barrakka Gardens Malta Grand Harbour"
        ),
        searchItem(
            "w7s3",
            "Trzy Miasta to wspólna nazwa historycznych miejscowości Vittoriosa, Senglea i Cospicua leżących przy Grand Harbour.",
            "The Three Cities is the collective name for the historic towns of Vittoriosa, Senglea and Cospicua around the Grand Harbour.",
            "Ich fortyfikacje i wąskie uliczki przypominają o czasach joannitów i o obronnym znaczeniu maltańskiego portu.",
            "Their fortifications and narrow streets recall the Knights of St John and the strategic importance of Malta's harbour.",
            "Three Cities Malta Vittoriosa Senglea Cospicua"
        ),
        searchItem(
            "w7s4",
            "Mdina to dawna stolica Malty otoczona potężnymi murami. Jej spokojne, wąskie uliczki sprawiły, że nazywana jest Cichym Miastem.",
            "Mdina is Malta's former capital, enclosed by strong walls. Its quiet narrow streets have earned it the nickname the Silent City.",
            "W Mdina ruch samochodowy jest mocno ograniczony, dzięki czemu historyczne centrum zachowuje wyjątkowo spokojny charakter.",
            "Vehicle traffic is heavily restricted in Mdina, helping the historic centre keep its unusually quiet atmosphere.",
            "Mdina Malta old city gate streets"
        ),
        searchItem(
            "w7s5",
            "Rabat leży tuż obok murów Mdiny. Miasto jest znane z zabytków związanych z długą historią Malty, w tym podziemnych katakumb.",
            "Rabat lies just outside the walls of Mdina and is known for sites linked to Malta's long history, including underground catacombs.",
            "Katakumby św. Pawła tworzą rozległy zespół podziemnych korytarzy i grobowców używanych w czasach rzymskich.",
            "St Paul's Catacombs form a large network of underground passages and tombs used in Roman times.",
            "Rabat Malta St Paul catacombs"
        ),
        searchItem(
            "w7s6",
            "Blue Grotto to zespół morskich jaskiń i skalnych łuków na południowym wybrzeżu Malty. Przy spokojnym morzu można je oglądać z łodzi.",
            "Blue Grotto is a group of sea caves and rock arches on Malta's southern coast. In calm weather they can be visited by boat.",
            "Światło odbijające się od dna i skał może nadawać wodzie intensywne odcienie błękitu.",
            "Light reflecting from the seabed and rocks can give the water intense shades of blue.",
            "Blue Grotto Malta sea caves"
        ),
        searchItem(
            "w7s7",
            "Marsaxlokk to rybacka miejscowość znana z kolorowych łodzi stojących w zatoce i z targu rybnego.",
            "Marsaxlokk is a fishing village known for colourful boats in its bay and for its fish market.",
            "Tradycyjne maltańskie łodzie luzzu często mają namalowane na dziobie oczy, które według dawnej tradycji miały chronić rybaków.",
            "Traditional Maltese luzzu boats often have eyes painted on the bow, a custom once believed to protect fishermen.",
            "Marsaxlokk Malta luzzu boats"
        ),
        searchItem(
            "w7s8",
            "Gozo jest drugą co do wielkości wyspą archipelagu maltańskiego. Jest bardziej zielona i spokojniejsza niż główna wyspa Malta.",
            "Gozo is the second-largest island in the Maltese archipelago and is generally greener and quieter than the main island of Malta.",
            "Na Gozo znajdują się megalityczne świątynie Ġgantija, należące do najstarszych wolnostojących kamiennych budowli świata.",
            "Gozo is home to the megalithic Ġgantija temples, among the world's oldest free-standing stone structures.",
            "Gozo Malta Citadel Ggantija coast"
        ),
        searchItem(
            "w7s9",
            "Comino to niewielka wyspa pomiędzy Maltą i Gozo. Najbardziej znanym miejscem jest Blue Lagoon z bardzo jasną, turkusową wodą.",
            "Comino is a small island between Malta and Gozo. Its best-known spot is the Blue Lagoon with bright turquoise water.",
            "Comino ma bardzo mało stałych mieszkańców i nie ma dużych miast, dlatego jej krajobraz różni się od zatłoczonej Valletty.",
            "Comino has very few permanent residents and no large towns, giving it a very different landscape from busy Valletta.",
            "Comino Blue Lagoon Malta turquoise water"
        ),
        searchItem(
            "w7s10",
            "Ħaġar Qim to zespół prehistorycznych świątyń na południu Malty, zbudowany z ogromnych bloków wapienia.",
            "Ħaġar Qim is a prehistoric temple complex in southern Malta built from enormous limestone blocks.",
            "Świątynie powstały ponad pięć tysięcy lat temu i należą do maltańskich stanowisk wpisanych na listę UNESCO.",
            "The temples were built more than five thousand years ago and form part of Malta's UNESCO-listed megalithic sites.",
            "Hagar Qim Malta temples"
        )
    ).associateBy { it.stageId }

    fun forStage(stage: Stage): AttractionInfo? = cards[stage.id]

    fun all(): List<AttractionInfo> = cards.values.sortedBy { it.stageId }
}
