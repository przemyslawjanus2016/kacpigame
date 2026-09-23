package pl.kacperikapi.mathadventure.data

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentSanityTest {
    @Test
    fun hasSevenWorldsAndSeventyStages() {
        assertEquals(7, GameContent.worlds.size)
        assertTrue(GameContent.worlds.all { it.stages.size == GameRules.STAGES_PER_WORLD })
        assertEquals(70, GameContent.worlds.sumOf { it.stages.size })
    }

    @Test
    fun stageDifficultyCoversAgesFourToEight() {
        assertTrue(GameContent.worlds.all { world ->
            world.stages.first().targetAge == 4 &&
                world.stages.last().targetAge == 8 &&
                world.stages.zipWithNext().all { (a, b) -> a.targetAge <= b.targetAge }
        })
    }

    @Test
    fun onlyPolishLanguageIsConfigured() {
        assertEquals(listOf("pl"), AppLanguages.supported.map { it.tag })
        assertEquals("pl", AppLanguages.normalize("en"))
        assertEquals("pl", AppLanguages.next("pl"))
        assertTrue(AppLanguages.supported.all { it.currencyCode.isNotBlank() && it.capital.isNotBlank() && it.ttsLocale.language.isNotBlank() })
    }

    @Test
    fun everyLanguageAndCategoryProducesValidQuestionsForAgesFourToEight() {
        AppLanguages.supported.forEachIndexed { languageIndex, language ->
            LearningCategory.entries.forEachIndexed { categoryIndex, category ->
                for (age in GameRules.MIN_AGE..GameRules.MAX_AGE) {
                    val stage = GameContent.worlds[(languageIndex + categoryIndex) % GameContent.worlds.size]
                        .stages[(age - GameRules.MIN_AGE).coerceIn(0, GameRules.STAGES_PER_WORLD - 1)]
                    repeat(8) { sample ->
                        val q = LocalizedQuestionFactory.generate(
                            category = category,
                            stage = stage,
                            age = age,
                            language = language.tag,
                            random = Random(languageIndex * 10000 + categoryIndex * 1000 + age * 100 + sample)
                        )
                        assertTrue("blank prompt for ${language.tag}/$category/$age", q.prompt(language.tag).isNotBlank())
                        assertTrue("too few options for ${language.tag}/$category/$age", q.options(language.tag).size >= 2)
                        assertTrue("invalid answer for ${language.tag}/$category/$age", q.correctIndex in q.options(language.tag).indices)
                        assertTrue("blank answer for ${language.tag}/$category/$age", q.correctAnswer(language.tag).isNotBlank())
                    }
                }
            }
        }
    }

    @Test
    fun earlyMathDoesNotUseAmbiguousPictureCountingPrompts() {
        val stage = GameContent.stage(1, 1)
        for (age in 4..5) {
            repeat(80) { sample ->
                val q = LocalizedQuestionFactory.generate(
                    category = LearningCategory.MATH,
                    stage = stage,
                    age = age,
                    language = "pl",
                    random = Random(age * 1000 + sample)
                )
                assertTrue(!q.promptPl.contains("Policz obrazki", ignoreCase = true))
                assertTrue(!q.id.contains("math:count"))
            }
        }
    }

    @Test
    fun englishBankIsLargeEnoughForVariety() {
        assertTrue(QuestionBank.englishWords.size >= 90)
        assertTrue(QuestionBank.englishWords.map { it.en }.distinct().size >= 90)
    }

    @Test
    fun orthographySeedsDoNotPutCorrectWordAmongDistractors() {
        assertTrue(QuestionBank.orthography.all { seed -> seed.correct !in seed.wrong })
    }

    @Test
    fun allFactsHaveMatchingLanguageOptionsAndValidAnswer() {
        assertTrue(QuestionBank.facts.all { fact ->
            fact.optionsPl.size == fact.optionsEn.size && fact.correctIndex in fact.optionsPl.indices
        })
    }
    @Test
    fun everyStageHasAnAttractionCardForTheExplorerAlbum() {
        val expectedIds = GameContent.worlds.flatMap { world -> world.stages.map { it.id } }.toSet()
        val actualIds = AttractionContent.all().map { it.stageId }.toSet()
        assertEquals(70, actualIds.size)
        assertEquals(expectedIds, actualIds)
    }

    @Test
    fun everyWorldHasTenDistinctStoryChapters() {
        GameContent.worlds.forEach { world ->
            val stories = world.stages.map { GameContent.story(it) }
            assertEquals(GameRules.STAGES_PER_WORLD, stories.map { it.titlePl }.distinct().size)
            assertTrue(stories.first().titlePl.contains("1/10"))
            assertTrue(stories.last().titlePl.contains("10/10"))
            assertTrue(stories.last().titlePl.contains("Finał"))
        }
    }

    @Test
    fun kapiAndKacperStoryInteractionsHaveVarietyAndCorrectWieliczkaForms() {
        val texts = (1..GameRules.STAGES_PER_WORLD).map { stageNumber ->
            GameContent.story(GameContent.stage(1, stageNumber)).textPl
        }
        assertTrue(texts.distinct().size >= 8)
        assertTrue(texts.all { it.contains("Kapi") && it.contains("Kacper") })
        assertTrue(texts.any { it.contains("Wieliczkę") })
        assertTrue(texts.any { it.contains("Wieliczki") })
        assertTrue(texts.any { it.contains("Wieliczce") })
    }
}
