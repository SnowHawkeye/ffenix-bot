package bot.features.scheduling

import bot.features.Feature
import bot.features.core.addChatInputCommandForEveryGuild
import bot.features.core.addChatInputCommandResponse
import bot.features.core.data.RequiresData
import bot.features.core.noChangeUpdate
import bot.features.core.permissions.FeatureRolesContract
import bot.features.core.permissions.NecessaryRole
import bot.features.core.permissions.PermissionsHelper
import bot.features.scheduling.data.*
import bot.features.scheduling.model.SchedulingEngine
import bot.features.scheduling.subcommands.*
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.application.GuildChatInputCommand
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.*

object SchedulingFeature : Feature() {
    override val name: String = schedulingFeatureName

    private val engine = SchedulingEngine.instance()

    private lateinit var getTimezonesCommands: List<GuildChatInputCommand>
    private lateinit var displayScheduleCommands: List<GuildChatInputCommand>
    private lateinit var editScheduleCommands: List<GuildChatInputCommand>

    override val featureDataContract = RequiresData.guild(
        createNewData = { engine.makeInitialDataStructure() },
        updateExistingData = { data -> noChangeUpdate(data) }
    )

    private val editScheduleNecessaryRole = NecessaryRole(schedulerNecessaryRoleName, schedulerNecessaryRoleColor)
    override val featureRolesContract = FeatureRolesContract.RequiresRoles(setOf(editScheduleNecessaryRole))

    override suspend fun Kord.addFeatureGlobalCommands() {}

    override suspend fun Kord.addFeatureGuildCommands() {
        getTimezonesCommands = addChatInputCommandForEveryGuild(
            name = getTimezonesCommandName,
            description = getTimezonesCommandDescription,
        )

        displayScheduleCommands = addChatInputCommandForEveryGuild(
            name = displayScheduleCommandName,
            description = displayScheduleCommandDescription
        ) {
            defaultPermission = true
            displayScheduleCommand()
        }

        editScheduleCommands = addChatInputCommandForEveryGuild(
            name = editScheduleCommandName,
            description = editScheduleCommandDescription,
        ) {
            defaultPermission = false
            editDefaultScheduleSubcommandGroup()
            cancellationSubcommandGroup()
            exceptionalRaidSubcommandGroup()
            absenceSubcommandGroup()
        }

        defineEditPermissions()
    }


