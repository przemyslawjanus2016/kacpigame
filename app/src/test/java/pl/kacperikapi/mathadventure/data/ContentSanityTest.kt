package pl.kacperikapi.mathadventure.data

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
