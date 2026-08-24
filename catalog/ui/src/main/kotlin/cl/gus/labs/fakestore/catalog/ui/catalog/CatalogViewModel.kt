package cl.gus.labs.fakestore.catalog.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.gus.labs.fakestore.catalog.domain.model.Category
import cl.gus.labs.fakestore.catalog.domain.model.Product
import cl.gus.labs.fakestore.catalog.domain.usecase.ObserveCatalog
import cl.gus.labs.fakestore.catalog.domain.usecase.ObserveCategories
import cl.gus.labs.fakestore.catalog.domain.usecase.ObserveLastSyncedAt
import cl.gus.labs.fakestore.catalog.domain.usecase.RefreshCatalog
import cl.gus.labs.fakestore.catalog.ui.RefreshState
import cl.gus.labs.fakestore.catalog.ui.mapper.toCard
import cl.gus.labs.fakestore.core.common.result.fold
import cl.gus.labs.fakestore.core.connectivity.NetworkMonitor
import cl.gus.labs.fakestore.favorites.domain.usecase.ObserveFavoriteIds
import cl.gus.labs.fakestore.favorites.domain.usecase.ToggleFavorite
import cl.gus.labs.fakestore.shared.kernel.AppError
import cl.gus.labs.fakestore.shared.kernel.ProductId
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val StopTimeoutMillis = 5_000L

@HiltViewModel
internal class CatalogViewModel @Inject constructor(
    observeCatalog: ObserveCatalog,
    observeCategories: ObserveCategories,
    observeLastSyncedAt: ObserveLastSyncedAt,
    observeFavoriteIds: ObserveFavoriteIds,
    private val refreshCatalog: RefreshCatalog,
    private val toggleFavorite: ToggleFavorite,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val selectedCategory = MutableStateFlow<Category?>(null)
    private val refreshState = MutableStateFlow<RefreshState>(RefreshState.Idle)
    private val errors = Channel<AppError>(Channel.BUFFERED)

    val errorEvents: Flow<AppError> = errors.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val slice: Flow<CatalogSlice> = combine(
        selectedCategory.flatMapLatest { category ->
            observeCatalog(category).map { products -> category to products }
        },
        observeFavoriteIds(),
    ) { (category, products), favoriteIds -> CatalogSlice(category, products, favoriteIds) }

    val uiState: StateFlow<CatalogUiState> = combine(
        slice,
        observeCategories(),
        observeLastSyncedAt(),
        refreshState,
        networkMonitor.isOnline,
    ) { slice, categories, lastSyncedAt, refresh, isOnline ->
        CatalogUiState(
            content = reduce(slice.products, lastSyncedAt, refresh, isOnline),
            categories = categories.map(Category::value),
            selectedCategory = slice.category?.value,
            favoriteIds = slice.favoriteIds.mapTo(mutableSetOf(), ProductId::value),
            lastSyncedAt = lastSyncedAt,
            isStale = refresh is RefreshState.Failed && slice.products.isNotEmpty(),
            isRefreshing = refresh is RefreshState.InFlight && slice.products.isNotEmpty(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(StopTimeoutMillis),
        initialValue = CatalogUiState(),
    )

    init {
        launchRefresh(userInitiated = false)
        viewModelScope.launch {
            networkMonitor.isOnline
                .distinctUntilChanged()
                .drop(1)
                .filter { isOnline -> isOnline }
                .collect {
                    if (refreshState.value is RefreshState.Failed) launchRefresh(userInitiated = false)
                }
        }
    }

    fun refresh() = launchRefresh(userInitiated = true)

    fun onCategorySelect(category: String?) {
        selectedCategory.value = category?.let(::Category)
    }

    fun onFavoriteToggle(productId: Int) {
        viewModelScope.launch { toggleFavorite(ProductId(productId)) }
    }

    private fun launchRefresh(userInitiated: Boolean) {
        if (refreshState.value == RefreshState.InFlight) return
        refreshState.value = RefreshState.InFlight
        viewModelScope.launch {
            refreshCatalog().fold(
                onError = { error ->
                    refreshState.value = RefreshState.Failed(error)
                    if (userInitiated) errors.trySend(error)
                },
                onSuccess = { refreshState.value = RefreshState.Idle },
            )
        }
    }
}

private data class CatalogSlice(
    val category: Category?,
    val products: List<Product>,
    val favoriteIds: Set<ProductId>,
)

private fun reduce(
    products: List<Product>,
    lastSyncedAt: Instant?,
    refresh: RefreshState,
    isOnline: Boolean,
): CatalogContent = when {
    products.isNotEmpty() -> CatalogContent.Ready(products.map(Product::toCard))
    refresh is RefreshState.Failed -> CatalogContent.Failure(refresh.error, offline = !isOnline)
    lastSyncedAt != null -> CatalogContent.Empty
    else -> CatalogContent.Loading
}
