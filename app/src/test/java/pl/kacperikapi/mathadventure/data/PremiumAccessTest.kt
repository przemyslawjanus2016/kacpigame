package pl.kacperikapi.mathadventure.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumAccessTest {
    @Test
    fun wieliczkaIsAlwaysFreeWhenProgressAllowsIt() {
        assertFalse(PremiumAccess.requiresPremium(1))
        assertTrue(PremiumAccess.canOpenWorld(1, progressUnlocked = true, premiumUnlocked = false))
    }

    @Test
    fun laterWorldsNeedPremiumAndProgress() {
        assertTrue(PremiumAccess.requiresPremium(2))
        assertFalse(PremiumAccess.canOpenWorld(2, progressUnlocked = true, premiumUnlocked = false))
        assertFalse(PremiumAccess.canOpenWorld(2, progressUnlocked = false, premiumUnlocked = true))
        assertTrue(PremiumAccess.canOpenWorld(2, progressUnlocked = true, premiumUnlocked = true))
    }

    @Test
    fun paywallOnlyAppliesToPremiumWorldsWithoutEntitlement() {
        assertFalse(PremiumAccess.shouldShowPaywall(1, premiumUnlocked = false))
        assertTrue(PremiumAccess.shouldShowPaywall(2, premiumUnlocked = false))
        assertFalse(PremiumAccess.shouldShowPaywall(2, premiumUnlocked = true))
    }
}
