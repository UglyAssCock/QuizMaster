package com.quizmaster.app.data

import com.google.gson.Gson
import com.quizmaster.app.model.*

object SeedDataRepository {
    private val gson = Gson()

    val categories: List<CategoryGroup> = listOf(
        CategoryGroup(
            id = "GEOGRAPHY",
            name = "Geography",
            colorHex = 0xFF0284C7,
            subcategories = listOf(
                "Countries", "Capitals", "Flags", "Cities", "Rivers", "Mountains",
                "Lakes", "Islands", "US States", "European Countries", "African Countries",
                "Asian Countries", "Population", "World Maps", "National Borders",
                "Currencies", "Languages"
            )
        ),
        CategoryGroup(
            id = "SPORTS",
            name = "Sports",
            colorHex = 0xFF16A34A,
            subcategories = listOf(
                "Football", "Basketball", "Tennis", "Formula 1", "Cricket", "Baseball",
                "Rugby", "Olympic Games", "Boxing", "Athletics", "Volleyball", "American Football"
            )
        ),
        CategoryGroup(
            id = "FOOTBALL",
            name = "Football Vault",
            colorHex = 0xFF15803D,
            subcategories = listOf(
                "Football Players", "Football Clubs", "Club Badges", "National Teams",
                "World Cup", "Champions League", "Europa League", "Premier League",
                "La Liga", "Serie A", "Bundesliga", "Ligue 1", "Football Stadiums",
                "Ballon d'Or Winners", "Football Managers", "Football Records",
                "Famous Transfers", "Football Nicknames", "Football Shirt Numbers"
            )
        ),
        CategoryGroup(
            id = "ENTERTAINMENT",
            name = "Entertainment",
            colorHex = 0xFF9333EA,
            subcategories = listOf(
                "Movies", "TV Series", "Anime", "Cartoons", "Actors", "Directors",
                "Music", "Singers", "Bands", "Video Games", "Famous Characters",
                "Disney", "Marvel", "DC"
            )
        ),
        CategoryGroup(
            id = "HISTORY",
            name = "History",
            colorHex = 0xFFB45309,
            subcategories = listOf(
                "Ancient History", "World Wars", "Historical Figures", "Ancient Civilizations",
                "Empires", "Inventions", "Historical Dates", "Archaeology", "Historical Geography"
            )
        ),
        CategoryGroup(
            id = "SCIENCE",
            name = "Science",
            colorHex = 0xFF0D9488,
            subcategories = listOf(
                "Biology", "Chemistry", "Physics", "Astronomy", "Space",
                "Human Body", "Animals", "Plants", "Mathematics", "Technology"
            )
        ),
        CategoryGroup(
            id = "GENERAL_KNOWLEDGE",
            name = "General Knowledge",
            colorHex = 0xFFE11D48,
            subcategories = listOf(
                "Brands", "Food", "Drinks", "Inventions", "Literature",
                "Famous People", "Architecture", "Languages", "Education", "Internet Culture"
            )
        )
    )

    fun initialAchievements(): List<AchievementEntity> = listOf(
        AchievementEntity("ach_first", "First Quiz", "Complete your very first quiz.", AchievementTier.BRONZE.name, 0, 1, false),
        AchievementEntity("ach_perfect", "Perfect Score", "Score 100% accuracy on any quiz.", AchievementTier.SILVER.name, 0, 1, false),
        AchievementEntity("ach_geo_50", "Geography Expert", "Complete 50 Geography quizzes.", AchievementTier.GOLD.name, 3, 50, false),
        AchievementEntity("ach_footy_50", "Football Expert", "Complete 50 Football quizzes.", AchievementTier.GOLD.name, 4, 50, false),
        AchievementEntity("ach_addict_100", "Quiz Addict", "Complete 100 total quizzes across all modes.", AchievementTier.PLATINUM.name, 7, 100, false),
        AchievementEntity("ach_speed", "Speed Demon", "Finish a quiz in under 25 seconds.", AchievementTier.SILVER.name, 0, 1, false),
        AchievementEntity("ach_creator", "Creator", "Publish your first custom quiz in Quiz Studio.", AchievementTier.BRONZE.name, 0, 1, false),
        AchievementEntity("ach_popular", "Popular Creator", "Reach 1,000 community plays on your created quizzes.", AchievementTier.GOLD.name, 120, 1000, false),
        AchievementEntity("ach_streak_7", "Daily Player", "Maintain a 7-day daily challenge streak.", AchievementTier.SILVER.name, 5, 7, false),
        AchievementEntity("ach_legend", "Knowledge Master", "Reach Quiz Legend rank.", AchievementTier.PLATINUM.name, 1450, 20000, false)
    )

