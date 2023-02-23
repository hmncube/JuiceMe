package com.hmncube.juiceme.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CardNumberDao {
    @Insert
    suspend fun insertCardNumber(cardNumber: CardNumber)

    @Query("SELECT * FROM CardNumber")
    suspend fun selectAll(): List<CardNumber>

    @Delete
    suspend fun delete(cardNumber: CardNumber)

    @Query("DELETE FROM CardNumber")
    suspend fun clearAll()
}
