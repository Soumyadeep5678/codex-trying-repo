package com.yourapp.noveltts.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(bookmark: BookmarkEntity)

    @Delete
    suspend fun remove(bookmark: BookmarkEntity)

    @Query("SELECT * FROM bookmarks ORDER BY addedAt DESC")
    fun all(): Flow<List<BookmarkEntity>>
}