    override suspend fun Kord.addFeatureResponses() {
        on<GuildChatInputCommandInteractionCreateEvent> {
            addChatInputCommandResponse(getTimezonesCommands) { getTimezonesResponses() }
            addChatInputCommandResponse(displayScheduleCommands) { displayScheduleResponses(this@addFeatureResponses) }
            addChatInputCommandResponse(editScheduleCommands) { editScheduleResponses() }
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.getTimezonesResponses() {
        interaction.respondEphemeral { content = getTimezonesResponse }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.displayScheduleResponses(client: Kord) {
        val options = interaction.data.data.options
        options.value?.forEach { option ->
            when (option.name) {
                displayDefaultScheduleCommandName ->
                    DisplayScheduleSubcommands.displayDefaultSchedule(engine, option, interaction.guildId, this)
                displayScheduleByNumberCommandName ->
                    DisplayScheduleSubcommands.displayScheduleByNumber(
                        engine = engine,
                        option = option,
                        guildId = interaction.guildId,
                        interaction = this,
                        client = client
                    )
                displayScheduleByDateCommandName ->
                    DisplayScheduleSubcommands.displayScheduleByDate(
                        engine = engine,
                        option = option,
                        guildId = interaction.guildId,
                        interaction = this,
                        client = client
                    )
            }
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.editScheduleResponses() {
        val options = interaction.data.data.options
        options.value?.forEach { option ->
            when (option.name) {
                editDefaultScheduleCommandName ->
                    EditDefaultScheduleSubcommands.editDefaultSchedule(engine, option, interaction.guildId, this)
                cancellationsCommandName ->
                    CancellationSubcommands.manageCancellations(engine, option, interaction.guildId, this)
                exceptionsCommandName ->
                    ExceptionalRaidsSubcommands.manageExceptionalRaids(engine, option, interaction.guildId, this)
                absencesCommandName ->
                    AbsenceSubcommands.manageAbsences(engine, option, interaction.guildId, this)
            }
        }
    }

    private suspend fun Kord.defineEditPermissions() {
        editScheduleCommands.forEach { command ->
            getGuild(command.guildId)?.let { guild ->
                PermissionsHelper.authorizeRoleForCommandInGuild(
                    role = editScheduleNecessaryRole,
                    commandId = command.id,
                    guild = guild,
                    featureRolesContract = featureRolesContract,
                    client = this
                )
            }
        }
    }

    private fun ChatInputCreateBuilder.editDefaultScheduleSubcommandGroup() {
        group(editDefaultScheduleCommandName, editDefaultScheduleCommandDescription) {
            subCommand(editDefaultScheduleTimezoneCommandName, editDefaultScheduleTimezoneCommandDescription) {
                string(timezoneDisplayArgumentName, timezoneDisplayArgumentDescription) { required = true }
            }

            subCommand(addDefaultRaidCommandName, addDefaultRaidCommandDescription) {
                dayMultipleChoiceArgument()
                string(timeArgumentName, timeArgumentDescription) { required = true }
                string(timezoneArgumentName, timezoneArgumentDescription) { required = true }
                string(raidCommentArgumentName, raidCommentArgumentDescription) { required = false }
            }

            subCommand(removeDefaultRaidCommandName, removeDefaultRaidCommandDescription) {
                dayMultipleChoiceArgument()
                string(timeArgumentName, timeArgumentDescription) { required = true }
                string(timezoneArgumentName, timezoneArgumentDescription) { required = true }
            }
        }
    }

    private fun ChatInputCreateBuilder.cancellationSubcommandGroup() {
        group(cancellationsCommandName, cancellationsCommandDescription) {
            subCommand(cancelDefaultRaidCommandName, cancelDefaultRaidCommandDescription) {
                string(dateArgumentName, dateArgumentDescription) { required = true }
                string(timeArgumentName, timeArgumentDescription) { required = true }
                string(timezoneArgumentName, timezoneArgumentDescription) { required = true }
                string(cancellationCommentArgumentName, cancellationCommentArgumentDescription) { required = false }
            }

            subCommand(revertDefaultRaidCancellationCommandName, revertDefaultRaidCancellationCommandDescription) {
                string(dateArgumentName, dateArgumentDescription) { required = true }
                string(timeArgumentName, timeArgumentDescription) { required = true }
                string(timezoneArgumentName, timezoneArgumentDescription) { required = true }
            }
        }
    }

    private fun ChatInputCreateBuilder.exceptionalRaidSubcommandGroup() {
        group(exceptionsCommandName, exceptionsCommandDescription) {
            subCommand(addExceptionalRaidCommandName, addExceptionalRaidCommandDescription) {
                string(dateArgumentName, dateArgumentDescription) { required = true }
                string(timeArgumentName, timeArgumentDescription) { required = true }
                string(timezoneArgumentName, timezoneArgumentDescription) { required = true }
                string(raidCommentArgumentName, raidCommentArgumentDescription) { required = false }
            }

            subCommand(cancelExceptionalRaidCommandName, cancelExceptionalRaidCommandDescription) {
                string(dateArgumentName, dateArgumentDescription) { required = true }
                string(timeArgumentName, timeArgumentDescription) { required = true }
                string(timezoneArgumentName, timezoneArgumentDescription) { required = true }
            }
        }
    }

    private fun ChatInputCreateBuilder.absenceSubcommandGroup() {
        group(absencesCommandName, absencesCommandDescription) {
            subCommand(addAbsenceCommandName, addAbsenceCommandDescription) {
                user(absentUserArgumentName, absentUserArgumentDescription) { required = true }
                string(absenceDateArgumentName, absenceDateArgumentDescription) { required = true }
                string(absenceCommentArgumentName, absenceCommentArgumentDescription) { required = false }
            }

            subCommand(removeAbsenceCommandName, removeAbsenceCommandDescription) {
                user(absentUserArgumentName, absentUserArgumentDescription) { required = true }
                string(absenceDateArgumentName, absenceDateArgumentDescription) { required = true }
            }
        }
    }

    private fun SubCommandBuilder.dayMultipleChoiceArgument() {
        string(dayArgumentName, dayArgumentDescription) {
            required = true
            choice(mondayChoice, mondayChoice)
            choice(tuesdayChoice, tuesdayChoice)
            choice(wednesdayChoice, wednesdayChoice)
            choice(thursdayChoice, thursdayChoice)
            choice(fridayChoice, fridayChoice)
            choice(saturdayChoice, saturdayChoice)
            choice(sundayChoice, sundayChoice)
        }
    }

    private fun ChatInputCreateBuilder.displayScheduleCommand() {
        subCommand(displayDefaultScheduleCommandName, displayDefaultScheduleCommandDescription) {
            string(timezoneDisplayArgumentName, timezoneDisplayArgumentDescription) { required = false }
        }

        subCommand(displayScheduleByNumberCommandName, displayScheduleByNumberCommandDescription) {
            int(numberOfRaidsToDisplayArgumentName, numberOfRaidsToDisplayArgumentDescription) { required = false }
            string(timezoneDisplayArgumentName, timezoneDisplayArgumentDescription) { required = false }
        }

        subCommand(displayScheduleByDateCommandName, displayScheduleByDateCommandDescription) {
            string(limitDisplayDateArgumentName, limitDisplayDateArgumentDescription) { required = true }
            string(timezoneDisplayArgumentName, timezoneDisplayArgumentDescription) { required = false }
        }
    }

}