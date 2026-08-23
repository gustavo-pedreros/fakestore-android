package cl.gus.labs.fakestore.catalog.data.di

import cl.gus.labs.fakestore.catalog.data.api.CatalogApi
import cl.gus.labs.fakestore.catalog.domain.CatalogRepository
import cl.gus.labs.fakestore.catalog.domain.usecase.ObserveCatalog
import cl.gus.labs.fakestore.catalog.domain.usecase.ObserveCategories
import cl.gus.labs.fakestore.catalog.domain.usecase.ObserveLastSyncedAt
import cl.gus.labs.fakestore.catalog.domain.usecase.ObserveProductDetail
import cl.gus.labs.fakestore.catalog.domain.usecase.RefreshCatalog
import cl.gus.labs.fakestore.core.network.client.ApiServiceFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Clock

@Module
@InstallIn(SingletonComponent::class)
internal object CatalogDataModule {

    @Provides
    @Singleton
    fun provideCatalogApi(factory: ApiServiceFactory): CatalogApi = factory.create(CatalogApi::class.java)

    @Provides
    fun provideClock(): Clock = Clock.System

    @Provides
    fun provideObserveCatalog(repository: CatalogRepository): ObserveCatalog =
        ObserveCatalog { category -> repository.observeAll(category) }

    @Provides
    fun provideObserveProductDetail(repository: CatalogRepository): ObserveProductDetail =
        ObserveProductDetail { id -> repository.observeById(id) }

    @Provides
    fun provideObserveLastSyncedAt(repository: CatalogRepository): ObserveLastSyncedAt =
        ObserveLastSyncedAt { repository.observeLastSyncedAt() }

    @Provides
    fun provideRefreshCatalog(repository: CatalogRepository): RefreshCatalog =
        RefreshCatalog { repository.refresh() }

    @Provides
    fun provideObserveCategories(repository: CatalogRepository): ObserveCategories =
        ObserveCategories(repository)
}
