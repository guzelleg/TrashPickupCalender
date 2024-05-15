package com.guzel1018.trashpickupcalender.ui

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.guzel1018.trashpickupcalender.R
import com.guzel1018.trashpickupcalender.clickable
import com.guzel1018.trashpickupcalender.model.Town
import com.guzel1018.trashpickupcalender.utils.getEventsByTown
import com.guzel1018.trashpickupcalender.utils.getEventsByTownAndRegion

enum class FilterScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    StreetFilter(title = R.string.street_filter_title),
    Details(title = R.string.details_screen_title)
}


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartFilterScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val searchUiState by viewModel.searchUiState.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    if (isLoading) {
        CircularProgressIndicator()
    } else { Scaffold()
    { innerPadding ->
        val startDestination: String = when {
            (searchUiState.currentSelectedTown != null && searchUiState.currentSelectedTown!!.regions.isEmpty()) -> FilterScreen.Details.name
            (searchUiState.currentSelectedTown != null && searchUiState.currentSelectedTown!!.regions.isNotEmpty() && searchUiState.currentSelectedStreet != null) -> FilterScreen.Details.name
            (searchUiState.currentSelectedTown != null && searchUiState.currentSelectedTown!!.regions.isNotEmpty() && searchUiState.currentSelectedStreet == null) -> FilterScreen.StreetFilter.name
            else -> FilterScreen.Start.name
        }

        NavHost(
            navController,
            startDestination = startDestination,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(FilterScreen.Start.name) {
                FilterScreen(
                    viewModel = viewModel,
                    onTownClick = {
                        if (it.regions.isEmpty()) {
                            viewModel.setEvents(getEventsByTown(it))
                            navController.navigate(FilterScreen.Details.name) {
                                popUpTo(FilterScreen.Details.name)
                            }
                        } else {
                            viewModel.setRegions(it)
                            navController.navigate(FilterScreen.StreetFilter.name)
                        }
                    }
                )
            }
            composable(
                FilterScreen.Details.name
            ) {
                EventCalenderScreen(viewModel, navController)
            }
            composable(
                FilterScreen.StreetFilter.name
            ) {
                StreetFilterScreen(viewModel = viewModel,
                    onRegionClick = {
                        if (searchUiState.currentSelectedTown != null) {
                            viewModel.setEvents(
                                getEventsByTownAndRegion(
                                    searchUiState.currentSelectedTown!!,
                                    it
                                )
                            )
                        }
                        navController.navigate(FilterScreen.Details.name) {
                            popUpTo(FilterScreen.Details.name)
                        }
                    })
            }
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    viewModel: MainViewModel,
    onTownClick: (Town) -> Unit
) {
    val searchText by viewModel.searchText.collectAsState()
    val towns by viewModel.towns.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "GABL ABFUHRKALENDER",
            fontSize = 25.sp, modifier = Modifier.padding(top = 5.dp)
        )
        OutlinedTextField(
            value = searchText,
            label = { Text(text = "Gemeinde wählen") },
            onValueChange = viewModel::onSearchTextChange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))
        if (isSearching) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(towns) { town ->
                    Text(text = town.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 16.dp)
                            .clickable {
                                viewModel.clearSearchText()
                                viewModel.setSelectedTown(town)
                                onTownClick(town)
                            }
                    )
                }
            }
        }
    }
}
