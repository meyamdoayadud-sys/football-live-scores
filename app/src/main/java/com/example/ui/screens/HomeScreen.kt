package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Match
import com.example.ui.components.BannerAdComposable
import com.example.ui.components.MatchCard
import com.example.ui.theme.*
import com.example.util.StringsHelper
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.MatchFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToMatchDetail: (Match) -> Unit,
    onNavigateToRewards: () -> Unit
) {
    val matches by viewModel.filteredMatches.collectAsState()
    val selectedLeague by viewModel.selectedLeague.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val wallet by viewModel.wallet.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val language by viewModel.language.collectAsState()
    val isAutoRefresh by viewModel.isAutoRefresh.collectAsState()

    val favSet = remember(favorites) { favorites.map { it.matchId }.toSet() }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StadiumSurface)
            ) {
                // Top App Bar with Branding, Coin Chip, Language Toggle & Manual Refresh
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F172A))
                                .border(1.5.dp, PitchGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_logo),
                                contentDescription = "Football Live Score Logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (language == StringsHelper.Language.BN) "ফুটবল লাইভ স্কোর" else "Football Live Score",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isAutoRefresh) PitchGreen else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAutoRefresh) StringsHelper.autoUpdate(language) else "Manual",
                                    fontSize = 10.sp,
                                    color = if (isAutoRefresh) PitchGreenLight else TextDim
                                )
                            }
                        }
                    }

                    // Coins Balance Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .clickable { onNavigateToRewards() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("home_coins_pill")
                    ) {
                        Text("🪙", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${wallet.coins}",
                            color = GoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "বিকাশ",
                            color = BkashPink,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Language Switcher
                    IconButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier.size(36.dp).testTag("lang_toggle_btn")
                    ) {
                        Text(
                            text = if (language == StringsHelper.Language.BN) "EN" else "বাং",
                            color = PitchGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Manual Refresh Button
                    IconButton(
                        onClick = { viewModel.manualRefresh() },
                        modifier = Modifier.size(36.dp).testTag("manual_refresh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            if (language == StringsHelper.Language.BN) "ক্লাব, দল বা লিগ খুঁজুন..." else "Search team, club, or league...",
                            fontSize = 13.sp,
                            color = TextDim
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = TextDim)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .height(50.dp)
                        .testTag("match_search_bar"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = StadiumCardBg,
                        unfocusedContainerColor = StadiumCardBg,
                        focusedBorderColor = PitchGreen,
                        unfocusedBorderColor = StadiumCardBorder
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Match Status Filter Chips (All, Live, Today, Finished, Favorites)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filters = listOf(
                        MatchFilter.ALL to if (language == StringsHelper.Language.BN) "সব" else "All",
                        MatchFilter.LIVE to "🔴 " + StringsHelper.live(language),
                        MatchFilter.TODAY to if (language == StringsHelper.Language.BN) "আজ" else "Today",
                        MatchFilter.FINISHED to StringsHelper.finished(language),
                        MatchFilter.FAVORITES to "⭐ " + if (language == StringsHelper.Language.BN) "ফেভারিট" else "Favorites"
                    )

                    filters.forEach { (filter, label) ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PitchGreen else StadiumCardBg)
                                .border(
                                    1.dp,
                                    if (isSelected) PitchGreen else StadiumCardBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setFilter(filter) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("filter_chip_${filter.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else TextWhite
                            )
                        }
                    }
                }

                // Horizontal League Filter Chips (All 13 Competitions)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.allLeagues) { league ->
                        val isSelected = selectedLeague == league.id
                        val title = if (language == StringsHelper.Language.BN) league.nameBn else league.nameEn

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFF064E3B) else Color(0xFF1E293B))
                                .border(
                                    1.dp,
                                    if (isSelected) PitchGreen else StadiumCardBorder,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { viewModel.selectLeague(league.id) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("league_chip_${league.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(league.icon, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PitchGreenLight else TextWhite
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Google AdMob Banner Ad View
            BannerAdComposable()
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Hero Stadium Card / Promotion
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = StadiumCardBg)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_stadium_banner),
                            contentDescription = "Stadium Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.85f),
                                            Color.Black.copy(alpha = 0.35f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BkashPink)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("bKash", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == StringsHelper.Language.BN) "লাইভ স্কোর দেখুন ও কয়েন জিতুন" else "Live Football & Win Coins",
                                    color = GoldAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (language == StringsHelper.Language.BN)
                                    "গোল, কর্নার, কার্ড, BTTS% এবং লাইনআপ রিয়েল-টাইম আপডেট"
                                else
                                    "Real-time Goals, Corners, Cards, Lineups & BTTS%",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            // Matches List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (language == StringsHelper.Language.BN)
                            "ম্যাচসমূহ (${matches.size})"
                        else
                            "Matches (${matches.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🚩 = কর্নার • 🟨 = হলুদ কার্ড • 🟥 = লাল কার্ড",
                            fontSize = 10.sp,
                            color = TextDim
                        )
                    }
                }
            }

            // Matches Cards
            if (matches.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⚽", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (language == StringsHelper.Language.BN)
                                "কোনো ম্যাচ পাওয়া যায়নি"
                            else
                                "No matches found",
                            color = TextMuted,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                items(matches, key = { it.id }) { match ->
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
