package bot.features.scheduling.subcommands

import bot.features.core.catchCastExceptions
import bot.features.core.ephemeralResponse
import bot.features.core.typealiases.GuildId
import bot.features.core.typealiases.UserId
import bot.features.scheduling.data.*
import bot.features.scheduling.model.SchedulingEngine
import bot.features.scheduling.model.results.AddAbsenceResult
import bot.features.scheduling.model.results.RemoveAbsenceResult
import dev.kord.common.entity.SubCommand
import dev.kord.core.cache.data.OptionData
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent

object AbsenceSubcommands {

    suspend fun manageAbsences(
        engine: SchedulingEngine,
        option: OptionData,
        guildId: GuildId,
        interaction: GuildChatInputCommandInteractionCreateEvent,
    ) {
        return interaction.manageAbsences(engine, option, guildId)
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.manageAbsences(
        engine: SchedulingEngine,
        option: OptionData,
        guildId: GuildId
    ) {
        option.subCommands.value?.forEach { subCommand ->
            when (subCommand.name) {
                addAbsenceCommandName -> addAbsence(engine, subCommand, guildId)
                removeAbsenceCommandName -> removeAbsence(engine, subCommand, guildId)
            }
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.addAbsence(
        engine: SchedulingEngine,
        subCommand: SubCommand,
        guildId: GuildId,
    ) {
        var date = ""
        var userId: UserId? = null
        var comment: String? = null

        subCommand.options.value?.forEach {
            catchCastExceptions {
                when (it.name) {
                    absentUserArgumentName -> userId = it.value as UserId
                    dateArgumentName -> date = it.value.toString()
                    absenceCommentArgumentName -> comment = it.value.toString()
                }
            }
        }

        if (userId == null) {
            ephemeralResponse(userNotFoundErrorMessage)
            return
        }

        when (engine.addAbsence(date, userId!!, comment, guildId)) {
            AddAbsenceResult.Failure -> ephemeralResponse(genericErrorMessage)
            AddAbsenceResult.DateIsInThePast -> ephemeralResponse(dateIsInThePastErrorMessage)
            AddAbsenceResult.IncorrectDate -> ephemeralResponse(incorrectDateErrorMessage)
            AddAbsenceResult.AbsenceAlreadyExists -> ephemeralResponse(absenceAlreadyExistsErrorMessage)
            AddAbsenceResult.Success -> ephemeralResponse(addAbsenceSuccessMessage)
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.removeAbsence(
        engine: SchedulingEngine,
        subCommand: SubCommand,
        guildId: GuildId,
    ) {
        var date = ""
        var userId: UserId? = null

        subCommand.options.value?.forEach {
            catchCastExceptions {
                when (it.name) {
                    absentUserArgumentName -> userId = it.value as UserId
                    dateArgumentName -> date = it.value.toString()
                }
            }
        }

        if (userId == null) {
            ephemeralResponse(genericErrorMessage)
            return
        }

        when (engine.removeAbsence(date, userId!!, guildId)) {
            RemoveAbsenceResult.Failure -> ephemeralResponse(genericErrorMessage)
            RemoveAbsenceResult.DateIsInThePast -> ephemeralResponse(dateIsInThePastErrorMessage)
            RemoveAbsenceResult.IncorrectDate -> ephemeralResponse(incorrectDateErrorMessage)
            RemoveAbsenceResult.NoSuchAbsence -> ephemeralResponse(noSuchAbsenceErrorMessage)
            RemoveAbsenceResult.Success -> ephemeralResponse(removeAbsenceSuccessMessage)
        }
    }
}