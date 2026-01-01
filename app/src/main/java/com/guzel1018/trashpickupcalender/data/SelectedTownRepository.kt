package com.guzel1018.trashpickupcalender.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface DataStoreManager {
    suspend fun saveAddress(townName: String?, streetName: String?)
    fun getAddress(): Flow<UserAddress>
    suspend fun deleteAddress()
    suspend fun saveReminderPreferences(dayOption: String, hour: Int, minute: Int)
    fun getReminderPreferences(): Flow<ReminderPreferences>
}

data class UserAddress(
    val townName: String? = null,
    val streetName: String? = null
)

data class ReminderPreferences(
    val dayOption: String = "DAY_BEFORE",
    val hour: Int = 15,
    val minute: Int = 0
)

class DataStoreManagerImpl @Inject constructor(
    private val addressPreferenceStore: DataStore<Preferences>
): DataStoreManager {

    private val SELECTED_TOWN = stringPreferencesKey("selected_town")
    private val SELECTED_STREET = stringPreferencesKey("selected_street")
    private val REMINDER_DAY_OPTION = stringPreferencesKey("reminder_day_option")
    private val REMINDER_HOUR = intPreferencesKey("reminder_hour")
    private val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    
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
    
    override suspend fun saveReminderPreferences(dayOption: String, hour: Int, minute: Int) {
        addressPreferenceStore.edit { preferences ->
            preferences[REMINDER_DAY_OPTION] = dayOption
            preferences[REMINDER_HOUR] = hour
            preferences[REMINDER_MINUTE] = minute
        }
    }
    
    override fun getReminderPreferences(): Flow<ReminderPreferences> = 
        addressPreferenceStore.data.map { preferences ->
            ReminderPreferences(
                dayOption = preferences[REMINDER_DAY_OPTION] ?: "DAY_BEFORE",
                hour = preferences[REMINDER_HOUR] ?: 15,
                minute = preferences[REMINDER_MINUTE] ?: 0
            )
        }
}


