package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class LiveGoalAlert(
    val matchId: String,
    val homeTeam: String,
    val awayTeam: String,
    val scorer: String,
    val minute: Int,
    val newScore: String,
    val isRedCard: Boolean = false
)

class MatchRepository {
    private val scope = CoroutineScope(Dispatchers.Default)

    val allLeagues = listOf(
        LeagueItem("all", "All Leagues", "সব লিগ", "🌐", "Global"),
        LeagueItem("fifa_world_cup", "FIFA World Cup", "ফিফা বিশ্বকাপ", "🏆", "World"),
        LeagueItem("ucl", "UEFA Champions League", "উয়েফা চ্যাম্পিয়ন্স লিগ", "⭐", "Europe"),
        LeagueItem("epl", "Premier League", "প্রিমিয়ার লিগ", "🦁", "England"),
        LeagueItem("euro", "UEFA European Championship", "উয়েফা ইউরো", "🇪🇺", "Europe"),
        LeagueItem("laliga", "La Liga", "লা লিগা", "🇪🇸", "Spain"),
        LeagueItem("club_world_cup", "FIFA Club World Cup", "ফিফা ক্লাব বিশ্বকাপ", "🌐", "World"),
        LeagueItem("copa_america", "Copa América", "কোপা আমেরিকা", "🌎", "South America"),
        LeagueItem("serie_a", "Serie A", "সিরি আ", "🇮🇹", "Italy"),
        LeagueItem("bundesliga", "Bundesliga", "বুন্দেসলিগা", "🇩🇪", "Germany"),
        LeagueItem("ligue_1", "Ligue 1", "লিগ ১", "🇫🇷", "France"),
        LeagueItem("europa_league", "UEFA Europa League", "উয়েফা ইউরোপা লিগ", "🥈", "Europe"),
        LeagueItem("copa_libertadores", "Copa Libertadores", "কোপা লিবার্তাদোরেস", "🏆", "South America"),
        LeagueItem("nations_league", "UEFA Nations League", "উয়েফা নেশনস লিগ", "🛡️", "Europe")
    )

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

    private val _liveAlerts = MutableSharedFlow<LiveGoalAlert>(extraBufferCapacity = 5)
    val liveAlerts: SharedFlow<LiveGoalAlert> = _liveAlerts.asSharedFlow()

    private var isAutoRefreshActive = true
    private var refreshIntervalSeconds = 15

    init {
        _matches.value = generateInitialMatches()
        startLiveScoreEngine()
    }

    fun setAutoRefresh(enabled: Boolean, intervalSeconds: Int = 15) {
        isAutoRefreshActive = enabled
        refreshIntervalSeconds = intervalSeconds
    }

    fun manualRefresh() {
        simulateLiveTick(forceEvent = true)
    }

    private fun startLiveScoreEngine() {
        scope.launch {
            while (true) {
                delay(refreshIntervalSeconds * 1000L)
                if (isAutoRefreshActive) {
                    simulateLiveTick(forceEvent = false)
                }
            }
        }
    }

    private fun simulateLiveTick(forceEvent: Boolean) {
        val currentList = _matches.value.toMutableList()
        var updated = false

        for (i in currentList.indices) {
            val match = currentList[i]
            if (match.status == MatchStatus.LIVE) {
                // Advance minute
                val newMinute = if (match.minute < 90) match.minute + 1 else 90
                val randomChance = Random.nextInt(100)

                var homeScore = match.homeScore
                var awayScore = match.awayScore
                var cornersHome = match.stats.cornersHome
                var cornersAway = match.stats.cornersAway
                var yellowHome = match.stats.yellowCardsHome
                var yellowAway = match.stats.yellowCardsAway
                var redHome = match.stats.redCardsHome
                var redAway = match.stats.redCardsAway
                val events = match.events.toMutableList()

                // Chance of Corner (every few ticks)
                if (randomChance in 10..22 || (forceEvent && i == 0)) {
                    if (Random.nextBoolean()) {
                        cornersHome++
                        events.add(
                            MatchEvent(
                                minute = newMinute,
                                type = EventType.CORNER,
                                isHomeTeam = true,
                                playerName = match.lineup.homeStartingXI.filter { it.position == "FW" || it.position == "MF" }.randomOrNull()?.name ?: match.homeTeam,
                                description = "Corner awarded to ${match.homeTeam}"
                            )
                        )
                    } else {
                        cornersAway++
                        events.add(
                            MatchEvent(
                                minute = newMinute,
                                type = EventType.CORNER,
                                isHomeTeam = false,
                                playerName = match.lineup.awayStartingXI.filter { it.position == "FW" || it.position == "MF" }.randomOrNull()?.name ?: match.awayTeam,
                                description = "Corner awarded to ${match.awayTeam}"
                            )
                        )
                    }
                    updated = true
                }

                // Chance of Yellow Card
                if (randomChance in 30..35) {
                    val isHome = Random.nextBoolean()
                    if (isHome) {
                        yellowHome++
                        val p = match.lineup.homeStartingXI.filter { it.position != "GK" }.randomOrNull()?.name ?: "Player"
                        events.add(MatchEvent(newMinute, 0, EventType.YELLOW_CARD, true, p, null, "Tactical foul, yellow card shown."))
                    } else {
                        yellowAway++
                        val p = match.lineup.awayStartingXI.filter { it.position != "GK" }.randomOrNull()?.name ?: "Player"
                        events.add(MatchEvent(newMinute, 0, EventType.YELLOW_CARD, false, p, null, "Reckless challenge, yellow card shown."))
                    }
                    updated = true
                }

                // Chance of Goal (exciting live update!)
                if (randomChance in 92..95 || (forceEvent && i == 0 && randomChance > 50)) {
                    val isHomeGoal = Random.nextBoolean()
                    val scorer: String
                    val assist: String?
                    if (isHomeGoal) {
                        homeScore++
                        val attackers = match.lineup.homeStartingXI.filter { it.position == "FW" || it.position == "MF" }
                        scorer = attackers.randomOrNull()?.name ?: "${match.homeTeam} Striker"
                        assist = attackers.filter { it.name != scorer }.randomOrNull()?.name
                        events.add(
                            MatchEvent(
                                minute = newMinute,
                                type = EventType.GOAL,
                                isHomeTeam = true,
                                playerName = scorer,
                                assistPlayer = assist,
                                description = "GOAL! Magnificent strike into the top corner!"
                            )
                        )
                    } else {
                        awayScore++
                        val attackers = match.lineup.awayStartingXI.filter { it.position == "FW" || it.position == "MF" }
                        scorer = attackers.randomOrNull()?.name ?: "${match.awayTeam} Striker"
                        assist = attackers.filter { it.name != scorer }.randomOrNull()?.name
                        events.add(
                            MatchEvent(
                                minute = newMinute,
                                type = EventType.GOAL,
                                isHomeTeam = false,
                                playerName = scorer,
                                assistPlayer = assist,
                                description = "GOAL! Clinical counter-attack finish!"
                            )
                        )
                    }
                    _liveAlerts.tryEmit(
                        LiveGoalAlert(
                            matchId = match.id,
                            homeTeam = match.homeTeam,
                            awayTeam = match.awayTeam,
                            scorer = scorer,
                            minute = newMinute,
                            newScore = "$homeScore - $awayScore"
                        )
                    )
                    updated = true
                }

                val newStats = match.stats.copy(
                    cornersHome = cornersHome,
                    cornersAway = cornersAway,
                    yellowCardsHome = yellowHome,
                    yellowCardsAway = yellowAway,
                    redCardsHome = redHome,
                    redCardsAway = redAway,
                    shotsHome = match.stats.shotsHome + (if (Random.nextInt(10) > 6) 1 else 0),
                    shotsAway = match.stats.shotsAway + (if (Random.nextInt(10) > 6) 1 else 0)
                )

                currentList[i] = match.copy(
                    minute = newMinute,
                    homeScore = homeScore,
                    awayScore = awayScore,
                    stats = newStats,
                    events = events.sortedByDescending { it.minute }
                )
            }
        }

        if (updated || forceEvent) {
            _matches.value = currentList
        }
    }

