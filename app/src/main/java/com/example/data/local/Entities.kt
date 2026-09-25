package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: String,
    val bkashNumber: String,
    val accountType: String, // Personal, Agent, Merchant
    val amountBdt: Double,
    val coinsDeducted: Int,
    val status: String, // Pending, Approved, Completed
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_wallet")
data class UserWalletEntity(
    @PrimaryKey
    val id: Int = 1,
    val coins: Int = 1500, // 1500 starting bonus
    val lastCheckInDate: String = "",
    val totalWithdrawnBdt: Double = 0.0,
    val totalPredictions: Int = 0,
    val successfulPredictions: Int = 0
)

@Entity(tableName = "favorite_matches")
data class FavoriteMatchEntity(
    @PrimaryKey
    val matchId: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "predictions")
data class PredictionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val matchId: String,
    val matchTitle: String,
    val pickType: String, // e.g. "BTTS Yes", "Double Chance 1X", "Home Win"
    val odds: Double,
    val coinsWagered: Int,
    val potentialWinCoins: Int,
    val status: String = "Pending", // Pending, Won, Lost
    val timestamp: Long = System.currentTimeMillis()
)
