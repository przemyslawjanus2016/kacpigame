package pl.kacperikapi.mathadventure.data

import kotlin.random.Random

/**
 * Adds country/language specific content on top of the existing adaptive engine.
 * The base engine still chooses categories according to the child's progress.
 */
class LocalizedQuestionEngine(
    private val history: QuestionHistoryStore,
    language: String,
    private val random: Random = Random.Default
) {
    private val base = QuestionEngine(history, random)
    private val languageTag = AppLanguages.normalize(language)
    private var localizedCounter = 0

    fun next(
        stage: Stage,
        fixedCategory: LearningCategory? = null,
        performance: Map<LearningCategory, CategoryStats> = emptyMap()
    ): LearningQuestion {
        val baseQuestion = base.next(stage, fixedCategory, performance)

        // Polish keeps the mature existing bank and receives extra localized variants.
        // Other languages use the locale-native generator for every question so no
        // Polish text leaks into a German/Spanish/Italian/Slovak/English round.
        val useLocalized = languageTag != "pl" || (++localizedCounter % 2 == 0)
        if (!useLocalized) return baseQuestion

        val category = fixedCategory ?: baseQuestion.category
        val stats = performance[category]
        val offset = when {
            stats == null || stats.solved < 6 -> 0
            stats.accuracyPercent >= 92 -> 1
            stats.accuracyPercent <= 58 -> -1
            else -> 0
        }
        val age = (stage.targetAge + offset).coerceIn(GameRules.MIN_AGE, GameRules.MAX_AGE)
        val recent = history.recentIds()

        var fallback: LearningQuestion? = null
        repeat(30) {
            val candidate = LocalizedQuestionFactory.generate(category, stage, age, languageTag, random)
            fallback = candidate
            if (candidate.id !in recent) return candidate
        }
        return fallback ?: baseQuestion
    }

    fun markSeen(question: LearningQuestion) {
        history.remember(question.id)
    }
}
