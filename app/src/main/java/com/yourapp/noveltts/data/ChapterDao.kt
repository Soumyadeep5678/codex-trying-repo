package com.yourapp.noveltts.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(chapter: ChapterEntity)

    @Query("SELECT * FROM chapters WHERE url = :url LIMIT 1")
    suspend fun getByUrl(url: String): ChapterEntity?

    @Query("SELECT * FROM chapters ORDER BY createdAt DESC LIMIT :limit")
    fun recent(limit: Int = 20): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters ORDER BY createdAt DESC LIMIT 1")
    suspend fun latest(): ChapterEntity?
}
