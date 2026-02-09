package com.yourapp.noveltts.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chapters", indices = [Index(value = ["url"], unique = true)])
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val plainText: String,
    val nextUrl: String?,
    val prevUrl: String?,
    val createdAt: Long = System.currentTimeMillis()
)
