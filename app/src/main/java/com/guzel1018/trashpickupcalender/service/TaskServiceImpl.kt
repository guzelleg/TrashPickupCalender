package com.guzel1018.trashpickupcalender.service

import com.guzel1018.trashpickupcalender.data.DataStoreManager
import com.guzel1018.trashpickupcalender.data.UserAddress
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface AddressService {
    fun getAddressFromPrefDataStore(): Flow<UserAddress>
    suspend fun addAddress(address: UserAddress)
}
class AddressServiceImpl @Inject constructor(
    private val DataStoreManager: DataStoreManager
) : AddressService {
    override fun getAddressFromPrefDataStore() =
        DataStoreManager.getAddress()

    override suspend fun addAddress(address: UserAddress) {
        DataStoreManager.saveAddress(address.townName, address.streetName)
    }

}
