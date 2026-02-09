package com.yourapp.noveltts.extractor

data class ExtractedChapter(
    val url: String,
    val title: String,
    val paragraphs: List<String>,
    val nextUrl: String?,
    val prevUrl: String?
)

interface ChapterExtractor {
    suspend fun extract(url: String): Result<ExtractedChapter>
}
