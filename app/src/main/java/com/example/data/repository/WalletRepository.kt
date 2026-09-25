package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.FavoriteMatchEntity
import com.example.data.local.PredictionEntity
import com.example.data.local.UserWalletEntity
import com.example.data.local.WithdrawalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class WalletRepository(private val database: AppDatabase) {
    private val walletDao = database.userWalletDao()
    private val withdrawalDao = database.withdrawalDao()
    private val favoriteDao = database.favoriteMatchDao()
    private val predictionDao = database.predictionDao()

    val walletFlow: Flow<UserWalletEntity> = walletDao.getWallet().map { wallet ->
        wallet ?: UserWalletEntity().also { defaultWallet ->
            walletDao.insertOrUpdateWallet(defaultWallet)
        }
    }

    val withdrawalsFlow: Flow<List<WithdrawalEntity>> = withdrawalDao.getAllWithdrawals()
    val favoritesFlow: Flow<List<FavoriteMatchEntity>> = favoriteDao.getAllFavorites()
    val predictionsFlow: Flow<List<PredictionEntity>> = predictionDao.getAllPredictions()

    suspend fun ensureWalletInitialized() {
        val current = walletDao.getWalletDirect()
        if (current == null) {
            walletDao.insertOrUpdateWallet(UserWalletEntity(coins = 1500))
        }
    }

    suspend fun requestWithdrawal(
        bkashNumber: String,
        accountType: String,
        amountBdt: Double,
        coinsDeducted: Int
    ): Result<WithdrawalEntity> {
        val current = walletDao.getWalletDirect() ?: UserWalletEntity()
        if (current.coins < coinsDeducted) {
            return Result.failure(Exception("Insufficient coins balance!"))
        }

        val txnId = "BK-${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
        val withdrawal = WithdrawalEntity(
            transactionId = txnId,
            bkashNumber = bkashNumber,
            accountType = accountType,
            amountBdt = amountBdt,
            coinsDeducted = coinsDeducted,
            status = "Pending"
        )

        withdrawalDao.insertWithdrawal(withdrawal)
        walletDao.deductCoinsForWithdrawal(coinsDeducted, amountBdt)
        return Result.success(withdrawal)
    }

    suspend fun claimDailyBonus(bonusCoins: Int = 100): Boolean {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val current = walletDao.getWalletDirect() ?: UserWalletEntity()
        if (current.lastCheckInDate == todayStr) {
            return false // Already claimed today
        }
        walletDao.recordCheckIn(todayStr, bonusCoins)
        return true
    }

    suspend fun addCoins(amount: Int) {
        walletDao.addCoins(amount)
    }

    suspend fun toggleFavorite(matchId: String, isCurrentlyFav: Boolean) {
        if (isCurrentlyFav) {
            favoriteDao.removeFavorite(matchId)
        } else {
            favoriteDao.addFavorite(FavoriteMatchEntity(matchId))
        }
    }

    suspend fun submitPrediction(
        matchId: String,
        matchTitle: String,
        pickType: String,
        odds: Double,
        wagerCoins: Int
    ): Boolean {
        val current = walletDao.getWalletDirect() ?: UserWalletEntity()
        if (current.coins < wagerCoins) return false

        walletDao.addCoins(-wagerCoins)
        val potentialWin = (wagerCoins * odds).toInt()
        val prediction = PredictionEntity(
            matchId = matchId,
            matchTitle = matchTitle,
            pickType = pickType,
            odds = odds,
            coinsWagered = wagerCoins,
            potentialWinCoins = potentialWin,
            status = "Pending"
        )
        predictionDao.insertPrediction(prediction)
        return true
    }
}
