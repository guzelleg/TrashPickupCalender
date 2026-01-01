package com.guzel1018.trashpickupcalender.service

import com.guzel1018.trashpickupcalender.data.DataStoreManager
import com.guzel1018.trashpickupcalender.data.ReminderPreferences
import com.guzel1018.trashpickupcalender.data.UserAddress
import com.guzel1018.trashpickupcalender.model.Region
import com.guzel1018.trashpickupcalender.model.Town
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.InternalSerializationApi
import javax.inject.Inject

@OptIn(InternalSerializationApi::class)
interface AddressService {
    fun getAddressFromDataStore(): Flow<UserAddress>
    suspend fun addAddress(town: Town, street: Region?)
    suspend fun deleteAddress()
    suspend fun saveReminderPreferences(dayOption: String, hour: Int, minute: Int)
    fun getReminderPreferences(): Flow<ReminderPreferences>
}

@OptIn(InternalSerializationApi::class)
class AddressServiceImpl @Inject constructor(
    private val DataStoreManager: DataStoreManager
) : AddressService {
    override fun getAddressFromDataStore(): Flow<UserAddress> =
        DataStoreManager.getAddress()

    override suspend fun addAddress(town: Town, street: Region?) {
        DataStoreManager.saveAddress(town.name, street?.name)
    }

    override suspend fun deleteAddress() = DataStoreManager.deleteAddress()
    
    override suspend fun saveReminderPreferences(dayOption: String, hour: Int, minute: Int) =
        DataStoreManager.saveReminderPreferences(dayOption, hour, minute)
    
    override fun getReminderPreferences(): Flow<ReminderPreferences> =
        DataStoreManager.getReminderPreferences()
}
