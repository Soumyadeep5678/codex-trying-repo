package com.yourapp.noveltts

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yourapp.noveltts.data.AppDatabase
import com.yourapp.noveltts.extractor.ReadabilityLikeExtractor
import com.yourapp.noveltts.repo.ChapterRepository
import com.yourapp.noveltts.service.TtsPlaybackService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.get(application)
    private val repository = ChapterRepository(db.chapterDao(), db.progressDao(), db.bookmarkDao(), ReadabilityLikeExtractor())

    val recent = repository.recentChapters.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val bookmarks = repository.bookmarks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val continueTitle: StateFlow<String> = recent.map { it.firstOrNull()?.title ?: "No chapter yet" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "No chapter yet")

    fun handleSharedUrl(url: String) {
        val context = getApplication<Application>()
        val intent = Intent(context, TtsPlaybackService::class.java).apply {
            action = TtsPlaybackService.ACTION_LOAD_URL
            putExtra(TtsPlaybackService.EXTRA_URL, url)
        }
        context.startForegroundService(intent)
    }
}
