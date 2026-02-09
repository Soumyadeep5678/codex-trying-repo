package com.yourapp.noveltts.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress")
data class ProgressEntity(
    @PrimaryKey val chapterUrl: String,
    val paragraphIndex: Int,
    val charOffset: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
