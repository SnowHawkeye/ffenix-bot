package bot.features.scheduling.model

import bot.features.scheduling.data.SchedulingDataStructure
import bot.features.scheduling.data.SchedulingRepository
import bot.features.scheduling.data.dateFormattingPattern
import bot.features.scheduling.data.defaultTimezone
import bot.features.scheduling.model.UpcomingEvent.*
import bot.features.scheduling.model.results.*
import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.datetime.*
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class SchedulingEngineTest {

    @Test
    fun `Should return initial data structure`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine.instance()

        val expectedDefaultRaidSchedule = DefaultRaidSchedule(listOf(), defaultTimezone)
        val schedule = Schedule(expectedDefaultRaidSchedule, listOf(), listOf(), listOf())

        val expected = SchedulingDataStructure(schedule)

        // WHEN
        val result = engine.makeInitialDataStructure()

        // THEN
        assertEquals(expected, result)

    }

    @Mock
    private val mockRepository: SchedulingRepository = Mockito.mock(SchedulingRepository::class.java)

    @Test
    fun `Should set default timezone with success`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val existingDefaultSchedule = DefaultRaidSchedule(listOf(), "CET")
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val timezoneId = "UTC+3"
        val expectedDefaultSchedule = existingDefaultSchedule.copy(defaultTimezoneId = timezoneId)
        val expectedSchedule = schedule.copy(defaultRaidSchedule = expectedDefaultSchedule)

        val expected = SetTimezoneResult.Success

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Success)

        // WHEN
        val result = engine.setDefaultTimezone(timezoneId, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when timezone ID is incorrect`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val existingDefaultSchedule = DefaultRaidSchedule(listOf(), "CET")
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val timezoneId = "1"
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

        val expected = SetTimezoneResult.IncorrectTimezoneId

        // WHEN
        val result = engine.setDefaultTimezone(timezoneId, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when problem occurred while setting default timezone`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val existingDefaultSchedule = DefaultRaidSchedule(listOf(), "CET")
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val timezoneId = "UTC+3"
        val expectedDefaultSchedule = existingDefaultSchedule.copy(defaultTimezoneId = timezoneId)
        val expectedSchedule = schedule.copy(defaultRaidSchedule = expectedDefaultSchedule)

        val expected = SetTimezoneResult.Failure

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Failure)

        // WHEN
        val result = engine.setDefaultTimezone(timezoneId, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success when default raid day was added successfully`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val existingDefaultSchedule = DefaultRaidSchedule(listOf(), "CET")
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val comment = "Some comment"

        val expectedDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, comment)
        val expectedDefaultRaidSchedule = DefaultRaidSchedule(listOf(expectedDefaultRaid), "CET")
        val expectedSchedule = Schedule(expectedDefaultRaidSchedule, listOf(), listOf(), listOf())

        val expected = AddDefaultRaidResult.Success(expectedDefaultRaidSchedule)

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Success)

        // WHEN
        val result = engine.addDefaultRaid(
            day = "Monday",
            time = "11:00",
            timezoneId = "UTC+2",
            comment = comment,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when adding a default raid day with an incorrect day of the week`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val comment = "Some comment"
        val expected = AddDefaultRaidResult.Failure

        // WHEN
        val result = engine.addDefaultRaid(
            day = "not a day",
            time = "11:00",
            timezoneId = "UTC+2",
            comment = comment,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when adding a default raid day with an incorrect timezone id`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val comment = "Some comment"
        val expected = AddDefaultRaidResult.IncorrectTimezoneId

        // WHEN
        val result = engine.addDefaultRaid(
            day = "Monday",
            time = "11:00",
            timezoneId = "not a timezone",
            comment = comment,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when adding a default raid day with incorrect time format`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val comment = "Some comment"
        val expected = AddDefaultRaidResult.IncorrectTimeFormat

        // WHEN
        val result = engine.addDefaultRaid(
            day = "Monday",
            time = "not a time",
            timezoneId = "UTC+2",
            comment = comment,
            forGuildId = guildId

        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when adding a default raid day with incorrect minutes`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val comment = "Some comment"
        val expected = AddDefaultRaidResult.IncorrectTimeFormat

        // WHEN
        val result = engine.addDefaultRaid(
            day = "Monday",
            time = "05:62",
            timezoneId = "UTC+2",
            comment = comment,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when adding a default raid day with incorrect hours`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val comment = "Some comment"
        val expected = AddDefaultRaidResult.IncorrectTimeFormat

        // WHEN
        val result = engine.addDefaultRaid(
            day = "Monday",
            time = "25:30",
            timezoneId = "UTC+2",
            comment = comment,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when adding a default raid day that already exists`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, comment = null)
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val comment = "Some comment"

        val expected = AddDefaultRaidResult.RaidAlreadyExists
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

        // WHEN
        val result = engine.addDefaultRaid(
            day = "Monday",
            time = "11:00",
            timezoneId = "UTC+2",
            comment = comment,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }


    @Test
    fun `Should return failure when an error occurred while adding a default raid day`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val existingDefaultSchedule = DefaultRaidSchedule(listOf(), "CET")
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val comment = "Some comment"

        val expectedDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, comment)
        val expectedDefaultRaidSchedule = DefaultRaidSchedule(listOf(expectedDefaultRaid), "CET")
        val expectedSchedule = Schedule(expectedDefaultRaidSchedule, listOf(), listOf(), listOf())

        val expected = AddDefaultRaidResult.Failure

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Failure)

        // WHEN
        val result = engine.addDefaultRaid(
            day = "Monday",
            time = "11:00",
            timezoneId = "UTC+2",
            comment = comment,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success when default raid day was removed successfully`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val expectedDefaultRaidSchedule = DefaultRaidSchedule(listOf(), "CET")
        val expectedSchedule = Schedule(expectedDefaultRaidSchedule, listOf(), listOf(), listOf())

        val expected = RemoveDefaultRaidResult.Success

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Success)

        // WHEN
        val result = engine.removeDefaultRaid(
            day = "Monday",
            time = "11:00",
            timezoneId = "UTC+2",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when something went wrong while removing a default raid`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val expectedDefaultRaidSchedule = DefaultRaidSchedule(listOf(), "CET")
        val expectedSchedule = Schedule(expectedDefaultRaidSchedule, listOf(), listOf(), listOf())

        val expected = RemoveDefaultRaidResult.Failure

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Failure)

        // WHEN
        val result = engine.removeDefaultRaid(
            day = "Monday",
            time = "11:00",
            timezoneId = "UTC+2",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to remove a default raid that does not exist`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val expected = RemoveDefaultRaidResult.RaidDoesNotExist

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

        // WHEN
        val result = engine.removeDefaultRaid(
            day = "Monday",
            time = "10:00",
            timezoneId = "UTC+2",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to remove a default raid with incorrect timezone id`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val expected = RemoveDefaultRaidResult.IncorrectTimezoneId

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

        // WHEN
        val result = engine.removeDefaultRaid(
            day = "Monday",
            time = "11:00",
            timezoneId = "bad timezone",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to remove a default raid with incorrect time format`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val expected = RemoveDefaultRaidResult.IncorrectTimeFormat

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

        // WHEN
        val result = engine.removeDefaultRaid(
            day = "Monday",
            time = "10:60",
            timezoneId = "UTC+2",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when trying to remove a default raid with incorrect day of the week`() =
        runBlockingTest {
            // GIVEN
            val engine = SchedulingEngine(mockRepository)
            val guildId = Snowflake(1)

            val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
            val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
            val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

            val expected = RemoveDefaultRaidResult.Failure

            Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

            // WHEN
            val result = engine.removeDefaultRaid(
                day = "Mon day",
                time = "11:00",
                timezoneId = "UTC+2",
                forGuildId = guildId
            )

            // THEN
            assertEquals(expected, result)
        }

    @Mock
    val mockClock: Clock = Mockito.mock(Clock::class.java)

    @Test
    fun `Should return success when default raid was successfully cancelled`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-01-04T00:00:00.00").toInstant(TimeZone.UTC) // Monday, 2021, Jan 4th

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val cancellationComment = "Some reason for cancellation"

        val expectedDate = LocalDateTime.parse("2022-07-04T09:00:00.00").toInstant(TimeZone.UTC)
        val expectedCancelledDefaultRaid = CancelledDefaultRaid(timestamp = expectedDate, cancellationComment)
        val expectedSchedule =
            Schedule(existingDefaultSchedule, listOf(expectedCancelledDefaultRaid), listOf(), listOf())
        val expected = CancelDefaultRaidResult.Success

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Success)

        // WHEN
        val result = engine.cancelDefaultRaid(
            date = "04/07/2022", // Monday, 2022, Jul 4th
            time = "11:00",
            timezoneId = "UTC+2",
            comment = cancellationComment,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when something went wrong while cancelling a default raid`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-01-04T00:00:00.00").toInstant(TimeZone.UTC) // Monday, 2021, Jan 4th

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val cancellationComment = "Some reason for cancellation"

        val expectedDate = LocalDateTime.parse("2022-07-04T09:00:00.00").toInstant(TimeZone.UTC)
        val expectedCancelledDefaultRaid = CancelledDefaultRaid(timestamp = expectedDate, cancellationComment)
        val expectedSchedule =
            Schedule(existingDefaultSchedule, listOf(expectedCancelledDefaultRaid), listOf(), listOf())
        val expected = CancelDefaultRaidResult.Failure

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Failure)

        // WHEN
        val result = engine.cancelDefaultRaid(
            date = "04/07/2022", // Monday, 2022, Jul 4th
            time = "11:00",
            timezoneId = "UTC+2",
            comment = cancellationComment,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to cancel a default raid that does not exist (mismatched day)`() =
        runBlockingTest {
            // GIVEN
            val engine = SchedulingEngine(mockRepository, mockClock)
            val guildId = Snowflake(1)

            val mockedNow =
                LocalDateTime.parse("2021-01-04T00:00:00.00").toInstant(TimeZone.UTC) // Monday, 2021, Jan 4th

            val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
            val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
            val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

            val cancellationComment = "Some reason for cancellation"

            val expected = CancelDefaultRaidResult.NoRaidPlanned

            Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
            Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

            // WHEN
            val result = engine.cancelDefaultRaid(
                date = "05/06/2022", // Tuesday, 2022, Jul 5th
                time = "11:00",
                timezoneId = "UTC+2",
                comment = cancellationComment,
                forGuildId = guildId
            )

            // THEN
            assertEquals(expected, result)
        }


    @Test
    fun `Should return error when trying to cancel a default raid that does not exist (mismatched hour)`() =
        runBlockingTest {
            // GIVEN
            val engine = SchedulingEngine(mockRepository, mockClock)
            val guildId = Snowflake(1)

            val mockedNow =
                LocalDateTime.parse("2021-01-04T00:00:00.00").toInstant(TimeZone.UTC) // Monday, 2021, Jan 4th

            val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
            val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
            val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

            val cancellationComment = "Some reason for cancellation"

            val expected = CancelDefaultRaidResult.NoRaidPlanned

            Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
            Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

            // WHEN
            val result = engine.cancelDefaultRaid(
                date = "04/07/2022", // Monday, 2022, Jul 4th
                time = "12:00",
                timezoneId = "UTC+2",
                comment = cancellationComment,
                forGuildId = guildId
            )

            // THEN
            assertEquals(expected, result)
        }

    @Test
    fun `Should return error when trying to cancel a default raid that does not exist (mismatched minutes)`() =
        runBlockingTest {
            // GIVEN
            val engine = SchedulingEngine(mockRepository, mockClock)
            val guildId = Snowflake(1)

            val mockedNow =
                LocalDateTime.parse("2021-01-04T00:00:00.00").toInstant(TimeZone.UTC) // Monday, 2021, Jan 4th

            val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
            val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
            val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

            val cancellationComment = "Some reason for cancellation"

            val expected = CancelDefaultRaidResult.NoRaidPlanned

            Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
            Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

            // WHEN
            val result = engine.cancelDefaultRaid(
                date = "04/07/2022", // Monday, 2022, Jul 4th
                time = "11:05",
                timezoneId = "UTC+2",
                comment = cancellationComment,
                forGuildId = guildId
            )

            // THEN
            assertEquals(expected, result)
        }

    @Test
    fun `Should return failure when trying to cancel a default raid that was already cancelled`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-01-04T00:00:00.00").toInstant(TimeZone.UTC) // Monday, 2021, Jan 4th

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")

        val cancellationDate = LocalDateTime.parse("2022-07-04T09:00:00.00").toInstant(TimeZone.UTC)
        val existingCancelledDefaultRaid = CancelledDefaultRaid(timestamp = cancellationDate, null)

        val schedule = Schedule(existingDefaultSchedule, listOf(existingCancelledDefaultRaid), listOf(), listOf())

        val cancellationComment = "Some reason for cancellation"

        val expected = CancelDefaultRaidResult.RaidAlreadyCancelled

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

        // WHEN
        val result = engine.cancelDefaultRaid(
            date = "04/07/2022", // Monday, 2022, Jul 4th
            time = "11:00",
            timezoneId = "UTC+2",
            comment = cancellationComment,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error trying to cancel a raid that is in the past`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-01-04T00:00:00.00").toInstant(TimeZone.UTC) // Monday, 2021, Jan 4th

        val expected = CancelDefaultRaidResult.DateIsInThePast

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)

        // WHEN
        val result = engine.cancelDefaultRaid(
            date = "24/04/1999", // Saturday, 1999, Apr 24th
            time = "11:00",
            timezoneId = "UTC+2",
            comment = null,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to cancel a raid with incorrect date format`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val expected = CancelDefaultRaidResult.IncorrectDate

        // WHEN
        val result = engine.cancelDefaultRaid(
            date = "24-04-99", // Saturday, 1999, Apr 24th
            time = "11:00",
            timezoneId = "UTC+2",
            comment = null,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to cancel a raid with incorrect timezone id`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val expected = CancelDefaultRaidResult.IncorrectTimezoneId

        // WHEN
        val result = engine.cancelDefaultRaid(
            date = "24/04/1999", // Saturday, 1999, Apr 24th
            time = "11:00",
            timezoneId = "bad",
            comment = null,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to cancel a raid with incorrect time format`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val expected = CancelDefaultRaidResult.IncorrectTimeFormat

        // WHEN
        val result = engine.cancelDefaultRaid(
            date = "24/04/1999", // Saturday, 1999, Apr 24th
            time = "24:00",
            timezoneId = "CET",
            comment = null,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success when trying to revert a cancelled raid`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")

        val cancellationDate = LocalDateTime.parse("2022-07-04T09:00:00.00").toInstant(TimeZone.UTC)
        val existingCancelledDefaultRaid = CancelledDefaultRaid(timestamp = cancellationDate, null)
        val schedule = Schedule(existingDefaultSchedule, listOf(existingCancelledDefaultRaid), listOf(), listOf())

        val expectedSchedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val expected = RevertDefaultRaidCancellationResult.Success

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Success)

        // WHEN
        val result = engine.revertDefaultRaidCancellation(
            date = "04/07/2022", // Monday, 2022, Jul 4th
            time = "11:00",
            timezoneId = "UTC+2",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when something went wrong while trying to revert a cancelled raid`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")

        val cancellationDate = LocalDateTime.parse("2022-07-04T09:00:00.00").toInstant(TimeZone.UTC)
        val existingCancelledDefaultRaid = CancelledDefaultRaid(timestamp = cancellationDate, null)
        val schedule = Schedule(existingDefaultSchedule, listOf(existingCancelledDefaultRaid), listOf(), listOf())

        val expectedSchedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val expected = RevertDefaultRaidCancellationResult.Failure

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Failure)

        // WHEN
        val result = engine.revertDefaultRaidCancellation(
            date = "04/07/2022", // Monday, 2022, Jul 4th
            time = "11:00",
            timezoneId = "UTC+2",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to revert a cancelled raid that does not exist`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")

        val cancellationDate = LocalDateTime.parse("2022-07-04T09:00:00.00").toInstant(TimeZone.UTC)
        val existingCancelledDefaultRaid = CancelledDefaultRaid(timestamp = cancellationDate, null)
        val schedule = Schedule(existingDefaultSchedule, listOf(existingCancelledDefaultRaid), listOf(), listOf())

        val expected = RevertDefaultRaidCancellationResult.NothingToRevert

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

        // WHEN
        val result = engine.revertDefaultRaidCancellation(
            date = "05/07/2022", // Monday, 2022, Jul 4th
            time = "11:00",
            timezoneId = "UTC+2",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to revert a cancelled raid with an incorrect date`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")

        val cancellationDate = LocalDateTime.parse("2022-07-04T09:00:00.00").toInstant(TimeZone.UTC)
        val existingCancelledDefaultRaid = CancelledDefaultRaid(timestamp = cancellationDate, null)
        val schedule = Schedule(existingDefaultSchedule, listOf(existingCancelledDefaultRaid), listOf(), listOf())

        val expected = RevertDefaultRaidCancellationResult.IncorrectDate

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

        // WHEN
        val result = engine.revertDefaultRaidCancellation(
            date = "bad date",
            time = "11:00",
            timezoneId = "UTC+2",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to revert a cancelled raid with an incorrect timezone`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")

        val cancellationDate = LocalDateTime.parse("2022-07-04T09:00:00.00").toInstant(TimeZone.UTC)
        val existingCancelledDefaultRaid = CancelledDefaultRaid(timestamp = cancellationDate, null)
        val schedule = Schedule(existingDefaultSchedule, listOf(existingCancelledDefaultRaid), listOf(), listOf())

        val expected = RevertDefaultRaidCancellationResult.IncorrectTimezoneId

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

        // WHEN
        val result = engine.revertDefaultRaidCancellation(
            date = "05/07/2022", // Monday, 2022, Jul 4th
            time = "11:00",
            timezoneId = "bad",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to revert a cancelled raid with an incorrect time`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")

        val cancellationDate = LocalDateTime.parse("2022-07-04T09:00:00.00").toInstant(TimeZone.UTC)
        val existingCancelledDefaultRaid = CancelledDefaultRaid(timestamp = cancellationDate, null)
        val schedule = Schedule(existingDefaultSchedule, listOf(existingCancelledDefaultRaid), listOf(), listOf())

        val expected = RevertDefaultRaidCancellationResult.IncorrectTimeFormat

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

        // WHEN
        val result = engine.revertDefaultRaidCancellation(
            date = "05/07/2022", // Monday, 2022, Jul 4th
            time = "bad",
            timezoneId = "UTC+2",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success when exceptional raid was successfully added`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-01-04T00:00:00.00").toInstant(TimeZone.UTC) // Monday, 2021, Jan 4th

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val comment = "exceptional raid comment"
        val expectedDate = LocalDateTime.parse("2022-04-24T19:00:00.00").toInstant(TimeZone.UTC)
        val expectedExceptionalRaid = ExceptionalRaid(timestamp = expectedDate, comment)
        val expectedSchedule =
            Schedule(existingDefaultSchedule, listOf(), listOf(expectedExceptionalRaid), listOf())
        val expected = AddExceptionalRaidResult.Success

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Success)

        // WHEN
        val result = engine.addExceptionalRaid(
            date = "24/04/2022", // 2022, Apr 24th
            time = "21:00",
            timezoneId = "UTC+2",
            comment = comment,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when something went wrong while adding an exceptional raid`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-01-04T00:00:00.00").toInstant(TimeZone.UTC) // Monday, 2021, Jan 4th

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val comment = "exceptional raid comment"
        val expectedDate = LocalDateTime.parse("2022-04-24T19:00:00.00").toInstant(TimeZone.UTC)
        val expectedExceptionalRaid = ExceptionalRaid(timestamp = expectedDate, comment)
        val expectedSchedule =
            Schedule(existingDefaultSchedule, listOf(), listOf(expectedExceptionalRaid), listOf())
        val expected = AddExceptionalRaidResult.Failure

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Failure)

        // WHEN
        val result = engine.addExceptionalRaid(
            date = "24/04/2022", // 2022, Apr 24th
            time = "21:00",
            timezoneId = "UTC+2",
            comment = comment,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when adding an exceptional raid that already exists`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-01-04T00:00:00.00").toInstant(TimeZone.UTC) // Monday, 2021, Jan 4th

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val comment = "exceptional raid comment"
        val date = LocalDateTime.parse("2022-04-24T19:00:00.00").toInstant(TimeZone.UTC)
        val exceptionalRaid = ExceptionalRaid(timestamp = date, comment)
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(exceptionalRaid), listOf())

        val expected = AddExceptionalRaidResult.RaidAlreadyExists

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

        // WHEN
        val result = engine.addExceptionalRaid(
            date = "24/04/2022", // 2022, Apr 24th
            time = "21:00",
            timezoneId = "UTC+2",
            comment = comment,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to add an exceptional raid in the past`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-01-04T00:00:00.00").toInstant(TimeZone.UTC) // Monday, 2021, Jan 4th

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val comment = "exceptional raid comment"
        val expected = AddExceptionalRaidResult.DateIsInThePast

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

        // WHEN
        val result = engine.addExceptionalRaid(
            date = "24/04/2020", // 2020, Apr 24th
            time = "21:00",
            timezoneId = "UTC+2",
            comment = comment,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to add an exceptional raid with an incorrect date`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-01-04T00:00:00.00").toInstant(TimeZone.UTC) // Monday, 2021, Jan 4th

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val comment = "exceptional raid comment"
        val expected = AddExceptionalRaidResult.IncorrectDate

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

        // WHEN
        val result = engine.addExceptionalRaid(
            date = "24/14/2022",
            time = "21:00",
            timezoneId = "UTC+2",
            comment = comment,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to add an exceptional raid with an incorrect timezone`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-01-04T00:00:00.00").toInstant(TimeZone.UTC) // Monday, 2021, Jan 4th

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val comment = "exceptional raid comment"
        val expected = AddExceptionalRaidResult.IncorrectTimezoneId

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

        // WHEN
        val result = engine.addExceptionalRaid(
            date = "24/04/2022",
            time = "21:00",
            timezoneId = "bad",
            comment = comment,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to add an exceptional raid with an incorrect time`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-01-04T00:00:00.00").toInstant(TimeZone.UTC) // Monday, 2021, Jan 4th

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val comment = "exceptional raid comment"
        val expected = AddExceptionalRaidResult.IncorrectTimeFormat

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

        // WHEN
        val result = engine.addExceptionalRaid(
            date = "24/04/2022",
            time = "21:95",
            timezoneId = "UTC+2",
            comment = comment,
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success when exceptional raid was successfully cancelled`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-01-04T00:00:00.00").toInstant(TimeZone.UTC) // Monday, 2021, Jan 4th

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val comment = "exceptional raid comment"
        val date = LocalDateTime.parse("2022-04-24T19:00:00.00").toInstant(TimeZone.UTC)
        val exceptionalRaid = ExceptionalRaid(timestamp = date, comment)
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(exceptionalRaid), listOf())

        val expectedSchedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val expected = CancelExceptionalRaidResult.Success

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Success)

        // WHEN
        val result = engine.cancelExceptionalRaid(
            date = "24/04/2022", // 2022, Apr 24th
            time = "21:00",
            timezoneId = "UTC+2",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success when something went wrong while cancelling an exceptional raid`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-01-04T00:00:00.00").toInstant(TimeZone.UTC) // Monday, 2021, Jan 4th

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val comment = "exceptional raid comment"
        val date = LocalDateTime.parse("2022-04-24T19:00:00.00").toInstant(TimeZone.UTC)
        val exceptionalRaid = ExceptionalRaid(timestamp = date, comment)
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(exceptionalRaid), listOf())

        val expectedSchedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val expected = CancelExceptionalRaidResult.Failure

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Failure)

        // WHEN
        val result = engine.cancelExceptionalRaid(
            date = "24/04/2022", // 2022, Apr 24th
            time = "21:00",
            timezoneId = "UTC+2",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to cancel an exceptional raid that does not exist`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-01-04T00:00:00.00").toInstant(TimeZone.UTC) // Monday, 2021, Jan 4th

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val comment = "exceptional raid comment"
        val date = LocalDateTime.parse("2022-04-24T19:00:00.00").toInstant(TimeZone.UTC)
        val exceptionalRaid = ExceptionalRaid(timestamp = date, comment)
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(exceptionalRaid), listOf())

        val expectedSchedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val expected = CancelExceptionalRaidResult.NothingToCancel

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Failure)

        // WHEN
        val result = engine.cancelExceptionalRaid(
            date = "24/04/2022", // 2022, Apr 24th
            time = "22:00",
            timezoneId = "UTC+2",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to cancel an exceptional raid with an incorrect date`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-01-04T00:00:00.00").toInstant(TimeZone.UTC) // Monday, 2021, Jan 4th

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val comment = "exceptional raid comment"
        val date = LocalDateTime.parse("2022-04-24T19:00:00.00").toInstant(TimeZone.UTC)
        val exceptionalRaid = ExceptionalRaid(timestamp = date, comment)
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(exceptionalRaid), listOf())

        val expectedSchedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val expected = CancelExceptionalRaidResult.IncorrectDate

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Failure)

        // WHEN
        val result = engine.cancelExceptionalRaid(
            date = "bad", // 2022, Apr 24th
            time = "21:00",
            timezoneId = "UTC+2",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to cancel an exceptional raid with an incorrect timezone`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-01-04T00:00:00.00").toInstant(TimeZone.UTC) // Monday, 2021, Jan 4th

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val comment = "exceptional raid comment"
        val date = LocalDateTime.parse("2022-04-24T19:00:00.00").toInstant(TimeZone.UTC)
        val exceptionalRaid = ExceptionalRaid(timestamp = date, comment)
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(exceptionalRaid), listOf())

        val expectedSchedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val expected = CancelExceptionalRaidResult.IncorrectTimezoneId

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Failure)

        // WHEN
        val result = engine.cancelExceptionalRaid(
            date = "24/04/2022", // 2022, Apr 24th
            time = "21:00",
            timezoneId = "bad",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to cancel an exceptional raid with an incorrect time`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-01-04T00:00:00.00").toInstant(TimeZone.UTC) // Monday, 2021, Jan 4th

        val existingDefaultRaid = DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 9, minutesUTC = 0, "comment")
        val existingDefaultSchedule = DefaultRaidSchedule(listOf(existingDefaultRaid), "CET")
        val comment = "exceptional raid comment"
        val date = LocalDateTime.parse("2022-04-24T19:00:00.00").toInstant(TimeZone.UTC)
        val exceptionalRaid = ExceptionalRaid(timestamp = date, comment)
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(exceptionalRaid), listOf())

        val expectedSchedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val expected = CancelExceptionalRaidResult.IncorrectTimeFormat

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Failure)

        // WHEN
        val result = engine.cancelExceptionalRaid(
            date = "24/04/2022", // 2022, Apr 24th
            time = "bad",
            timezoneId = "UTC+2",
            forGuildId = guildId
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success when adding absence`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)
        val userId = Snowflake(2)

        val existingDefaultSchedule = DefaultRaidSchedule(listOf(), "CET")

        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val comment = "Reason for absence"
        val date = LocalDate.parse("24/04/2022", DateTimeFormatter.ofPattern(dateFormattingPattern)).toKotlinLocalDate()
        val expectedAbsence = Absence(date = date, userId = userId, comment = comment)
        val expectedSchedule = schedule.copy(absences = listOf(expectedAbsence))

        val expected = AddAbsenceResult.Success

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Success)

        // WHEN
        val result = engine.addAbsence(
            date = "24/04/2022", // 2022, Apr 24th
            userId = userId,
            comment = comment,
            forGuildId = guildId,
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when something went wrong while adding absence`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)
        val userId = Snowflake(2)

        val existingDefaultSchedule = DefaultRaidSchedule(listOf(), "CET")

        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())

        val comment = "Reason for absence"
        val date = LocalDate.parse("24/04/2022", DateTimeFormatter.ofPattern(dateFormattingPattern)).toKotlinLocalDate()
        val expectedAbsence = Absence(date = date, userId = userId, comment = comment)
        val expectedSchedule = schedule.copy(absences = listOf(expectedAbsence))

        val expected = AddAbsenceResult.Failure

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Failure)

        // WHEN
        val result = engine.addAbsence(
            date = "24/04/2022", // 2022, Apr 24th
            userId = userId,
            comment = comment,
            forGuildId = guildId,
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when adding absence that already exists`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)
        val userId = Snowflake(2)

        val existingDefaultSchedule = DefaultRaidSchedule(listOf(), "CET")
        val date = LocalDate.parse("24/04/2022", DateTimeFormatter.ofPattern(dateFormattingPattern)).toKotlinLocalDate()
        val existingAbsence = Absence(date = date, userId = userId, comment = null)
        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf(existingAbsence))

        val comment = "Reason for absence"

        val expected = AddAbsenceResult.AbsenceAlreadyExists

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

        // WHEN
        val result = engine.addAbsence(
            date = "24/04/2022", // 2022, Apr 24th
            userId = userId,
            comment = comment,
            forGuildId = guildId,
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when adding absence with a past date`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)
        val userId = Snowflake(2)

        val mockedNow = LocalDateTime.parse("2021-11-04T17:42:00.00").toInstant(TimeZone.UTC) // Thu, 2021, Nov 4th

        val existingDefaultSchedule = DefaultRaidSchedule(listOf(), "CET")

        val comment = "Reason for absence"

        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())
        val expected = AddAbsenceResult.DateIsInThePast

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

        // WHEN
        val result = engine.addAbsence(
            date = "24/04/1970",
            userId = userId,
            comment = comment,
            forGuildId = guildId,
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when adding absence with an incorrect date`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)
        val userId = Snowflake(2)

        val existingDefaultSchedule = DefaultRaidSchedule(listOf(), "CET")

        val comment = "Reason for absence"

        val schedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())
        val expected = AddAbsenceResult.IncorrectDate

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(schedule)

        // WHEN
        val result = engine.addAbsence(
            date = "32/04/2022", // 2022, Apr 24th
            userId = userId,
            comment = comment,
            forGuildId = guildId,
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success when removing absence`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)
        val userId = Snowflake(2)

        val existingDefaultSchedule = DefaultRaidSchedule(listOf(), "CET")

        val comment = "Reason for absence"
        val date = LocalDate.parse("24/04/2022", DateTimeFormatter.ofPattern(dateFormattingPattern)).toKotlinLocalDate()
        val existingAbsence = Absence(date = date, userId = userId, comment = comment)
        val existingSchedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf(existingAbsence))

        val expectedSchedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())
        val expected = RemoveAbsenceResult.Success

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(existingSchedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Success)

        // WHEN
        val result = engine.removeAbsence(
            date = "24/04/2022", // 2022, Apr 24th
            userId = userId,
            forGuildId = guildId,
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when something went wrong while removing absence`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)
        val userId = Snowflake(2)

        val existingDefaultSchedule = DefaultRaidSchedule(listOf(), "CET")

        val comment = "Reason for absence"
        val date = LocalDate.parse("24/04/2022", DateTimeFormatter.ofPattern(dateFormattingPattern)).toKotlinLocalDate()
        val existingAbsence = Absence(date = date, userId = userId, comment = comment)
        val existingSchedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf(existingAbsence))

        val expectedSchedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf())
        val expected = RemoveAbsenceResult.Failure

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(existingSchedule)
        Mockito.`when`(mockRepository.updateSchedule(expectedSchedule, guildId))
            .thenReturn(SchedulingRepository.UploadScheduleResult.Failure)

        // WHEN
        val result = engine.removeAbsence(
            date = "24/04/2022", // 2022, Apr 24th
            userId = userId,
            forGuildId = guildId,
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when trying to remove absence that does not exist`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)
        val userId = Snowflake(2)

        val existingDefaultSchedule = DefaultRaidSchedule(listOf(), "CET")

        val comment = "Reason for absence"
        val date = LocalDate.parse("24/04/2022", DateTimeFormatter.ofPattern(dateFormattingPattern)).toKotlinLocalDate()
        val existingAbsence = Absence(date = date, userId = userId, comment = comment)
        val existingSchedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf(existingAbsence))

        val expected = RemoveAbsenceResult.NoSuchAbsence

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(existingSchedule)

        // WHEN
        val result = engine.removeAbsence(
            date = "22/04/2022", // 2022, Apr 24th
            userId = userId,
            forGuildId = guildId,
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when removing an absence with a past date`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)
        val userId = Snowflake(2)

        val mockedNow = LocalDateTime.parse("2021-11-04T17:42:00.00").toInstant(TimeZone.UTC) // Thu, 2021, Nov 4th


        val expected = RemoveAbsenceResult.DateIsInThePast

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)

        // WHEN
        val result = engine.removeAbsence(
            date = "24/04/1970",
            userId = userId,
            forGuildId = guildId,
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when removing an absence with an incorrect date`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository)
        val guildId = Snowflake(1)
        val userId = Snowflake(2)

        val existingDefaultSchedule = DefaultRaidSchedule(listOf(), "CET")

        val comment = "Reason for absence"
        val date = LocalDate.parse("24/04/2022", DateTimeFormatter.ofPattern(dateFormattingPattern)).toKotlinLocalDate()
        val existingAbsence = Absence(date = date, userId = userId, comment = comment)
        val existingSchedule = Schedule(existingDefaultSchedule, listOf(), listOf(), listOf(existingAbsence))

        val expected = RemoveAbsenceResult.IncorrectDate

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(existingSchedule)

        // WHEN
        val result = engine.removeAbsence(
            date = "bad", // 2022, Apr 24th
            userId = userId,
            forGuildId = guildId,
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return next raids by date`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-11-04T17:42:00.00").toInstant(TimeZone.UTC) // Thu, 2021, Nov 4th

        val saturdayRaid1Instant = LocalDateTime.parse("2021-11-06T19:30:00.00").toInstant(TimeZone.UTC)
        val mondayRaidInstant = LocalDateTime.parse("2021-11-08T19:00:00.00").toInstant(TimeZone.UTC)
        val wednesdayRaidInstant = LocalDateTime.parse("2021-11-10T20:00:00.00").toInstant(TimeZone.UTC)
        val saturdayRaid2Instant = LocalDateTime.parse("2021-11-13T19:30:00.00").toInstant(TimeZone.UTC)

        val expectedUIModel = ScheduleUIModel(
            timezoneId = "CET", upcomingEvents = listOf(
                DefaultRaidEvent(saturdayRaid1Instant, "Weekly raid 3", UpcomingCancellation(false, null)),
                AbsenceEvent(closeAbsenceDate, userId2, "Close absence"),
                DefaultRaidEvent(mondayRaidInstant, "Weekly raid 1", UpcomingCancellation(true, "Close")),
                DefaultRaidEvent(wednesdayRaidInstant, "Weekly raid 2", UpcomingCancellation(false, null)),
                ExceptionalRaidEvent(closeExceptionalRaidInstant, "Close exceptional raid"),
                DefaultRaidEvent(saturdayRaid2Instant, "Weekly raid 3", UpcomingCancellation(false, null)),

                )
        )

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(completeSchedule)

        // WHEN
        val targetDate = "14/11/2021"
        val desiredTimezone = "CET"
        val result = engine.getScheduleByDate(targetDate, desiredTimezone, guildId)

        // THEN
        assert(result is GetScheduleResult.UpcomingSchedule)
        assertEquals(expectedUIModel.timezoneId, (result as GetScheduleResult.UpcomingSchedule).uiModel.timezoneId)
        assertContentEquals(expectedUIModel.upcomingEvents, result.uiModel.upcomingEvents)
    }

    @Test
    fun `Should return error when there is nothing to display while requesting next raids by date`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-11-04T17:42:00.00").toInstant(TimeZone.UTC) // Thu, 2021, Nov 4th

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(completeSchedule)

        val expected = GetScheduleResult.NothingToDisplay

        // WHEN
        val targetDate = "05/11/2021"
        val desiredTimezone = "CET"
        val result = engine.getScheduleByDate(targetDate, desiredTimezone, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return next raids by number`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-11-04T17:42:00.00").toInstant(TimeZone.UTC) // Thu, 2021, Nov 4th

        val saturdayRaid1Instant = LocalDateTime.parse("2021-11-06T19:30:00.00").toInstant(TimeZone.UTC)
        val mondayRaidInstant = LocalDateTime.parse("2021-11-08T19:00:00.00").toInstant(TimeZone.UTC)
        val wednesdayRaidInstant = LocalDateTime.parse("2021-11-10T20:00:00.00").toInstant(TimeZone.UTC)

        val expectedUIModel = ScheduleUIModel(
            timezoneId = "CET", upcomingEvents = listOf(
                DefaultRaidEvent(saturdayRaid1Instant, "Weekly raid 3", UpcomingCancellation(false, null)),
                AbsenceEvent(closeAbsenceDate, userId2, "Close absence"),
                DefaultRaidEvent(mondayRaidInstant, "Weekly raid 1", UpcomingCancellation(true, "Close")),
                DefaultRaidEvent(wednesdayRaidInstant, "Weekly raid 2", UpcomingCancellation(false, null)),
                ExceptionalRaidEvent(closeExceptionalRaidInstant, "Close exceptional raid"),
                )
        )

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(completeSchedule)

        // WHEN
        val targetNumberOfRaids = 4
        val desiredTimezone = "CET"
        val result = engine.getScheduleByNumberOfRaids(targetNumberOfRaids, desiredTimezone, guildId)

        // THEN
        assert(result is GetScheduleResult.UpcomingSchedule)
        assertEquals(expectedUIModel.timezoneId, (result as GetScheduleResult.UpcomingSchedule).uiModel.timezoneId)
        assertContentEquals(expectedUIModel.upcomingEvents, result.uiModel.upcomingEvents)
    }

    @Test
    fun `Should return error when requesting schedule with an incorrect number`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val expected = GetScheduleResult.IncorrectNumberOfRaids

        // WHEN
        val targetNumberOfRaids = 0
        val desiredTimezone = "CET"
        val result = engine.getScheduleByNumberOfRaids(targetNumberOfRaids, desiredTimezone, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when there is nothing to display while requesting schedule by number`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val emptySchedule = Schedule(DefaultRaidSchedule(listOf(), "CET"), listOf(), listOf(), listOf(closeAbsence))
        val expected = GetScheduleResult.NothingToDisplay

        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(emptySchedule)


        // WHEN
        val targetNumberOfRaids = 1
        val desiredTimezone = "CET"
        val result = engine.getScheduleByNumberOfRaids(targetNumberOfRaids, desiredTimezone, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when requesting schedule by date with an incorrect date`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val expected = GetScheduleResult.IncorrectDate

        // WHEN
        val targetDate = "bad"
        val desiredTimezone = "CET"
        val result = engine.getScheduleByDate(targetDate, desiredTimezone, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when requesting schedule by date with a past date`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-11-04T17:42:00.00").toInstant(TimeZone.UTC) // Thu, 2021, Nov 4th

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(completeSchedule)

        val expected = GetScheduleResult.DateIsInThePast

        // WHEN
        val targetDate = "03/11/2021"
        val desiredTimezone = "CET"
        val result = engine.getScheduleByDate(targetDate, desiredTimezone, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return default raid schedule`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val mockedNow = LocalDateTime.parse("2021-11-04T17:42:00.00").toInstant(TimeZone.UTC) // Thu, 2021, Nov 4th

        Mockito.`when`(mockClock.now()).thenReturn(mockedNow)
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(completeSchedule)

        val expected = GetScheduleResult.DefaultSchedule(defaultRaidSchedule)

        // WHEN
        val result = engine.getDefaultSchedule(guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when there is no default schedule to be displayed`() = runBlockingTest {
        // GIVEN
        val engine = SchedulingEngine(mockRepository, mockClock)
        val guildId = Snowflake(1)

        val emptySchedule = Schedule(DefaultRaidSchedule(listOf(), "CET"), listOf(), listOf(), listOf(closeAbsence))
        Mockito.`when`(mockRepository.getSchedule(guildId)).thenReturn(emptySchedule)

        val expected = GetScheduleResult.NothingToDisplay

        // WHEN
        val result = engine.getDefaultSchedule(guildId)

        // THEN
        assertEquals(expected, result)
    }

    // MOCKED COMPLETE SCHEDULE

    private companion object {
        val defaultRaidSchedule = DefaultRaidSchedule(
            defaultTimezoneId = "CET", defaultRaids = listOf(
                DefaultRaid(DayOfWeek.MONDAY, hoursUTC = 19, minutesUTC = 0, "Weekly raid 1"),
                DefaultRaid(DayOfWeek.WEDNESDAY, hoursUTC = 20, minutesUTC = 0, "Weekly raid 2"),
                DefaultRaid(DayOfWeek.SATURDAY, hoursUTC = 19, minutesUTC = 30, "Weekly raid 3"),
            )
        )

        val farawayExceptionalRaidInstant = LocalDateTime.parse("2103-01-04T00:00:00.00").toInstant(TimeZone.UTC)
        val closeExceptionalRaidInstant = LocalDateTime.parse("2021-11-11T18:00:00.00").toInstant(TimeZone.UTC)
        val farawayExceptionalRaid = ExceptionalRaid(farawayExceptionalRaidInstant, "Faraway exceptional raid")
        val closeExceptionalRaid = ExceptionalRaid(closeExceptionalRaidInstant, "Close exceptional raid")
        val exceptionalRaids = listOf(farawayExceptionalRaid, closeExceptionalRaid)

        val farawayCancelledRaidInstant = LocalDateTime.parse("2023-02-27T19:00:00.00").toInstant(TimeZone.UTC)
        val closeCancelledRaidInstant = LocalDateTime.parse("2021-11-08T19:00:00.00").toInstant(TimeZone.UTC)
        val farawayCancelledRaid = CancelledDefaultRaid(farawayCancelledRaidInstant, "Faraway cancelled raid")
        val closeCancelledRaid = CancelledDefaultRaid(closeCancelledRaidInstant, "Close")
        val cancelledRaids = listOf(farawayCancelledRaid, closeCancelledRaid)

        val farawayAbsenceDate =
            LocalDate.parse("24/04/2023", DateTimeFormatter.ofPattern(dateFormattingPattern)).toKotlinLocalDate()
        val closeAbsenceDate =
            LocalDate.parse("08/11/2021", DateTimeFormatter.ofPattern(dateFormattingPattern)).toKotlinLocalDate()
        val userId1 = Snowflake(1)
        val userId2 = Snowflake(2)
        val farawayAbsence = Absence(farawayAbsenceDate, userId1, "Faraway absence")
        val closeAbsence = Absence(closeAbsenceDate, userId2, "Close absence")
        val absences = listOf(farawayAbsence, closeAbsence)

        val completeSchedule = Schedule(
            defaultRaidSchedule = defaultRaidSchedule,
            cancelledDefaultRaids = cancelledRaids,
            exceptionalRaids = exceptionalRaids,
            absences = absences
        )
    }

}