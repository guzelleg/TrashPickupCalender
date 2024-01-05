package com.guzel1018.trashpickupcalender.di

import com.guzel1018.trashpickupcalender.data.DataStoreManager
import com.guzel1018.trashpickupcalender.data.DataStoreManagerImpl
import com.guzel1018.trashpickupcalender.service.AddressService
import com.guzel1018.trashpickupcalender.service.AddressServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataStoreManagerModule {

    @Singleton
    @Binds
    abstract fun bindDataStoreRepository(dataStoreManagerImpl: DataStoreManagerImpl): DataStoreManager

    @Singleton
    @Binds
    abstract fun bindTaskService(addressServiceImpl: AddressServiceImpl): AddressService
}