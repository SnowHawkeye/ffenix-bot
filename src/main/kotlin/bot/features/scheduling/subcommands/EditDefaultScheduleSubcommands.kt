package bot.features.scheduling.subcommands

import bot.features.core.catchCastExceptions
import bot.features.core.ephemeralResponse
import bot.features.core.typealiases.GuildId
import bot.features.scheduling.data.*
import bot.features.scheduling.model.SchedulingEngine
import bot.features.scheduling.model.results.AddDefaultRaidResult
import bot.features.scheduling.model.results.RemoveDefaultRaidResult
import bot.features.scheduling.model.results.SetTimezoneResult
import dev.kord.common.entity.SubCommand
import dev.kord.core.cache.data.OptionData
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent

object EditDefaultScheduleSubcommands {

    suspend fun editDefaultSchedule(
        engine: SchedulingEngine,
        option: OptionData,
        guildId: GuildId,
        interaction: GuildChatInputCommandInteractionCreateEvent,
    ) {
        return interaction.editDefaultSchedule(engine, option, guildId)
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.editDefaultSchedule(
        engine: SchedulingEngine,
        option: OptionData,
        guildId: GuildId
    ) {
        option.subCommands.value?.forEach { subCommand ->
            when (subCommand.name) {
                editDefaultScheduleTimezoneCommandName -> editDefaultScheduleTimezone(engine, subCommand, guildId)
                addDefaultRaidCommandName -> addDefaultRaid(engine, subCommand, guildId)
                removeDefaultRaidCommandName -> removeDefaultRaid(engine, subCommand, guildId)
            }
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.editDefaultScheduleTimezone(
        engine: SchedulingEngine,
        subCommand: SubCommand,
        guildId: GuildId,
    ) {
        var timezoneId = ""
        subCommand.options.value?.forEach {
            catchCastExceptions {
                if (it.name == timezoneArgumentName) timezoneId = it.value.toString()
            }
        }

        when (engine.setDefaultTimezone(timezoneId, guildId)) {
            SetTimezoneResult.Failure -> ephemeralResponse(genericErrorMessage)
            SetTimezoneResult.IncorrectTimezoneId -> ephemeralResponse(incorrectTimezoneIdErrorMessage)
            SetTimezoneResult.Success -> ephemeralResponse(setDefaultTimezoneSuccessMessage)
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.addDefaultRaid(
        engine: SchedulingEngine,
        subCommand: SubCommand,
        guildId: GuildId,
    ) {
        var day = ""
        var time = ""
        var timezoneId = ""
        var comment: String? = null

        subCommand.options.value?.forEach {
            catchCastExceptions {
                when (it.name) {
                    dayArgumentName -> day = it.value.toString()
                    timeArgumentName -> time = it.value.toString()
                    timezoneArgumentName -> timezoneId = it.value.toString()
                    raidCommentArgumentName -> comment = it.value.toString()
                }
            }
        }

        when (engine.addDefaultRaid(day, time, timezoneId, comment, guildId)) {
            AddDefaultRaidResult.Failure -> ephemeralResponse(genericErrorMessage)
            AddDefaultRaidResult.IncorrectTimeFormat -> ephemeralResponse(incorrectTimeErrorMessage)
            AddDefaultRaidResult.IncorrectTimezoneId -> ephemeralResponse(incorrectTimezoneIdErrorMessage)
            AddDefaultRaidResult.RaidAlreadyExists -> ephemeralResponse(raidAlreadyExistsErrorMessage)
            is AddDefaultRaidResult.Success -> ephemeralResponse(addDefaultRaidSuccessMessage)
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.removeDefaultRaid(
        engine: SchedulingEngine,
        subCommand: SubCommand,
        guildId: GuildId,
    ) {
        var day = ""
        var time = ""
        var timezoneId = ""

        subCommand.options.value?.forEach {
            catchCastExceptions {
                when (it.name) {
                    dayArgumentName -> day = it.value.toString()
                    timeArgumentName -> time = it.value.toString()
                    timezoneArgumentName -> timezoneId = it.value.toString()
                }
            }
        }

        when (engine.removeDefaultRaid(day, time, timezoneId, guildId)) {
            RemoveDefaultRaidResult.Failure -> ephemeralResponse(genericErrorMessage)
            RemoveDefaultRaidResult.IncorrectTimeFormat -> ephemeralResponse(incorrectTimeErrorMessage)
            RemoveDefaultRaidResult.IncorrectTimezoneId -> ephemeralResponse(incorrectTimezoneIdErrorMessage)
            RemoveDefaultRaidResult.RaidDoesNotExist -> ephemeralResponse(raidDoesNotExistErrorMessage)
            RemoveDefaultRaidResult.Success -> ephemeralResponse(removeDefaultRaidSuccessMessage)
        }
    }
}