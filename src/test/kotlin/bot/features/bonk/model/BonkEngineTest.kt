package bot.features.bonk.model

import bot.features.bonk.data.BonkRepository
import bot.features.bonk.model.results.BonkStandingsResult
import bot.features.bonk.model.results.IncrementBonkResult
import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class BonkEngineTest {

    @Mock
    val mockRepository: BonkRepository = Mockito.mock(BonkRepository::class.java)

    @Test
    fun `Should add the user to list and increment bonk count`() = runBlockingTest {
        // GIVEN
        val engine = BonkEngine(mockRepository)
        val guildId = Snowflake(0)
        val userId = Snowflake(1)

        val bonkStatistics = BonkStatistics(listOf())
        val expectedBonkedUser = BonkedUser(userId, bonkNumber = 1)
        val expectedBonkedStatistics = BonkStatistics(listOf(expectedBonkedUser))

        val expected = IncrementBonkResult.Success(expectedBonkedUser)

        Mockito.`when`(mockRepository.getBonkStatistics(guildId)).thenReturn(bonkStatistics)
        Mockito.`when`(mockRepository.updateBonkStatistics(expectedBonkedStatistics, guildId))
            .thenReturn(BonkRepository.UploadBonkStatisticsResult.Success)

        // WHEN
        val result = engine.incrementBonkCount(userId, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should increment bonk count of existing user`() = runBlockingTest {
        // GIVEN
        val engine = BonkEngine(mockRepository)
        val guildId = Snowflake(0)
        val userId = Snowflake(1)

        val bonkedUser = BonkedUser(userId, bonkNumber = 1)
        val bonkStatistics = BonkStatistics(listOf(bonkedUser))

        val expectedBonkedUser = BonkedUser(userId, bonkNumber = 2)
        val expectedBonkedStatistics = BonkStatistics(listOf(expectedBonkedUser))

        val expected = IncrementBonkResult.Success(expectedBonkedUser)

        Mockito.`when`(mockRepository.getBonkStatistics(guildId)).thenReturn(bonkStatistics)
        Mockito.`when`(mockRepository.updateBonkStatistics(expectedBonkedStatistics, guildId))
            .thenReturn(BonkRepository.UploadBonkStatisticsResult.Success)

        // WHEN
        val result = engine.incrementBonkCount(userId, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when something went wrong while updating bonk count`() = runBlockingTest {
        // GIVEN
        val engine = BonkEngine(mockRepository)
        val guildId = Snowflake(0)
        val userId = Snowflake(1)

        val bonkStatistics = BonkStatistics(listOf())
        val expectedBonkedUser = BonkedUser(userId, bonkNumber = 1)
        val expectedBonkedStatistics = BonkStatistics(listOf(expectedBonkedUser))

        val expected = IncrementBonkResult.Failure

        Mockito.`when`(mockRepository.getBonkStatistics(guildId)).thenReturn(bonkStatistics)
        Mockito.`when`(mockRepository.updateBonkStatistics(expectedBonkedStatistics, guildId))
            .thenReturn(BonkRepository.UploadBonkStatisticsResult.Failure)

        // WHEN
        val result = engine.incrementBonkCount(userId, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return the users with the most bonks`() = runBlockingTest {
        // GIVEN
        val engine = BonkEngine(mockRepository)
        val guildId = Snowflake(0)
        val userId1 = Snowflake(1)
        val userId2 = Snowflake(2)
        val userId3 = Snowflake(3)

        val bonkedUser1 = BonkedUser(userId1, bonkNumber = 1)
        val bonkedUser2 = BonkedUser(userId2, bonkNumber = 13)
        val bonkedUser3 = BonkedUser(userId3, bonkNumber = 5)
        val bonkStatistics = BonkStatistics(listOf(bonkedUser1, bonkedUser2, bonkedUser3))

        val expected = BonkStandingsResult.Success(listOf(bonkedUser2, bonkedUser3))

        Mockito.`when`(mockRepository.getBonkStatistics(guildId)).thenReturn(bonkStatistics)

        // WHEN
        val numberOfUsers = 2
        val result = engine.getBonkStandings(numberOfUsers, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when there are no bonked users to display`() = runBlockingTest {
        // GIVEN
        val engine = BonkEngine(mockRepository)
        val guildId = Snowflake(0)

        val bonkStatistics = BonkStatistics(listOf())

        val expected = BonkStandingsResult.NothingToDisplay

        Mockito.`when`(mockRepository.getBonkStatistics(guildId)).thenReturn(bonkStatistics)

        // WHEN
        val numberOfUsers = 2
        val result = engine.getBonkStandings(numberOfUsers, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return the number of bonks for the given user`() = runBlockingTest {
        // GIVEN
        val engine = BonkEngine(mockRepository)
        val guildId = Snowflake(0)
        val userId1 = Snowflake(1)
        val userId2 = Snowflake(2)
        val userId3 = Snowflake(3)

        val bonkedUser1 = BonkedUser(userId1, bonkNumber = 1)
        val bonkedUser2 = BonkedUser(userId2, bonkNumber = 13)
        val bonkedUser3 = BonkedUser(userId3, bonkNumber = 5)
        val bonkStatistics = BonkStatistics(listOf(bonkedUser1, bonkedUser2, bonkedUser3))

        val expected = 5

        Mockito.`when`(mockRepository.getBonkStatistics(guildId)).thenReturn(bonkStatistics)

        // WHEN
        val result = engine.getBonkScore(userId3, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return the number of bonks for an unregistered user`() = runBlockingTest {
        // GIVEN
        val engine = BonkEngine(mockRepository)
        val guildId = Snowflake(0)
        val userId = Snowflake(1)

        val bonkStatistics = BonkStatistics(listOf())

        val expected = 0
        Mockito.`when`(mockRepository.getBonkStatistics(guildId)).thenReturn(bonkStatistics)

        // WHEN
        val result = engine.getBonkScore(userId, guildId)

        // THEN
        assertEquals(expected, result)
    }
}