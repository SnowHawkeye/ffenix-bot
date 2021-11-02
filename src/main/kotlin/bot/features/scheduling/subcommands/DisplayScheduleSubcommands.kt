package bot.features.scheduling.subcommands

import bot.features.core.catchCastExceptions
import bot.features.core.ephemeralResponse
import bot.features.core.typealiases.GuildId
import bot.features.scheduling.data.*
import bot.features.scheduling.model.DefaultRaid
import bot.features.scheduling.model.DefaultRaidSchedule
import bot.features.scheduling.model.SchedulingEngine
import bot.features.scheduling.model.UpcomingEvent
import bot.features.scheduling.model.results.GetScheduleResult
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.cache.data.OptionData
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.*

object DisplayScheduleSubcommands {

    suspend fun displayDefaultSchedule(
        engine: SchedulingEngine,
        option: OptionData,
        guildId: GuildId,
        interaction: GuildChatInputCommandInteractionCreateEvent,
    ) {
        return interaction.displayDefaultSchedule(engine, option, guildId)
    }

    suspend fun displayScheduleByNumber(
        engine: SchedulingEngine,
        option: OptionData,
        guildId: GuildId,
        interaction: GuildChatInputCommandInteractionCreateEvent,
        client: Kord,
    ) {
        return interaction.displayScheduleByNumber(engine, option, guildId, client)
    }

