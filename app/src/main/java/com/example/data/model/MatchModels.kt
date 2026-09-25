package com.example.data.model

enum class MatchStatus {
    LIVE,
    UPCOMING,
    FINISHED,
    HALFTIME
}

enum class EventType {
    GOAL,
    PENALTY_GOAL,
    YELLOW_CARD,
    RED_CARD,
    CORNER,
    SUBSTITUTION,
    VAR_DECISION
}

data class MatchEvent(
    val minute: Int,
    val extraMinute: Int = 0,
    val type: EventType,
    val isHomeTeam: Boolean,
    val playerName: String,
    val assistPlayer: String? = null,
    val description: String = ""
)

data class Player(
    val id: String,
    val name: String,
    val number: Int,
    val position: String, // GK, DF, MF, FW
    val rating: Double = 7.0,
    val goals: Int = 0,
    val assists: Int = 0,
    val yellowCards: Int = 0,
    val redCards: Int = 0,
    val shotsOnTarget: Int = 0,
    val passesAccuracy: Int = 85,
    val tackles: Int = 2,
    val saves: Int = 0
)

data class Lineup(
    val homeFormation: String, // e.g. "4-3-3"
    val awayFormation: String, // e.g. "4-2-3-1"
    val homeCoach: String,
    val awayCoach: String,
    val homeStartingXI: List<Player>,
    val awayStartingXI: List<Player>,
    val homeBench: List<Player>,
    val awayBench: List<Player>
)

data class MatchStats(
    val possessionHome: Int,
    val possessionAway: Int,
    val shotsHome: Int,
    val shotsAway: Int,
    val shotsOnTargetHome: Int,
    val shotsOnTargetAway: Int,
    val cornersHome: Int,
    val cornersAway: Int,
    val foulsHome: Int,
    val foulsAway: Int,
    val yellowCardsHome: Int,
    val yellowCardsAway: Int,
    val redCardsHome: Int,
    val redCardsAway: Int,
    val offsidesHome: Int,
    val offsidesAway: Int,
    val savesHome: Int,
    val savesAway: Int
)

data class DoubleChance(
    val homeOrDraw: Double,      // 1X odds
    val homeOrDrawProb: Int,     // 1X %
    val homeOrAway: Double,      // 12 odds
    val homeOrAwayProb: Int,     // 12 %
    val drawOrAway: Double,      // X2 odds
    val drawOrAwayProb: Int      // X2 %
)

data class Match(
    val id: String,
    val league: String,
    val leagueIcon: String = "⚽",
    val homeTeam: String,
    val homeShort: String,
    val homeFlag: String,
    val awayTeam: String,
    val awayShort: String,
    val awayFlag: String,
    val homeScore: Int,
    val awayScore: Int,
    val status: MatchStatus,
    val minute: Int,
    val date: String,
    val time: String,
    val venue: String,
    val stats: MatchStats,
    val bttsPercentage: Int, // Both teams to score probability %
    val doubleChance: DoubleChance,
    val lineup: Lineup,
    val events: List<MatchEvent>,
    val isFavorite: Boolean = false
)

data class LeagueItem(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val icon: String,
    val country: String
)
