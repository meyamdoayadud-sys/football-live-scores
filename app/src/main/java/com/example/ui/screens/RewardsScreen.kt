package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WithdrawalEntity
import com.example.ui.components.BKashWithdrawSheet
import com.example.ui.components.BannerAdComposable
import com.example.ui.components.RewardedAdSimulatorDialog
import com.example.ui.theme.*
import com.example.util.StringsHelper
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val wallet by viewModel.wallet.collectAsState()
    val withdrawals by viewModel.withdrawals.collectAsState()
    val language by viewModel.language.collectAsState()

    var showWithdrawSheet by remember { mutableStateOf(false) }
    var showRewardedAdDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val activity = context as? Activity

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(BkashPink),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("bK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == StringsHelper.Language.BN) "বিকাশ ক্যাশআউট ও রিওয়ার্ডস" else "bKash Cashout & Rewards",
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            fontSize = 17.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showWithdrawSheet = true }) {
                        Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = "Cashout", tint = BkashPink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StadiumSurface)
            )
        },
        bottomBar = {
            BannerAdComposable()
        },
        containerColor = StadiumDarkBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // bKash Balance Card with Neon Gradient
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, BkashPink.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = StadiumCardBg)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF2E0854),
                                        Color(0xFF0F172A),
                                        Color(0xFF880E4F).copy(alpha = 0.7f)
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(BkashPink)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("bKash", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (language == StringsHelper.Language.BN) "টাকা উইথড্র ওয়ালেট" else "bKash Cashout Wallet",
                                        color = TextWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Text(
                                    text = "1000 C = 50 ৳",
                                    color = GoldAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "${wallet.coins}",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "≈ ${(wallet.coins * 0.05).toInt()} ৳ BDT",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PitchGreen
                                    )
                                }

                                Button(
                                    onClick = { showWithdrawSheet = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = BkashPink),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("open_withdraw_sheet_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (language == StringsHelper.Language.BN) "উইথড্র বিকাশ" else "Withdraw",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total Withdrawn: ${wallet.totalWithdrawnBdt.toInt()} ৳",
                                    color = TextDim,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Minimum: 50 ৳ (1,000 Coins)",
                                    color = TextDim,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Coin Earning Methods Header
            item {
                Text(
                    text = if (language == StringsHelper.Language.BN) "কয়েন ইনকাম করুন" else "Earn Free Coins",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
            }

            // Task 1: Daily Check-in Card
            item {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val isClaimedToday = wallet.lastCheckInDate == todayStr

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = StadiumCardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF064E3B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎁", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (language == StringsHelper.Language.BN) "প্রতিদিনের বোনাস" else "Daily Bonus Check-in",
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "+100 Coins every day",
                                    color = PitchGreenLight,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.claimDailyBonus { success ->
                                    snackbarMessage = if (success)
                                        "🎉 +100 Coins claimed successfully!"
                                    else
                                        "Already claimed today. Come back tomorrow!"
                                }
                            },
                            enabled = !isClaimedToday,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PitchGreen,
                                disabledContainerColor = Color(0xFF374151)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("claim_daily_bonus_btn")
                        ) {
                            Text(
                                text = if (isClaimedToday) "Claimed" else "Claim",
                                color = if (isClaimedToday) TextDim else Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Task 2: Watch Rewarded Interstitial Ad Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = StadiumCardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF312E81)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📺", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (language == StringsHelper.Language.BN) "ভিডিও বিজ্ঞাপন দেখুন" else "Watch Sponsored Ad",
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Earn +50 Coins immediately",
                                    color = GoldAccent,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (activity != null) {
                                    viewModel.adMobManager.showRewardedInterstitialAd(
                                        activity = activity,
                                        onUserEarnedReward = { amount, _ ->
                                            viewModel.rewardAdCoins(50)
                                            snackbarMessage = "🎉 +50 Coins earned from rewarded ad!"
                                        },
                                        onAdClosedOrFailed = {
                                            // Fallback interactive simulator dialog
                                            showRewardedAdDialog = true
                                        }
                                    )
                                } else {
                                    showRewardedAdDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("watch_ad_btn")
                        ) {
                            Text(
                                text = "Watch (+50)",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Withdrawal History Header ("history সব থাকবে")
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (language == StringsHelper.Language.BN)
                            "বিকাশ উইথড্র হিস্ট্রি (${withdrawals.size})"
                        else
                            "bKash Withdrawal History (${withdrawals.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            }

            // Withdrawal History Items
            if (withdrawals.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = StadiumCardBg)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("💸", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (language == StringsHelper.Language.BN)
                                    "কোনো উইথড্র হিস্ট্রি নেই"
                                else
                                    "No withdrawals yet",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (language == StringsHelper.Language.BN)
                                    "১,০০০ কয়েন জমিয়ে ৫০ টাকা বিকাশে উইথড্র করুন!"
                                else
                                    "Collect 1,000 coins to withdraw 50 ৳ to bKash!",
                                color = TextDim,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(withdrawals, key = { it.id }) { txn ->
                    WithdrawalHistoryItem(txn = txn)
                }
            }
        }
    }

    // Bottom Sheet for bKash Withdrawal
    if (showWithdrawSheet) {
        BKashWithdrawSheet(
            userCoins = wallet.coins,
            language = language,
            onDismiss = { showWithdrawSheet = false },
            onSubmitWithdrawal = { num, type, bdt, coins ->
                viewModel.requestBkashWithdrawal(num, type, bdt, coins) { success, txnId ->
                    if (success) {
                        snackbarMessage = "✅ Withdrawal request for $bdt ৳ submitted ($txnId)!"
                    } else {
                        snackbarMessage = "❌ Withdrawal failed. Please try again."
                    }
                }
                true
            }
        )
    }

    // Rewarded Ad Simulator Dialog
    if (showRewardedAdDialog) {
        RewardedAdSimulatorDialog(
            rewardCoins = 50,
            onRewardEarned = { amount ->
                viewModel.rewardAdCoins(amount)
                snackbarMessage = "🎉 +$amount Coins added to your wallet!"
            },
            onDismiss = { showRewardedAdDialog = false }
        )
    }

    // Toast/Snackbar notification
    snackbarMessage?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(3000L)
            snackbarMessage = null
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 70.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Snackbar(
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(msg, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun WithdrawalHistoryItem(txn: WithdrawalEntity) {
    val dateStr = remember(txn.timestamp) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(txn.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, StadiumCardBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = StadiumCardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BkashDarkPink),
                    contentAlignment = Alignment.Center
                ) {
                    Text("bK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${txn.bkashNumber} (${txn.accountType})",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${txn.transactionId} • $dateStr",
                        color = TextDim,
                        fontSize = 11.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${txn.amountBdt.toInt()} ৳",
                    color = PitchGreen,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (txn.status) {
                                "Approved", "Completed" -> PitchGreenDark
                                else -> Color(0xFF332A15)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = txn.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (txn.status) {
                            "Approved", "Completed" -> PitchGreen
                            else -> GoldAccent
                        }
                    )
                }
            }
        }
    }
}
