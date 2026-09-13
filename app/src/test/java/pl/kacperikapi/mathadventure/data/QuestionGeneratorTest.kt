package pl.kacperikapi.mathadventure.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.kacperikapi.mathadventure.R
import kotlin.random.Random

class QuestionGeneratorTest {
    @Test
    fun generatedQuestionAlwaysContainsCorrectAnswer() {
        val generator = QuestionGenerator(Random(1234))
        val level = Level(4, R.string.landmark_danilowicz, Operation.MULTIPLY, .5f, .5f)

        repeat(50) {
            val q = generator.generate(level)
            assertTrue(q.options.contains(q.answer))
            assertEquals(4, q.options.distinct().size)
        }
    }

    @Test
    fun subtractionNeverProducesNegativeResult() {
        val generator = QuestionGenerator(Random(55))
        val level = Level(2, R.string.landmark_castle, Operation.SUBTRACT, .5f, .5f)

        repeat(50) {
            assertTrue(generator.generate(level).answer >= 0)
        }
    }
}
