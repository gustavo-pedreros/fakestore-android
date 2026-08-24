package cl.gus.labs.fakestore.favorites.data.di

import cl.gus.labs.fakestore.favorites.domain.FavoritesRepository
import cl.gus.labs.fakestore.favorites.domain.usecase.ObserveFavoriteIds
import cl.gus.labs.fakestore.favorites.domain.usecase.ToggleFavorite
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object FavoritesDataModule {

    @Provides
    fun provideObserveFavoriteIds(repository: FavoritesRepository): ObserveFavoriteIds =
        ObserveFavoriteIds { repository.observeIds() }

    @Provides
    fun provideToggleFavorite(repository: FavoritesRepository): ToggleFavorite =
        ToggleFavorite { id -> repository.toggle(id) }
}
