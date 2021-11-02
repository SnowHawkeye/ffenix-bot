package bot.features.scheduling.subcommands

import bot.features.core.catchCastExceptions
import bot.features.core.ephemeralResponse
import bot.features.core.typealiases.GuildId
import bot.features.scheduling.data.*
import bot.features.scheduling.model.SchedulingEngine
import bot.features.scheduling.model.results.AddExceptionalRaidResult
import bot.features.scheduling.model.results.CancelExceptionalRaidResult
import dev.kord.common.entity.SubCommand
import dev.kord.core.cache.data.OptionData
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent

object ExceptionalRaidsSubcommands {

    suspend fun manageExceptionalRaids(
        engine: SchedulingEngine,
        option: OptionData,
        guildId: GuildId,
        interaction: GuildChatInputCommandInteractionCreateEvent,
    ) {
        return interaction.manageExceptionalRaids(engine, option, guildId)
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.manageExceptionalRaids(
        engine: SchedulingEngine,
        option: OptionData,
        guildId: GuildId
    ) {
        option.subCommands.value?.forEach { subCommand ->
            when (subCommand.name) {
                addExceptionalRaidCommandName -> addExceptionalRaid(engine, subCommand, guildId)
                cancelExceptionalRaidCommandName -> cancelExceptionalRaid(engine, subCommand, guildId)
            }
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.addExceptionalRaid(
        engine: SchedulingEngine,
        subCommand: SubCommand,
        guildId: GuildId,
    ) {
        var date = ""
        var time = ""
        var timezoneId = ""
        var comment: String? = null

        subCommand.options.value?.forEach {
            catchCastExceptions {
                when (it.name) {
                    dateArgumentName -> date = it.value.toString()
                    timeArgumentName -> time = it.value.toString()
                    timezoneArgumentName -> timezoneId = it.value.toString()
                    raidCommentArgumentName -> comment = it.value.toString()
                }
            }
        }

        when (engine.addExceptionalRaid(date, time, timezoneId, comment, guildId)) {
            AddExceptionalRaidResult.Failure -> ephemeralResponse(genericErrorMessage)
            AddExceptionalRaidResult.DateIsInThePast -> ephemeralResponse(dateIsInThePastErrorMessage)
            AddExceptionalRaidResult.IncorrectDate -> ephemeralResponse(incorrectDateErrorMessage)
            AddExceptionalRaidResult.IncorrectTimeFormat -> ephemeralResponse(incorrectTimeErrorMessage)
            AddExceptionalRaidResult.IncorrectTimezoneId -> ephemeralResponse(incorrectTimezoneIdErrorMessage)
            AddExceptionalRaidResult.RaidAlreadyExists -> ephemeralResponse(raidAlreadyExistsErrorMessage)
            AddExceptionalRaidResult.Success -> ephemeralResponse(addExceptionalRaidSuccessMessage)
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.cancelExceptionalRaid(
        engine: SchedulingEngine,
        subCommand: SubCommand,
        guildId: GuildId,
    ) {
        var date = ""
        var time = ""
        var timezoneId = ""

        subCommand.options.value?.forEach {
            catchCastExceptions {
                when (it.name) {
                    dateArgumentName -> date = it.value.toString()
                    timeArgumentName -> time = it.value.toString()
                    timezoneArgumentName -> timezoneId = it.value.toString()
                }
            }
        }

        when (engine.cancelExceptionalRaid(date, time, timezoneId, guildId)) {
            CancelExceptionalRaidResult.Failure -> ephemeralResponse(genericErrorMessage)
            CancelExceptionalRaidResult.IncorrectDate -> ephemeralResponse(incorrectDateErrorMessage)
            CancelExceptionalRaidResult.IncorrectTimeFormat -> ephemeralResponse(incorrectTimeErrorMessage)
            CancelExceptionalRaidResult.IncorrectTimezoneId -> ephemeralResponse(incorrectTimezoneIdErrorMessage)
            CancelExceptionalRaidResult.NothingToCancel -> ephemeralResponse(nothingToCancelErrorMessage)
            CancelExceptionalRaidResult.Success -> ephemeralResponse(cancelExceptionalRaidSuccessMessage)
        }
    }
}