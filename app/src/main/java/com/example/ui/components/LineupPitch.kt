package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Lineup
import com.example.data.model.Player
import com.example.ui.theme.*

@Composable
fun LineupPitchView(
    lineup: Lineup,
    homeTeamName: String,
    awayTeamName: String,
    onPlayerSelected: (Player) -> Unit = {}
) {
    var selectedTeamTab by remember { mutableIntStateOf(0) } // 0 = Home, 1 = Away
    val currentXI = if (selectedTeamTab == 0) lineup.homeStartingXI else lineup.awayStartingXI
    val formation = if (selectedTeamTab == 0) lineup.homeFormation else lineup.awayFormation
    val coach = if (selectedTeamTab == 0) lineup.homeCoach else lineup.awayCoach
    val bench = if (selectedTeamTab == 0) lineup.homeBench else lineup.awayBench

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        // Team Selector Tabs
        TabRow(
            selectedTabIndex = selectedTeamTab,
            containerColor = StadiumCardBg,
            contentColor = PitchGreen,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, StadiumCardBorder, RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedTeamTab == 0,
                onClick = { selectedTeamTab = 0 },
                text = {
                    Text(
                        text = "$homeTeamName ($formation)",
                        fontWeight = if (selectedTeamTab == 0) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            )
            Tab(
                selected = selectedTeamTab == 1,
                onClick = { selectedTeamTab = 1 },
                text = {
                    Text(
                        text = "$awayTeamName (${lineup.awayFormation})",
                        fontWeight = if (selectedTeamTab == 1) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Visual Soccer Pitch
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF14532D)) // Rich pitch turf green
                .border(1.5.dp, Color(0xFF22C55E).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
        ) {
            // Pitch markings canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val lineColor = Color.White.copy(alpha = 0.25f)
                val strokeWidth = 2.dp.toPx()

                // Border
                drawRect(
                    color = lineColor,
                    style = Stroke(width = strokeWidth)
                )

                // Halfway line
                drawLine(
                    color = lineColor,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = strokeWidth
                )

                // Center circle
                drawCircle(
                    color = lineColor,
                    radius = size.width * 0.18f,
                    center = Offset(size.width / 2, size.height / 2),
                    style = Stroke(width = strokeWidth)
                )

                // Penalty Box (Top & Bottom)
                val boxWidth = size.width * 0.55f
                val boxHeight = size.height * 0.16f
                drawRect(
                    color = lineColor,
                    topLeft = Offset((size.width - boxWidth) / 2, 0f),
                    size = Size(boxWidth, boxHeight),
                    style = Stroke(width = strokeWidth)
                )
                drawRect(
                    color = lineColor,
                    topLeft = Offset((size.width - boxWidth) / 2, size.height - boxHeight),
                    size = Size(boxWidth, boxHeight),
                    style = Stroke(width = strokeWidth)
                )
            }

            // Player Formation Layout on Pitch
            val gk = currentXI.filter { it.position == "GK" }
            val df = currentXI.filter { it.position == "DF" }
            val mf = currentXI.filter { it.position == "MF" }
            val fw = currentXI.filter { it.position == "FW" }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Forwards (Top)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    fw.forEach { player ->
                        PitchPlayerNode(player = player, onClick = { onPlayerSelected(player) })
                    }
                }

                // Midfielders (Center-High)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    mf.forEach { player ->
                        PitchPlayerNode(player = player, onClick = { onPlayerSelected(player) })
                    }
                }

                // Defenders (Center-Low)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    df.forEach { player ->
                        PitchPlayerNode(player = player, onClick = { onPlayerSelected(player) })
                    }
                }

                // Goalkeeper (Bottom)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    gk.forEach { player ->
                        PitchPlayerNode(player = player, isGk = true, onClick = { onPlayerSelected(player) })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Manager / Head Coach
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(StadiumCardBg)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("👔", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Head Coach: ",
                color = TextMuted,
                fontSize = 13.sp
            )
            Text(
                text = coach,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Starting XI Detailed List with Ratings & Stats
        Text(
            text = "Starting XI & Player Ratings",
            style = MaterialTheme.typography.titleSmall,
            color = GoldAccent,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        currentXI.forEach { player ->
            PlayerListRow(player = player, onClick = { onPlayerSelected(player) })
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bench Substitutes
        Text(
            text = "Substitutes / Bench",
            style = MaterialTheme.typography.titleSmall,
            color = TextMuted,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        bench.forEach { player ->
            PlayerListRow(player = player, isSub = true, onClick = { onPlayerSelected(player) })
        }
    }
}

@Composable
fun PitchPlayerNode(
    player: Player,
    isGk: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isGk) GoldAccent else Color(0xFF1E293B))
                .border(1.5.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${player.number}",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isGk) Color.Black else Color.White
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = player.name.split(" ").lastOrNull() ?: player.name,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
        // Rating pill
        Text(
            text = "${player.rating}",
            color = PitchGreen,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PlayerListRow(
    player: Player,
    isSub: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSub) Color(0xFF111827) else StadiumCardBg)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF374151)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${player.number}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = player.name,
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${player.position} • Acc: ${player.passesAccuracy}%",
                    color = TextDim,
                    fontSize = 11.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (player.goals > 0) {
                Text("⚽ ${player.goals}", fontSize = 11.sp)
                Spacer(modifier = Modifier.width(6.dp))
            }
            if (player.yellowCards > 0) {
                Box(
                    modifier = Modifier
                        .size(7.dp, 10.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(YellowCardColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            if (player.redCards > 0) {
                Box(
                    modifier = Modifier
                        .size(7.dp, 10.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(RedCardColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${player.rating}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (player.rating >= 8.0) PitchGreen else GoldAccent
                )
            }
        }
    }
}
