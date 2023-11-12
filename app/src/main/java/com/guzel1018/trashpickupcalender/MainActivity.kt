package com.guzel1018.trashpickupcalender

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.guzel1018.trashpickupcalender.ui.FilterScreen
import com.guzel1018.trashpickupcalender.ui.theme.TrashPickupCalenderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "FilterScreen"){
                composable (route = "FilterScreen") {
                    FilterScreen("Towns", navController = navController)
                }
                composable (route = "MainScreen") {

                }
            }
            TrashPickupCalenderTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                   Column {
                       FilterScreen("Towns", navController)
                   }
                }
            }
        }
    }
}