package pl.kacperikapi.mathadventure.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.kacperikapi.mathadventure.R
import pl.kacperikapi.mathadventure.data.DevOptions
import pl.kacperikapi.mathadventure.data.GameContent
import pl.kacperikapi.mathadventure.data.GameProgress
import pl.kacperikapi.mathadventure.data.GameRules
import pl.kacperikapi.mathadventure.data.PremiumAccess
import pl.kacperikapi.mathadventure.data.WorldDefinition
import pl.kacperikapi.mathadventure.ui.components.GameTitle
import pl.kacperikapi.mathadventure.ui.components.ResourceBar
import pl.kacperikapi.mathadventure.ui.theme.*

@Composable
fun WorldSelectScreen(
    progress: GameProgress,
    premiumUnlocked: Boolean,
    onWorld: (Int) -> Unit,
    onPremium: () -> Unit,
    onPractice: () -> Unit,
    onDaily: () -> Unit,
    onPassport: () -> Unit,
    onRewards: () -> Unit,
    onParent: () -> Unit,
    onSettings: () -> Unit
) {
    BoxWithConstraints(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyBlue.copy(.35f), Cream, Parchment)))
            .statusBarsPadding()
    ) {
        val tablet = maxWidth >= 700.dp
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            ResourceBar(progress = progress, onSettingsClick = onSettings)
            GameTitle(compact = !tablet)
            Spacer(Modifier.height(12.dp))
            if (tablet) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    HeroPanel(Modifier.weight(.9f))
                    WorldGrid(progress, premiumUnlocked, onWorld, onPremium, Modifier.weight(1.35f))
                }
            } else {
                HeroPanel(Modifier.padding(horizontal = 12.dp))
                Spacer(Modifier.height(12.dp))
                WorldGrid(progress, premiumUnlocked, onWorld, onPremium, Modifier.padding(horizontal = 12.dp))
            }
            if (!premiumUnlocked && !DevOptions.UNLOCK_ALL_CONTENT) {
                FilledTonalButton(
                    onClick = onPremium,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp).heightIn(min = 54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("🔐 ${stringResource(R.string.premium_unlock_title)}", fontWeight = FontWeight.Black)
                }
            }
            ActionGrid(onDaily, onPassport, onPractice, onRewards, onParent)
            Spacer(Modifier.height(22.dp))
        }
    }
}

@Composable
private fun HeroPanel(modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(24.dp), color = Parchment, shadowElevation = 5.dp) {
        Column {
            Image(
                painter = painterResource(R.drawable.adventure_splash),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().heightIn(min = 260.dp, max = 360.dp).aspectRatio(1.08f).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            )
            Column(Modifier.padding(14.dp)) {
                Text(stringResource(R.string.all_worlds_adventure), fontWeight = FontWeight.Black, fontSize = 21.sp, color = AdventureGreen)
                Text(stringResource(R.string.all_worlds_adventure_desc), color = Ink)
            }
        }
    }
}

@Composable
private fun WorldGrid(
    progress: GameProgress,
    premiumUnlocked: Boolean,
    onWorld: (Int) -> Unit,
    onPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(stringResource(R.string.choose_world), fontWeight = FontWeight.Black, fontSize = 24.sp, color = WoodBrown)
        GameContent.worlds.forEach { world ->
            WorldCard(
                world = world,
                unlocked = progress.isWorldUnlocked(world.id),
                premiumUnlocked = premiumUnlocked,
                maxStage = progress.maxStage(world.id),
                progress = progress,
                onWorld = onWorld,
                onPremium = onPremium
            )
        }
    }
}

@Composable
private fun WorldCard(
    world: WorldDefinition,
    unlocked: Boolean,
    premiumUnlocked: Boolean,
    maxStage: Int,
    progress: GameProgress,
    onWorld: (Int) -> Unit,
    onPremium: () -> Unit
) {
    val premiumLocked = PremiumAccess.shouldShowPaywall(world.id, premiumUnlocked)
    val canOpen = PremiumAccess.canOpenWorld(world.id, unlocked, premiumUnlocked)

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(enabled = canOpen || premiumLocked) {
            if (premiumLocked) onPremium() else if (canOpen) onWorld(world.id)
        },
        shape = RoundedCornerShape(18.dp),
        color = if (canOpen) androidx.compose.ui.graphics.Color.White.copy(.92f) else LockedGrey.copy(.14f),
        shadowElevation = if (canOpen || premiumLocked) 3.dp else 0.dp
    ) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            if (world.thumbnailRes != null) {
                Image(
                    painter = painterResource(world.thumbnailRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.width(104.dp).height(72.dp).clip(RoundedCornerShape(14.dp))
                )
            } else Text(world.icon, fontSize = 30.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(world.nameRes), fontWeight = FontWeight.Black, color = Ink, fontSize = 18.sp)
                Text(
                    when {
                        world.id == PremiumAccess.FREE_WORLD_ID -> stringResource(R.string.premium_free_world)
                        premiumLocked -> stringResource(R.string.premium_required)
                        !unlocked -> stringResource(R.string.locked)
                        progress.isWorldCompleted(world.id) -> "✅ ${stringResource(R.string.world_completed)}"
                        DevOptions.UNLOCK_ALL_CONTENT -> stringResource(R.string.preview_all_unlocked)
                        else -> stringResource(R.string.stage_progress, maxStage.coerceAtLeast(1), GameRules.STAGES_PER_WORLD)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (canOpen || world.id == PremiumAccess.FREE_WORLD_ID) AdventureGreen else LockedGrey
                )
            }
            Text(
                when {
                    premiumLocked -> "🔐"
                    canOpen -> "›"
                    else -> "🔒"
                },
                fontSize = 24.sp
            )
        }
    }
}

@Composable
private fun ActionGrid(onDaily: () -> Unit, onPassport: () -> Unit, onPractice: () -> Unit, onRewards: () -> Unit, onParent: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallAction("🎁", stringResource(R.string.daily_mission), onDaily, Modifier.weight(1f))
            SmallAction("🛂", stringResource(R.string.traveler_passport), onPassport, Modifier.weight(1f))
            SmallAction("📚", stringResource(R.string.practice), onPractice, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallAction("🏆", stringResource(R.string.rewards), onRewards, Modifier.weight(1f))
            SmallAction("👨‍👩‍👦", stringResource(R.string.parent), onParent, Modifier.weight(1f))
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun SmallAction(icon: String, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalButton(onClick = onClick, modifier = modifier.heightIn(min = 60.dp), shape = RoundedCornerShape(16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 20.sp)
            Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
        }
    }
}
