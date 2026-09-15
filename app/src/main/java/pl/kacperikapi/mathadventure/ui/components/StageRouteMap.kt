package pl.kacperikapi.mathadventure.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.kacperikapi.mathadventure.data.GameProgress
import pl.kacperikapi.mathadventure.data.Stage
import pl.kacperikapi.mathadventure.data.WorldDefinition
import pl.kacperikapi.mathadventure.ui.theme.*

@Composable
fun StageRouteMap(
    world: WorldDefinition,
    progress: GameProgress,
    selectedStage: Int,
    onSelect: (Int) -> Unit,
    onLocked: (Stage) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 620.dp
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(28.dp))
    ) {
        when {
            world.heroArtRes != null -> {
                Image(
                    painter = painterResource(world.heroArtRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Box(Modifier.matchParentSize().background(Color.White.copy(alpha = .035f)))
            }
            world.thumbnailRes != null -> {
                Image(
                    painter = painterResource(world.thumbnailRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    Modifier.matchParentSize().background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(.34f), Color(0xFFD9F0C0).copy(.62f), Parchment.copy(.86f))
                        )
                    )
                )
            }
            else -> {
                Box(
                    Modifier.matchParentSize().background(
                        Brush.verticalGradient(listOf(SkyBlue.copy(.8f), Color(0xFFD9F0C0), Parchment))
                    )
                )
            }
        }

        Canvas(Modifier.matchParentSize()) {
            val path = Path()
            world.stages.forEachIndexed { index, stage ->
                val p = Offset(stage.x * size.width, stage.y * size.height)
                if (index == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            drawPath(path, Color.White.copy(.82f), style = Stroke(15f, cap = StrokeCap.Round))
            drawPath(path, WoodBrown.copy(.55f), style = Stroke(4f, cap = StrokeCap.Round))
        }

        val nodeSize = if (maxWidth >= 700.dp) 72.dp else 58.dp
        world.stages.forEach { stage ->
            val unlocked = progress.isStageUnlocked(stage)
            val x = maxWidth * stage.x - nodeSize / 2
            val y = maxHeight * stage.y - nodeSize / 2
            StageNode(
                stage = stage,
                unlocked = unlocked,
                completed = progress.isStageCompleted(stage),
                stars = progress.stageStars(stage),
                selected = stage.number == selectedStage,
                onClick = { if (unlocked) onSelect(stage.number) else onLocked(stage) },
                modifier = Modifier.offset(x, y).size(nodeSize)
            )
        }
    }
}

@Composable
private fun StageNode(
    stage: Stage,
    unlocked: Boolean,
    completed: Boolean,
    stars: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when {
        !unlocked -> LockedGrey
        selected -> SkyBlue
        completed -> BrightGreen
        else -> AdventureGreen
    }
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = CircleShape,
        color = color,
        shadowElevation = if (selected) 10.dp else 5.dp,
        border = androidx.compose.foundation.BorderStroke(if (selected) 5.dp else 3.dp, StarYellow)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (!unlocked) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text(stage.number.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (completed) Text("★".repeat(stars.coerceIn(1, 3)), color = StarYellow, fontSize = 9.sp)
                    Text(stage.number.toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 19.sp)
                }
            }
        }
    }
}
