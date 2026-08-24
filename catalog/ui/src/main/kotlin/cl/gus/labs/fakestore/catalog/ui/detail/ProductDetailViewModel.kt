package cl.gus.labs.fakestore.catalog.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.gus.labs.fakestore.catalog.domain.model.Product
import cl.gus.labs.fakestore.catalog.domain.usecase.ObserveLastSyncedAt
import cl.gus.labs.fakestore.catalog.domain.usecase.ObserveProductDetail
import cl.gus.labs.fakestore.catalog.domain.usecase.RefreshCatalog
import cl.gus.labs.fakestore.catalog.ui.RefreshState
import cl.gus.labs.fakestore.catalog.ui.mapper.toDetail
import cl.gus.labs.fakestore.core.common.result.fold
import cl.gus.labs.fakestore.core.connectivity.NetworkMonitor
import cl.gus.labs.fakestore.shared.kernel.ProductId
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val StopTimeoutMillis = 5_000L

@HiltViewModel(assistedFactory = ProductDetailViewModel.Factory::class)
internal class ProductDetailViewModel @AssistedInject constructor(
    @Assisted productId: Int,
    private val observeProductDetail: ObserveProductDetail,
    observeLastSyncedAt: ObserveLastSyncedAt,
    private val refreshCatalog: RefreshCatalog,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(productId: Int): ProductDetailViewModel
    }

    private val id = ProductId(productId)
    private val refreshState = MutableStateFlow<RefreshState>(RefreshState.Idle)

    val uiState: StateFlow<ProductDetailUiState> = combine(
        observeProductDetail(id),
        observeLastSyncedAt(),
        refreshState,
        networkMonitor.isOnline,
    ) { product, lastSyncedAt, refresh, isOnline ->
        ProductDetailUiState(
            content = reduce(product, lastSyncedAt, refresh, isOnline),
            lastSyncedAt = lastSyncedAt,
            isStale = refresh is RefreshState.Failed && product != null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(StopTimeoutMillis),
        initialValue = ProductDetailUiState(),
    )

    init {
        viewModelScope.launch {
            if (observeProductDetail(id).first() == null) launchRefresh()
        }
        viewModelScope.launch {
            networkMonitor.isOnline
                .distinctUntilChanged()
                .drop(1)
                .filter { isOnline -> isOnline }
                .collect {
                    if (refreshState.value is RefreshState.Failed) launchRefresh()
                }
        }
    }

    fun refresh() = launchRefresh()

    private fun launchRefresh() {
        if (refreshState.value == RefreshState.InFlight) return
        refreshState.value = RefreshState.InFlight
        viewModelScope.launch {
            refreshCatalog().fold(
                onError = { error -> refreshState.value = RefreshState.Failed(error) },
                onSuccess = { refreshState.value = RefreshState.Idle },
            )
        }
    }
}

private fun reduce(
    product: Product?,
    lastSyncedAt: Instant?,
    refresh: RefreshState,
    isOnline: Boolean,
): ProductDetailContent = when {
    product != null -> ProductDetailContent.Ready(product.toDetail())
    refresh is RefreshState.Failed -> ProductDetailContent.Failure(refresh.error, offline = !isOnline)
    lastSyncedAt != null -> ProductDetailContent.Unavailable
    else -> ProductDetailContent.Loading
}
