package com.hmncube.juiceme.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CardNumber(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    val number: String,
    val date: Long,
)
