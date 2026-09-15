package pl.kacperikapi.mathadventure.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentSanityTest {
    @Test
    fun hasSevenWorldsAndFortyNineStages() {
        assertEquals(7, GameContent.worlds.size)
        assertTrue(GameContent.worlds.all { it.stages.size == GameRules.STAGES_PER_WORLD })
        assertEquals(49, GameContent.worlds.sumOf { it.stages.size })
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