    private fun generateInitialMatches(): List<Match> {
        return listOf(
            // 1. UEFA Champions League - LIVE MATCH
            createMatch(
                id = "ucl_1",
                league = "UEFA Champions League",
                leagueIcon = "⭐",
                homeTeam = "Real Madrid",
                homeShort = "RMA",
                homeFlag = "🇪🇸",
                awayTeam = "Manchester City",
                awayShort = "MCI",
                awayFlag = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
                homeScore = 2,
                awayScore = 2,
                status = MatchStatus.LIVE,
                minute = 76,
                date = "Today",
                time = "21:00",
                venue = "Santiago Bernabéu, Madrid",
                corners = 6 to 8,
                yellows = 2 to 1,
                reds = 0 to 0,
                possession = 46 to 54,
                shots = 14 to 18,
                shotsOnTarget = 6 to 7,
                btts = 78,
                doubleChance = DoubleChance(1.36, 73, 1.28, 78, 1.45, 69),
                homeFormation = "4-3-1-2",
                awayFormation = "4-3-3",
                homeCoach = "Carlo Ancelotti",
                awayCoach = "Pep Guardiola",
                homePlayers = listOf(
                    Player("rm1", "Thibaut Courtois", 1, "GK", 7.8, saves = 5),
                    Player("rm2", "Dani Carvajal", 2, "DF", 7.2, tackles = 3),
                    Player("rm3", "Éder Militão", 3, "DF", 7.4, tackles = 4),
                    Player("rm4", "Antonio Rüdiger", 22, "DF", 8.0, tackles = 5),
                    Player("rm5", "Ferland Mendy", 23, "DF", 7.1, tackles = 2),
                    Player("rm6", "Federico Valverde", 15, "MF", 8.5, goals = 1, shotsOnTarget = 2),
                    Player("rm7", "Aurélien Tchouaméni", 14, "MF", 7.6, yellowCards = 1),
                    Player("rm8", "Eduardo Camavinga", 12, "MF", 7.7, tackles = 4),
                    Player("rm9", "Jude Bellingham", 5, "MF", 8.6, assists = 1, passesAccuracy = 91),
                    Player("rm10", "Rodrygo", 11, "FW", 8.2, goals = 1, shotsOnTarget = 2),
                    Player("rm11", "Vinícius Júnior", 7, "FW", 8.8, assists = 1, shotsOnTarget = 3)
                ),
                awayPlayers = listOf(
                    Player("mc1", "Ederson", 31, "GK", 7.2, saves = 4),
                    Player("mc2", "Kyle Walker", 2, "DF", 7.5, tackles = 4),
                    Player("mc3", "Rúben Dias", 3, "DF", 7.3, tackles = 3),
                    Player("mc4", "Manuel Akanji", 25, "DF", 7.0, yellowCards = 1),
                    Player("mc5", "Josko Gvardiol", 24, "DF", 8.1, goals = 1),
                    Player("mc6", "Rodri", 16, "MF", 8.4, passesAccuracy = 94, tackles = 3),
                    Player("mc7", "Bernardo Silva", 20, "MF", 8.3, goals = 1),
                    Player("mc8", "Kevin De Bruyne", 17, "MF", 8.7, assists = 2, shotsOnTarget = 2),
                    Player("mc9", "Phil Foden", 47, "FW", 8.0, shotsOnTarget = 2),
                    Player("mc10", "Erling Haaland", 9, "FW", 7.8, shotsOnTarget = 3),
                    Player("mc11", "Jack Grealish", 10, "FW", 7.6, tackles = 2)
                ),
                events = listOf(
                    MatchEvent(71, 0, EventType.CORNER, false, "Kevin De Bruyne", null, "Corner conceded by Rüdiger"),
                    MatchEvent(66, 0, EventType.GOAL, true, "Federico Valverde", "Vinícius Júnior", "Sensational first-time volley into bottom left corner!"),
                    MatchEvent(58, 0, EventType.YELLOW_CARD, true, "Aurélien Tchouaméni", null, "Late sliding tackle on Rodri"),
                    MatchEvent(42, 0, EventType.CORNER, true, "Rodrygo", null, "Corner for Real Madrid"),
                    MatchEvent(35, 0, EventType.GOAL, false, "Josko Gvardiol", "Kevin De Bruyne", "Curling right-footed rocket outside the box!"),
                    MatchEvent(14, 0, EventType.GOAL, true, "Rodrygo", "Jude Bellingham", "Counter attack slotted past the keeper"),
                    MatchEvent(2, 0, EventType.GOAL, false, "Bernardo Silva", null, "Clever free-kick catching keeper off guard")
                )
            ),

            // 2. Premier League - LIVE MATCH
            createMatch(
                id = "epl_1",
                league = "Premier League",
                leagueIcon = "🦁",
                homeTeam = "Arsenal",
                homeShort = "ARS",
                homeFlag = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
                awayTeam = "Liverpool",
                awayShort = "LIV",
                awayFlag = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
                homeScore = 1,
                awayScore = 0,
                status = MatchStatus.LIVE,
                minute = 63,
                date = "Today",
                time = "17:30",
                venue = "Emirates Stadium, London",
                corners = 5 to 4,
                yellows = 1 to 2,
                reds = 0 to 1,
                possession = 52 to 48,
                shots = 11 to 8,
                shotsOnTarget = 5 to 2,
                btts = 65,
                doubleChance = DoubleChance(1.22, 82, 1.30, 77, 2.10, 48),
                homeFormation = "4-3-3",
                awayFormation = "4-3-3",
                homeCoach = "Mikel Arteta",
                awayCoach = "Arne Slot",
                homePlayers = listOf(
                    Player("ar1", "David Raya", 22, "GK", 7.5),
                    Player("ar2", "Ben White", 4, "DF", 7.3),
                    Player("ar3", "William Saliba", 2, "DF", 8.2, tackles = 5),
                    Player("ar4", "Gabriel Magalhães", 6, "DF", 7.9),
                    Player("ar5", "Jurriën Timber", 12, "DF", 7.4),
                    Player("ar6", "Declan Rice", 41, "MF", 8.3, passesAccuracy = 92),
                    Player("ar7", "Thomas Partey", 5, "MF", 7.5),
                    Player("ar8", "Martin Ødegaard", 8, "MF", 8.5, assists = 1),
                    Player("ar9", "Bukayo Saka", 7, "FW", 8.7, goals = 1, shotsOnTarget = 3),
                    Player("ar10", "Kai Havertz", 29, "FW", 7.6),
                    Player("ar11", "Gabriel Martinelli", 11, "FW", 7.8)
                ),
                awayPlayers = listOf(
                    Player("lv1", "Alisson Becker", 1, "GK", 7.4),
                    Player("lv2", "Trent Alexander-Arnold", 66, "DF", 7.1),
                    Player("lv3", "Ibrahima Konaté", 5, "DF", 6.8, yellowCards = 1),
                    Player("lv4", "Virgil van Dijk", 4, "DF", 7.5),
                    Player("lv5", "Andrew Robertson", 26, "DF", 6.5, redCards = 1),
                    Player("lv6", "Ryan Gravenberch", 38, "MF", 7.2),
                    Player("lv7", "Alexis Mac Allister", 10, "MF", 7.4, yellowCards = 1),
                    Player("lv8", "Dominik Szoboszlai", 8, "MF", 7.0),
                    Player("lv9", "Mohamed Salah", 11, "FW", 7.9, shotsOnTarget = 2),
                    Player("lv10", "Darwin Núñez", 9, "FW", 6.9),
                    Player("lv11", "Luis Díaz", 7, "FW", 7.3)
                ),
                events = listOf(
                    MatchEvent(55, 0, EventType.RED_CARD, false, "Andrew Robertson", null, "Denying an obvious goal-scoring opportunity!"),
                    MatchEvent(38, 0, EventType.YELLOW_CARD, false, "Alexis Mac Allister", null, "Dissent against referee decision"),
                    MatchEvent(24, 0, EventType.GOAL, true, "Bukayo Saka", "Martin Ødegaard", "Cut inside from the right wing and fired into the far corner!"),
                    MatchEvent(12, 0, EventType.CORNER, true, "Bukayo Saka", null, "Corner curled into the near post")
                )
            ),

            // 3. La Liga - LIVE MATCH
            createMatch(
                id = "laliga_1",
                league = "La Liga",
                leagueIcon = "🇪🇸",
                homeTeam = "Barcelona",
                homeShort = "BAR",
                homeFlag = "🇪🇸",
                awayTeam = "Atlético Madrid",
                awayShort = "ATM",
                awayFlag = "🇪🇸",
                homeScore = 3,
                awayScore = 1,
                status = MatchStatus.LIVE,
                minute = 82,
                date = "Today",
                time = "20:00",
                venue = "Montjuïc Olympic Stadium, Barcelona",
                corners = 7 to 3,
                yellows = 2 to 4,
                reds = 0 to 0,
                possession = 64 to 36,
                shots = 16 to 7,
                shotsOnTarget = 8 to 3,
                btts = 84,
                doubleChance = DoubleChance(1.08, 93, 1.15, 87, 3.80, 26),
                homeFormation = "4-2-3-1",
                awayFormation = "5-3-2",
                homeCoach = "Hansi Flick",
                awayCoach = "Diego Simeone",
                homePlayers = listOf(
                    Player("bar1", "Marc-André ter Stegen", 1, "GK", 7.4),
                    Player("bar2", "Jules Koundé", 23, "DF", 7.8, tackles = 3),
                    Player("bar3", "Pau Cubarsí", 2, "DF", 8.0),
                    Player("bar4", "Iñigo Martínez", 5, "DF", 7.4),
                    Player("bar5", "Alejandro Balde", 3, "DF", 7.7),
                    Player("bar6", "Marc Casadó", 17, "MF", 7.6),
                    Player("bar7", "Pedri", 8, "MF", 8.9, assists = 1, passesAccuracy = 95),
                    Player("bar8", "Lamine Yamal", 19, "FW", 9.2, goals = 1, assists = 1, shotsOnTarget = 3),
                    Player("bar9", "Dani Olmo", 20, "MF", 8.4, goals = 1),
                    Player("bar10", "Raphinha", 11, "FW", 8.6, assists = 1),
                    Player("bar11", "Robert Lewandowski", 9, "FW", 8.8, goals = 1, shotsOnTarget = 4)
                ),
                awayPlayers = listOf(
                    Player("atm1", "Jan Oblak", 13, "GK", 7.1),
                    Player("atm2", "Nahuel Molina", 16, "DF", 6.6, yellowCards = 1),
                    Player("atm3", "Robin Le Normand", 24, "DF", 6.9),
                    Player("atm4", "José María Giménez", 2, "DF", 7.0, yellowCards = 1),
                    Player("atm5", "Reinildo", 23, "DF", 6.8),
                    Player("atm6", "Rodrigo De Paul", 5, "MF", 7.3, yellowCards = 1),
                    Player("atm7", "Koke", 6, "MF", 7.0),
                    Player("atm8", "Conor Gallagher", 4, "MF", 7.2),
                    Player("atm9", "Antoine Griezmann", 7, "FW", 8.0, goals = 1),
                    Player("atm10", "Julián Álvarez", 19, "FW", 7.4),
                    Player("atm11", "Alexander Sørloth", 9, "FW", 6.7)
                ),
                events = listOf(
                    MatchEvent(79, 0, EventType.CORNER, true, "Raphinha", null, "Corner kick Barcelona"),
                    MatchEvent(68, 0, EventType.GOAL, true, "Dani Olmo", "Lamine Yamal", "Sublime pass from Yamal, tucked away neatly!"),
                    MatchEvent(51, 0, EventType.GOAL, false, "Antoine Griezmann", null, "Brilliant chip over the keeper"),
                    MatchEvent(39, 0, EventType.GOAL, true, "Robert Lewandowski", "Pedri", "Header from close range"),
                    MatchEvent(15, 0, EventType.GOAL, true, "Lamine Yamal", "Raphinha", "Sensational left-foot strike from outside the box!")
                )
            ),

            // 4. FIFA World Cup - UPCOMING
            createMatch(
                id = "wc_1",
                league = "FIFA World Cup",
                leagueIcon = "🏆",
                homeTeam = "Argentina",
                homeShort = "ARG",
                homeFlag = "🇦🇷",
                awayTeam = "France",
                awayShort = "FRA",
                awayFlag = "🇫🇷",
                homeScore = 0,
                awayScore = 0,
                status = MatchStatus.UPCOMING,
                minute = 0,
                date = "Tomorrow",
                time = "22:00",
                venue = "Lusail Iconic Stadium, Qatar",
                corners = 0 to 0,
                yellows = 0 to 0,
                reds = 0 to 0,
                possession = 50 to 50,
                shots = 0 to 0,
                shotsOnTarget = 0 to 0,
                btts = 72,
                doubleChance = DoubleChance(1.35, 74, 1.33, 75, 1.48, 68),
                homeFormation = "4-3-3",
                awayFormation = "4-2-3-1",
                homeCoach = "Lionel Scaloni",
                awayCoach = "Didier Deschamps",
                homePlayers = listOf(
                    Player("arg1", "Emiliano Martínez", 23, "GK", 8.1),
                    Player("arg2", "Nahuel Molina", 26, "DF", 7.4),
                    Player("arg3", "Cristian Romero", 13, "DF", 8.2),
                    Player("arg4", "Nicolás Otamendi", 19, "DF", 7.8),
                    Player("arg5", "Nicolás Tagliafico", 3, "DF", 7.3),
                    Player("arg6", "Rodrigo De Paul", 7, "MF", 8.0),
                    Player("arg7", "Enzo Fernández", 24, "MF", 8.2),
                    Player("arg8", "Alexis Mac Allister", 20, "MF", 8.3),
                    Player("arg9", "Lionel Messi", 10, "FW", 9.4),
                    Player("arg10", "Julián Álvarez", 9, "FW", 8.4),
                    Player("arg11", "Lautaro Martínez", 22, "FW", 8.3)
                ),
                awayPlayers = listOf(
                    Player("fra1", "Mike Maignan", 16, "GK", 8.0),
                    Player("fra2", "Jules Koundé", 5, "DF", 7.7),
                    Player("fra3", "William Saliba", 4, "DF", 8.3),
                    Player("fra4", "Dayot Upamecano", 15, "DF", 7.6),
                    Player("fra5", "Theo Hernández", 22, "DF", 8.0),
                    Player("fra6", "Aurélien Tchouaméni", 8, "MF", 8.0),
                    Player("fra7", "Eduardo Camavinga", 6, "MF", 8.1),
                    Player("fra8", "Ousmane Dembélé", 11, "FW", 8.2),
                    Player("fra9", "Antoine Griezmann", 7, "MF", 8.5),
                    Player("fra10", "Bradley Barcola", 20, "FW", 7.9),
                    Player("fra11", "Kylian Mbappé", 10, "FW", 9.3)
                ),
                events = emptyList()
            ),

            // 5. UEFA European Championship (Euro) - FINISHED
            createMatch(
                id = "euro_1",
                league = "UEFA European Championship",
                leagueIcon = "🇪🇺",
                homeTeam = "Spain",
                homeShort = "ESP",
                homeFlag = "🇪🇸",
                awayTeam = "England",
                awayShort = "ENG",
                awayFlag = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
                homeScore = 2,
                awayScore = 1,
                status = MatchStatus.FINISHED,
                minute = 90,
                date = "Finished",
                time = "FT",
                venue = "Olympiastadion, Berlin",
                corners = 10 to 2,
                yellows = 2 to 3,
                reds = 0 to 0,
                possession = 66 to 34,
                shots = 16 to 9,
                shotsOnTarget = 6 to 4,
                btts = 100,
                doubleChance = DoubleChance(1.24, 81, 1.28, 78, 1.95, 51),
                homeFormation = "4-3-3",
                awayFormation = "4-2-3-1",
                homeCoach = "Luis de la Fuente",
                awayCoach = "Gareth Southgate",
                homePlayers = listOf(
                    Player("esp1", "Unai Simón", 23, "GK", 7.5),
                    Player("esp2", "Dani Carvajal", 2, "DF", 7.7),
                    Player("esp3", "Robin Le Normand", 3, "DF", 7.4),
                    Player("esp4", "Aymeric Laporte", 14, "DF", 7.8),
                    Player("esp5", "Marc Cucurella", 24, "DF", 8.1, assists = 1),
                    Player("esp6", "Rodri", 16, "MF", 8.3),
                    Player("esp7", "Fabián Ruiz", 8, "MF", 8.0),
                    Player("esp8", "Dani Olmo", 10, "MF", 8.5),
                    Player("esp9", "Lamine Yamal", 19, "FW", 8.8, assists = 1),
                    Player("esp10", "Álvaro Morata", 7, "FW", 7.4),
                    Player("esp11", "Nico Williams", 17, "FW", 8.9, goals = 1)
                ),
                awayPlayers = listOf(
                    Player("eng1", "Jordan Pickford", 1, "GK", 7.8),
                    Player("eng2", "Kyle Walker", 2, "DF", 7.0),
                    Player("eng3", "John Stones", 5, "DF", 7.2),
                    Player("eng4", "Marc Guéhi", 6, "DF", 7.3),
                    Player("eng5", "Luke Shaw", 3, "DF", 7.1),
                    Player("eng6", "Kobbie Mainoo", 26, "MF", 7.2),
                    Player("eng7", "Declan Rice", 4, "MF", 7.6),
                    Player("eng8", "Bukayo Saka", 7, "FW", 7.7),
                    Player("eng9", "Jude Bellingham", 10, "MF", 7.9, assists = 1),
                    Player("eng10", "Phil Foden", 11, "FW", 7.3),
                    Player("eng11", "Harry Kane", 9, "FW", 7.0)
                ),
                events = listOf(
                    MatchEvent(86, 0, EventType.GOAL, true, "Mikel Oyarzabal", "Marc Cucurella", "Late winner sliding into the six yard box!"),
                    MatchEvent(73, 0, EventType.GOAL, false, "Cole Palmer", "Jude Bellingham", "Stunning equalizer guided into bottom corner"),
                    MatchEvent(47, 0, EventType.GOAL, true, "Nico Williams", "Lamine Yamal", "Clinical finish across Pickford")
                )
            ),

            // 6. UEFA Nations League - LIVE MATCH
            createMatch(
                id = "unl_1",
                league = "UEFA Nations League",
                leagueIcon = "🛡️",
                homeTeam = "Portugal",
                homeShort = "POR",
                homeFlag = "🇵🇹",
                awayTeam = "Croatia",
                awayShort = "CRO",
                awayFlag = "🇭🇷",
                homeScore = 2,
                awayScore = 1,
                status = MatchStatus.LIVE,
                minute = 68,
                date = "Today",
                time = "19:45",
                venue = "Estádio da Luz, Lisbon",
                corners = 7 to 5,
                yellows = 1 to 2,
                reds = 0 to 0,
                possession = 56 to 44,
                shots = 13 to 9,
                shotsOnTarget = 6 to 4,
                btts = 75,
                doubleChance = DoubleChance(1.15, 87, 1.25, 80, 2.50, 40),
                homeFormation = "4-3-3",
                awayFormation = "3-5-2",
                homeCoach = "Roberto Martínez",
                awayCoach = "Zlatko Dalić",
                homePlayers = listOf(
                    Player("por1", "Diogo Costa", 1, "GK", 7.5),
                    Player("por2", "Nuno Mendes", 19, "DF", 8.2, assists = 1),
                    Player("por3", "Rúben Dias", 4, "DF", 7.8),
                    Player("por4", "Gonçalo Inácio", 14, "DF", 7.4),
                    Player("por5", "Diogo Dalot", 2, "DF", 7.9, goals = 1),
                    Player("por6", "Vitinha", 8, "MF", 8.1),
                    Player("por7", "Bruno Fernandes", 8, "MF", 8.6, assists = 1),
                    Player("por8", "Bernardo Silva", 10, "MF", 8.0),
                    Player("por9", "Pedro Neto", 20, "FW", 7.8),
                    Player("por10", "Cristiano Ronaldo", 7, "FW", 8.9, goals = 1, shotsOnTarget = 4),
                    Player("por11", "Rafael Leão", 17, "FW", 8.3)
                ),
                awayPlayers = listOf(
                    Player("cro1", "Dominik Livaković", 1, "GK", 7.3),
                    Player("cro2", "Josip Sutalo", 6, "DF", 7.0),
                    Player("cro3", "Marin Pongracic", 3, "DF", 6.8),
                    Player("cro4", "Josko Gvardiol", 4, "DF", 7.9),
                    Player("cro5", "Kristijan Jakic", 2, "MF", 6.9),
                    Player("cro6", "Luka Modrić", 10, "MF", 8.4, passesAccuracy = 94),
                    Player("cro7", "Mateo Kovačić", 8, "MF", 7.8),
                    Player("cro8", "Mario Pasalic", 15, "MF", 7.1),
                    Player("cro9", "Borna Sosa", 19, "DF", 7.2),
                    Player("cro10", "Andrej Kramarić", 9, "FW", 7.4),
                    Player("cro11", "Igor Matanovic", 20, "FW", 7.0)
                ),
                events = listOf(
                    MatchEvent(62, 0, EventType.CORNER, true, "Bruno Fernandes", null, "Portugal corner kick"),
                    MatchEvent(41, 0, EventType.GOAL, false, "Diogo Dalot", null, "Unfortunate own goal deflection"),
                    MatchEvent(34, 0, EventType.GOAL, true, "Cristiano Ronaldo", "Nuno Mendes", "Milestone 900th career goal volleyed into the roof of the net!"),
                    MatchEvent(7, 0, EventType.GOAL, true, "Diogo Dalot", "Bruno Fernandes", "Low drive between the keeper's legs")
                )
            ),

            // 7. Bundesliga - LIVE MATCH
            createMatch(
                id = "bundes_1",
                league = "Bundesliga",
                leagueIcon = "🇩🇪",
                homeTeam = "Bayern München",
                homeShort = "BAY",
                homeFlag = "🇩🇪",
                awayTeam = "Bayer Leverkusen",
                awayShort = "B04",
                awayFlag = "🇩🇪",
                homeScore = 1,
                awayScore = 1,
                status = MatchStatus.LIVE,
                minute = 73,
                date = "Today",
                time = "18:30",
                venue = "Allianz Arena, Munich",
                corners = 9 to 3,
                yellows = 2 to 3,
                reds = 0 to 0,
                possession = 68 to 32,
                shots = 18 to 4,
                shotsOnTarget = 7 to 2,
                btts = 88,
                doubleChance = DoubleChance(1.22, 82, 1.25, 80, 2.10, 48),
                homeFormation = "4-2-3-1",
                awayFormation = "3-4-2-1",
                homeCoach = "Vincent Kompany",
                awayCoach = "Xabi Alonso",
                homePlayers = listOf(
                    Player("bay1", "Manuel Neuer", 1, "GK", 7.2),
                    Player("bay2", "Raphaël Guerreiro", 22, "DF", 7.6),
                    Player("bay3", "Dayot Upamecano", 2, "DF", 7.9),
                    Player("bay4", "Min-jae Kim", 3, "DF", 7.8),
                    Player("bay5", "Alphonso Davies", 19, "DF", 8.0),
                    Player("bay6", "Joshua Kimmich", 6, "MF", 8.4),
                    Player("bay7", "Aleksandar Pavlović", 45, "MF", 8.6, goals = 1),
                    Player("bay8", "Michael Olise", 17, "FW", 8.2),
                    Player("bay9", "Jamal Musiala", 42, "MF", 8.7, shotsOnTarget = 3),
                    Player("bay10", "Serge Gnabry", 7, "FW", 7.5),
                    Player("bay11", "Harry Kane", 9, "FW", 8.0, shotsOnTarget = 3)
                ),
                awayPlayers = listOf(
                    Player("lev1", "Lukás Hrádecky", 1, "GK", 8.1, saves = 6),
                    Player("lev2", "Edmond Tapsoba", 12, "DF", 7.4),
                    Player("lev3", "Jonathan Tah", 4, "DF", 7.6),
                    Player("lev4", "Piero Hincapié", 3, "DF", 7.2),
                    Player("lev5", "Jeremie Frimpong", 30, "MF", 7.7),
                    Player("lev6", "Granit Xhaka", 34, "MF", 7.8, yellowCards = 1),
                    Player("lev7", "Robert Andrich", 8, "MF", 8.2, goals = 1, yellowCards = 1),
                    Player("lev8", "Alejandro Grimaldo", 20, "MF", 7.6),
                    Player("lev9", "Martin Terrier", 11, "FW", 6.9),
                    Player("lev10", "Florian Wirtz", 10, "MF", 8.3),
                    Player("lev11", "Victor Boniface", 22, "FW", 7.0)
                ),
                events = listOf(
                    MatchEvent(69, 0, EventType.CORNER, true, "Joshua Kimmich", null, "Bayern continuous pressure corner"),
                    MatchEvent(39, 0, EventType.GOAL, true, "Aleksandar Pavlović", null, "Incredible 30-yard chest control and half-volley into the top corner!"),
                    MatchEvent(31, 0, EventType.GOAL, false, "Robert Andrich", "Granit Xhaka", "Stunning drilled shot from outside the penalty box!")
                )
            ),

            // 8. Serie A - LIVE MATCH
            createMatch(
                id = "seriea_1",
                league = "Serie A",
                leagueIcon = "🇮🇹",
                homeTeam = "Inter Milan",
                homeShort = "INT",
                homeFlag = "🇮🇹",
                awayTeam = "AC Milan",
                awayShort = "MIL",
                awayFlag = "🇮🇹",
                homeScore = 1,
                awayScore = 2,
                status = MatchStatus.LIVE,
                minute = 88,
                date = "Today",
                time = "20:45",
                venue = "San Siro, Milan",
                corners = 8 to 5,
                yellows = 3 to 2,
                reds = 0 to 0,
                possession = 57 to 43,
                shots = 14 to 12,
                shotsOnTarget = 5 to 6,
                btts = 90,
                doubleChance = DoubleChance(1.70, 59, 1.20, 83, 1.12, 89),
                homeFormation = "3-5-2",
                awayFormation = "4-4-2",
                homeCoach = "Simone Inzaghi",
                awayCoach = "Paulo Fonseca",
                homePlayers = listOf(
                    Player("int1", "Yann Sommer", 1, "GK", 6.8),
                    Player("int2", "Benjamin Pavard", 28, "DF", 7.0),
                    Player("int3", "Francesco Acerbi", 15, "DF", 6.9),
                    Player("int4", "Alessandro Bastoni", 95, "DF", 7.4),
                    Player("int5", "Denzel Dumfries", 2, "MF", 7.2),
                    Player("int6", "Nicolò Barella", 23, "MF", 7.8),
                    Player("int7", "Hakan Çalhanoğlu", 20, "MF", 7.5),
                    Player("int8", "Henrikh Mkhitaryan", 22, "MF", 7.1),
                    Player("int9", "Federico Dimarco", 32, "MF", 8.2, goals = 1),
                    Player("int10", "Marcus Thuram", 9, "FW", 7.3),
                    Player("int11", "Lautaro Martínez", 10, "FW", 7.7, assists = 1)
                ),
                awayPlayers = listOf(
                    Player("mil1", "Mike Maignan", 16, "GK", 8.0),
                    Player("mil2", "Emerson Royal", 22, "DF", 7.1),
                    Player("mil3", "Matteo Gabbia", 46, "DF", 8.5, goals = 1),
                    Player("mil4", "Fikayo Tomori", 23, "DF", 7.5),
                    Player("mil5", "Theo Hernández", 19, "DF", 7.6),
                    Player("mil6", "Christian Pulisic", 11, "MF", 8.6, goals = 1),
                    Player("mil7", "Youssouf Fofana", 29, "MF", 7.6),
                    Player("mil8", "Tijjani Reijnders", 14, "MF", 8.4, assists = 1),
                    Player("mil9", "Rafael Leão", 10, "FW", 7.8),
                    Player("mil10", "Álvaro Morata", 7, "FW", 7.5),
                    Player("mil11", "Tammy Abraham", 90, "FW", 7.4)
                ),
                events = listOf(
                    MatchEvent(89, 0, EventType.GOAL, false, "Matteo Gabbia", "Tijjani Reijnders", "Late dramatic header into the top corner!"),
                    MatchEvent(27, 0, EventType.GOAL, true, "Federico Dimarco", "Lautaro Martínez", "Drilled diagonal finish"),
                    MatchEvent(10, 0, EventType.GOAL, false, "Christian Pulisic", null, "Solo dribble through three defenders and scored!")
                )
            ),

            // 9. Ligue 1 - UPCOMING
            createMatch(
                id = "ligue1_1",
                league = "Ligue 1",
                leagueIcon = "🇫🇷",
                homeTeam = "Paris Saint-Germain",
                homeShort = "PSG",
                homeFlag = "🇫🇷",
                awayTeam = "Marseille",
                awayShort = "OM",
                awayFlag = "🇫🇷",
                homeScore = 0,
                awayScore = 0,
                status = MatchStatus.UPCOMING,
                minute = 0,
                date = "Tomorrow",
                time = "20:45",
                venue = "Parc des Princes, Paris",
                corners = 0 to 0,
                yellows = 0 to 0,
                reds = 0 to 0,
                possession = 50 to 50,
                shots = 0 to 0,
                shotsOnTarget = 0 to 0,
                btts = 76,
                doubleChance = DoubleChance(1.18, 85, 1.22, 82, 2.45, 41),
                homeFormation = "4-3-3",
                awayFormation = "4-2-3-1",
                homeCoach = "Luis Enrique",
                awayCoach = "Roberto De Zerbi",
                homePlayers = listOf(
                    Player("psg1", "Gianluigi Donnarumma", 1, "GK", 7.6),
                    Player("psg2", "Achraf Hakimi", 2, "DF", 8.4),
                    Player("psg3", "Marquinhos", 5, "DF", 8.0),
                    Player("psg4", "Willian Pacho", 51, "DF", 7.6),
                    Player("psg5", "Nuno Mendes", 25, "DF", 8.1),
                    Player("psg6", "Warren Zaïre-Emery", 33, "MF", 8.0),
                    Player("psg7", "Vitinha", 17, "MF", 8.3),
                    Player("psg8", "João Neves", 87, "MF", 8.2),
                    Player("psg9", "Ousmane Dembélé", 10, "FW", 8.6),
                    Player("psg10", "Bradley Barcola", 29, "FW", 8.8),
                    Player("psg11", "Marco Asensio", 11, "FW", 7.8)
                ),
                awayPlayers = listOf(
                    Player("om1", "Gerónimo Rulli", 1, "GK", 7.2),
                    Player("om2", "Michael Murillo", 62, "DF", 7.0),
                    Player("om3", "Leonardo Balerdi", 5, "DF", 7.3),
                    Player("om4", "Derek Cornelius", 13, "DF", 7.0),
                    Player("om5", "Lilian Brassier", 20, "DF", 7.1),
                    Player("om6", "Pierre-Emile Højbjerg", 23, "MF", 7.9),
                    Player("om7", "Geoffrey Kondogbia", 19, "MF", 7.4),
                    Player("om8", "Mason Greenwood", 10, "FW", 8.4),
                    Player("om9", "Amine Harit", 11, "MF", 7.6),
                    Player("om10", "Luis Henrique", 44, "FW", 7.5),
                    Player("om11", "Elye Wahi", 9, "FW", 7.3)
                ),
                events = emptyList()
            ),

            // 10. UEFA Europa League - LIVE MATCH
            createMatch(
                id = "uel_1",
                league = "UEFA Europa League",
                leagueIcon = "🥈",
                homeTeam = "Manchester United",
                homeShort = "MUN",
                homeFlag = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
                awayTeam = "Porto",
                awayShort = "FCP",
                awayFlag = "🇵🇹",
                homeScore = 3,
                awayScore = 3,
                status = MatchStatus.LIVE,
                minute = 90,
                date = "Today",
                time = "21:00",
                venue = "Estádio do Dragão, Porto",
                corners = 7 to 9,
                yellows = 2 to 2,
                reds = 1 to 0,
                possession = 48 to 52,
                shots = 15 to 19,
                shotsOnTarget = 7 to 9,
                btts = 100,
                doubleChance = DoubleChance(1.30, 77, 1.20, 83, 1.30, 77),
                homeFormation = "4-2-3-1",
                awayFormation = "4-3-3",
                homeCoach = "Erik ten Hag",
                awayCoach = "Vítor Bruno",
                homePlayers = listOf(
                    Player("mun1", "André Onana", 24, "GK", 6.8),
                    Player("mun2", "Noussair Mazraoui", 3, "DF", 7.1),
                    Player("mun3", "Matthijs de Ligt", 4, "DF", 6.7),
                    Player("mun4", "Lisandro Martínez", 6, "DF", 7.0),
                    Player("mun5", "Diogo Dalot", 20, "DF", 7.2),
                    Player("mun6", "Casemiro", 18, "MF", 7.3),
                    Player("mun7", "Christian Eriksen", 14, "MF", 7.8, assists = 2),
                    Player("mun8", "Amad Diallo", 16, "FW", 7.6),
                    Player("mun9", "Bruno Fernandes", 8, "MF", 6.5, redCards = 1),
                    Player("mun10", "Marcus Rashford", 10, "FW", 8.4, goals = 1, assists = 1),
                    Player("mun11", "Rasmus Højlund", 9, "FW", 8.1, goals = 1)
                ),
                awayPlayers = listOf(
                    Player("por1", "Diogo Costa", 99, "GK", 6.9),
                    Player("por2", "João Mário", 23, "DF", 7.2),
                    Player("por3", "Zé Pedro", 97, "DF", 6.8),
                    Player("por4", "Nehuén Pérez", 24, "DF", 7.0),
                    Player("por5", "Francisco Moura", 74, "DF", 7.4),
                    Player("por6", "Alan Varela", 22, "MF", 7.5),
                    Player("por7", "Stephen Eustáquio", 6, "MF", 7.3),
                    Player("por8", "Pepê", 11, "FW", 8.2, goals = 1),
                    Player("por9", "Nico González", 16, "MF", 7.7),
                    Player("por10", "Galeno", 13, "FW", 7.8),
                    Player("por11", "Samu Omorodion", 9, "FW", 8.9, goals = 2)
                ),
                events = listOf(
                    MatchEvent(91, 0, EventType.GOAL, true, "Harry Maguire", "Christian Eriksen", "Bullet header from late corner to salvage a point!"),
                    MatchEvent(81, 0, EventType.RED_CARD, true, "Bruno Fernandes", null, "Second yellow card for high boot"),
                    MatchEvent(50, 0, EventType.GOAL, false, "Samu Omorodion", "Pepê", "Powerful near-post smash"),
                    MatchEvent(34, 0, EventType.GOAL, false, "Samu Omorodion", null, "Header into the net"),
                    MatchEvent(27, 0, EventType.GOAL, false, "Pepê", null, "Rebound header"),
                    MatchEvent(20, 0, EventType.GOAL, true, "Rasmus Højlund", "Marcus Rashford", "Near post squirmed in"),
                    MatchEvent(7, 0, EventType.GOAL, true, "Marcus Rashford", "Christian Eriksen", "Superb solo run and finish")
                )
            ),

            // 11. Copa América - FINISHED
            createMatch(
                id = "ca_1",
                league = "Copa América",
                leagueIcon = "🌎",
                homeTeam = "Argentina",
                homeShort = "ARG",
                homeFlag = "🇦🇷",
                awayTeam = "Colombia",
                awayShort = "COL",
                awayFlag = "🇨🇴",
                homeScore = 1,
                awayScore = 0,
                status = MatchStatus.FINISHED,
                minute = 120,
                date = "Finished",
                time = "AET",
                venue = "Hard Rock Stadium, Miami",
                corners = 4 to 7,
                yellows = 3 to 4,
                reds = 0 to 0,
                possession = 54 to 46,
                shots = 11 to 18,
                shotsOnTarget = 4 to 5,
                btts = 0,
                doubleChance = DoubleChance(1.20, 83, 1.30, 77, 2.15, 46),
                homeFormation = "4-3-3",
                awayFormation = "4-2-3-1",
                homeCoach = "Lionel Scaloni",
                awayCoach = "Néstor Lorenzo",
                homePlayers = listOf(
                    Player("ca_a1", "Emiliano Martínez", 23, "GK", 8.4, saves = 5),
                    Player("ca_a2", "Gonzalo Montiel", 4, "DF", 7.4),
                    Player("ca_a3", "Cristian Romero", 13, "DF", 8.6, tackles = 6),
                    Player("ca_a4", "Lisandro Martínez", 25, "DF", 8.1),
                    Player("ca_a5", "Nicolás Tagliafico", 3, "DF", 7.5),
                    Player("ca_a6", "Rodrigo De Paul", 7, "MF", 8.2),
                    Player("ca_a7", "Enzo Fernández", 24, "MF", 7.8),
                    Player("ca_a8", "Alexis Mac Allister", 20, "MF", 8.0),
                    Player("ca_a9", "Lionel Messi", 10, "FW", 7.5),
                    Player("ca_a10", "Ángel Di María", 11, "FW", 8.5),
                    Player("ca_a11", "Lautaro Martínez", 22, "FW", 8.9, goals = 1)
                ),
                awayPlayers = listOf(
                    Player("ca_c1", "Camilo Vargas", 12, "GK", 7.6),
                    Player("ca_c2", "Santiago Arias", 4, "DF", 7.2),
                    Player("ca_c3", "Carlos Cuesta", 2, "DF", 7.4),
                    Player("ca_c4", "Davinson Sánchez", 23, "DF", 7.8),
                    Player("ca_c5", "Johan Mojica", 17, "DF", 7.3),
                    Player("ca_c6", "Richard Ríos", 6, "MF", 7.9),
                    Player("ca_c7", "Jefferson Lerma", 16, "MF", 7.5),
                    Player("ca_c8", "Jhon Arias", 11, "FW", 7.4),
                    Player("ca_c9", "James Rodríguez", 10, "MF", 8.3),
                    Player("ca_c10", "Luis Díaz", 7, "FW", 7.8),
                    Player("ca_c11", "Jhon Córdoba", 24, "FW", 7.1)
                ),
                events = listOf(
                    MatchEvent(112, 0, EventType.GOAL, true, "Lautaro Martínez", "Giovani Lo Celso", "Extra time golden tournament-winning goal!")
                )
            ),

            // 12. Copa Libertadores - LIVE MATCH
            createMatch(
                id = "lib_1",
                league = "Copa Libertadores",
                leagueIcon = "🏆",
                homeTeam = "Flamengo",
                homeShort = "FLA",
                homeFlag = "🇧🇷",
                awayTeam = "River Plate",
                awayShort = "RIV",
                awayFlag = "🇦🇷",
                homeScore = 2,
                awayScore = 1,
                status = MatchStatus.LIVE,
                minute = 79,
                date = "Today",
                time = "21:30",
                venue = "Maracanã, Rio de Janeiro",
                corners = 6 to 4,
                yellows = 4 to 5,
                reds = 0 to 1,
                possession = 59 to 41,
                shots = 14 to 8,
                shotsOnTarget = 6 to 3,
                btts = 82,
                doubleChance = DoubleChance(1.18, 85, 1.25, 80, 2.30, 43),
                homeFormation = "4-2-3-1",
                awayFormation = "4-3-1-2",
                homeCoach = "Filipe Luís",
                awayCoach = "Marcelo Gallardo",
                homePlayers = listOf(
                    Player("fla1", "Agustín Rossi", 1, "GK", 7.4),
                    Player("fla2", "Wesley", 43, "DF", 7.6),
                    Player("fla3", "Fabrício Bruno", 15, "DF", 7.8),
                    Player("fla4", "Léo Ortiz", 3, "DF", 7.7),
                    Player("fla5", "Alex Sandro", 26, "DF", 7.5),
                    Player("fla6", "Erick Pulgar", 5, "MF", 7.9, yellowCards = 1),
                    Player("fla7", "Nicolás De La Cruz", 18, "MF", 8.4, assists = 1),
                    Player("fla8", "Gerson", 8, "MF", 8.7, goals = 1),
                    Player("fla9", "Giorgian de Arrascaeta", 14, "MF", 8.6, assists = 1),
                    Player("fla10", "Bruno Henrique", 27, "FW", 7.9),
                    Player("fla11", "Gabriel Barbosa", 99, "FW", 8.5, goals = 1)
                ),
                awayPlayers = listOf(
                    Player("riv1", "Franco Armani", 1, "GK", 7.2),
                    Player("riv2", "Fabricio Bustos", 16, "DF", 6.8),
                    Player("riv3", "Germán Pezzella", 6, "DF", 7.3),
                    Player("riv4", "Paulo Díaz", 17, "DF", 7.0, redCards = 1),
                    Player("riv5", "Marcos Acuña", 24, "DF", 7.2, yellowCards = 1),
                    Player("riv6", "Matías Kranevitter", 5, "MF", 6.9),
                    Player("riv7", "Santiago Simón", 31, "MF", 7.1),
                    Player("riv8", "Ignacio Fernández", 26, "MF", 7.6),
                    Player("riv9", "Franco Mastantuono", 30, "MF", 7.8),
                    Player("riv10", "Facundo Colidio", 11, "FW", 7.4),
                    Player("riv11", "Miguel Borja", 9, "FW", 8.0, goals = 1)
                ),
                events = listOf(
                    MatchEvent(75, 0, EventType.RED_CARD, false, "Paulo Díaz", null, "Dangerous studs-up tackle, straight red card"),
                    MatchEvent(63, 0, EventType.GOAL, true, "Gabriel Barbosa", "Giorgian de Arrascaeta", "Poacher's tap in at the far post!"),
                    MatchEvent(49, 0, EventType.GOAL, false, "Miguel Borja", null, "Header from corner kick"),
                    MatchEvent(22, 0, EventType.GOAL, true, "Gerson", "Nicolás De La Cruz", "Superb left footed curler")
                )
            ),

            // 13. FIFA Club World Cup - UPCOMING
            createMatch(
                id = "cwc_1",
                league = "FIFA Club World Cup",
                leagueIcon = "🌐",
                homeTeam = "Real Madrid",
                homeShort = "RMA",
                homeFlag = "🇪🇸",
                awayTeam = "Palmeiras",
                awayShort = "PAL",
                awayFlag = "🇧🇷",
                homeScore = 0,
                awayScore = 0,
                status = MatchStatus.UPCOMING,
                minute = 0,
                date = "Saturday",
                time = "21:00",
                venue = "MetLife Stadium, New Jersey",
                corners = 0 to 0,
                yellows = 0 to 0,
                reds = 0 to 0,
                possession = 50 to 50,
                shots = 0 to 0,
                shotsOnTarget = 0 to 0,
                btts = 68,
                doubleChance = DoubleChance(1.12, 89, 1.18, 85, 3.20, 31),
                homeFormation = "4-3-3",
                awayFormation = "4-2-3-1",
                homeCoach = "Carlo Ancelotti",
                awayCoach = "Abel Ferreira",
                homePlayers = listOf(
                    Player("cwc_r1", "Thibaut Courtois", 1, "GK", 8.0),
                    Player("cwc_r2", "Dani Carvajal", 2, "DF", 7.8),
                    Player("cwc_r3", "Éder Militão", 3, "DF", 8.1),
                    Player("cwc_r4", "Antonio Rüdiger", 22, "DF", 8.2),
                    Player("cwc_r5", "Ferland Mendy", 23, "DF", 7.6),
                    Player("cwc_r6", "Federico Valverde", 15, "MF", 8.5),
                    Player("cwc_r7", "Aurélien Tchouaméni", 14, "MF", 8.1),
                    Player("cwc_r8", "Jude Bellingham", 5, "MF", 8.9),
                    Player("cwc_r9", "Rodrygo", 11, "FW", 8.4),
                    Player("cwc_r10", "Kylian Mbappé", 9, "FW", 9.2),
                    Player("cwc_r11", "Vinícius Júnior", 7, "FW", 9.1)
                ),
                awayPlayers = listOf(
                    Player("cwc_p1", "Weverton", 21, "GK", 7.8),
                    Player("cwc_p2", "Marcos Rocha", 2, "DF", 7.2),
                    Player("cwc_p3", "Gustavo Gómez", 15, "DF", 8.0),
                    Player("cwc_p4", "Murilo", 26, "DF", 7.6),
                    Player("cwc_p5", "Joaquín Piquerez", 22, "DF", 7.7),
                    Player("cwc_p6", "Aníbal Moreno", 5, "MF", 7.8),
                    Player("cwc_p7", "Zé Rafael", 8, "MF", 7.5),
                    Player("cwc_p8", "Raphael Veiga", 23, "MF", 8.4),
                    Player("cwc_p9", "Maurício", 18, "MF", 7.6),
                    Player("cwc_p10", "Felipe Anderson", 9, "FW", 7.9),
                    Player("cwc_p11", "Flaco López", 42, "FW", 8.1)
                ),
                events = emptyList()
            )
        )
    }

