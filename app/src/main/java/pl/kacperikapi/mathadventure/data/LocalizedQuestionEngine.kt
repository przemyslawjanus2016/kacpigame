package pl.kacperikapi.mathadventure.data

import kotlin.random.Random

/**
 * Adds country/language specific content on top of the existing adaptive engine.
 * Questions are randomized and recently displayed IDs are avoided.
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
        val recent = history.recentIds()

        // Polish alternates between the mature base bank and localized procedural variants.
        // Other languages always use the locale-native generator.
        val useLocalized = languageTag != "pl" || (++localizedCounter % 2 == 0)
        if (!useLocalized) {
            repeat(80) {
                val candidate = normalizeVisual(base.next(stage, fixedCategory, performance))
                if (candidate.id !in recent) return candidate
            }
            return normalizeVisual(base.next(stage, fixedCategory, performance))
        }

        val probe = base.next(stage, fixedCategory, performance)
        val category = fixedCategory ?: probe.category
        val stats = performance[category]
        val offset = when {
            stats == null || stats.solved < 8 -> 0
            stats.accuracyPercent >= 90 -> 1
            stats.accuracyPercent < 60 -> -1
            else -> 0
        }
        val age = (stage.targetAge + offset).coerceIn(GameRules.MIN_AGE, GameRules.MAX_AGE)

        repeat(80) {
            val candidate = normalizeVisual(LocalizedQuestionFactory.generate(category, stage, age, languageTag, random))
            if (candidate.id !in recent) return candidate
        }

        // If a small category pool is temporarily exhausted, generate a fresh base variant
        // instead of deliberately returning the last repeated localized question.
        return normalizeVisual(base.next(stage, fixedCategory, performance))
    }

    /**
     * The Unicode 🐾 glyph visually contains two paw prints. In counting exercises that
     * made e.g. four symbols look like eight objects. Use a single-object dog symbol there.
     */
    private fun normalizeVisual(question: LearningQuestion): LearningQuestion {
        if (!question.id.contains("math:count")) return question
        val visual = question.visual ?: return question
        if (!visual.contains("🐾")) return question
        return question.copy(visual = visual.replace("🐾", "🐶"))
    }

    fun markSeen(question: LearningQuestion) {
        history.remember(question.id)
    }
}
