package bot.features.core.data

import bot.features.core.noChangeUpdate
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class UtilsKtTest {

    @Test
    fun `Should return the same object`() {
        // GIVEN
        val expected = "Subject"

        // WHEN
        val result = noChangeUpdate<String>(expected)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should throw an exception when data types do not correspond`() {
        // GIVEN
        val expected = 24

        // THEN
        assertFailsWith(IllegalStateException::class) { noChangeUpdate<String>(expected) }
    }
}