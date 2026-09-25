package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Match
import com.example.data.model.MatchStatus
import com.example.ui.theme.*
import com.example.util.StringsHelper

@Composable
fun MatchCard(
    match: Match,
    isFavorite: Boolean,
    language: StringsHelper.Language,
    onMatchClick: (Match) -> Unit,
    onFavoriteToggle: (Match) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (match.status == MatchStatus.LIVE) 1.5.dp else 1.dp,
                color = if (match.status == MatchStatus.LIVE) PitchGreen.copy(alpha = 0.6f) else StadiumCardBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onMatchClick(match) }
            .testTag("match_card_${match.id}"),
        colors = CardDefaults.cardColors(containerColor = StadiumCardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: League name & status pill & favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = match.leagueIcon,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = match.league,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Status pill
                when (match.status) {
                    MatchStatus.LIVE -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(PitchGreenDark)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(PitchGreen.copy(alpha = pulseAlpha))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${match.minute}'",
                                color = PitchGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    MatchStatus.FINISHED -> {
                        Text(
                            text = StringsHelper.finished(language),
                            color = TextDim,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF263238))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    MatchStatus.UPCOMING -> {
                        Text(
                            text = "${match.date} • ${match.time}",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF2E2A1C))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    MatchStatus.HALFTIME -> {
                        Text(
                            text = "HT",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF332A15))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { onFavoriteToggle(match) },
                    modifier = Modifier.size(32.dp).testTag("fav_button_${match.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) GoldAccent else TextDim,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scoreboard Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Home Team
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = match.homeFlag, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = match.homeTeam,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (match.stats.redCardsHome > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp, 10.dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(RedCardColor)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${match.stats.redCardsHome}",
                                    fontSize = 10.sp,
                                    color = RedCardColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Score Box
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF111827))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (match.status == MatchStatus.UPCOMING) {
                        Text(
                            text = "VS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${match.homeScore}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (match.homeScore > match.awayScore) PitchGreen else TextWhite
                            )
                            Text(
                                text = " - ",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDim
                            )
                            Text(
                                text = "${match.awayScore}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (match.awayScore > match.homeScore) PitchGreen else TextWhite
                            )
                        }
                    }
                }

                // Away Team
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = match.awayTeam,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (match.stats.redCardsAway > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${match.stats.redCardsAway}",
                                    fontSize = 10.sp,
                                    color = RedCardColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Box(
                                    modifier = Modifier
                                        .size(7.dp, 10.dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(RedCardColor)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = match.awayFlag, fontSize = 22.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Football Stats Footer Chips: Corners, Yellow/Red cards, BTTS %, Double Chance
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF111827).copy(alpha = 0.7f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Corners (কর্নার)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🚩", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${match.stats.cornersHome}-${match.stats.cornersAway} ${StringsHelper.corners(language)}",
                        fontSize = 11.sp,
                        color = CornerColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Yellow Cards (হলুদ কার্ড)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp, 10.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(YellowCardColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${match.stats.yellowCardsHome}-${match.stats.yellowCardsAway}",
                        fontSize = 11.sp,
                        color = YellowCardColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // BTTS %
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BTTS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDim
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${match.bttsPercentage}%",
                        fontSize = 11.sp,
                        color = if (match.bttsPercentage >= 70) PitchGreen else GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Double Chance 1X
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "1X: ${match.doubleChance.homeOrDrawProb}%",
                        fontSize = 11.sp,
                        color = TextWhite,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
