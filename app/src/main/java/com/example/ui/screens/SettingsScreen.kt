package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobConfig
import com.example.ui.components.BannerAdComposable
import com.example.ui.theme.*
import com.example.util.StringsHelper
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val isAutoRefresh by viewModel.isAutoRefresh.collectAsState()
    val refreshInterval by viewModel.refreshInterval.collectAsState()

    var soundVibrationEnabled by remember { mutableStateOf(true) }
    var showApkDownloadInfo by remember { mutableStateOf(false) }
    var testFeedback by remember { mutableStateOf<String?>(null) }

    val activity = context as? Activity

    fun vibratePhone() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(150)
            }
        } catch (_: Exception) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = PitchGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == StringsHelper.Language.BN) "অ্যাপ সেটিংস" else "App Settings",
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            fontSize = 17.sp
                        )
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
            // Language Selection Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = StadiumCardBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (language == StringsHelper.Language.BN) "ভাষা নির্বাচন (Language)" else "App Language",
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Bengali
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (language == StringsHelper.Language.BN) PitchGreenDark else Color(0xFF1E293B))
                                    .border(
                                        1.dp,
                                        if (language == StringsHelper.Language.BN) PitchGreen else StadiumCardBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setLanguage(StringsHelper.Language.BN) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🇧🇩", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "বাংলা (Bengali)",
                                        fontWeight = FontWeight.Bold,
                                        color = if (language == StringsHelper.Language.BN) PitchGreen else TextWhite,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            // English
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (language == StringsHelper.Language.EN) PitchGreenDark else Color(0xFF1E293B))
                                    .border(
                                        1.dp,
                                        if (language == StringsHelper.Language.EN) PitchGreen else StadiumCardBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setLanguage(StringsHelper.Language.EN) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🇬🇧", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "English",
                                        fontWeight = FontWeight.Bold,
                                        color = if (language == StringsHelper.Language.EN) PitchGreen else TextWhite,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Real-Time Auto Refresh Configuration
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = StadiumCardBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (language == StringsHelper.Language.BN) "প্রতিদিন অটো আপডেট" else "Real-time Auto Refresh",
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Auto-update scores, corners, and cards",
                                    fontSize = 12.sp,
                                    color = TextDim
                                )
                            }
                            Switch(
                                checked = isAutoRefresh,
                                onCheckedChange = { checked ->
                                    viewModel.setAutoRefresh(checked, refreshInterval)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = PitchGreen
                                )
                            )
                        }

                        if (isAutoRefresh) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Refresh Frequency:",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(15, 30, 60).forEach { seconds ->
                                    val isSelected = refreshInterval == seconds
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setAutoRefresh(true, seconds) },
                                        label = { Text("${seconds}s") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PitchGreen,
                                            selectedLabelColor = Color.Black
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Notifications & Vibration
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
                        Column {
                            Text(
                                text = if (language == StringsHelper.Language.BN) "গোল ও লাল কার্ড ভাইব্রেশন" else "Goal & Red Card Vibration",
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Haptic pulse whenever a goal is scored",
                                fontSize = 12.sp,
                                color = TextDim
                            )
                        }

                        Switch(
                            checked = soundVibrationEnabled,
                            onCheckedChange = {
                                soundVibrationEnabled = it
                                if (it) vibratePhone()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = PitchGreen
                            )
                        )
                    }
                }
            }

            // Google AdMob Configuration Inspector Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = StadiumCardBg)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📢", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Google AdMob Configuration",
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        AdUnitDetailRow(label = "App ID", id = AdMobConfig.APP_ID)
                        AdUnitDetailRow(label = "1. Banner Ad", id = AdMobConfig.BANNER_AD_ID)
                        AdUnitDetailRow(label = "2. Interstitial Ad", id = AdMobConfig.INTERSTITIAL_AD_ID)
                        AdUnitDetailRow(label = "3. Rewarded Interstitial Ad", id = AdMobConfig.REWARDED_INTERSTITIAL_AD_ID)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (activity != null) {
                                        viewModel.adMobManager.showInterstitialAd(activity) {
                                            testFeedback = "Interstitial Ad dismissed"
                                        }
                                    } else {
                                        testFeedback = "Testing on device/container"
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Test Interstitial", fontSize = 11.sp, color = TextWhite)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.rewardAdCoins(50)
                                    testFeedback = "Rewarded Ad Test: +50 Coins Added!"
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Test Rewarded", fontSize = 11.sp, color = GoldAccent)
                            }
                        }

                        testFeedback?.let { msg ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(msg, color = PitchGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Download APK Action Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showApkDownloadInfo = true },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F291E))
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PitchGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (language == StringsHelper.Language.BN) "ডাউনলোড APK (Download APK)" else "Download APK",
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Export and install Football Live Score on device",
                                    color = PitchGreenLight,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                    }
                }
            }

            // About & Version Info
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(2.dp, PitchGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_app_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Football Live Score v1.0.0",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time match scores • Lineups • BTTS • bKash Cashout",
                        color = TextDim,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    if (showApkDownloadInfo) {
        AlertDialog(
            onDismissRequest = { showApkDownloadInfo = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = PitchGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download APK")
                }
            },
            text = {
                Column {
                    Text(
                        text = "To download and install the Football Live Score APK on your Android device:",
                        fontSize = 13.sp,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("1. In Google AI Studio Build, tap the Settings menu (top right).", fontSize = 12.sp, color = TextMuted)
                    Text("2. Click 'Export APK' or 'Download Project ZIP'.", fontSize = 12.sp, color = TextMuted)
                    Text("3. Install the APK directly on any Android smartphone or tablet.", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Package: com.aistudio.footballscore.kxmvzq", fontSize = 11.sp, color = GoldAccent)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showApkDownloadInfo = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PitchGreen)
                ) {
                    Text("Got it", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
fun AdUnitDetailRow(label: String, id: String) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = label, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
        Text(
            text = id,
            fontSize = 11.sp,
            color = TextWhite,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}
