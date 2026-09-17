package pl.kacperikapi.mathadventure.data

object PremiumAccess {
    const val PRODUCT_ID = "full_game_unlock"
    const val FREE_WORLD_ID = 1

    fun requiresPremium(worldId: Int): Boolean = worldId > FREE_WORLD_ID

    fun canOpenWorld(worldId: Int, progressUnlocked: Boolean, premiumUnlocked: Boolean): Boolean =
        DevOptions.UNLOCK_ALL_CONTENT || (progressUnlocked && (!requiresPremium(worldId) || premiumUnlocked))

    fun shouldShowPaywall(worldId: Int, premiumUnlocked: Boolean): Boolean =
        requiresPremium(worldId) && !premiumUnlocked && !DevOptions.UNLOCK_ALL_CONTENT
}
