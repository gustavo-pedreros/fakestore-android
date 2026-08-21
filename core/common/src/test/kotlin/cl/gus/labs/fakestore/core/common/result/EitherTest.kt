package cl.gus.labs.fakestore.core.common.result

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Either")
class EitherTest {

    private val success: Either<String, Int> = Either.Success(2)
    private val error: Either<String, Int> = Either.Error("boom")

    @Nested
    @DisplayName("flags")
    inner class Flags {

        @Test
        @DisplayName("isSuccess is true only for Success")
        fun isSuccessIsTrueOnlyForSuccess() {
            assertTrue(success.isSuccess)
            assertFalse(error.isSuccess)
        }

        @Test
        @DisplayName("isError is true only for Error")
        fun isErrorIsTrueOnlyForError() {
            assertTrue(error.isError)
            assertFalse(success.isError)
        }
    }

    @Nested
    @DisplayName("map")
    inner class Map {

        @Test
        @DisplayName("transforms the success value")
        fun transformsSuccessValue() {
            assertEquals(Either.Success(4), success.map { it * 2 })
        }

        @Test
        @DisplayName("leaves an error untouched")
        fun leavesErrorUntouched() {
            assertEquals(error, error.map { it * 2 })
        }
    }

    @Nested
    @DisplayName("flatMap")
    inner class FlatMap {

        @Test
        @DisplayName("chains another step on success")
        fun chainsAnotherStepOnSuccess() {
            assertEquals(Either.Success(20), success.flatMap { Either.Success(it * 10) })
        }

        @Test
        @DisplayName("can short-circuit to an error on success")
        fun shortCircuitsToErrorOnSuccess() {
            assertEquals(Either.Error("invalid"), success.flatMap { Either.Error("invalid") })
        }

        @Test
        @DisplayName("leaves an error untouched")
        fun leavesErrorUntouched() {
            assertEquals(error, error.flatMap { Either.Success(it * 10) })
        }
    }

    @Nested
    @DisplayName("mapError")
    inner class MapError {

        @Test
        @DisplayName("transforms the error value")
        fun transformsErrorValue() {
            assertEquals(Either.Error(4), error.mapError { it.length })
        }

        @Test
        @DisplayName("leaves a success untouched")
        fun leavesSuccessUntouched() {
            assertEquals(success, success.mapError { it.length })
        }
    }

    @Nested
    @DisplayName("fold")
    inner class Fold {

        @Test
        @DisplayName("collapses a success via onSuccess")
        fun collapsesSuccess() {
            assertEquals("ok:2", success.fold(onError = { "err:$it" }, onSuccess = { "ok:$it" }))
        }

        @Test
        @DisplayName("collapses an error via onError")
        fun collapsesError() {
            assertEquals("err:boom", error.fold(onError = { "err:$it" }, onSuccess = { "ok:$it" }))
        }
    }

    @Nested
    @DisplayName("getOrNull / errorOrNull")
    inner class Extraction {

        @Test
        @DisplayName("getOrNull returns the value or null")
        fun getOrNullReturnsValueOrNull() {
            assertEquals(2, success.getOrNull())
            assertNull(error.getOrNull())
        }

        @Test
        @DisplayName("errorOrNull returns the error or null")
        fun errorOrNullReturnsErrorOrNull() {
            assertEquals("boom", error.errorOrNull())
            assertNull(success.errorOrNull())
        }
    }
}
