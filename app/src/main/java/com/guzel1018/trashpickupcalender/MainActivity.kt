package com.guzel1018.trashpickupcalender

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.guzel1018.trashpickupcalender.ui.TrashPickupSearchScreen
import com.guzel1018.trashpickupcalender.ui.theme.TrashPickupCalenderTheme
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrashPickupCalenderTheme {
                TrashPickupSearchScreen(viewModel = hiltViewModel())
            }
        }
    }
}