    private fun createMatch(
        id: String,
        league: String,
        leagueIcon: String,
        homeTeam: String,
        homeShort: String,
        homeFlag: String,
        awayTeam: String,
        awayShort: String,
        awayFlag: String,
        homeScore: Int,
        awayScore: Int,
        status: MatchStatus,
        minute: Int,
        date: String,
        time: String,
        venue: String,
        corners: Pair<Int, Int>,
        yellows: Pair<Int, Int>,
        reds: Pair<Int, Int>,
        possession: Pair<Int, Int>,
        shots: Pair<Int, Int>,
        shotsOnTarget: Pair<Int, Int>,
        btts: Int,
        doubleChance: DoubleChance,
        homeFormation: String,
        awayFormation: String,
        homeCoach: String,
        awayCoach: String,
        homePlayers: List<Player>,
        awayPlayers: List<Player>,
        events: List<MatchEvent>
    ): Match {
        val stats = MatchStats(
            possessionHome = possession.first,
            possessionAway = possession.second,
            shotsHome = shots.first,
            shotsAway = shots.second,
            shotsOnTargetHome = shotsOnTarget.first,
            shotsOnTargetAway = shotsOnTarget.second,
            cornersHome = corners.first,
            cornersAway = corners.second,
            foulsHome = 10 + Random.nextInt(5),
            foulsAway = 11 + Random.nextInt(5),
            yellowCardsHome = yellows.first,
            yellowCardsAway = yellows.second,
            redCardsHome = reds.first,
            redCardsAway = reds.second,
            offsidesHome = Random.nextInt(4),
            offsidesAway = Random.nextInt(4),
            savesHome = Random.nextInt(6),
            savesAway = Random.nextInt(6)
        )

        val lineup = Lineup(
            homeFormation = homeFormation,
            awayFormation = awayFormation,
            homeCoach = homeCoach,
            awayCoach = awayCoach,
            homeStartingXI = homePlayers,
            awayStartingXI = awayPlayers,
            homeBench = listOf(
                Player("hb1", "$homeTeam Sub 1", 12, "MF", 6.8),
                Player("hb2", "$homeTeam Sub 2", 18, "FW", 7.0),
                Player("hb3", "$homeTeam Sub 3", 21, "DF", 6.7),
                Player("hb4", "$homeTeam Sub 4", 30, "GK", 6.5)
            ),
            awayBench = listOf(
                Player("ab1", "$awayTeam Sub 1", 14, "MF", 6.8),
                Player("ab2", "$awayTeam Sub 2", 19, "FW", 7.1),
                Player("ab3", "$awayTeam Sub 3", 27, "DF", 6.6),
                Player("ab4", "$awayTeam Sub 4", 31, "GK", 6.5)
            )
        )

        return Match(
            id = id,
            league = league,
            leagueIcon = leagueIcon,
            homeTeam = homeTeam,
            homeShort = homeShort,
            homeFlag = homeFlag,
            awayTeam = awayTeam,
            awayShort = awayShort,
            awayFlag = awayFlag,
            homeScore = homeScore,
            awayScore = awayScore,
            status = status,
            minute = minute,
            date = date,
            time = time,
            venue = venue,
            stats = stats,
            bttsPercentage = btts,
            doubleChance = doubleChance,
            lineup = lineup,
            events = events.sortedByDescending { it.minute }
        )
    }
}
