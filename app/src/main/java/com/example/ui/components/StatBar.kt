package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CornerColor
import com.example.ui.theme.PitchGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun ComparativeStatBar(
    title: String,
    homeValue: Int,
    awayValue: Int,
    isPercentage: Boolean = false,
    homeColor: Color = PitchGreen,
    awayColor: Color = CornerColor,
    modifier: Modifier = Modifier
) {
    val total = if (isPercentage) 100 else (homeValue + awayValue).coerceAtLeast(1)
    val homeRatio = (homeValue.toFloat() / total).coerceIn(0.05f, 0.95f)
    val awayRatio = 1f - homeRatio

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isPercentage) "$homeValue%" else "$homeValue",
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (isPercentage) "$awayValue%" else "$awayValue",
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1F2937))
        ) {
            Box(
                modifier = Modifier
                    .weight(homeRatio)
                    .fillMaxHeight()
                    .background(homeColor)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Box(
                modifier = Modifier
                    .weight(awayRatio)
                    .fillMaxHeight()
                    .background(awayColor)
            )
        }
    }
}
