package com.guzel1018.trashpickupcalender.service

import com.guzel1018.trashpickupcalender.data.DataStoreManager
import com.guzel1018.trashpickupcalender.data.UserAddress
import com.guzel1018.trashpickupcalender.model.Region
import com.guzel1018.trashpickupcalender.model.Town
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface AddressService {
    fun getAddressFromDataStore(): Flow<UserAddress>
    suspend fun addAddress(town: Town, street: Region?)
}
class AddressServiceImpl @Inject constructor(
    private val DataStoreManager: DataStoreManager
) : AddressService {
    override fun getAddressFromDataStore(): Flow<UserAddress> =
        DataStoreManager.getAddress()

    override suspend fun addAddress(town: Town, street: Region?) {
        DataStoreManager.saveAddress(town.name, street?.name)
    }
}
