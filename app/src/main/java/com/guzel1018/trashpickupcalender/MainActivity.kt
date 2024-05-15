package com.guzel1018.trashpickupcalender

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.guzel1018.trashpickupcalender.ui.StartFilterScreen
import com.guzel1018.trashpickupcalender.ui.theme.TrashPickupCalenderTheme
import androidx.hilt.navigation.compose.hiltViewModel
import com.guzel1018.trashpickupcalender.service.AddressService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userAddressService: AddressService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrashPickupCalenderTheme {
                StartFilterScreen(viewModel = hiltViewModel())
            }
        }
    }
}
