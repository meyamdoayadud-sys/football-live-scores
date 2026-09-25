package com.example

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.data.model.Match
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.util.StringsHelper
import com.example.viewmodel.MainViewModel

enum class NavigationScreen {
    HOME,
    REWARDS,
    HISTORY,
    SETTINGS,
    MATCH_DETAIL
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppContent(viewModel = viewModel, activity = this)
                }
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel, activity: Activity) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(NavigationScreen.HOME) }
    var detailMatchId by remember { mutableStateOf<String?>(null) }
    val language by viewModel.language.collectAsState()
    val liveGoalAlert by viewModel.liveGoalAlert.collectAsState()

    // Handle back button on detail screen
    BackHandler(enabled = currentScreen == NavigationScreen.MATCH_DETAIL) {
        currentScreen = NavigationScreen.HOME
        detailMatchId = null
    }

    // Trigger haptic vibration on goal alert
    LaunchedEffect(liveGoalAlert) {
        liveGoalAlert?.let {
            try {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 150, 80, 200), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(300)
                }
            } catch (_: Exception) {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (currentScreen != NavigationScreen.MATCH_DETAIL) {
                    NavigationBar(
                        containerColor = StadiumSurface,
                        contentColor = TextWhite,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .testTag("main_bottom_nav")
                    ) {
                        // 1. Matches
                        NavigationBarItem(
                            selected = currentScreen == NavigationScreen.HOME,
                            onClick = { currentScreen = NavigationScreen.HOME },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.SportsSoccer,
                                    contentDescription = "Matches",
                                    tint = if (currentScreen == NavigationScreen.HOME) PitchGreen else TextDim
                                )
                            },
                            label = {
                                Text(
                                    text = if (language == StringsHelper.Language.BN) "লাইভ স্কোর" else "Matches",
                                    fontSize = 11.sp,
                                    fontWeight = if (currentScreen == NavigationScreen.HOME) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = PitchGreenDark
                            )
                        )

                        // 2. Rewards & bKash
                        NavigationBarItem(
                            selected = currentScreen == NavigationScreen.REWARDS,
                            onClick = { currentScreen = NavigationScreen.REWARDS },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Rewards",
                                    tint = if (currentScreen == NavigationScreen.REWARDS) BkashPink else TextDim
                                )
                            },
                            label = {
                                Text(
                                    text = if (language == StringsHelper.Language.BN) "বিকাশ ও কয়েন" else "Rewards",
                                    fontSize = 11.sp,
                                    fontWeight = if (currentScreen == NavigationScreen.REWARDS) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = BkashDarkPink.copy(alpha = 0.5f)
                            )
                        )

                        // 3. History
                        NavigationBarItem(
                            selected = currentScreen == NavigationScreen.HISTORY,
                            onClick = { currentScreen = NavigationScreen.HISTORY },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "History",
                                    tint = if (currentScreen == NavigationScreen.HISTORY) GoldAccent else TextDim
                                )
                            },
                            label = {
                                Text(
                                    text = if (language == StringsHelper.Language.BN) "হিস্ট্রি" else "History",
                                    fontSize = 11.sp,
                                    fontWeight = if (currentScreen == NavigationScreen.HISTORY) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0xFF332A15)
                            )
                        )

                        // 4. Settings
                        NavigationBarItem(
                            selected = currentScreen == NavigationScreen.SETTINGS,
                            onClick = { currentScreen = NavigationScreen.SETTINGS },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = if (currentScreen == NavigationScreen.SETTINGS) PitchGreen else TextDim
                                )
                            },
                            label = {
                                Text(
                                    text = if (language == StringsHelper.Language.BN) "সেটিংস" else "Settings",
                                    fontSize = 11.sp,
                                    fontWeight = if (currentScreen == NavigationScreen.SETTINGS) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = PitchGreenDark
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    NavigationScreen.HOME -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToMatchDetail = { match ->
                                viewModel.selectMatch(match)
                                detailMatchId = match.id
                                // Show interstitial ad gracefully
                                viewModel.adMobManager.showInterstitialAd(activity)
                                currentScreen = NavigationScreen.MATCH_DETAIL
                            },
                            onNavigateToRewards = {
                                currentScreen = NavigationScreen.REWARDS
                            }
                        )
                    }
                    NavigationScreen.REWARDS -> {
                        RewardsScreen(
                            viewModel = viewModel,
                            onNavigateBack = { currentScreen = NavigationScreen.HOME }
                        )
                    }
                    NavigationScreen.HISTORY -> {
                        HistoryScreen(
                            viewModel = viewModel,
                            onNavigateToMatchDetail = { match ->
                                viewModel.selectMatch(match)
                                detailMatchId = match.id
                                currentScreen = NavigationScreen.MATCH_DETAIL
                            }
                        )
                    }
                    NavigationScreen.SETTINGS -> {
                        SettingsScreen(viewModel = viewModel)
                    }
                    NavigationScreen.MATCH_DETAIL -> {
                        detailMatchId?.let { id ->
                            MatchDetailScreen(
                                matchId = id,
                                viewModel = viewModel,
                                onBackClick = {
                                    currentScreen = NavigationScreen.HOME
                                    detailMatchId = null
                                }
                            )
                        }
                    }
                }
            }
        }

        // Live Floating Goal Alert Toast
        liveGoalAlert?.let { alert ->
            LaunchedEffect(alert) {
                kotlinx.coroutines.delay(4000L)
                viewModel.clearLiveAlert()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 10.dp, start = 14.dp, end = 14.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, PitchGreen, RoundedCornerShape(12.dp))
                        .clickable { viewModel.clearLiveAlert() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F291E))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚽", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "GOAL! ${alert.homeTeam} vs ${alert.awayTeam}",
                                    fontWeight = FontWeight.Black,
                                    color = PitchGreen,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${alert.scorer} (${alert.minute}') • New Score: ${alert.newScore}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.clearLiveAlert() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }
                }
            }
        }
    }
}
