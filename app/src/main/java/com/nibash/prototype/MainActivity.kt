package com.nibash.prototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.nibash.prototype.model.Building
import com.nibash.prototype.model.DummyDataProvider
import com.nibash.prototype.ui.screens.DetailScreen
import com.nibash.prototype.ui.screens.FormScreen
import com.nibash.prototype.ui.screens.HomeScreen

sealed interface Screen {
    data object Home : Screen
    data object AddBuildingForm : Screen
    data class BuildingDetail(val buildingId: String) : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NibashApp()
                }
            }
        }
    }
}

@Composable
fun NibashApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("nibash_prefs", android.content.Context.MODE_PRIVATE) }

    // Shared reactive state for list of buildings
    var buildings by remember {
        mutableStateOf(
            run {
                val savedJson = sharedPrefs.getString("buildings_data", null)
                if (savedJson != null) {
                    try {
                        val arr = org.json.JSONArray(savedJson)
                        val list = mutableListOf<Building>()
                        for (i in 0 until arr.length()) {
                            list.add(Building.fromJson(arr.getJSONObject(i)))
                        }
                        list
                    } catch (e: Exception) {
                        emptyList<Building>()
                    }
                } else {
                    emptyList<Building>()
                }
            }
        )
    }

    LaunchedEffect(buildings) {
        val arr = org.json.JSONArray()
        buildings.forEach { arr.put(it.toJson()) }
        sharedPrefs.edit().putString("buildings_data", arr.toString()).apply()
    }

    // Lightweight navigation state stack
    // Home is always at the base.
    val navigationStack = remember { mutableStateListOf<Screen>(Screen.Home) }

    val currentScreen = navigationStack.lastOrNull() ?: Screen.Home

    // BackPress Handler for the global app stack
    BackHandler(enabled = navigationStack.size > 1) {
        navigationStack.removeAt(navigationStack.lastIndex)
    }

    when (currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                buildings = buildings,
                onBuildingClick = { id ->
                    navigationStack.add(Screen.BuildingDetail(id))
                },
                onAddBuildingClick = {
                    navigationStack.add(Screen.AddBuildingForm)
                }
            )
        }

        is Screen.AddBuildingForm -> {
            FormScreen(
                onBuildingCreated = { newBuilding ->
                    // Pre-fill a tenant to show mock details on newly generated building
                    val updatedBuilding = newBuilding.copy(id = "b_${System.currentTimeMillis()}")
                    buildings = buildings + updatedBuilding
                    navigationStack.removeAt(navigationStack.lastIndex) // Navigate back to Home
                },
                onBackToHome = {
                    navigationStack.removeAt(navigationStack.lastIndex)
                }
            )
        }

        is Screen.BuildingDetail -> {
            val bId = currentScreen.buildingId
            val selectedBuilding = buildings.find { it.id == bId }

            if (selectedBuilding != null) {
                DetailScreen(
                    building = selectedBuilding,
                    onBack = {
                        navigationStack.removeAt(navigationStack.lastIndex)
                    },
                    onUpdateBuilding = { updatedBuilding ->
                        buildings = buildings.map {
                            if (it.id == updatedBuilding.id) updatedBuilding else it
                        }
                    }
                )
            } else {
                // Safe fallback
                LaunchedEffect(Unit) {
                    navigationStack.removeAt(navigationStack.lastIndex)
                }
            }
        }
    }
}
