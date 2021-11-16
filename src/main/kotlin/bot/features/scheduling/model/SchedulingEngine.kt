package bot.features.scheduling.model

import bot.features.core.typealiases.GuildId
import bot.features.core.typealiases.UserId
import bot.features.scheduling.data.SchedulingDataStructure
import bot.features.scheduling.data.SchedulingRepository
import bot.features.scheduling.data.dateFormattingPattern
import bot.features.scheduling.data.defaultTimezone
import bot.features.scheduling.model.UpcomingEvent.*
import bot.features.scheduling.model.results.*
import bot.features.scheduling.model.results.ParsingResult.*
import kotlinx.datetime.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class SchedulingEngine(
    private val repository: SchedulingRepository,
    private val clock: Clock = Clock.System
) {

    fun makeInitialDataStructure(): SchedulingDataStructure {
        val defaultRaidSchedule = DefaultRaidSchedule(defaultRaids = listOf(), defaultTimezoneId = defaultTimezone)
        val schedule = Schedule(defaultRaidSchedule, listOf(), listOf(), listOf())
        return SchedulingDataStructure(schedule)
    }

    suspend fun setDefaultTimezone(timezoneId: String, forGuildId: GuildId): SetTimezoneResult {
        return if (timezoneId.isTimezoneId()) {
            val currentSchedule = repository.getSchedule(forGuildId)
            val updatedDefaultSchedule = currentSchedule.defaultRaidSchedule.copy(defaultTimezoneId = timezoneId)
            val updatedSchedule = currentSchedule.copy(defaultRaidSchedule = updatedDefaultSchedule)

            when (repository.updateSchedule(updatedSchedule, forGuildId)) {
                SchedulingRepository.UploadScheduleResult.Failure -> SetTimezoneResult.Failure
                SchedulingRepository.UploadScheduleResult.Success -> SetTimezoneResult.Success
            }

        } else SetTimezoneResult.IncorrectTimezoneId
    }

    suspend fun addDefaultRaid(
        day: String,
        time: String,
        timezoneId: String,
        comment: String?,
        forGuildId: GuildId,
    ): AddDefaultRaidResult {
        if (!timezoneId.isTimezoneId()) return AddDefaultRaidResult.IncorrectTimezoneId
        val parseTimeResult = time.parseTime()
        val parseDayOfWeekResult = day.parseDayOfWeek()
        val dayOfWeek =
            if (parseDayOfWeekResult is ParsedDayOfWeek) parseDayOfWeekResult.dayOfWeek else return AddDefaultRaidResult.Failure
        val (hours, minutes) = if (parseTimeResult is ParsedTime) parseTimeResult.hoursToMinutes else return AddDefaultRaidResult.IncorrectTimeFormat
        val (hoursUTC, minutesUTC) = toUTCHoursAndMinutes(hours, minutes, TimeZone.of(timezoneId))

        val existingSchedule = repository.getSchedule(forGuildId)

        val newDefaultRaid = DefaultRaid(
            dayOfWeek = dayOfWeek,
            hoursUTC = hoursUTC,
            minutesUTC = minutesUTC,
            comment = comment
        )

        if (newDefaultRaid.isInDefaultSchedule(existingSchedule.defaultRaidSchedule)) return AddDefaultRaidResult.RaidAlreadyExists

        val newDefaultRaids = existingSchedule.defaultRaidSchedule.defaultRaids
            .toMutableList().apply { add(newDefaultRaid) }
        val newDefaultSchedule = existingSchedule.defaultRaidSchedule.copy(defaultRaids = newDefaultRaids)
        val newSchedule = existingSchedule.copy(defaultRaidSchedule = newDefaultSchedule)

        return when (repository.updateSchedule(newSchedule, forGuildId)) {
            SchedulingRepository.UploadScheduleResult.Failure -> AddDefaultRaidResult.Failure
            SchedulingRepository.UploadScheduleResult.Success -> AddDefaultRaidResult.Success(newDefaultSchedule)
        }
    }

    suspend fun removeDefaultRaid(
        day: String,
        time: String,
        timezoneId: String,
        forGuildId: GuildId,
    ): RemoveDefaultRaidResult {
        if (!timezoneId.isTimezoneId()) return RemoveDefaultRaidResult.IncorrectTimezoneId
        val parseTimeResult = time.parseTime()
        val parseDayOfWeekResult = day.parseDayOfWeek()
        val dayOfWeek =
            if (parseDayOfWeekResult is ParsedDayOfWeek) parseDayOfWeekResult.dayOfWeek else return RemoveDefaultRaidResult.Failure
        val (hours, minutes) = if (parseTimeResult is ParsedTime) parseTimeResult.hoursToMinutes else return RemoveDefaultRaidResult.IncorrectTimeFormat
        val (hoursUTC, minutesUTC) = toUTCHoursAndMinutes(hours, minutes, TimeZone.of(timezoneId))

        val existingSchedule = repository.getSchedule(forGuildId)
        val existingDefaultRaids = existingSchedule.defaultRaidSchedule.defaultRaids

        val raidToRemove = existingDefaultRaids.find {
            it.dayOfWeek == dayOfWeek && it.hoursUTC == hoursUTC && it.minutesUTC == minutesUTC
        } ?: return RemoveDefaultRaidResult.RaidDoesNotExist

        val newDefaultRaids = existingDefaultRaids.toMutableList().apply { remove(raidToRemove) }
        val newDefaultRaidSchedule = existingSchedule.defaultRaidSchedule.copy(defaultRaids = newDefaultRaids)
        val newSchedule = existingSchedule.copy(defaultRaidSchedule = newDefaultRaidSchedule)

        return when (repository.updateSchedule(newSchedule, forGuildId)) {
            SchedulingRepository.UploadScheduleResult.Failure -> RemoveDefaultRaidResult.Failure
            SchedulingRepository.UploadScheduleResult.Success -> RemoveDefaultRaidResult.Success
        }
    }

    suspend fun cancelDefaultRaid(
        date: String,
        time: String,
        timezoneId: String,
        comment: String?,
        forGuildId: GuildId,
    ): CancelDefaultRaidResult {
        // Check timezone ID
        if (!timezoneId.isTimezoneId()) return CancelDefaultRaidResult.IncorrectTimezoneId

        // Check time format
        val parseTimeResult = time.parseTime()
        val (hours, minutes) = if (parseTimeResult is ParsedTime) parseTimeResult.hoursToMinutes
        else return CancelDefaultRaidResult.IncorrectTimeFormat

        // Check date
        val parsedLocalDateTime = parseLocalDateTime(date, hours, minutes)
        val targetLocalDate = if (parsedLocalDateTime is ParsedLocalDateTime) parsedLocalDateTime.localDateTime
        else return CancelDefaultRaidResult.IncorrectDate

        // Check that raid is in the future
        val targetTimestamp = targetLocalDate.toInstant(TimeZone.of(timezoneId))
        if (targetTimestamp < clock.now()) return CancelDefaultRaidResult.DateIsInThePast

        val existingSchedule = repository.getSchedule(forGuildId)
        if (existingSchedule.cancelledDefaultRaids.any { it.timestamp == targetTimestamp }) return CancelDefaultRaidResult.RaidAlreadyCancelled

        // Check that a default raid is actually planned
        if (!existingSchedule.defaultRaidSchedule.defaultRaids.any {
                val dateUTC = targetLocalDate.toInstant(TimeZone.of(timezoneId)).toLocalDateTime(TimeZone.UTC)
                it.dayOfWeek == dateUTC.dayOfWeek
                        && it.hoursUTC == dateUTC.hour
                        && it.minutesUTC == dateUTC.minute
            }
        ) return CancelDefaultRaidResult.NoRaidPlanned

        val cancelledRaid = CancelledDefaultRaid(timestamp = targetTimestamp, comment = comment)
        val newCancelledDefaultRaids = existingSchedule.cancelledDefaultRaids
            .toMutableList().apply { add(cancelledRaid) }
        val newSchedule = existingSchedule.copy(cancelledDefaultRaids = newCancelledDefaultRaids)

        return when (repository.updateSchedule(newSchedule, forGuildId)) {
            SchedulingRepository.UploadScheduleResult.Failure -> CancelDefaultRaidResult.Failure
            SchedulingRepository.UploadScheduleResult.Success -> CancelDefaultRaidResult.Success
        }
    }

    suspend fun revertDefaultRaidCancellation(
        date: String,
        time: String,
        timezoneId: String,
        forGuildId: GuildId,
    ): RevertDefaultRaidCancellationResult {
        // Check timezone ID
        if (!timezoneId.isTimezoneId()) return RevertDefaultRaidCancellationResult.IncorrectTimezoneId

        // Check time format
        val parseTimeResult = time.parseTime()
        val (hours, minutes) = if (parseTimeResult is ParsedTime) parseTimeResult.hoursToMinutes
        else return RevertDefaultRaidCancellationResult.IncorrectTimeFormat

        // Check date
        val parsedLocalDateTime = parseLocalDateTime(date, hours, minutes)
        val targetLocalDate = if (parsedLocalDateTime is ParsedLocalDateTime) parsedLocalDateTime.localDateTime
        else return RevertDefaultRaidCancellationResult.IncorrectDate

        // Find the corresponding cancelled raid
        val targetTimestamp = targetLocalDate.toInstant(TimeZone.of(timezoneId))
        val existingSchedule = repository.getSchedule(forGuildId)
        val toRevert = existingSchedule.cancelledDefaultRaids.find { it.timestamp == targetTimestamp }
            ?: return RevertDefaultRaidCancellationResult.NothingToRevert

        // Remove the raid to revert
        val newCancelledDefaultRaids = existingSchedule.cancelledDefaultRaids
            .toMutableList().apply { remove(toRevert) }
        val newSchedule = existingSchedule.copy(cancelledDefaultRaids = newCancelledDefaultRaids)

        return when (repository.updateSchedule(newSchedule, forGuildId)) {
            SchedulingRepository.UploadScheduleResult.Failure -> RevertDefaultRaidCancellationResult.Failure
            SchedulingRepository.UploadScheduleResult.Success -> RevertDefaultRaidCancellationResult.Success
        }
    }

    suspend fun addExceptionalRaid(
        date: String,
        time: String,
        timezoneId: String,
        comment: String?,
        forGuildId: GuildId,
    ): AddExceptionalRaidResult {
        // Check timezone ID
        if (!timezoneId.isTimezoneId()) return AddExceptionalRaidResult.IncorrectTimezoneId

        // Check time format
        val parseTimeResult = time.parseTime()
        val (hours, minutes) = if (parseTimeResult is ParsedTime) parseTimeResult.hoursToMinutes
        else return AddExceptionalRaidResult.IncorrectTimeFormat

        // Check date
        val parsedLocalDateTime = parseLocalDateTime(date, hours, minutes)
        val targetLocalDate = if (parsedLocalDateTime is ParsedLocalDateTime) parsedLocalDateTime.localDateTime
        else return AddExceptionalRaidResult.IncorrectDate

        // Check that raid is in the future
        val targetTimestamp = targetLocalDate.toInstant(TimeZone.of(timezoneId))
        if (targetTimestamp < clock.now()) return AddExceptionalRaidResult.DateIsInThePast

        // Check for duplicates
        val existingSchedule = repository.getSchedule(forGuildId)
        if (existingSchedule.exceptionalRaids.any { it.timestamp == targetTimestamp }) return AddExceptionalRaidResult.RaidAlreadyExists

        val exceptionalRaid = ExceptionalRaid(timestamp = targetTimestamp, comment = comment)
        val newExceptionalRaids = existingSchedule.exceptionalRaids
            .toMutableList().apply { add(exceptionalRaid) }
        val newSchedule = existingSchedule.copy(exceptionalRaids = newExceptionalRaids)

        return when (repository.updateSchedule(newSchedule, forGuildId)) {
            SchedulingRepository.UploadScheduleResult.Failure -> AddExceptionalRaidResult.Failure
            SchedulingRepository.UploadScheduleResult.Success -> AddExceptionalRaidResult.Success
        }
    }

    suspend fun cancelExceptionalRaid(
        date: String,
        time: String,
        timezoneId: String,
        forGuildId: GuildId,
    ): CancelExceptionalRaidResult {
        // Check timezone ID
        if (!timezoneId.isTimezoneId()) return CancelExceptionalRaidResult.IncorrectTimezoneId

        // Check time format
        val parseTimeResult = time.parseTime()
        val (hours, minutes) = if (parseTimeResult is ParsedTime) parseTimeResult.hoursToMinutes
        else return CancelExceptionalRaidResult.IncorrectTimeFormat

        // Check date
        val parsedLocalDateTime = parseLocalDateTime(date, hours, minutes)
        val targetLocalDate = if (parsedLocalDateTime is ParsedLocalDateTime) parsedLocalDateTime.localDateTime
        else return CancelExceptionalRaidResult.IncorrectDate

        // Find the corresponding exceptional raid
        val targetTimestamp = targetLocalDate.toInstant(TimeZone.of(timezoneId))
        val existingSchedule = repository.getSchedule(forGuildId)
        val toRevert = existingSchedule.exceptionalRaids.find { it.timestamp == targetTimestamp }
            ?: return CancelExceptionalRaidResult.NothingToCancel

        // Remove the raid to revert
        val newExceptionalRaids = existingSchedule.exceptionalRaids
            .toMutableList().apply { remove(toRevert) }
        val newSchedule = existingSchedule.copy(exceptionalRaids = newExceptionalRaids)

        return when (repository.updateSchedule(newSchedule, forGuildId)) {
            SchedulingRepository.UploadScheduleResult.Failure -> CancelExceptionalRaidResult.Failure
            SchedulingRepository.UploadScheduleResult.Success -> CancelExceptionalRaidResult.Success
        }
    }

    suspend fun addAbsence(
        date: String,
        userId: UserId,
        comment: String?,
        forGuildId: GuildId,
    ): AddAbsenceResult {
        // Check date
        val parsedLocalDate = parseLocalDate(date)
        val targetLocalDate = if (parsedLocalDate is ParsedLocalDate) parsedLocalDate.localDate
        else return AddAbsenceResult.IncorrectDate

        // Check that date is in the future
        val targetTimestamp = targetLocalDate.atStartOfDayIn(TimeZone.UTC)
        if (targetTimestamp < clock.now()) return AddAbsenceResult.DateIsInThePast

        // Check for duplicates
        val existingSchedule = repository.getSchedule(forGuildId)
        if (existingSchedule.absences.any { it.date == targetLocalDate && it.userId == userId })
            return AddAbsenceResult.AbsenceAlreadyExists

        // Add absence
        val absence = Absence(date = targetLocalDate, userId = userId, comment = comment)
        val newAbsences = existingSchedule.absences.toMutableList().apply { add(absence) }
        val newSchedule = existingSchedule.copy(absences = newAbsences)

        return when (repository.updateSchedule(newSchedule, forGuildId)) {
            SchedulingRepository.UploadScheduleResult.Failure -> AddAbsenceResult.Failure
            SchedulingRepository.UploadScheduleResult.Success -> AddAbsenceResult.Success
        }
    }


    suspend fun removeAbsence(
        date: String,
        userId: UserId,
        forGuildId: GuildId,
    ): RemoveAbsenceResult {
        // Check date
        val parsedLocalDate = parseLocalDate(date)
        val targetLocalDate = if (parsedLocalDate is ParsedLocalDate) parsedLocalDate.localDate
        else return RemoveAbsenceResult.IncorrectDate

        // Check that date is in the future
        val targetTimestamp = targetLocalDate.atStartOfDayIn(TimeZone.UTC)
        if (targetTimestamp < clock.now()) return RemoveAbsenceResult.DateIsInThePast

        // Check for duplicates
        val existingSchedule = repository.getSchedule(forGuildId)
        val absenceToRemove = existingSchedule.absences.find { it.date == targetLocalDate && it.userId == userId }
            ?: return RemoveAbsenceResult.NoSuchAbsence

        // Remove absence
        val newAbsences = existingSchedule.absences.toMutableList().apply { remove(absenceToRemove) }
        val newSchedule = existingSchedule.copy(absences = newAbsences)

        return when (repository.updateSchedule(newSchedule, forGuildId)) {
            SchedulingRepository.UploadScheduleResult.Failure -> RemoveAbsenceResult.Failure
            SchedulingRepository.UploadScheduleResult.Success -> RemoveAbsenceResult.Success
        }
    }

    suspend fun getDefaultSchedule(
        forGuildId: GuildId,
    ): GetScheduleResult {
        val existingSchedule = repository.getSchedule(forGuildId)
        if (existingSchedule.defaultRaidSchedule.defaultRaids.isEmpty()) return GetScheduleResult.NothingToDisplay
        return GetScheduleResult.DefaultSchedule(existingSchedule.defaultRaidSchedule)
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getScheduleByDate(
        date: String,
        timezoneId: String?,
        forGuildId: GuildId,
    ): GetScheduleResult {
        // Check date
        val parsedLocalDate = parseLocalDate(date)
        val targetLocalDate = if (parsedLocalDate is ParsedLocalDate) parsedLocalDate.localDate
        else return GetScheduleResult.IncorrectDate

        // Check that date is in the future
        val targetTimestamp = targetLocalDate.atStartOfDayIn(TimeZone.UTC).plus(Duration.Companion.days(1))
        if (targetTimestamp < clock.now()) return GetScheduleResult.DateIsInThePast

        val existingSchedule = repository.getSchedule(forGuildId)

        // Get the timezone id to use
        val effectiveTimezoneId =
            if (timezoneId != null && timezoneId.isTimezoneId()) timezoneId
            else existingSchedule.defaultRaidSchedule.defaultTimezoneId

        val upcomingEvents = makeEventsUntil(
            untilInstant = targetTimestamp,
            withSchedule = existingSchedule,
            withTimezoneId = effectiveTimezoneId
        )

        if (upcomingEvents.isEmpty()) return GetScheduleResult.NothingToDisplay

        return GetScheduleResult.UpcomingSchedule(
            ScheduleUIModel(
                timezoneId = effectiveTimezoneId,
                upcomingEvents = upcomingEvents
            )
        )
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getScheduleByNumberOfRaids(
        numberOfRaids: Int,
        timezoneId: String?,
        forGuildId: GuildId,
    ): GetScheduleResult {
        // Check number of raids
        if (numberOfRaids < 1) return GetScheduleResult.IncorrectNumberOfRaids

        // Check that there is something to display
        val existingSchedule = repository.getSchedule(forGuildId)
        if (existingSchedule.defaultRaidSchedule.defaultRaids.isEmpty()
            && existingSchedule.exceptionalRaids.isEmpty()
        ) return GetScheduleResult.NothingToDisplay

        // Get the timezone id to use
        val effectiveTimezoneId =
            if (timezoneId != null && timezoneId.isTimezoneId()) timezoneId
            else existingSchedule.defaultRaidSchedule.defaultTimezoneId

        val targetTimestamp = if (existingSchedule.defaultRaidSchedule.defaultRaids.isNotEmpty()) {
            val numberOfRaidsPerWeek = existingSchedule.defaultRaidSchedule.defaultRaids.size
            val numberOfNecessaryWeeksToCheck = numberOfRaids / numberOfRaidsPerWeek + 1
            clock.now().plus(Duration.Companion.days(7 * numberOfNecessaryWeeksToCheck))
        } else {
            existingSchedule.exceptionalRaids.maxOf { it.timestamp }
        }

        val upcomingDefaultRaidsWithCancellations =
            makeDefaultRaidsWithCancellationsUntil(existingSchedule, effectiveTimezoneId, targetTimestamp)

        // Add exceptional raids to be able to count raids
        val upcomingEventsWithExceptionalRaids =
            upcomingDefaultRaidsWithCancellations.toMutableList<UpcomingEvent>().apply {
                addAll(existingSchedule.exceptionalRaids.map { ExceptionalRaidEvent(it.timestamp, it.comment) })
                removeIf { it.referenceTimestamp < clock.now() }
                sortBy { it.referenceTimestamp }
            }

        // Take the right number of raids
        val upcomingEventsWithoutAbsences = upcomingEventsWithExceptionalRaids.take(numberOfRaids)

        // Add absences and sort by timestamp
        val upcomingEvents = upcomingEventsWithoutAbsences.toMutableList().apply {
            addAll(existingSchedule.absences.map { AbsenceEvent(it.date, it.userId, it.comment) })
            removeIf { it.referenceTimestamp > targetTimestamp }
            removeIf { it.referenceTimestamp < clock.now() }
            sortBy { it.referenceTimestamp }
        }

        if (upcomingEvents.isEmpty()) return GetScheduleResult.NothingToDisplay

        return GetScheduleResult.UpcomingSchedule(
            ScheduleUIModel(
                timezoneId = effectiveTimezoneId,
                upcomingEvents = upcomingEvents
            )
        )

    }

    @OptIn(ExperimentalTime::class)
    private fun makeEventsUntil(
        untilInstant: Instant,
        withSchedule: Schedule,
        withTimezoneId: String,
    ): List<UpcomingEvent> {
        val upcomingDefaultRaidsWithCancellations =
            makeDefaultRaidsWithCancellationsUntil(withSchedule, withTimezoneId, untilInstant)

        return upcomingDefaultRaidsWithCancellations.toMutableList<UpcomingEvent>().apply {
            addAll(withSchedule.exceptionalRaids.map { ExceptionalRaidEvent(it.timestamp, it.comment) })
            addAll(withSchedule.absences.map { AbsenceEvent(it.date, it.userId, it.comment) })
            removeIf { it.referenceTimestamp > untilInstant }
            removeIf { it.referenceTimestamp < clock.now() }
            sortBy { it.referenceTimestamp }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun makeDefaultRaidsWithCancellationsUntil(
        withSchedule: Schedule,
        withTimezoneId: String,
        untilInstant: Instant
    ): List<DefaultRaidEvent> {
        val sortedDefaultRaids = withSchedule.defaultRaidSchedule.defaultRaids.toMutableList()
            .sortedBy { it.minutesUTC }
            .sortedBy { it.hoursUTC }
            .sortedBy { it.dayOfWeek }

        var instantPointer = clock.now()
        val localTimeZone = TimeZone.of(withTimezoneId)
        val listOfUpcomingDefaultRaids = mutableListOf<DefaultRaidEvent>()
        while (instantPointer < untilInstant) { // iterate on every day until target day
            val localDateTime = instantPointer.toLocalDateTime(localTimeZone)
            sortedDefaultRaids.forEach {
                if (it.dayOfWeek == localDateTime.dayOfWeek) { // if this is the right day of the week
                    val instant = instantPointer
                        .toLocalDateTime(TimeZone.UTC) // instants are in UTC
                        .toJavaLocalDateTime()
                        .withNano(0)
                        .withSecond(0)
                        .withMinute(it.minutesUTC) // get a reference to this day at raid time
                        .withHour(it.hoursUTC)
                        .toKotlinLocalDateTime()
                        .toInstant(TimeZone.UTC) // convert back to instant

                    val event = DefaultRaidEvent(
                        timestamp = instant,
                        comment = it.comment,
                        cancellation = UpcomingCancellation(false, null) // will be updated
                    )
                    listOfUpcomingDefaultRaids.add(event)
                }
            }
            instantPointer += Duration.days(1)
        }

        val cancelledRaids = withSchedule.cancelledDefaultRaids
        val upcomingDefaultRaidsWithCancellations = listOfUpcomingDefaultRaids.map { event ->
            val correspondingCancelledRaid =
                cancelledRaids.find { cancelledRaid -> cancelledRaid.timestamp == event.timestamp }
                    ?: return@map event
            event.copy(cancellation = UpcomingCancellation(true, correspondingCancelledRaid.comment))
        }
        return upcomingDefaultRaidsWithCancellations
    }


    private fun parseLocalDateTime(date: String, hours: Int, minutes: Int): ParsingResult {

        return try {
            val parsedLocalDateTime = LocalDate.parse(date, DateTimeFormatter.ofPattern(dateFormattingPattern))
                .toKotlinLocalDate()
                .atTime(hours, minutes)
            ParsedLocalDateTime(parsedLocalDateTime)
        } catch (e: Throwable) {
            ParsingError
        }

    }

    private fun parseLocalDate(date: String): ParsingResult {
        return try {
            val parsedLocalDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(dateFormattingPattern))
                .toKotlinLocalDate()
            ParsedLocalDate(parsedLocalDate)
        } catch (e: Throwable) {
            ParsingError
        }
    }

    fun isCorrectTimezoneId(timezoneId: String): Boolean = timezoneId.isTimezoneId()

    private fun String.isTimezoneId(): Boolean {
        return try {
            Clock.System.now().toLocalDateTime(TimeZone.of(this))
            true
        } catch (e: IllegalTimeZoneException) {
            false
        }
    }

    private fun String.parseDayOfWeek(): ParsingResult {
        return when (this) {
            "Monday" -> ParsedDayOfWeek(DayOfWeek.MONDAY)
            "Tuesday" -> ParsedDayOfWeek(DayOfWeek.TUESDAY)
            "Wednesday" -> ParsedDayOfWeek(DayOfWeek.WEDNESDAY)
            "Thursday" -> ParsedDayOfWeek(DayOfWeek.THURSDAY)
            "Friday" -> ParsedDayOfWeek(DayOfWeek.FRIDAY)
            "Saturday" -> ParsedDayOfWeek(DayOfWeek.SATURDAY)
            "Sunday" -> ParsedDayOfWeek(DayOfWeek.SUNDAY)
            else -> ParsingError
        }
    }

    private fun String.parseTime(): ParsingResult {
        return try {
            val numbers = split(':').toMutableList()
            assert(numbers.size == 2)
            val hoursToMinutes = numbers[0].toInt() to numbers[1].toInt()
            assert(hoursToMinutes.first in 0..23)
            assert(hoursToMinutes.second in 0..59)
            ParsedTime(hoursToMinutes)
        } catch (e: Throwable) {
            ParsingError
        }
    }

    private fun toUTCHoursAndMinutes(hours: Int, minutes: Int, localTimeZone: TimeZone): Pair<Int, Int> {
        val referenceInstant = Instant.fromEpochMilliseconds(0)
            .toLocalDateTime(localTimeZone) // to have a reference in the local timezone
            .toJavaLocalDateTime().withHour(hours).withMinute(minutes) // to input the desired hours and minutes
            .toKotlinLocalDateTime().toInstant(localTimeZone) // to convert to an easy-to-present instant

        val referenceInUTC = referenceInstant.toLocalDateTime(TimeZone.UTC)
        return referenceInUTC.hour to referenceInUTC.minute
    }

    private fun DefaultRaid.isInDefaultSchedule(defaultSchedule: DefaultRaidSchedule): Boolean {
        return defaultSchedule.defaultRaids.any {
            it.dayOfWeek == this.dayOfWeek
                    && it.hoursUTC == this.hoursUTC
                    && it.minutesUTC == this.minutesUTC
        }
    }

    companion object {
        fun instance() = SchedulingEngine(SchedulingRepository(), Clock.System)
    }

}