    fun initialQuizzes(): List<QuizEntity> {
        val list = mutableListOf<QuizEntity>()

        // 1. NAME ALL (Sporcle-style FIFA World Cup Winners)
        list += QuizEntity(
            id = "quiz_wc_winners",
            title = "All FIFA World Cup Winning Nations",
            description = "Can you name all 8 national teams that have lifted the men's FIFA World Cup trophy?",
            category = "Football Vault",
            subcategory = "World Cup",
            difficulty = "Easy",
            gameModeId = GameMode.NAME_ALL.id,
            timeLimitSeconds = 180,
            creatorName = "FootyVault",
            playCount = 14820,
            ratingAverage = 4.9f,
            ratingCount = 920,
            tagsCsv = "World Cup,Sporcle,Countries,Football",
            questionsJson = gson.toJson(
                listOf(
                    QuestionItem("wc1", "5 Titles (1958, 1962, 1970, 1994, 2002)", correctAnswer = "Brazil", acceptableAnswers = listOf("Brasil"), hint = "South American Samba kings"),
                    QuestionItem("wc2", "4 Titles (1954, 1974, 1990, 2014)", correctAnswer = "Germany", acceptableAnswers = listOf("West Germany", "Deutschland"), hint = "Die Mannschaft"),
                    QuestionItem("wc3", "4 Titles (1934, 1938, 1982, 2006)", correctAnswer = "Italy", acceptableAnswers = listOf("Italia"), hint = "The Azzurri"),
                    QuestionItem("wc4", "3 Titles (1978, 1986, 2022)", correctAnswer = "Argentina", hint = "La Albiceleste"),
                    QuestionItem("wc5", "2 Titles (1998, 2018)", correctAnswer = "France", hint = "Les Bleus"),
                    QuestionItem("wc6", "2 Titles (1930, 1950)", correctAnswer = "Uruguay", hint = "First ever hosts and winners"),
                    QuestionItem("wc7", "1 Title (1966)", correctAnswer = "England", hint = "Won at Wembley Stadium"),
                    QuestionItem("wc8", "1 Title (2010)", correctAnswer = "Spain", acceptableAnswers = listOf("Espana"), hint = "La Roja in South Africa")
                )
            )
        )

        // 2. CLASSIC TRIVIA (World Capitals)
        list += QuizEntity(
            id = "quiz_capitals_classic",
            title = "World Capitals Masterclass",
            description = "Test your precision on tricky national capitals that often fool travelers.",
            category = "Geography",
            subcategory = "Capitals",
            difficulty = "Medium",
            gameModeId = GameMode.CLASSIC_TRIVIA.id,
            timeLimitSeconds = 120,
            creatorName = "GeoAtlas",
            playCount = 9340,
            ratingAverage = 4.8f,
            ratingCount = 412,
            tagsCsv = "Capitals,Geography,World",
            questionsJson = gson.toJson(
                listOf(
                    QuestionItem("cap1", "What is the capital city of Australia?", listOf("Sydney", "Melbourne", "Canberra", "Perth"), "Canberra", explanation = "Canberra was chosen in 1908 as a compromise between rivals Sydney and Melbourne.", hint = "Starts with C"),
                    QuestionItem("cap2", "What is the capital city of Canada?", listOf("Toronto", "Vancouver", "Montreal", "Ottawa"), "Ottawa", explanation = "Queen Victoria selected Ottawa in 1857.", hint = "Located in Ontario along the Ottawa River"),
                    QuestionItem("cap3", "What is the capital city of Brazil?", listOf("Rio de Janeiro", "São Paulo", "Brasília", "Salvador"), "Brasília", explanation = "Planned by Oscar Niemeyer and Lúcio Costa, inaugurated in 1960.", hint = "Airplane-shaped planned city"),
                    QuestionItem("cap4", "What is the capital of Morocco?", listOf("Casablanca", "Marrakesh", "Rabat", "Fes"), "Rabat", explanation = "Rabat is the administrative and royal capital of Morocco.", hint = "Coastal city on the Bou Regreg river"),
                    QuestionItem("cap5", "What is the capital of Kazakhstan?", listOf("Almaty", "Astana", "Tashkent", "Bishkek"), "Astana", explanation = "Astana replaced Almaty as capital in 1997.", hint = "Futuristic steppe capital")
                )
            )
        )

        // 3. TYPE THE ANSWER (Ballon d'Or Legends)
        list += QuizEntity(
            id = "quiz_ballon_dor_type",
            title = "Type the Ballon d'Or Winner",
            description = "Type the surname of these iconic Ballon d'Or winners from the clue provided.",
            category = "Football Vault",
            subcategory = "Ballon d'Or Winners",
            difficulty = "Medium",
            gameModeId = GameMode.TYPE_ANSWER.id,
            timeLimitSeconds = 150,
            creatorName = "FootyVault",
            playCount = 7230,
            ratingAverage = 4.9f,
            ratingCount = 310,
            tagsCsv = "Ballon d'Or,Football,Type",
            questionsJson = gson.toJson(
                listOf(
                    QuestionItem("bd1", "Record 8-time Ballon d'Or winner from Rosario, Argentina.", correctAnswer = "Messi", acceptableAnswers = listOf("Lionel Messi", "Leo Messi"), hint = "Played for Barcelona, PSG, and Inter Miami"),
                    QuestionItem("bd2", "5-time Ballon d'Or winner from Madeira, Portugal.", correctAnswer = "Ronaldo", acceptableAnswers = listOf("Cristiano Ronaldo", "CR7"), hint = "Real Madrid's all-time top scorer"),
                    QuestionItem("bd3", "Croatian midfield maestro who broke the Messi-Ronaldo duopoly in 2018.", correctAnswer = "Modric", acceptableAnswers = listOf("Luka Modric", "Modrić"), hint = "Wears number 10 for Real Madrid and Croatia"),
                    QuestionItem("bd4", "French striker who won the 2022 Ballon d'Or after a historic Champions League campaign.", correctAnswer = "Benzema", acceptableAnswers = listOf("Karim Benzema"), hint = "Nicknamed KB9")
                )
            )
        )

        // 4. FLAG QUIZ (Sovereign Flags)
        list += QuizEntity(
            id = "quiz_flags_world",
            title = "Flags of the World Challenge",
            description = "Identify each country from its rendered national flag design.",
            category = "Geography",
            subcategory = "Flags",
            difficulty = "Easy",
            gameModeId = GameMode.FLAG_QUIZ.id,
            timeLimitSeconds = 90,
            creatorName = "GeoAtlas",
            playCount = 11200,
            ratingAverage = 4.7f,
            ratingCount = 520,
            tagsCsv = "Flags,Colors,Nations",
            questionsJson = gson.toJson(
                listOf(
                    QuestionItem("fl1", "Identify this vertical tricolor of Blue, White, and Red.", listOf("France", "Italy", "Belgium", "Netherlands"), "France", visualCode = "FLAG_FR", hint = "Home of the Eiffel Tower"),
                    QuestionItem("fl2", "Identify this white banner featuring a crimson sun disc in the center.", listOf("South Korea", "Japan", "Bangladesh", "Palau"), "Japan", visualCode = "FLAG_JP", hint = "Land of the Rising Sun"),
                    QuestionItem("fl3", "Identify this vertical tricolor of Green, White, and Red.", listOf("Ireland", "Mexico", "Italy", "Hungary"), "Italy", visualCode = "FLAG_IT", hint = "Il Tricolore"),
                    QuestionItem("fl4", "Identify this horizontal tricolor of Black, Red, and Gold.", listOf("Belgium", "Germany", "Spain", "Austria"), "Germany", visualCode = "FLAG_DE", hint = "Central European economic powerhouse")
                )
            )
        )

        // 5. MAP QUIZ (World Regions Coordinate Pinpoint)
        list += QuizEntity(
            id = "quiz_map_pinpoint",
            title = "World Map Pinpoint: Continents & Hubs",
            description = "Tap the glowing region marker on the interactive world map that matches the target location!",
            category = "Geography",
            subcategory = "World Maps",
            difficulty = "Medium",
            gameModeId = GameMode.MAP_QUIZ.id,
            timeLimitSeconds = 120,
            creatorName = "GeoAtlas",
            playCount = 6400,
            ratingAverage = 4.8f,
            ratingCount = 290,
            tagsCsv = "Map,Interactive,Atlas",
            questionsJson = gson.toJson(
                listOf(
                    QuestionItem("mp1", "Tap the marker located in South America (Brazil / Amazon Basin)", listOf("North America", "South America", "Europe", "East Asia"), "South America", visualCode = "0.32,0.68", hint = "Southern Western Hemisphere"),
                    QuestionItem("mp2", "Tap the marker located in Western Europe (Paris / London Hub)", listOf("North America", "South America", "Europe", "East Asia"), "Europe", visualCode = "0.49,0.28", hint = "North of the Mediterranean Sea"),
                    QuestionItem("mp3", "Tap the marker located in East Asia (Tokyo / Pacific Rim)", listOf("North America", "South America", "Europe", "East Asia"), "East Asia", visualCode = "0.82,0.36", hint = "Far Eastern hemisphere"),
                    QuestionItem("mp4", "Tap the marker located in North America (United States / Canada)", listOf("North America", "South America", "Europe", "East Asia"), "North America", visualCode = "0.22,0.32", hint = "Northwest quadrant of the map")
                )
            )
        )

        // 6. CONNECTIONS 4x4
        list += QuizEntity(
            id = "quiz_connections_grid",
            title = "Connections: 16 Words, 4 Hidden Links",
            description = "Select groups of 4 items that share a common thread.",
            category = "General Knowledge",
            subcategory = "Education",
            difficulty = "Hard",
            gameModeId = GameMode.CONNECTIONS.id,
            timeLimitSeconds = 180,
            creatorName = "SporcleLegends",
            playCount = 8900,
            ratingAverage = 4.9f,
            ratingCount = 610,
            tagsCsv = "Connections,Logic,Grid",
            questionsJson = gson.toJson(
                listOf(
                    QuestionItem(
                        id = "conn1",
                        prompt = "Group the 16 tiles into 4 categories of 4:",
                        options = listOf(
                            "Mercury", "Venus", "Mars", "Saturn",
                            "Arsenal", "Chelsea", "Liverpool", "Everton",
                            "Gold", "Silver", "Iron", "Copper",
                            "Haiku", "Sonnet", "Limerick", "Ode"
                        ),
                        correctAnswer = "SOLVED",
                        extraPairs = mapOf(
                            "Mercury" to "Solar System Planets", "Venus" to "Solar System Planets", "Mars" to "Solar System Planets", "Saturn" to "Solar System Planets",
                            "Arsenal" to "Premier League Clubs", "Chelsea" to "Premier League Clubs", "Liverpool" to "Premier League Clubs", "Everton" to "Premier League Clubs",
                            "Gold" to "Metallic Elements", "Silver" to "Metallic Elements", "Iron" to "Metallic Elements", "Copper" to "Metallic Elements",
                            "Haiku" to "Poetic Forms", "Sonnet" to "Poetic Forms", "Limerick" to "Poetic Forms", "Ode" to "Poetic Forms"
                        ),
                        hint = "Look for Planets, English Football Clubs, Metals, and Poetry types!"
                    )
                )
            )
        )

        // 7. HIGHER OR LOWER
        list += QuizEntity(
            id = "quiz_higher_lower",
            title = "Higher or Lower: Global Records",
            description = "Decide whether the second item has a HIGHER or LOWER value than the benchmark!",
            category = "Science",
            subcategory = "Mathematics",
            difficulty = "Easy",
            gameModeId = GameMode.HIGHER_LOWER.id,
            timeLimitSeconds = 90,
            creatorName = "SporcleLegends",
            playCount = 5400,
            ratingAverage = 4.7f,
            ratingCount = 215,
            tagsCsv = "Stats,Numbers,HigherLower",
            questionsJson = gson.toJson(
                listOf(
                    QuestionItem("hl1", "Mount Everest (8,848m) vs K2 (8,611m): Is K2 HIGHER or LOWER than Everest?", listOf("HIGHER", "LOWER"), "LOWER", hint = "Everest is the tallest peak on Earth"),
                    QuestionItem("hl2", "Nile River (6,650 km) vs Thames River (346 km): Is the Nile HIGHER or LOWER in length?", listOf("HIGHER", "LOWER"), "HIGHER", hint = "The Nile spans northeastern Africa"),
                    QuestionItem("hl3", "Speed of Light vs Speed of Sound: Is the Speed of Light HIGHER or LOWER?", listOf("HIGHER", "LOWER"), "HIGHER", hint = "300,000 km/s vs 343 m/s")
                )
            )
        )

        // 8. WHO AM I? (Progressive Clues)
        list += QuizEntity(
            id = "quiz_who_am_i",
            title = "Who Am I? Historical & Science Icons",
            description = "Reveal up to 4 progressive clues to deduce the mystery person.",
            category = "History",
            subcategory = "Historical Figures",
            difficulty = "Medium",
            gameModeId = GameMode.WHO_AM_I.id,
            timeLimitSeconds = 120,
            creatorName = "SporcleLegends",
            playCount = 4890,
            ratingAverage = 4.8f,
            ratingCount = 180,
            tagsCsv = "Clues,Mystery,History",
            questionsJson = gson.toJson(
                listOf(
                    QuestionItem(
                        id = "wai1",
                        prompt = "Identify this pioneering scientist:",
                        options = listOf("Marie Curie", "Rosalind Franklin", "Ada Lovelace", "Florence Nightingale"),
                        correctAnswer = "Marie Curie",
                        clues = listOf(
                            "Clue 1 (400 pts): I was born in Warsaw, Poland in 1867.",
                            "Clue 2 (300 pts): I moved to Paris and studied at the Sorbonne.",
                            "Clue 3 (200 pts): I discovered the elements Polonium and Radium.",
                            "Clue 4 (100 pts): I am the only person to win Nobel Prizes in both Physics and Chemistry."
                        ),
                        hint = "Coined the term radioactivity"
                    ),
                    QuestionItem(
                        id = "wai2",
                        prompt = "Identify this legendary polymath:",
                        options = listOf("Leonardo da Vinci", "Michelangelo", "Galileo Galilei", "Raphael"),
                        correctAnswer = "Leonardo da Vinci",
                        clues = listOf(
                            "Clue 1 (400 pts): I was born in Republic of Florence in 1452.",
                            "Clue 2 (300 pts): My notebooks sketched flying machines and anatomical studies.",
                            "Clue 3 (200 pts): I painted The Last Supper in Milan.",
                            "Clue 4 (100 pts): I painted the Mona Lisa."
                        ),
                        hint = "True Renaissance Man"
                    )
                )
            )
        )

        // 9. MATCHING COLUMNS
        list += QuizEntity(
            id = "quiz_matching_clubs",
            title = "Match the Football Club to its Iconic Stadium",
            description = "Tap a club on the left, then tap its home stadium on the right.",
            category = "Football Vault",
            subcategory = "Football Stadiums",
            difficulty = "Medium",
            gameModeId = GameMode.MATCHING.id,
            timeLimitSeconds = 120,
            creatorName = "FootyVault",
            playCount = 6100,
            ratingAverage = 4.8f,
            ratingCount = 240,
            tagsCsv = "Stadiums,Matching,Football",
            questionsJson = gson.toJson(
                listOf(
                    QuestionItem(
                        id = "mat1",
                        prompt = "Match every European giant to its home ground:",
                        correctAnswer = "ALL_MATCHED",
                        extraPairs = mapOf(
                            "Real Madrid" to "Santiago Bernabéu",
                            "FC Barcelona" to "Camp Nou",
                            "Manchester United" to "Old Trafford",
                            "Bayern Munich" to "Allianz Arena",
                            "AC Milan" to "San Siro"
                        ),
                        hint = "Old Trafford is the Theatre of Dreams"
                    )
                )
            )
        )

        // 10. TIMELINE & ORDERING & REMAINING 11 MODES
        val extraModeConfigs = listOf(
            Triple(GameMode.PICTURE_QUIZ, "Famous Wonders Visual Quiz", "Entertainment"),
            Triple(GameMode.MISSING_WORDS, "Complete the Famous Movie Quote", "Entertainment"),
            Triple(GameMode.TRUE_FALSE, "Science Fact or Fiction Blitz", "Science"),
            Triple(GameMode.MULTIPLE_ANSWERS, "Select ALL Landlocked European Nations", "Geography"),
            Triple(GameMode.ORDERING, "Order Planets by Distance from the Sun", "Science"),
            Triple(GameMode.TIMELINE, "World History Timeline: Ancient to Modern", "History"),
            Triple(GameMode.SPEED_QUIZ, "60-Second General Knowledge Speed Blitz", "General Knowledge"),
            Triple(GameMode.IMAGE_REVEAL, "Tile Uncover: Iconic World Landmarks", "Geography"),
            Triple(GameMode.CROSSWORD, "Mini Trivia Crossword Puzzle", "General Knowledge"),
            Triple(GameMode.WORD_SCRAMBLE, "Unscramble Capital Cities", "Geography"),
            Triple(GameMode.ENDLESS_MODE, "Sudden Death Endless Trivia Survival", "General Knowledge")
        )

        extraModeConfigs.forEachIndexed { idx, (mode, title, cat) ->
            list += QuizEntity(
                id = "quiz_mode_${mode.id}",
                title = title,
                description = mode.description,
                category = cat,
                subcategory = categories.firstOrNull { it.name == cat }?.subcategories?.firstOrNull() ?: "General",
                difficulty = if (idx % 2 == 0) "Medium" else "Hard",
                gameModeId = mode.id,
                timeLimitSeconds = if (mode == GameMode.SPEED_QUIZ) 60 else 120,
                creatorName = "QuizMasterOfficial",
                playCount = 2500 + idx * 430,
                ratingAverage = 4.7f,
                ratingCount = 95 + idx * 12,
                tagsCsv = "${mode.displayName},$cat,Featured",
                questionsJson = gson.toJson(
                    listOf(
                        QuestionItem(
                            id = "${mode.id}_q1",
                            prompt = when (mode) {
                                GameMode.MISSING_WORDS -> "Complete the quote: 'May the ____ be with you.'"
                                GameMode.TRUE_FALSE -> "True or False: Light from the Sun takes approximately 8 minutes and 20 seconds to reach Earth."
                                GameMode.WORD_SCRAMBLE -> "Unscramble the letters 'O K Y T O' to name a major capital city:"
                                GameMode.ORDERING -> "Tap the items in order from closest to the Sun to farthest:"
                                GameMode.TIMELINE -> "Tap the historical events from earliest year to latest year:"
                                GameMode.MULTIPLE_ANSWERS -> "Select ALL options that are landlocked countries in Europe (Select Switzerland & Austria):"
                                GameMode.CROSSWORD -> "1-ACROSS (5 Letters): Planet known as the Red Planet + 1-DOWN (4 Letters): Earth's natural satellite -> Answer: MARS"
                                else -> "Which element has the chemical symbol 'Au' and atomic number 79?"
                            },
                            options = when (mode) {
                                GameMode.TRUE_FALSE -> listOf("True", "False")
                                GameMode.ORDERING -> listOf("Mercury", "Venus", "Earth", "Mars")
                                GameMode.TIMELINE -> listOf("1789 French Revolution", "1914 WWI Begins", "1969 Moon Landing", "1989 Berlin Wall Falls")
                                GameMode.MULTIPLE_ANSWERS -> listOf("Switzerland", "Austria", "Spain", "Portugal")
                                else -> listOf("Gold", "Silver", "Argon", "Force")
                            },
                            correctAnswer = when (mode) {
                                GameMode.MISSING_WORDS -> "Force"
                                GameMode.TRUE_FALSE -> "True"
                                GameMode.WORD_SCRAMBLE -> "TOKYO"
                                GameMode.ORDERING -> "Mercury,Venus,Earth,Mars"
                                GameMode.TIMELINE -> "1789 French Revolution,1914 WWI Begins,1969 Moon Landing,1989 Berlin Wall Falls"
                                GameMode.MULTIPLE_ANSWERS -> "Austria,Switzerland"
                                GameMode.CROSSWORD -> "MARS"
                                else -> "Gold"
                            },
                            acceptableAnswers = listOf("Force", "Gold", "TOKYO", "MARS", "True"),
                            explanation = "Verified encyclopedic answer from the QuizMaster knowledge base.",
                            hint = "Check the first letter carefully!",
                            visualCode = "LANDMARK_PYRAMID",
                            clues = listOf("Mercury", "Venus", "Earth", "Mars")
                        ),
                        QuestionItem(
                            id = "${mode.id}_q2",
                            prompt = "Which country hosted and won the 1998 FIFA World Cup?",
                            options = listOf("France", "Brazil", "Germany", "Italy"),
                            correctAnswer = "France",
                            acceptableAnswers = listOf("France"),
                            explanation = "France defeated Brazil 3-0 in the final at Stade de France.",
                            hint = "Zinedine Zidane scored twice in the final."
                        )
                    )
                )
            )
        }

        return list
    }

    fun initialNotifications(): List<NotificationEntity> = listOf(
        NotificationEntity("n1", "Daily Challenges Ready!", "Today's 4 deterministic daily quizzes (General, Geography, Sports, Random) are live.", "DAILY"),
        NotificationEntity("n2", "5-Day Streak Active!", "Keep your streak alive today to unlock the Daily Player Silver Badge.", "STREAK"),
        NotificationEntity("n3", "FootyVault Published a New Quiz", "Play 'All FIFA World Cup Winning Nations' in Name All mode!", "FRIEND")
    )

    fun initialRooms(): List<MultiplayerRoomEntity> = listOf(
        MultiplayerRoomEntity("QM-8821", "Nadia_Trivia", "Open Slot", "quiz_capitals_classic", "World Capitals Masterclass", 420, 0, "WAITING"),
        MultiplayerRoomEntity("QM-4092", "Karim_DZ", "Alex_Explorer", "quiz_wc_winners", "All FIFA World Cup Winning Nations", 750, 800, "COMPLETED")
    )
}
