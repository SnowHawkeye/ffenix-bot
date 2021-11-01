package bot.features.speech.model

import bot.features.speech.data.SpeechRepository
import bot.features.speech.model.results.SetUwuModeResult
import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class SpeechEngineTest {

    @Mock
    val mockRepository: SpeechRepository = Mockito.mock(SpeechRepository::class.java)

    @Test
    fun `Should return true when mode is activated`() = runBlockingTest {
        // GIVEN
        val engine = SpeechEngine(mockRepository)
        val guildId = Snowflake(1)

        Mockito.`when`(mockRepository.getUwuMode(guildId)).thenReturn(true)
        val expected = true

        // WHEN
        val result = engine.isUwuModeActivated(guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return false when mode is disabled`() = runBlockingTest {
        // GIVEN
        val engine = SpeechEngine(mockRepository)
        val guildId = Snowflake(1)

        Mockito.`when`(mockRepository.getUwuMode(guildId)).thenReturn(false)
        val expected = false

        // WHEN
        val result = engine.isUwuModeActivated(guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success when mode is successfully updated`() = runBlockingTest {
        // GIVEN
        val engine = SpeechEngine(mockRepository)
        val guildId = Snowflake(1)

        Mockito.`when`(mockRepository.setUwuMode(true, guildId))
            .thenReturn(SpeechRepository.UpdateSpeechModeResult.Success)
        val expected = SetUwuModeResult.Success

        // WHEN
        val result = engine.setUwuMode(true, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when an error occurs while updating speech pattern`() = runBlockingTest {
        // GIVEN
        val engine = SpeechEngine(mockRepository)
        val guildId = Snowflake(1)

        Mockito.`when`(mockRepository.setUwuMode(true, guildId))
            .thenReturn(SpeechRepository.UpdateSpeechModeResult.Failure)
        val expected = SetUwuModeResult.Failure

        // WHEN
        val result = engine.setUwuMode(true, guildId)

        // THEN
        assertEquals(expected, result)
    }

}