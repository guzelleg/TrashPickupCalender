package com.guzel1018.trashpickupcalender.ui

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.guzel1018.trashpickupcalender.clickable
import com.guzel1018.trashpickupcalender.model.Region
import kotlinx.serialization.InternalSerializationApi


@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class)
@Composable
fun StreetFilterScreen(
    searchText: String,
    regions: List<Region>,
    isSearching: Boolean,
    onSearchTextChange: (String) -> Unit,
    onClearSearchText: () -> Unit,
    onSetSelectedRegion: (Region) -> Unit,
    onRegionClick: (Region) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchText,
            label = { Text(text = "Straße finden") },
            onValueChange = onSearchTextChange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
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
                items(regions) { region ->
                    Text(text = region.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 16.dp)
                            .clickable {
                                onClearSearchText()
                                onSetSelectedRegion(region)
                                onRegionClick(region)
                            }
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun StreetFilterPreview() {
    // FilterScreen()
}
