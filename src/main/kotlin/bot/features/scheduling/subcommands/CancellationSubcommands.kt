package bot.features.scheduling.subcommands

import bot.features.core.catchCastExceptions
import bot.features.core.ephemeralResponse
import bot.features.core.typealiases.GuildId
import bot.features.scheduling.data.*
import bot.features.scheduling.model.SchedulingEngine
import bot.features.scheduling.model.results.CancelDefaultRaidResult
import bot.features.scheduling.model.results.RevertDefaultRaidCancellationResult
import dev.kord.common.entity.SubCommand
import dev.kord.core.cache.data.OptionData
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent

object CancellationSubcommands {

    suspend fun manageCancellations(
        engine: SchedulingEngine,
        option: OptionData,
        guildId: GuildId,
        interaction: GuildChatInputCommandInteractionCreateEvent,
    ) {
        return interaction.manageCancellations(engine, option, guildId)
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.manageCancellations(
        engine: SchedulingEngine,
        option: OptionData,
        guildId: GuildId
    ) {
        option.subCommands.value?.forEach { subCommand ->
            when (subCommand.name) {
                cancelDefaultRaidCommandName -> cancelDefaultRaid(engine, subCommand, guildId)
                revertDefaultRaidCancellationCommandName -> revertDefaultRaidCancellation(engine, subCommand, guildId)
            }
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.cancelDefaultRaid(
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
                    cancellationCommentArgumentName -> comment = it.value.toString()
                }
            }
        }

        when (engine.cancelDefaultRaid(date, time, timezoneId, comment, guildId)) {
            CancelDefaultRaidResult.Failure -> ephemeralResponse(genericErrorMessage)
            CancelDefaultRaidResult.DateIsInThePast -> ephemeralResponse(dateIsInThePastErrorMessage)
            CancelDefaultRaidResult.IncorrectDate -> ephemeralResponse(incorrectDateErrorMessage)
            CancelDefaultRaidResult.IncorrectTimeFormat -> ephemeralResponse(incorrectTimeErrorMessage)
            CancelDefaultRaidResult.IncorrectTimezoneId -> ephemeralResponse(incorrectTimezoneIdErrorMessage)
            CancelDefaultRaidResult.NoRaidPlanned -> ephemeralResponse(noRaidPlannedErrorMessage)
            CancelDefaultRaidResult.RaidAlreadyCancelled -> ephemeralResponse(raidAlreadyCancelledErrorMessage)
            CancelDefaultRaidResult.Success -> ephemeralResponse(cancelDefaultRaidSuccess)
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.revertDefaultRaidCancellation(
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

        when (engine.revertDefaultRaidCancellation(date, time, timezoneId, guildId)) {
            RevertDefaultRaidCancellationResult.Failure -> ephemeralResponse(genericErrorMessage)
            RevertDefaultRaidCancellationResult.IncorrectDate -> ephemeralResponse(incorrectDateErrorMessage)
            RevertDefaultRaidCancellationResult.IncorrectTimeFormat -> ephemeralResponse(incorrectTimeErrorMessage)
            RevertDefaultRaidCancellationResult.IncorrectTimezoneId -> ephemeralResponse(incorrectTimezoneIdErrorMessage)
            RevertDefaultRaidCancellationResult.NothingToRevert -> ephemeralResponse(noCancellationToRevertErrorMessage)
            RevertDefaultRaidCancellationResult.Success -> ephemeralResponse(revertCancellationSuccess)
        }
    }
}