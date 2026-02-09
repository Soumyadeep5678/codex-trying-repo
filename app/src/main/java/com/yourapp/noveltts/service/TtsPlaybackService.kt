package com.yourapp.noveltts.service

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.yourapp.noveltts.data.AppDatabase
import com.yourapp.noveltts.extractor.ReadabilityLikeExtractor
import com.yourapp.noveltts.player.PlaybackQueue
import com.yourapp.noveltts.player.TtsEngine
import com.yourapp.noveltts.repo.ChapterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TtsPlaybackService : LifecycleService() {
    companion object {
        const val NOTIFICATION_ID = 42
        const val ACTION_LOAD_URL = "ACTION_LOAD_URL"
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_NEXT_PARAGRAPH = "ACTION_NEXT_PARAGRAPH"
        const val ACTION_PREV_PARAGRAPH = "ACTION_PREV_PARAGRAPH"
        const val ACTION_NEXT_CHAPTER = "ACTION_NEXT_CHAPTER"
        const val ACTION_PREV_CHAPTER = "ACTION_PREV_CHAPTER"
        const val EXTRA_URL = "extra_url"
    }

    private val binder = LocalBinder()
    private lateinit var repository: ChapterRepository
    private lateinit var tts: TtsEngine
    private lateinit var media: MediaSessionManager
    private lateinit var notifications: MediaNotificationManager
    private lateinit var audioManager: AudioManager
    private lateinit var audioFocusRequest: AudioFocusRequest
    private val queue = PlaybackQueue()

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state

    inner class LocalBinder : Binder() {
        fun service() = this@TtsPlaybackService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.get(this)
        repository = ChapterRepository(db.chapterDao(), db.progressDao(), db.bookmarkDao(), ReadabilityLikeExtractor())
        tts = TtsEngine(this) { lifecycleScope.launch { onUtteranceDone(it) } }
        notifications = MediaNotificationManager(this)
        media = MediaSessionManager(this, object : android.support.v4.media.session.MediaSessionCompat.Callback() {
            override fun onPlay() = play()
            override fun onPause() = pause()
            override fun onSkipToNext() = nextChapter()
            override fun onSkipToPrevious() = prevChapter()
        })
        audioManager = getSystemService(Service.AUDIO_SERVICE) as AudioManager
        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            .setOnAudioFocusChangeListener { change ->
                when (change) {
                    AudioManager.AUDIOFOCUS_LOSS,
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
                    AudioManager.AUDIOFOCUS_GAIN -> if (_state.value.wasInterrupted) play()
                }
            }
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_LOAD_URL -> intent.getStringExtra(EXTRA_URL)?.let { loadUrl(it) }
            ACTION_PLAY -> play()
            ACTION_PAUSE -> pause()
            ACTION_NEXT_PARAGRAPH -> nextParagraph()
            ACTION_PREV_PARAGRAPH -> prevParagraph()
            ACTION_NEXT_CHAPTER -> nextChapter()
            ACTION_PREV_CHAPTER -> prevChapter()
        }
        return START_STICKY
    }

    fun loadUrl(url: String) {
        lifecycleScope.launch {
            repository.loadOrExtract(url).onSuccess { chapter ->
                queue.chapterUrl = chapter.url
                queue.title = chapter.title
                queue.paragraphs = chapter.plainText.split("\n\n").filter { it.isNotBlank() }
                queue.index = repository.getProgress(url)?.paragraphIndex ?: 0
                _state.value = _state.value.copy(title = queue.title, chapterUrl = queue.chapterUrl, index = queue.index, total = queue.paragraphs.size, nextUrl = chapter.nextUrl, prevUrl = chapter.prevUrl)
                startForeground(NOTIFICATION_ID, notifications.build(queue.title, _state.value.isPlaying, media.mediaSession.sessionToken))
            }.onFailure {
                _state.value = _state.value.copy(error = it.message ?: "Extraction failed")
            }
        }
    }

    fun play() {
        if (queue.current() == null) return
        val ok = audioManager.requestAudioFocus(audioFocusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        if (!ok) return
        _state.value = _state.value.copy(isPlaying = true, wasInterrupted = false)
        media.setPlaying(true)
        tts.speak(queue.current().orEmpty(), "${queue.chapterUrl}#${queue.index}")
        notifyState()
    }

    fun pause() {
        tts.stop()
        _state.value = _state.value.copy(isPlaying = false, wasInterrupted = true)
        media.setPlaying(false)
        lifecycleScope.launch { persistProgress() }
        notifyState()
    }

    fun nextParagraph() {
        if (queue.hasNext()) {
            queue.index++
            _state.value = _state.value.copy(index = queue.index)
            if (_state.value.isPlaying) play()
        }
    }

    fun prevParagraph() {
        if (queue.hasPrev()) {
            queue.index--
            _state.value = _state.value.copy(index = queue.index)
            if (_state.value.isPlaying) play()
        }
    }

    fun nextChapter() {
        _state.value.nextUrl?.let { loadUrl(it) }
    }

    fun prevChapter() {
        _state.value.prevUrl?.let { loadUrl(it) }
    }

    private suspend fun onUtteranceDone(utteranceId: String) {
        if (!_state.value.isPlaying) return
        if (queue.hasNext()) {
            queue.index++
            _state.value = _state.value.copy(index = queue.index)
            persistProgress()
            play()
        } else {
            pause()
            if (_state.value.autoNext && _state.value.nextUrl != null) nextChapter()
        }
    }

    private suspend fun persistProgress() {
        if (queue.chapterUrl.isNotBlank()) repository.saveProgress(queue.chapterUrl, queue.index)
    }

    private fun notifyState() {
        startForeground(NOTIFICATION_ID, notifications.build(queue.title.ifBlank { "NovelTTS" }, _state.value.isPlaying, media.mediaSession.sessionToken))
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
        media.release()
    }
}

data class PlaybackState(
    val chapterUrl: String = "",
    val title: String = "",
    val index: Int = 0,
    val total: Int = 0,
    val isPlaying: Boolean = false,
    val nextUrl: String? = null,
    val prevUrl: String? = null,
    val autoNext: Boolean = true,
    val wasInterrupted: Boolean = false,
    val error: String? = null
)
