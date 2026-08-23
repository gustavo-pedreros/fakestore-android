package cl.gus.labs.fakestore.catalog.data.di

import cl.gus.labs.fakestore.catalog.data.CatalogRepositoryImpl
import cl.gus.labs.fakestore.catalog.data.datasource.CatalogLocalDataSource
import cl.gus.labs.fakestore.catalog.data.datasource.CatalogLocalDataSourceImpl
import cl.gus.labs.fakestore.catalog.data.datasource.CatalogRemoteDataSource
import cl.gus.labs.fakestore.catalog.data.datasource.CatalogRemoteDataSourceImpl
import cl.gus.labs.fakestore.catalog.domain.CatalogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface CatalogDataBindsModule {

    @Binds
    fun bindCatalogRepository(impl: CatalogRepositoryImpl): CatalogRepository

    @Binds
    fun bindLocalDataSource(impl: CatalogLocalDataSourceImpl): CatalogLocalDataSource

    @Binds
    fun bindRemoteDataSource(impl: CatalogRemoteDataSourceImpl): CatalogRemoteDataSource
}
