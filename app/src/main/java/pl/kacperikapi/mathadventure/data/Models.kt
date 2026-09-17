package pl.kacperikapi.mathadventure.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

enum class LearningCategory {
    MATH,
    POLISH,
    ENGLISH,
    LOGIC,
    NATURE,
    WORLD,
    DAILY
}

enum class QuestionType {
    CHOICE,
    TRUE_FALSE,
    IMAGE_CHOICE,
    SEQUENCE,
    MATCHING,
    ORDERING,
    MEMORY
}

data class CategoryInfo(
    val id: LearningCategory,
    @StringRes val nameRes: Int,
    @StringRes val shortDescriptionRes: Int,
    val icon: String
)

private fun usePolish(language: String): Boolean = AppLanguages.normalize(language) == "pl"

data class StoryBeat(
    val titlePl: String,
    val titleEn: String,
    val textPl: String,
    val textEn: String,
    val factPl: String,
    val factEn: String,
    val emoji: String = "🐾"
) {
    fun title(language: String) = if (usePolish(language)) titlePl else titleEn
    fun text(language: String) = if (usePolish(language)) textPl else textEn
    fun fact(language: String) = if (usePolish(language)) factPl else factEn
}

data class AttractionInfo(
    val stageId: String,
    val descriptionPl: String,
    val descriptionEn: String,
    val factPl: String,
    val factEn: String,
    val photoFileName: String,
    val photoAuthor: String,
    val photoLicense: String,
    val sourcePage: String,
    val photoSearchQuery: String? = null
) {
    fun description(language: String): String = if (usePolish(language)) descriptionPl else descriptionEn
    fun fact(language: String): String = if (usePolish(language)) factPl else factEn
    fun credit(language: String): String = photoCredit(language, photoAuthor, photoLicense)
}

data class CommonsPhotoAttribution(
    val author: String,
    val license: String,
    val sourcePage: String
) {
    fun credit(language: String): String = photoCredit(language, author, license)
}

private fun photoCredit(language: String, author: String, license: String): String = when (AppLanguages.normalize(language)) {
    "de" -> "Foto: $author • $license • Wikimedia Commons"
    "es" -> "Foto: $author • $license • Wikimedia Commons"
    "it" -> "Foto: $author • $license • Wikimedia Commons"
    "sk" -> "Fotografia: $author • $license • Wikimedia Commons"
    "en" -> "Photo: $author • $license • Wikimedia Commons"
    else -> "Zdjęcie: $author • $license • Wikimedia Commons"
}

data class WorldDefinition(
    val id: Int,
    @StringRes val nameRes: Int,
    val icon: String,
    val subtitlePl: String,
    val subtitleEn: String,
    @DrawableRes val heroArtRes: Int? = null,
    @DrawableRes val thumbnailRes: Int? = null,
    val stages: List<Stage>
) {
    fun subtitle(language: String): String = if (usePolish(language)) subtitlePl else subtitleEn
}

data class Stage(
    val id: String,
    val worldId: Int,
    val number: Int,
    val namePl: String,
    val nameEn: String,
    val categories: List<LearningCategory>,
    val x: Float,
    val y: Float,
    val targetAge: Int = GameRules.targetAge(number)
) {
    fun name(language: String): String = if (usePolish(language)) namePl else nameEn
}

data class MatchPair(
    val leftPl: String,
    val leftEn: String,
    val rightPl: String,
    val rightEn: String
) {
    fun left(language: String) = if (usePolish(language)) leftPl else leftEn
    fun right(language: String) = if (usePolish(language)) rightPl else rightEn
}

data class LearningQuestion(
    val id: String,
    val category: LearningCategory,
    val type: QuestionType = QuestionType.CHOICE,
    val promptPl: String,
    val promptEn: String,
    val optionsPl: List<String>,
    val optionsEn: List<String> = optionsPl,
    val correctIndex: Int,
    val hintPl: String,
    val hintEn: String,
    val visual: String? = null,
    val pairs: List<MatchPair> = emptyList(),
    val correctOrderPl: List<String> = emptyList(),
    val correctOrderEn: List<String> = emptyList()
) {
    fun prompt(language: String): String = if (usePolish(language)) promptPl else promptEn
    fun options(language: String): List<String> = if (usePolish(language)) optionsPl else optionsEn
    fun hint(language: String): String = if (usePolish(language)) hintPl else hintEn
    fun correctAnswer(language: String): String = options(language).getOrElse(correctIndex) { "" }
    fun correctOrder(language: String): List<String> = if (usePolish(language)) correctOrderPl else correctOrderEn
}

data class CategoryStats(
    val solved: Int = 0,
    val correct: Int = 0
) {
    val accuracyPercent: Int
        get() = if (solved == 0) 0 else correct * 100 / solved
}

data class RoundResult(
    val correct: Int,
    val total: Int,
    val earnedStars: Int,
    val categoryStats: Map<LearningCategory, CategoryStats>
)

data class GameProgress(
    val schemaVersion: Int = 2,
    val coins: Int = 0,
    val stars: Int = 0,
    val unlockedWorldId: Int = 1,
    val maxStageByWorld: Map<Int, Int> = mapOf(1 to 1),
    val completedStageIds: Set<String> = emptySet(),
    val starsByStage: Map<String, Int> = emptyMap(),
    val solvedTasks: Int = 0,
    val correctTasks: Int = 0,
    val categoryStats: Map<LearningCategory, CategoryStats> = LearningCategory.entries.associateWith { CategoryStats() },
    val dailyStreak: Int = 0,
    val dailyLastCompletedDate: String = "",
    val totalDailyMissions: Int = 0,
    val collectedPostcards: Set<Int> = emptySet()
) {
    val accuracyPercent: Int
        get() = if (solvedTasks == 0) 0 else correctTasks * 100 / solvedTasks

    fun maxStage(worldId: Int): Int = maxStageByWorld[worldId] ?: if (worldId == 1) 1 else 0

    fun availableMaxStage(worldId: Int): Int =
        if (DevOptions.UNLOCK_ALL_CONTENT) GameRules.STAGES_PER_WORLD else maxStage(worldId)

    fun isWorldUnlocked(worldId: Int): Boolean =
        DevOptions.UNLOCK_ALL_CONTENT || worldId <= unlockedWorldId

    fun isStageUnlocked(stage: Stage): Boolean =
        DevOptions.UNLOCK_ALL_CONTENT ||
            (isWorldUnlocked(stage.worldId) && stage.number <= maxStage(stage.worldId))

    fun isStageCompleted(stage: Stage): Boolean = stage.id in completedStageIds

    fun stageStars(stage: Stage): Int = starsByStage[stage.id] ?: 0

    fun isWorldCompleted(worldId: Int): Boolean =
        GameContent.world(worldId).stages.all { isStageCompleted(it) }
}
