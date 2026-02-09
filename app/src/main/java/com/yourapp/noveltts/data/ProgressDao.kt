package com.yourapp.noveltts.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: ProgressEntity)

    @Query("SELECT * FROM progress WHERE chapterUrl = :chapterUrl LIMIT 1")
    suspend fun get(chapterUrl: String): ProgressEntity?
}
