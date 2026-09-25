package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdMobManager
import com.example.data.local.AppDatabase
import com.example.data.local.FavoriteMatchEntity
import com.example.data.local.PredictionEntity
import com.example.data.local.UserWalletEntity
import com.example.data.local.WithdrawalEntity
import com.example.data.model.Match
import com.example.data.model.MatchStatus
import com.example.data.repository.LiveGoalAlert
import com.example.data.repository.MatchRepository
import com.example.data.repository.WalletRepository
import com.example.util.StringsHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MatchFilter {
    ALL, LIVE, TODAY, FINISHED, FAVORITES
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val walletRepository = WalletRepository(database)
    val matchRepository = MatchRepository()
    val adMobManager = AdMobManager(application)

    val allLeagues = matchRepository.allLeagues

    private val _selectedLeague = MutableStateFlow("all")
    val selectedLeague: StateFlow<String> = _selectedLeague.asStateFlow()

    private val _selectedFilter = MutableStateFlow(MatchFilter.ALL)
    val selectedFilter: StateFlow<MatchFilter> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _language = MutableStateFlow(StringsHelper.Language.BN) // Default Bengali
    val language: StateFlow<StringsHelper.Language> = _language.asStateFlow()

    private val _selectedMatch = MutableStateFlow<Match?>(null)
    val selectedMatch: StateFlow<Match?> = _selectedMatch.asStateFlow()

    private val _isAutoRefresh = MutableStateFlow(true)
    val isAutoRefresh: StateFlow<Boolean> = _isAutoRefresh.asStateFlow()

    private val _refreshInterval = MutableStateFlow(15) // seconds
    val refreshInterval: StateFlow<Int> = _refreshInterval.asStateFlow()

    private val _liveGoalAlert = MutableStateFlow<LiveGoalAlert?>(null)
    val liveGoalAlert: StateFlow<LiveGoalAlert?> = _liveGoalAlert.asStateFlow()

    // Room Database Flows
    val wallet: StateFlow<UserWalletEntity> = walletRepository.walletFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserWalletEntity()
    )

    val withdrawals: StateFlow<List<WithdrawalEntity>> = walletRepository.withdrawalsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favorites: StateFlow<List<FavoriteMatchEntity>> = walletRepository.favoritesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val predictions: StateFlow<List<PredictionEntity>> = walletRepository.predictionsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered matches stream combining raw matches, league, search, favorites and status filters
    val filteredMatches: StateFlow<List<Match>> = combine(
        matchRepository.matches,
        _selectedLeague,
        _selectedFilter,
        _searchQuery,
        favorites
    ) { rawMatches, leagueId, filter, query, favList ->
        val favIds = favList.map { it.matchId }.toSet()

        rawMatches.filter { match ->
            // League filter
            val matchesLeague = if (leagueId == "all") {
                true
            } else {
                val leagueItem = allLeagues.find { it.id == leagueId }
                leagueItem != null && match.league.equals(leagueItem.nameEn, ignoreCase = true)
            }

            // Status filter
            val matchesFilter = when (filter) {
                MatchFilter.ALL -> true
                MatchFilter.LIVE -> match.status == MatchStatus.LIVE
                MatchFilter.TODAY -> match.date.equals("Today", ignoreCase = true) || match.status == MatchStatus.LIVE
                MatchFilter.FINISHED -> match.status == MatchStatus.FINISHED
                MatchFilter.FAVORITES -> favIds.contains(match.id)
            }

            // Search query filter
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                match.homeTeam.contains(query, ignoreCase = true) ||
                        match.awayTeam.contains(query, ignoreCase = true) ||
                        match.league.contains(query, ignoreCase = true)
            }

            matchesLeague && matchesFilter && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            walletRepository.ensureWalletInitialized()
        }

        // Listen for live goal/red card alerts
        viewModelScope.launch {
            matchRepository.liveAlerts.collect { alert ->
                _liveGoalAlert.value = alert
            }
        }
    }

    fun selectLeague(leagueId: String) {
        _selectedLeague.value = leagueId
    }

    fun setFilter(filter: MatchFilter) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectMatch(match: Match?) {
        _selectedMatch.value = match
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == StringsHelper.Language.BN)
            StringsHelper.Language.EN
        else
            StringsHelper.Language.BN
    }

    fun setLanguage(lang: StringsHelper.Language) {
        _language.value = lang
    }

    fun setAutoRefresh(enabled: Boolean, intervalSeconds: Int = 15) {
        _isAutoRefresh.value = enabled
        _refreshInterval.value = intervalSeconds
        matchRepository.setAutoRefresh(enabled, intervalSeconds)
    }

    fun manualRefresh() {
        matchRepository.manualRefresh()
    }

    fun toggleFavorite(match: Match) {
        viewModelScope.launch {
            val isCurrentlyFav = favorites.value.any { it.matchId == match.id }
            walletRepository.toggleFavorite(match.id, isCurrentlyFav)
        }
    }

    fun claimDailyBonus(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = walletRepository.claimDailyBonus(100)
            onResult(success)
        }
    }

    fun rewardAdCoins(amount: Int = 50) {
        viewModelScope.launch {
            walletRepository.addCoins(amount)
        }
    }

    fun requestBkashWithdrawal(
        bkashNumber: String,
        accountType: String,
        amountBdt: Double,
        coinsDeducted: Int,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val res = walletRepository.requestWithdrawal(bkashNumber, accountType, amountBdt, coinsDeducted)
            if (res.isSuccess) {
                onResult(true, res.getOrNull()?.transactionId)
            } else {
                onResult(false, res.exceptionOrNull()?.message)
            }
        }
    }

    fun submitPrediction(
        match: Match,
        pickType: String,
        odds: Double,
        wagerCoins: Int,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val success = walletRepository.submitPrediction(
                matchId = match.id,
                matchTitle = "${match.homeTeam} vs ${match.awayTeam}",
                pickType = pickType,
                odds = odds,
                wagerCoins = wagerCoins
            )
            onResult(success)
        }
    }

    fun clearLiveAlert() {
        _liveGoalAlert.value = null
    }
}
