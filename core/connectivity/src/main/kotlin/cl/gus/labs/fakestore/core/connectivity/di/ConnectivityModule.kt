package cl.gus.labs.fakestore.core.connectivity.di

import cl.gus.labs.fakestore.core.connectivity.ConnectivityNetworkMonitor
import cl.gus.labs.fakestore.core.connectivity.NetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface ConnectivityModule {

    @Binds
    @Singleton
    fun bindNetworkMonitor(impl: ConnectivityNetworkMonitor): NetworkMonitor
}
