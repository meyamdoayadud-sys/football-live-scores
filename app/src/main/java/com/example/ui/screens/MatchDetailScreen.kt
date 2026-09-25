package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EventType
import com.example.data.model.Match
import com.example.data.model.MatchStatus
import com.example.ui.components.ComparativeStatBar
import com.example.ui.components.LineupPitchView
import com.example.ui.theme.*
import com.example.util.StringsHelper
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    matchId: String,
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val matches by viewModel.matchRepository.matches.collectAsState()
    val match = matches.find { it.id == matchId } ?: viewModel.selectedMatch.collectAsState().value
    val language by viewModel.language.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val wallet by viewModel.wallet.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val isFav = remember(favorites, match) {
        match != null && favorites.any { it.matchId == match.id }
    }

    var predictionDialogSelection by remember { mutableStateOf<Pair<String, Double>?>(null) }
    var predictionWagerCoins by remember { mutableIntStateOf(100) }
    var predictionFeedback by remember { mutableStateOf<String?>(null) }

    if (match == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Match details not available", color = TextWhite)
        }
        return
    }

    val tabTitles = listOf(
        StringsHelper.get(language, "Overview", "সামারি"),
        StringsHelper.get(language, "Stats", "পরিসংখ্যান"),
        StringsHelper.get(language, "Timeline", "ঘটনা"),
        StringsHelper.get(language, "Lineup", "লাইনআপ"),
        StringsHelper.get(language, "BTTS & Odds", "বিটিটিএস ও অডস"),
        StringsHelper.get(language, "Players", "খেলোয়াড়")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = match.league,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = match.venue,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDim,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(match) }) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) GoldAccent else TextDim
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StadiumSurface)
            )
        },
        containerColor = StadiumDarkBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Live Scoreboard Hero
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, PitchGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = StadiumCardBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Status badge
                        when (match.status) {
                            MatchStatus.LIVE -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(PitchGreenDark)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(PitchGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${match.minute}' LIVE",
                                        color = PitchGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            MatchStatus.FINISHED -> {
                                Text(
                                    text = "Full Time (FT)",
                                    color = TextDim,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFF263238))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            MatchStatus.UPCOMING -> {
                                Text(
                                    text = "${match.date} • ${match.time}",
                                    color = GoldAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFF332A15))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            MatchStatus.HALFTIME -> {
                                Text(
                                    text = "Half Time (HT)",
                                    color = GoldAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFF332A15))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Teams & Score
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Home Team
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(match.homeFlag, fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = match.homeTeam,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2
                                )
                                if (match.stats.redCardsHome > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp, 12.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(RedCardColor)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${match.stats.redCardsHome} লাল কার্ড", fontSize = 10.sp, color = RedCardColor)
                                    }
                                }
                            }

                            // Score Display
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (match.status == MatchStatus.UPCOMING) {
                                    Text(
                                        text = "VS",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextMuted
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${match.homeScore}",
                                            fontSize = 38.sp,
                                            fontWeight = FontWeight.Black,
                                            color = TextWhite
                                        )
                                        Text(
                                            text = " - ",
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDim
                                        )
                                        Text(
                                            text = "${match.awayScore}",
                                            fontSize = 38.sp,
                                            fontWeight = FontWeight.Black,
                                            color = TextWhite
                                        )
                                    }
                                }
                            }

                            // Away Team
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(match.awayFlag, fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = match.awayTeam,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2
                                )
                                if (match.stats.redCardsAway > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${match.stats.redCardsAway} লাল কার্ড", fontSize = 10.sp, color = RedCardColor)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp, 12.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(RedCardColor)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Match Pill Stats: Corners, Yellow Cards, BTTS %
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF111827))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🚩 ${match.stats.cornersHome}-${match.stats.cornersAway}", color = CornerColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(StringsHelper.corners(language), color = TextDim, fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🟨 ${match.stats.yellowCardsHome}-${match.stats.yellowCardsAway}", color = YellowCardColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(StringsHelper.yellowCard(language), color = TextDim, fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${match.bttsPercentage}%", color = PitchGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("BTTS", color = TextDim, fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("1X: ${match.doubleChance.homeOrDrawProb}%", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(StringsHelper.doubleChance(language), color = TextDim, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // Scrollable Tab Row
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = StadiumSurface,
                    contentColor = PitchGreen,
                    edgePadding = 14.dp,
                    divider = { HorizontalDivider(color = StadiumCardBorder) }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // TAB 0: OVERVIEW
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            // Key Events Summary
                            Text(
                                text = "Key Match Highlights",
                                style = MaterialTheme.typography.titleSmall,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (match.events.isEmpty()) {
                                Text("No match events yet.", color = TextDim, fontSize = 13.sp)
                            } else {
                                match.events.take(4).forEach { event ->
                                    EventItemRow(event = event)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Key Comparative Stats
                            Text(
                                text = "Match Statistics Summary",
                                style = MaterialTheme.typography.titleSmall,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            ComparativeStatBar(
                                title = StringsHelper.possession(language),
                                homeValue = match.stats.possessionHome,
                                awayValue = match.stats.possessionAway,
                                isPercentage = true
                            )
                            ComparativeStatBar(
                                title = StringsHelper.shots(language),
                                homeValue = match.stats.shotsHome,
                                awayValue = match.stats.shotsAway
                            )
                            ComparativeStatBar(
                                title = StringsHelper.shotsOnTarget(language),
                                homeValue = match.stats.shotsOnTargetHome,
                                awayValue = match.stats.shotsOnTargetAway
                            )
                            ComparativeStatBar(
                                title = StringsHelper.corners(language),
                                homeValue = match.stats.cornersHome,
                                awayValue = match.stats.cornersAway
                            )
                        }
                    }
                }

                1 -> {
                    // TAB 1: DETAILED STATS
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            ComparativeStatBar(title = StringsHelper.possession(language), homeValue = match.stats.possessionHome, awayValue = match.stats.possessionAway, isPercentage = true)
                            ComparativeStatBar(title = StringsHelper.shots(language), homeValue = match.stats.shotsHome, awayValue = match.stats.shotsAway)
                            ComparativeStatBar(title = StringsHelper.shotsOnTarget(language), homeValue = match.stats.shotsOnTargetHome, awayValue = match.stats.shotsOnTargetAway)
                            ComparativeStatBar(title = StringsHelper.corners(language), homeValue = match.stats.cornersHome, awayValue = match.stats.cornersAway)
                            ComparativeStatBar(title = StringsHelper.fouls(language), homeValue = match.stats.foulsHome, awayValue = match.stats.foulsAway)
                            ComparativeStatBar(title = StringsHelper.yellowCard(language), homeValue = match.stats.yellowCardsHome, awayValue = match.stats.yellowCardsAway, homeColor = YellowCardColor, awayColor = YellowCardColor)
                            ComparativeStatBar(title = StringsHelper.redCard(language), homeValue = match.stats.redCardsHome, awayValue = match.stats.redCardsAway, homeColor = RedCardColor, awayColor = RedCardColor)
                            ComparativeStatBar(title = StringsHelper.offsides(language), homeValue = match.stats.offsidesHome, awayValue = match.stats.offsidesAway)
                            ComparativeStatBar(title = StringsHelper.saves(language), homeValue = match.stats.savesHome, awayValue = match.stats.savesAway)
                        }
                    }
                }

                2 -> {
                    // TAB 2: EVENTS TIMELINE
                    if (match.events.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No events recorded for this match yet", color = TextMuted)
                            }
                        }
                    } else {
                        items(match.events) { event ->
                            EventItemRow(event = event, modifier = Modifier.padding(horizontal = 14.dp))
                        }
                    }
                }

                3 -> {
                    // TAB 3: LINEUPS (Tactical Pitch & Squad)
                    item {
                        LineupPitchView(
                            lineup = match.lineup,
                            homeTeamName = match.homeTeam,
                            awayTeamName = match.awayTeam
                        )
                    }
                }

                4 -> {
                    // TAB 4: BTTS & DOUBLE CHANCE PREDICTIONS
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            // BTTS Section
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = StadiumCardBg)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "BTTS (Both Teams To Score) %",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Probability based on recent goals scored & conceded by both clubs",
                                        fontSize = 12.sp,
                                        color = TextDim
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${match.bttsPercentage}%",
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Black,
                                            color = PitchGreen
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            LinearProgressIndicator(
                                                progress = { match.bttsPercentage / 100f },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(10.dp)
                                                    .clip(RoundedCornerShape(5.dp)),
                                                color = PitchGreen,
                                                trackColor = Color(0xFF1F2937)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (match.bttsPercentage >= 70) "High Goal Probability (হ্যাঁ - গোল হবে)" else "Moderate Probability",
                                                fontSize = 11.sp,
                                                color = PitchGreenLight
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Double Chance Odds Section
                            Text(
                                text = "Double Chance (ডাবল চান্স অডস ও সম্ভাবনা)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1X
                                DoubleChanceCard(
                                    title = "1X",
                                    desc = "${match.homeShort} or Draw",
                                    prob = match.doubleChance.homeOrDrawProb,
                                    odds = match.doubleChance.homeOrDraw,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        predictionDialogSelection = "Double Chance 1X" to match.doubleChance.homeOrDraw
                                    }
                                )

                                // 12
                                DoubleChanceCard(
                                    title = "12",
                                    desc = "${match.homeShort} or ${match.awayShort}",
                                    prob = match.doubleChance.homeOrAwayProb,
                                    odds = match.doubleChance.homeOrAway,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        predictionDialogSelection = "Double Chance 12" to match.doubleChance.homeOrAway
                                    }
                                )

                                // X2
                                DoubleChanceCard(
                                    title = "X2",
                                    desc = "Draw or ${match.awayShort}",
                                    prob = match.doubleChance.drawOrAwayProb,
                                    odds = match.doubleChance.drawOrAway,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        predictionDialogSelection = "Double Chance X2" to match.doubleChance.drawOrAway
                                    }
                                )
                            }

                            predictionFeedback?.let { feedback ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = feedback,
                                    color = PitchGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                5 -> {
                    // TAB 5: KEY PLAYERS & RATINGS
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "${match.homeTeam} Key Stars",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            match.lineup.homeStartingXI.take(6).forEach { player ->
                                PlayerStatCard(player = player)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "${match.awayTeam} Key Stars",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            match.lineup.awayStartingXI.take(6).forEach { player ->
                                PlayerStatCard(player = player)
                            }
                        }
                    }
                }
            }
        }
    }

    // Prediction Modal Dialog
    predictionDialogSelection?.let { (pick, odds) ->
        AlertDialog(
            onDismissRequest = { predictionDialogSelection = null },
            title = {
                Text("Match Prediction Challenge", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Selected: $pick (Odds: $odds)")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Wager Coins from your Wallet:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(50, 100, 200).forEach { wager ->
                            FilterChip(
                                selected = predictionWagerCoins == wager,
                                onClick = { predictionWagerCoins = wager },
                                label = { Text("$wager Coins") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Potential Win: ${(predictionWagerCoins * odds).toInt()} Coins!",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitPrediction(match, pick, odds, predictionWagerCoins) { success ->
                            if (success) {
                                predictionFeedback = "Prediction placed successfully! Track in Rewards History."
                            } else {
                                predictionFeedback = "Insufficient coins balance to place prediction."
                            }
                            predictionDialogSelection = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PitchGreen)
                ) {
                    Text("Confirm Pick", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { predictionDialogSelection = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
fun EventItemRow(
    event: com.example.data.model.MatchEvent,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(StadiumCardBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Minute pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF111827))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "${event.minute}'",
                color = PitchGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Event Icon
        when (event.type) {
            EventType.GOAL -> Text("⚽", fontSize = 16.sp)
            EventType.PENALTY_GOAL -> Text("⚽ (P)", fontSize = 14.sp)
            EventType.YELLOW_CARD -> Box(
                modifier = Modifier
                    .size(10.dp, 14.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(YellowCardColor)
            )
            EventType.RED_CARD -> Box(
                modifier = Modifier
                    .size(10.dp, 14.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(RedCardColor)
            )
            EventType.CORNER -> Text("🚩", fontSize = 14.sp)
            EventType.SUBSTITUTION -> Text("🔄", fontSize = 14.sp)
            EventType.VAR_DECISION -> Text("📺", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Player & Description
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.playerName,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            if (!event.assistPlayer.isNullOrBlank()) {
                Text(
                    text = "Assist: ${event.assistPlayer}",
                    color = TextDim,
                    fontSize = 11.sp
                )
            }
            if (event.description.isNotBlank()) {
                Text(
                    text = event.description,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun DoubleChanceCard(
    title: String,
    desc: String,
    prob: Int,
    odds: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, StadiumCardBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = StadiumCardBg)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PitchGreen
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$prob%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Text(
                text = "Odds: $odds",
                fontSize = 11.sp,
                color = GoldAccent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                fontSize = 10.sp,
                color = TextDim,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PlayerStatCard(player: com.example.data.model.Player) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(StadiumCardBg)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Text("${player.number}", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(player.name, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "${player.position} • Tackles: ${player.tackles} • Shots: ${player.shotsOnTarget}",
                    color = TextDim,
                    fontSize = 11.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0F172A))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "${player.rating} ★",
                color = if (player.rating >= 8.0) PitchGreen else GoldAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}
