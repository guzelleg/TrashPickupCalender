package com.guzel1018.trashpickupcalender.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface DataStoreManager {
    suspend fun saveAddress(townName: String?, streetName: String?)
    fun getAddress(): Flow<UserAddress>
    suspend fun deleteAddress()
}

data class UserAddress(
    val townName: String? = null,
    val streetName: String? = null
)

class DataStoreManagerImpl @Inject constructor(
    private val addressPreferenceStore: DataStore<Preferences>
): DataStoreManager {

    private val SELECTED_TOWN = stringPreferencesKey("selected_town")
    private val SELECTED_STREET = stringPreferencesKey("selected_street")
    override suspend fun saveAddress(townName: String?, streetName: String?) {
       addressPreferenceStore.edit {
           addressPreferenceStore -> addressPreferenceStore[SELECTED_TOWN] = townName ?: ""
           addressPreferenceStore[SELECTED_STREET] = streetName ?: ""
       }
    }
    override fun getAddress(): Flow<UserAddress> = addressPreferenceStore.data.map{
        addressPreference ->
        UserAddress(
            townName = addressPreference[SELECTED_TOWN] ?: "",
            streetName = addressPreference[SELECTED_STREET]
        )
    }

    override suspend fun deleteAddress() {
        addressPreferenceStore.edit { addressPreferenceStore ->
            addressPreferenceStore[SELECTED_TOWN] = ""
            addressPreferenceStore[SELECTED_STREET] = ""
        }
    }
}


