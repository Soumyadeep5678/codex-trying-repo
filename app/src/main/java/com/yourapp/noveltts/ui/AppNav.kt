package com.yourapp.noveltts.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yourapp.noveltts.MainViewModel

@Composable
fun NovelTtsApp(viewModel: MainViewModel) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route

    Scaffold(bottomBar = {
        NavigationBar {
            listOf("home", "player", "settings").forEach { tab ->
                NavigationBarItem(
                    selected = tab == route,
                    onClick = {
                        nav.navigate(tab) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Text(tab.take(1).uppercase()) },
                    label = { Text(tab) }
                )
            }
        }
    }) { padding ->
        NavHost(navController = nav, startDestination = "home", modifier = Modifier.padding(padding)) {
            composable("home") { HomeScreen(viewModel) }
            composable("player") { PlayerScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
