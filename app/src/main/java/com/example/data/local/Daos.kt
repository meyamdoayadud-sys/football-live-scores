package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WithdrawalDao {
    @Query("SELECT * FROM withdrawals ORDER BY timestamp DESC")
    fun getAllWithdrawals(): Flow<List<WithdrawalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity): Long

    @Query("SELECT COUNT(*) FROM withdrawals")
    suspend fun getWithdrawalCount(): Int
}

@Dao
interface UserWalletDao {
    @Query("SELECT * FROM user_wallet WHERE id = 1")
    fun getWallet(): Flow<UserWalletEntity?>

    @Query("SELECT * FROM user_wallet WHERE id = 1")
    suspend fun getWalletDirect(): UserWalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWallet(wallet: UserWalletEntity)

    @Query("UPDATE user_wallet SET coins = coins + :addedCoins WHERE id = 1")
    suspend fun addCoins(addedCoins: Int)

    @Query("UPDATE user_wallet SET coins = coins - :deductedCoins, totalWithdrawnBdt = totalWithdrawnBdt + :withdrawnBdt WHERE id = 1")
    suspend fun deductCoinsForWithdrawal(deductedCoins: Int, withdrawnBdt: Double)

    @Query("UPDATE user_wallet SET lastCheckInDate = :date, coins = coins + :bonus WHERE id = 1")
    suspend fun recordCheckIn(date: String, bonus: Int)
}

@Dao
interface FavoriteMatchDao {
    @Query("SELECT * FROM favorite_matches")
    fun getAllFavorites(): Flow<List<FavoriteMatchEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_matches WHERE matchId = :matchId)")
    fun isFavorite(matchId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(fav: FavoriteMatchEntity)

    @Query("DELETE FROM favorite_matches WHERE matchId = :matchId")
    suspend fun removeFavorite(matchId: String)
}

@Dao
interface PredictionDao {
    @Query("SELECT * FROM predictions ORDER BY timestamp DESC")
    fun getAllPredictions(): Flow<List<PredictionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: PredictionEntity): Long

    @Update
    suspend fun updatePrediction(prediction: PredictionEntity)
}
