package cl.gus.labs.fakestore.favorites.data.di

import cl.gus.labs.fakestore.favorites.data.FavoritesRepositoryImpl
import cl.gus.labs.fakestore.favorites.data.datasource.FavoritesLocalDataSource
import cl.gus.labs.fakestore.favorites.data.datasource.FavoritesLocalDataSourceImpl
import cl.gus.labs.fakestore.favorites.domain.FavoritesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface FavoritesDataBindsModule {

    @Binds
    fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds
    fun bindLocalDataSource(impl: FavoritesLocalDataSourceImpl): FavoritesLocalDataSource
}