    suspend fun displayScheduleByDate(
        engine: SchedulingEngine,
        option: OptionData,
        guildId: GuildId,
        interaction: GuildChatInputCommandInteractionCreateEvent,
        client: Kord
    ) {
        return interaction.displayScheduleByDate(engine, option, guildId, client)
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.displayDefaultSchedule(
        engine: SchedulingEngine,
        option: OptionData,
        guildId: GuildId
    ) {
        var timezoneId: String? = null

        option.values.value?.forEach {
            catchCastExceptions {
                when (it.name) {
                    timezoneDisplayArgumentName -> timezoneId = it.value.toString()
                }
            }
        }

        when (val defaultSchedule = engine.getDefaultSchedule(guildId)) {
            GetScheduleResult.NothingToDisplay -> ephemeralResponse(nothingToDisplayErrorMessage)
            is GetScheduleResult.DefaultSchedule ->
                makeDefaultScheduleMessage(engine, defaultSchedule.defaultRaidSchedule, timezoneId, interaction)
            else -> ephemeralResponse(genericErrorMessage)
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.displayScheduleByNumber(
        engine: SchedulingEngine,
        option: OptionData,
        guildId: GuildId,
        client: Kord
    ) {
        var timezoneId: String? = null
        var numberOfRaids = 3

        option.values.value?.forEach {
            catchCastExceptions {
                when (it.name) {
                    timezoneDisplayArgumentName -> timezoneId = it.value.toString()
                    numberOfRaidsToDisplayArgumentName -> {
                        if (it.value != null) numberOfRaids = (it.value as Long).toInt()
                    }
                }
            }
        }

        when (val upcomingSchedule = engine.getScheduleByNumberOfRaids(numberOfRaids, timezoneId, guildId)) {
            GetScheduleResult.IncorrectNumberOfRaids -> ephemeralResponse(incorrectNumberOfRaidsMessage)
            GetScheduleResult.NothingToDisplay -> ephemeralResponse(nothingToDisplayErrorMessage)
            is GetScheduleResult.UpcomingSchedule -> {
                interaction.respondPublic {
                    val message = makeMessageFromUIModel(upcomingSchedule, client)
                    content = message
                }
            }
            else -> ephemeralResponse(genericErrorMessage)
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.displayScheduleByDate(
        engine: SchedulingEngine,
        option: OptionData,
        guildId: GuildId,
        client: Kord
    ) {
        var timezoneId: String? = null
        var date = ""

        option.values.value?.forEach {
            catchCastExceptions {
                when (it.name) {
                    timezoneDisplayArgumentName -> timezoneId = it.value.toString()
                    limitDisplayDateArgumentName -> date = it.value.toString()
                }
            }
        }

        when (val upcomingSchedule = engine.getScheduleByDate(date, timezoneId, guildId)) {
            GetScheduleResult.DateIsInThePast -> ephemeralResponse(dateIsInThePastErrorMessage)
            GetScheduleResult.IncorrectDate -> ephemeralResponse(incorrectDateErrorMessage)
            GetScheduleResult.IncorrectNumberOfRaids -> ephemeralResponse(incorrectNumberOfRaidsMessage)
            GetScheduleResult.NothingToDisplay -> ephemeralResponse(nothingToDisplayErrorMessage)
            is GetScheduleResult.UpcomingSchedule -> {
                interaction.respondPublic {
                    val message = makeMessageFromUIModel(upcomingSchedule, client)
                    content = message
                }
            }
            else -> ephemeralResponse(genericErrorMessage)
        }
    }

    private suspend fun makeMessageFromUIModel(
        upcomingSchedule: GetScheduleResult.UpcomingSchedule,
        client: Kord
    ): String {
        val uiModel = upcomingSchedule.uiModel
        val timezoneId = uiModel.timezoneId
        val timezone = TimeZone.of(timezoneId)
        var message = "Here is the upcoming schedule (all times `$timezoneId`)\n\n"

        uiModel.upcomingEvents.forEach { upcomingEvent ->
            message += when (upcomingEvent) {
                is UpcomingEvent.DefaultRaidEvent -> defaultRaidText(upcomingEvent, timezone)
                is UpcomingEvent.ExceptionalRaidEvent -> exceptionalRaidText(upcomingEvent, timezone)
                is UpcomingEvent.AbsenceEvent -> absenceText(client, upcomingEvent)
            }
        }
        return message
    }

    private fun defaultRaidText(
        upcomingEvent: UpcomingEvent.DefaultRaidEvent,
        timezone: TimeZone
    ): String {
        val raidDateTime = upcomingEvent.timestamp.toLocalDateTime(timezone)
        val defaultRaidComment = formatComment(upcomingEvent.comment)

        var defaultRaidMessage = "Weekly raid$defaultRaidComment"
        if (upcomingEvent.cancellation.isCancelled) {
            val cancellationComment = formatComment(upcomingEvent.cancellation.comment)
            defaultRaidMessage = "~~$defaultRaidMessage~~\nCANCELLED$cancellationComment"
        }

        return "▷ [**${formatDateTime(raidDateTime)}**]\n$defaultRaidMessage\n\n"
    }


    private fun exceptionalRaidText(
        upcomingEvent: UpcomingEvent.ExceptionalRaidEvent,
        timezone: TimeZone
    ): String {
        val exceptionalRaidComment = formatComment(upcomingEvent.comment)
        val raidDateTime = upcomingEvent.timestamp.toLocalDateTime(timezone)
        val exceptionalRaidMessage = "Exceptional raid$exceptionalRaidComment"
        return "▷ [**${formatDateTime(raidDateTime)}**]\n$exceptionalRaidMessage\n\n"
    }

    private suspend fun absenceText(
        client: Kord,
        absence: UpcomingEvent.AbsenceEvent
    ): String = withContext(Dispatchers.IO) {
        val username = client.getUser(absence.userId)?.username ?: "REDACTED"
        val comment = absence.comment
        val commentMessage = formatComment(comment)
        "▷ [**${formatDayOfWeek(absence.date.dayOfWeek)} ${absence.date.dayOfMonth}/${absence.date.monthNumber}/${absence.date.year}**]\n" +
                "Absence of $username$commentMessage\n\n"
    }

    private suspend fun makeDefaultScheduleMessage(
        engine: SchedulingEngine,
        defaultRaidSchedule: DefaultRaidSchedule,
        timezoneId: String?,
        interaction: GuildChatInputCommandInteraction
    ) {
        interaction.respondPublic {
            val effectiveTimezoneId =
                if (timezoneId != null && engine.isCorrectTimezoneId(timezoneId)) timezoneId
                else defaultRaidSchedule.defaultTimezoneId

            var message = "Here is the default schedule (all times `$effectiveTimezoneId`)\n\n"

            defaultRaidSchedule.defaultRaids.forEach { defaultRaid ->
                val localTime = getLocalRaidTime(defaultRaid, effectiveTimezoneId)
                val (displayedHour, displayedMinute) = formatHourMinute(localTime)
                val commentMessage = formatComment(defaultRaid.comment)
                message += "▷ ${formatDayOfWeek(defaultRaid.dayOfWeek)} at $displayedHour:$displayedMinute $commentMessage\n"
            }

            content = message
        }
    }


    private fun getLocalRaidTime(
        defaultRaid: DefaultRaid,
        effectiveTimezoneId: String
    ) = Clock.System.now()
        .toLocalDateTime(TimeZone.UTC)
        .toJavaLocalDateTime()
        .withHour(defaultRaid.hoursUTC)
        .withMinute(defaultRaid.minutesUTC)
        .toKotlinLocalDateTime()
        .toInstant(TimeZone.UTC) // instant in UTC with the correct hours and minutes
        .toLocalDateTime(TimeZone.of(effectiveTimezoneId)) // converted to target timezone


    private fun formatDateTime(time: LocalDateTime): String {
        val (displayedHour, displayedMinute) = formatHourMinute(time)
        return "${formatDayOfWeek(time.date.dayOfWeek)} ${time.date.dayOfMonth}/${time.date.monthNumber}/${time.date.year} at ${displayedHour}:${displayedMinute}"
    }

    private fun formatHourMinute(time: LocalDateTime): Pair<String, String> {
        val displayedHour = if (time.hour < 10) "0" + time.hour.toString() else time.hour.toString()
        val displayedMinute = if (time.minute < 10) "0" + time.minute.toString() else time.minute.toString()
        return Pair(displayedHour, displayedMinute)
    }

    private fun formatDayOfWeek(dayOfWeek: DayOfWeek) =
        dayOfWeek.toString().lowercase().replaceFirstChar { it.uppercase() }

    private fun formatComment(comment: String?) = if (comment == null) "" else " - *${comment}*"

}