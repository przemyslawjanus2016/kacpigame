package pl.kacperikapi.mathadventure.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.kacperikapi.mathadventure.R
import pl.kacperikapi.mathadventure.data.GameProgress
import pl.kacperikapi.mathadventure.ui.theme.*

/** Compact, single-line resource bar that stays readable on narrow phones. */
@Composable
fun ResourceBar(
    progress: GameProgress,
    hearts: Int = 3,
    onSettingsClick: () -> Unit = {}
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        val compact = maxWidth < 410.dp
        val spacing = if (compact) 4.dp else 8.dp
        val counterMin = if (compact) 58.dp else 72.dp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CounterPill("🪙", progress.coins, compact, Modifier.widthIn(min = counterMin))
            CounterPill("⭐", progress.stars, compact, Modifier.widthIn(min = counterMin))
            CounterPill("❤️", hearts, compact, Modifier.widthIn(min = counterMin))
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(if (compact) 42.dp else 46.dp)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings),
                    tint = Ink
                )
            }
        }
    }
}

@Composable
private fun CounterPill(icon: String, value: Int, compact: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.heightIn(min = if (compact) 42.dp else 46.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.94f),
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 7.dp else 10.dp,
                vertical = if (compact) 6.dp else 7.dp
            ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = if (compact) 17.sp else 19.sp, maxLines = 1, softWrap = false)
            Spacer(Modifier.width(if (compact) 3.dp else 5.dp))
            Text(
                text = value.toString(),
                fontSize = if (compact) 16.sp else 18.sp,
                fontWeight = FontWeight.Black,
                color = Ink,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
fun GameTitle(compact: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = WoodBrown,
            shadowElevation = 5.dp
        ) {
            Text(
                text = stringResource(R.string.brand_full_name),
                modifier = Modifier.padding(horizontal = if (compact) 18.dp else 28.dp, vertical = 8.dp),
                color = Cream,
                fontSize = if (compact) 20.sp else 27.sp,
                fontWeight = FontWeight.Black
            )
        }
        Surface(
            modifier = Modifier.offset(y = (-4).dp),
            shape = RoundedCornerShape(50),
            color = Parchment,
            shadowElevation = 2.dp
        ) {
            Text(
                stringResource(R.string.game_subtitle_v2),
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 5.dp),
                color = WoodBrown,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PrimaryGameButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 58.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrightGreen),
        shape = RoundedCornerShape(24.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
    ) {
        Text("▶", fontSize = 24.sp)
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 24.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun ParchmentCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Parchment)
            .border(2.dp, WoodBrown.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
fun TinyLegendDot(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(text, style = MaterialTheme.typography.labelSmall)
    }
}
