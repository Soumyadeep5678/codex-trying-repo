package com.yourapp.noveltts.player

data class PlaybackQueue(
    var chapterUrl: String = "",
    var title: String = "",
    var paragraphs: List<String> = emptyList(),
    var index: Int = 0
) {
    fun current(): String? = paragraphs.getOrNull(index)
    fun hasNext(): Boolean = index < paragraphs.lastIndex
    fun hasPrev(): Boolean = index > 0
}
