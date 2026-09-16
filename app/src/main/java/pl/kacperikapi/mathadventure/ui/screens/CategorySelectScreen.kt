package pl.kacperikapi.mathadventure.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.kacperikapi.mathadventure.R
import pl.kacperikapi.mathadventure.data.AppLanguages
import pl.kacperikapi.mathadventure.data.GameContent
import pl.kacperikapi.mathadventure.data.LearningCategory
import pl.kacperikapi.mathadventure.ui.theme.*

@Composable
fun CategorySelectScreen(onCategory: (LearningCategory) -> Unit, onBack: () -> Unit) {
    val language = AppLanguages.normalize(LocalConfiguration.current.locales[0].language)
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SkyBlue.copy(.35f), Cream, Parchment))).statusBarsPadding()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth()) { TextButton(onClick = onBack) { Text("← ${stringResource(R.string.back)}") } }
            Text(stringResource(R.string.choose_category), fontSize = 29.sp, fontWeight = FontWeight.Black, color = AdventureGreen)
            Text(stringResource(R.string.choose_category_desc), color = WoodBrown)
            Spacer(Modifier.height(12.dp))
            Image(
                painter = painterResource(R.drawable.app_icon),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(150.dp)
            )
            Spacer(Modifier.height(8.dp))
            GameContent.categories.forEach { category ->
                val icon = if (category.id == LearningCategory.POLISH) AppLanguages.profile(language).countryFlag else category.icon
                Surface(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 720.dp).padding(vertical = 5.dp).clickable { onCategory(category.id) },
                    shape = RoundedCornerShape(18.dp),
                    color = androidx.compose.ui.graphics.Color.White.copy(.94f),
                    shadowElevation = 3.dp
                ) {
                    Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(icon, fontSize = 31.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(category.nameRes), fontSize = 18.sp, fontWeight = FontWeight.Black, color = Ink)
                            Text(stringResource(category.shortDescriptionRes), color = WoodBrown, fontSize = 13.sp)
                        }
                        Text("›", fontSize = 26.sp, color = AdventureGreen)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
