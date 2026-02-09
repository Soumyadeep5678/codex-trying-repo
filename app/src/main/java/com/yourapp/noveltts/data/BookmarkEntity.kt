package com.yourapp.noveltts.data

import androidx.room.Entity

@Entity(tableName = "bookmarks", primaryKeys = ["chapterUrl"])
data class BookmarkEntity(
    val chapterUrl: String,
    val title: String,
    val addedAt: Long = System.currentTimeMillis()
)
