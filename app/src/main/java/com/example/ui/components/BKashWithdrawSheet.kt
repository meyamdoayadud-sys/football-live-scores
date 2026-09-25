package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.StringsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BKashWithdrawSheet(
    userCoins: Int,
    language: StringsHelper.Language,
    onDismiss: () -> Unit,
    onSubmitWithdrawal: (bkashNumber: String, accountType: String, amountBdt: Double, coinsDeducted: Int) -> Boolean
) {
    val presetAmounts = listOf(
        Pair(50.0, 1000),
        Pair(100.0, 2000),
        Pair(200.0, 4000),
        Pair(500.0, 10000)
    )

    var selectedPresetIndex by remember { mutableIntStateOf(0) }
    var bkashNumber by remember { mutableStateOf("") }
    var selectedAccountType by remember { mutableStateOf("Personal") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successTxnId by remember { mutableStateOf<String?>(null) }

    val currentAmount = presetAmounts[selectedPresetIndex].first
    val requiredCoins = presetAmounts[selectedPresetIndex].second

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131A2A),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // bKash Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BkashPink),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "bK",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (language == StringsHelper.Language.BN) "বিকাশ টাকা উইথড্র" else "bKash Cashout",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Instant Mobile Money Transfer",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDim
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Balance Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (language == StringsHelper.Language.BN) "আপনার বর্তমান ব্যালেন্স" else "Your Current Balance",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$userCoins Coins",
                        color = GoldAccent,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PitchGreenDark)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "≈ ${(userCoins * 0.05).toInt()} ৳ BDT",
                        color = PitchGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Presets Selector
            Text(
                text = if (language == StringsHelper.Language.BN) "উইথড্রর পরিমাণ নির্বাচন করুন" else "Select Withdrawal Amount",
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetAmounts.forEachIndexed { index, pair ->
                    val isSelected = selectedPresetIndex == index
                    val hasEnough = userCoins >= pair.second

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) BkashPink else Color(0xFF1E293B))
                            .border(
                                1.dp,
                                if (isSelected) Color.White else StadiumCardBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                selectedPresetIndex = index
                                errorMessage = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${pair.first.toInt()} ৳",
                                color = if (isSelected) Color.White else TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "${pair.second} C",
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextDim,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Account Type Selector (Personal / Agent / Merchant)
            Text(
                text = if (language == StringsHelper.Language.BN) "অ্যাকাউন্টের ধরন" else "Account Type",
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Personal", "Agent", "Merchant").forEach { type ->
                    val isSel = selectedAccountType == type
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedAccountType = type },
                        label = { Text(type, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BkashDarkPink,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = TextMuted
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // bKash Number Input Field
            OutlinedTextField(
                value = bkashNumber,
                onValueChange = { input ->
                    if (input.length <= 11 && input.all { it.isDigit() }) {
                        bkashNumber = input
                        errorMessage = null
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bkash_phone_input"),
                label = { Text(if (language == StringsHelper.Language.BN) "বিকাশ মোবাইল নম্বর (১১ সংখ্যা)" else "bKash Number (11 digits)") },
                placeholder = { Text("017XXXXXXXX") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null, tint = BkashPink)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BkashPink,
                    unfocusedBorderColor = StadiumCardBorder,
                    focusedLabelColor = BkashPink,
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedContainerColor = Color(0xFF1E293B)
                )
            )

            // Error Message
            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = error,
                    color = RedCardColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = {
                    if (bkashNumber.length != 11 || !bkashNumber.startsWith("01")) {
                        errorMessage = if (language == StringsHelper.Language.BN)
                            "সঠিক ১১ ডিজিটের বিকাশ নম্বর দিন (যেমন 017...)"
                        else
                            "Enter a valid 11-digit bKash number starting with 01"
                        return@Button
                    }
                    if (userCoins < requiredCoins) {
                        errorMessage = if (language == StringsHelper.Language.BN)
                            "পর্যাপ্ত কয়েন নেই! প্রয়োজন $requiredCoins কয়েন।"
                        else
                            "Insufficient coins! Need $requiredCoins coins."
                        return@Button
                    }

                    val success = onSubmitWithdrawal(bkashNumber, selectedAccountType, currentAmount, requiredCoins)
                    if (success) {
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_bkash_cashout_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = BkashPink),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == StringsHelper.Language.BN)
                        "বিকাশে উইথড্র করুন (${currentAmount.toInt()} ৳)"
                    else
                        "Withdraw to bKash (${currentAmount.toInt()} ৳)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
