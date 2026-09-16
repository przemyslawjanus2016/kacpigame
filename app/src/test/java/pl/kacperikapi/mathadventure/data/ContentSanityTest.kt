package pl.kacperikapi.mathadventure.data

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentSanityTest {
    @Test
    fun hasSevenWorldsAndSeventyStages() {
        assertEquals(7, GameContent.worlds.size)
        assertTrue(GameContent.worlds.all { it.stages.size == 10 })
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
    fun sixLanguagesAreConfigured() {
        assertEquals(listOf("pl", "en", "de", "es", "it", "sk"), AppLanguages.supported.map { it.tag })
        assertTrue(AppLanguages.supported.all { it.currencyCode.isNotBlank() && it.capital.isNotBlank() && it.ttsLocale.language.isNotBlank() })
    }

    @Test
    fun everyLanguageAndCategoryProducesValidQuestionsForAgesFourToEight() {
        AppLanguages.supported.forEachIndexed { languageIndex, language ->
            LearningCategory.entries.forEachIndexed { categoryIndex, category ->
                for (age in GameRules.MIN_AGE..GameRules.MAX_AGE) {
                    val world = GameContent.worlds[(languageIndex + categoryIndex) % GameContent.worlds.size]
                    val stage = world.stages[(age - GameRules.MIN_AGE).coerceIn(0, world.stages.lastIndex)]
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
}
