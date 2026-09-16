package pl.kacperikapi.mathadventure.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import pl.kacperikapi.mathadventure.R
import pl.kacperikapi.mathadventure.billing.BillingIssue
import pl.kacperikapi.mathadventure.billing.PremiumBillingState
import pl.kacperikapi.mathadventure.ui.theme.*

@Composable
fun PremiumUnlockScreen(
    state: PremiumBillingState,
    onBuy: () -> Unit,
    onRestore: () -> Unit,
    onBack: () -> Unit
) {
    var showParentGate by remember { mutableStateOf(false) }
    var parentVerified by remember { mutableStateOf(false) }

    if (showParentGate && !parentVerified) {
        ParentGateDialog(
            onVerified = {
                parentVerified = true
                showParentGate = false
            },
            onDismiss = { showParentGate = false }
        )
    }

    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyBlue.copy(.35f), Cream, Parchment)))
            .statusBarsPadding()
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth()) {
                TextButton(onClick = onBack) { Text("← ${stringResource(R.string.back)}") }
            }
            Text("🔐", fontSize = 54.sp)
            Text(
                stringResource(R.string.premium_unlock_title),
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = AdventureGreen,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.premium_unlock_description),
                color = Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 720.dp)
            )
            Spacer(Modifier.height(18.dp))

            Surface(
                modifier = Modifier.fillMaxWidth().widthIn(max = 720.dp),
                shape = RoundedCornerShape(22.dp),
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = .94f),
                shadowElevation = 5.dp
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PremiumFeature("🗺️", stringResource(R.string.premium_feature_worlds))
                    PremiumFeature("🚫", stringResource(R.string.premium_feature_no_ads))
                    PremiumFeature("🌍", stringResource(R.string.premium_feature_languages))

                    HorizontalDivider(color = WoodBrown.copy(alpha = .15f))

                    if (state.premiumUnlocked) {
                        Surface(shape = RoundedCornerShape(16.dp), color = BrightGreen.copy(alpha = .16f)) {
                            Text(
                                "✅ ${stringResource(R.string.premium_owned)}",
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                color = AdventureGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        BillingStatus(state)

                        if (!parentVerified) {
                            Button(
                                onClick = { showParentGate = true },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AdventureGreen)
                            ) {
                                Text("👨‍👩‍👦 ${stringResource(R.string.premium_ask_parent)}", fontWeight = FontWeight.Black)
                            }
                        } else {
                            Text(
                                "✅ ${stringResource(R.string.premium_parent_check_title)}",
                                color = AdventureGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = onBuy,
                                enabled = state.productAvailable && !state.purchasePending,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ActionOrange)
                            ) {
                                Text(
                                    state.priceText?.let { stringResource(R.string.premium_buy_price, it) }
                                        ?: stringResource(R.string.premium_buy),
                                    fontWeight = FontWeight.Black
                                )
                            }
                            OutlinedButton(
                                onClick = onRestore,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("↻ ${stringResource(R.string.premium_restore)}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(
                        stringResource(R.string.premium_purchase_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = WoodBrown,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PremiumFeature(icon: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 26.sp)
        Spacer(Modifier.width(12.dp))
        Text(text, modifier = Modifier.weight(1f), color = Ink, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BillingStatus(state: PremiumBillingState) {
    val message = when {
        state.purchasePending -> stringResource(R.string.premium_pending)
        state.issue == BillingIssue.PLAY_UNAVAILABLE -> stringResource(R.string.premium_play_unavailable)
        state.issue == BillingIssue.PRODUCT_NOT_CONFIGURED -> stringResource(R.string.premium_product_missing)
        state.issue == BillingIssue.PURCHASE_CANCELLED -> stringResource(R.string.premium_purchase_cancelled)
        state.issue == BillingIssue.PURCHASE_ERROR -> stringResource(R.string.premium_purchase_error)
        else -> null
    }
    if (message != null) {
        Surface(shape = RoundedCornerShape(14.dp), color = StarYellow.copy(alpha = .18f)) {
            Text(message, modifier = Modifier.fillMaxWidth().padding(12.dp), color = Ink)
        }
    }
}

@Composable
private fun ParentGateDialog(onVerified: () -> Unit, onDismiss: () -> Unit) {
    val challenge = remember {
        val a = Random.nextInt(12, 20)
        val b = Random.nextInt(3, 7)
        val c = Random.nextInt(4, 12)
        Triple(a, b, c)
    }
    val expected = challenge.first * challenge.second - challenge.third
    var answer by remember { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.premium_parent_check_title), fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.premium_parent_check_message, challenge.first, challenge.second, challenge.third))
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it.filter(Char::isDigit).take(4); wrong = false },
                    singleLine = true,
                    label = { Text(stringResource(R.string.premium_parent_answer_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = wrong
                )
                if (wrong) {
                    Text(stringResource(R.string.premium_parent_wrong), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (answer.toIntOrNull() == expected) onVerified() else wrong = true
            }) {
                Text(stringResource(R.string.premium_continue_purchase))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
