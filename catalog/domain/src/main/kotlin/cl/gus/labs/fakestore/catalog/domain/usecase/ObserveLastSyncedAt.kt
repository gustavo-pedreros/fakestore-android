package cl.gus.labs.fakestore.catalog.domain.usecase

import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

fun interface ObserveLastSyncedAt {
    operator fun invoke(): Flow<Instant?>
}
