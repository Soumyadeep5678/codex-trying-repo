package com.yourapp.noveltts.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourapp.noveltts.MainViewModel

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val recent by viewModel.recent.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Continue", style = MaterialTheme.typography.titleLarge)
            Card(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = recent.firstOrNull()?.title ?: "Share a URL from Edge to begin",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        item { Text("Recent", style = MaterialTheme.typography.titleLarge) }
        items(recent) { chapter ->
            Card(modifier = Modifier.clickable { viewModel.handleSharedUrl(chapter.url) }) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(chapter.title)
                    Text(chapter.url, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item { Text("Bookmarks", style = MaterialTheme.typography.titleLarge) }
        items(bookmarks) { bm ->
            Card {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(bm.title)
                    Text(bm.chapterUrl, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
