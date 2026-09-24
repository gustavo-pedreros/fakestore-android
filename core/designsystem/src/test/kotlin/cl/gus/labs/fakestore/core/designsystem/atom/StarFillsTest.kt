package cl.gus.labs.fakestore.core.designsystem.atom

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("starFills")
class StarFillsTest {

    @Test
    @DisplayName("4.49 has no partial star")
    fun belowBoundary() {
        assertEquals(
            listOf(StarFill.Filled, StarFill.Filled, StarFill.Filled, StarFill.Filled, StarFill.Empty),
            starFills(4.49),
        )
    }

    @Test
    @DisplayName("4.5 fills a partial star")
    fun atBoundary() {
        assertEquals(
            listOf(StarFill.Filled, StarFill.Filled, StarFill.Filled, StarFill.Filled, StarFill.Partial),
            starFills(4.5),
        )
    }

    @Test
    @DisplayName("today's 4.95 rounds down to a partial 5th star, not a 6th")
    fun pinsTodaysRating() {
        assertEquals(
            listOf(StarFill.Filled, StarFill.Filled, StarFill.Filled, StarFill.Filled, StarFill.Partial),
            starFills(4.95),
        )
    }

    @Test
    @DisplayName("clamps a rate above 5 to all filled")
    fun clampsAboveMax() {
        assertEquals(List(5) { StarFill.Filled }, starFills(7.0))
    }

    @Test
    @DisplayName("clamps a negative rate to all empty")
    fun clampsBelowMin() {
        assertEquals(List(5) { StarFill.Empty }, starFills(-1.0))
    }

    @Test
    @DisplayName("treats NaN as all empty")
    fun handlesNaN() {
        assertEquals(List(5) { StarFill.Empty }, starFills(Double.NaN))
    }
}
