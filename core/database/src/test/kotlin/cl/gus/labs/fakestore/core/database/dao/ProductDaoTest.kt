package cl.gus.labs.fakestore.core.database.dao

import androidx.room.Room
import cl.gus.labs.fakestore.core.database.FakeStoreDatabase
import cl.gus.labs.fakestore.core.database.entity.ProductEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ProductDaoTest {
    private val databaseName = "product-dao-test-${System.nanoTime()}.db"
    private lateinit var database: FakeStoreDatabase
    private lateinit var dao: ProductDao

    private val product = ProductEntity(
        id = 1,
        title = "widget",
        price = 9.99,
        description = "a widget",
        category = "misc",
        imageUrl = "https://example.com/widget.png",
        ratingRate = 4.5,
        ratingCount = 10,
    )

    private val otherProduct = ProductEntity(
        id = 2,
        title = "gadget",
        price = 19.99,
        description = "a gadget",
        category = "tools",
        imageUrl = "https://example.com/gadget.png",
        ratingRate = 3.5,
        ratingCount = 5,
    )

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        database = Room.databaseBuilder(context, FakeStoreDatabase::class.java, databaseName).build()
        dao = database.productDao()
    }

    @After
    fun tearDown() {
        database.close()
        RuntimeEnvironment.getApplication().deleteDatabase(databaseName)
    }

    @Test
    fun `upsertAll then observeAll emits what was inserted`() = runTest {
        dao.upsertAll(listOf(product))

        assertEquals(listOf(product), dao.observeAll(category = null).first())
    }

    @Test
    fun `observeAll with a category returns only matching rows`() = runTest {
        dao.upsertAll(listOf(product, otherProduct))

        assertEquals(listOf(otherProduct), dao.observeAll(category = "tools").first())
    }

    @Test
    fun `observeAll orders by id ascending regardless of insert order`() = runTest {
        dao.upsertAll(listOf(otherProduct, product))

        assertEquals(listOf(product, otherProduct), dao.observeAll(category = null).first())
    }

    @Test
    fun `observeById returns the matching row`() = runTest {
        dao.upsertAll(listOf(product))

        assertEquals(product, dao.observeById(1).first())
    }

    @Test
    fun `observeById returns null when nothing matches`() = runTest {
        assertNull(dao.observeById(99).first())
    }

    @Test
    fun `upsertAll replaces a row with the same id`() = runTest {
        dao.upsertAll(listOf(product))
        val updated = product.copy(title = "updated widget")

        dao.upsertAll(listOf(updated))

        assertEquals(listOf(updated), dao.observeAll(category = null).first())
    }

    @Test
    fun `syncAll prunes rows that were not in the new snapshot`() = runTest {
        dao.upsertAll(listOf(product, otherProduct))

        dao.syncAll(listOf(otherProduct))

        assertEquals(listOf(otherProduct), dao.observeAll(category = null).first())
    }

    @Test
    fun `syncAll with an empty list empties the table`() = runTest {
        dao.upsertAll(listOf(product, otherProduct))

        dao.syncAll(emptyList())

        assertEquals(emptyList<ProductEntity>(), dao.observeAll(category = null).first())
    }
}
