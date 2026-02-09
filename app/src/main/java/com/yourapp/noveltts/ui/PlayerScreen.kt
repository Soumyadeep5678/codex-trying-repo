package com.yourapp.noveltts.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yourapp.noveltts.service.TtsPlaybackService

@Composable
fun PlayerScreen() {
    val context = LocalContext.current
    var speed by remember { mutableFloatStateOf(1.0f) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Player")
        Text("Use lock-screen media controls while playback service is active")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { send(context, TtsPlaybackService.ACTION_PREV_CHAPTER) }) { Text("Prev") }
            Button(onClick = { send(context, TtsPlaybackService.ACTION_PLAY) }) { Text("Play") }
            Button(onClick = { send(context, TtsPlaybackService.ACTION_PAUSE) }) { Text("Pause") }
            Button(onClick = { send(context, TtsPlaybackService.ACTION_NEXT_CHAPTER) }) { Text("Next") }
        }

        Text("Speed ${"%.2f".format(speed)}")
        Slider(value = speed, onValueChange = { speed = it }, valueRange = 0.5f..2f)

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text("Preview appears after extraction. Progress is paragraph-based in service.")
        }
    }
}

private fun send(context: android.content.Context, action: String) {
    context.startForegroundService(Intent(context, TtsPlaybackService::class.java).setAction(action))
}
