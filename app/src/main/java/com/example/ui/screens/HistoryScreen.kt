package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Match
import com.example.data.model.MatchStatus
import com.example.ui.components.BannerAdComposable
import com.example.ui.components.MatchCard
import com.example.ui.theme.*
import com.example.util.StringsHelper
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onNavigateToMatchDetail: (Match) -> Unit
) {
    val allMatches by viewModel.matchRepository.matches.collectAsState()
    val language by viewModel.language.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val favSet = remember(favorites) { favorites.map { it.matchId }.toSet() }

    var selectedLeagueFilter by remember { mutableStateOf("all") }
    var historySearchQuery by remember { mutableStateOf("") }

    // Filter finished or recent matches
    val finishedMatches = remember(allMatches, selectedLeagueFilter, historySearchQuery) {
        allMatches.filter { match ->
            val isFinishedOrPast = match.status == MatchStatus.FINISHED || match.date == "Finished" || match.status == MatchStatus.LIVE
            val leagueMatches = if (selectedLeagueFilter == "all") true else {
                val league = viewModel.allLeagues.find { it.id == selectedLeagueFilter }
                league != null && match.league.equals(league.nameEn, ignoreCase = true)
            }
            val queryMatches = if (historySearchQuery.isBlank()) true else {
                match.homeTeam.contains(historySearchQuery, ignoreCase = true) ||
                        match.awayTeam.contains(historySearchQuery, ignoreCase = true) ||
                        match.league.contains(historySearchQuery, ignoreCase = true)
            }
            isFinishedOrPast && leagueMatches && queryMatches
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == StringsHelper.Language.BN) "ম্যাচ রেজাল্ট ও হিস্ট্রি" else "Match Results History",
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Search Input
            item {
                OutlinedTextField(
                    value = historySearchQuery,
                    onValueChange = { historySearchQuery = it },
                    placeholder = { Text("Search club, team, or competition...", fontSize = 13.sp, color = TextDim) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                    trailingIcon = {
                        if (historySearchQuery.isNotEmpty()) {
                            IconButton(onClick = { historySearchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = TextDim)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = StadiumCardBg,
                        unfocusedContainerColor = StadiumCardBg,
                        focusedBorderColor = PitchGreen,
                        unfocusedBorderColor = StadiumCardBorder
                    ),
                    singleLine = true
                )
            }

            // Competition Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.allLeagues) { league ->
                        val isSel = selectedLeagueFilter == league.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSel) PitchGreen else StadiumCardBg)
                                .border(1.dp, if (isSel) PitchGreen else StadiumCardBorder, RoundedCornerShape(20.dp))
                                .clickable { selectedLeagueFilter = league.id }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (language == StringsHelper.Language.BN) league.nameBn else league.nameEn,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.Black else TextWhite
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Past Results & Scores (${finishedMatches.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
            }

            if (finishedMatches.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📅", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No match history found", color = TextDim, fontSize = 14.sp)
                    }
                }
            } else {
                items(finishedMatches, key = { it.id }) { match ->
                    MatchCard(
                        match = match,
                        isFavorite = favSet.contains(match.id),
                        language = language,
                        onMatchClick = { onNavigateToMatchDetail(match) },
                        onFavoriteToggle = { viewModel.toggleFavorite(match) }
                    )
                }
            }
        }
    }
}
