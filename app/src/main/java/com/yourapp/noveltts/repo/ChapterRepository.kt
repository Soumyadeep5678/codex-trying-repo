package com.yourapp.noveltts.repo

import com.yourapp.noveltts.data.BookmarkDao
import com.yourapp.noveltts.data.BookmarkEntity
import com.yourapp.noveltts.data.ChapterDao
import com.yourapp.noveltts.data.ChapterEntity
import com.yourapp.noveltts.data.ProgressDao
import com.yourapp.noveltts.data.ProgressEntity
import com.yourapp.noveltts.extractor.ChapterExtractor
import kotlinx.coroutines.flow.Flow

class ChapterRepository(
    private val chapterDao: ChapterDao,
    private val progressDao: ProgressDao,
    private val bookmarkDao: BookmarkDao,
    private val extractor: ChapterExtractor
) {
    val recentChapters: Flow<List<ChapterEntity>> = chapterDao.recent()
    val bookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.all()

    suspend fun loadOrExtract(url: String): Result<ChapterEntity> {
        chapterDao.getByUrl(url)?.let { return Result.success(it) }
        return extractor.extract(url).map { extracted ->
            val entity = ChapterEntity(
                url = extracted.url,
                title = extracted.title,
                plainText = extracted.paragraphs.joinToString("\n\n"),
                nextUrl = extracted.nextUrl,
                prevUrl = extracted.prevUrl
            )
            chapterDao.upsert(entity)
            chapterDao.getByUrl(url) ?: entity
        }
    }

    suspend fun saveProgress(url: String, paragraphIndex: Int) {
        progressDao.upsert(ProgressEntity(chapterUrl = url, paragraphIndex = paragraphIndex))
    }

    suspend fun getProgress(url: String): ProgressEntity? = progressDao.get(url)

    suspend fun latestChapter(): ChapterEntity? = chapterDao.latest()

    suspend fun addBookmark(chapter: ChapterEntity) {
        bookmarkDao.add(BookmarkEntity(chapter.url, chapter.title))
    }
}
