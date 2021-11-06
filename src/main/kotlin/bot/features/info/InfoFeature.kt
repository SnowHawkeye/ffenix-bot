package bot.features.info

import bot.features.Feature
import bot.features.core.*
import bot.features.core.data.FeatureDataContract
import bot.features.core.data.RequiresData
import bot.features.core.permissions.FeatureRolesContract
import bot.features.core.permissions.NecessaryRole
import bot.features.core.permissions.PermissionsHelper
import bot.features.info.data.*
import bot.features.info.model.InfoEngine
import bot.features.info.model.results.AddInfoCommandResult
import bot.features.info.model.results.EditInfoCommandResult
import bot.features.info.model.results.RemoveInfoCommandResult
import dev.kord.core.Kord
import dev.kord.core.cache.data.OptionData
import dev.kord.core.entity.application.GuildChatInputCommand
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.interaction.subCommand

object InfoFeature : Feature() {
    override val name = infoFeatureName

    private val engine = InfoEngine.instance()

    private lateinit var manageInfoCommands: List<GuildChatInputCommand>

    private val manageInfoCommandsRole = NecessaryRole(editInfoCommandsRoleName, editInfoCommandsRoleColor)
    override val featureRolesContract = FeatureRolesContract.RequiresRoles(setOf(manageInfoCommandsRole))

    override val featureDataContract: FeatureDataContract = RequiresData.guild(
        createNewData = { engine.makeInitialDataStructure() },
        updateExistingData = { data -> noChangeUpdate(data) }
    )

    override suspend fun Kord.addFeatureGlobalCommands() {
        on<MessageCreateEvent> {
            respondToInfoCommands(this@addFeatureGlobalCommands)
        }
    }

    override suspend fun Kord.addFeatureGuildCommands() {
        manageInfoCommands = addChatInputCommandForEveryGuild(
            name = infoCommandName,
            description = infoCommandDescription
        ) {
            manageInfoCommandsSubcommands()
        }

        defineManagePermissions()
    }

    override suspend fun Kord.addFeatureResponses() {
        on<GuildChatInputCommandInteractionCreateEvent> {
            addChatInputCommandResponse(manageInfoCommands) {
                val options = interaction.data.data.options
                options.value?.forEach { option ->
                    when (option.name) {
                        addInfoCommandName -> addInfoCommand(option)
                        editInfoCommandName -> editInfoCommand(option)
                        removeInfoCommandName -> removeInfoCommand(option)
                    }
                }
            }
        }
    }

    private suspend fun MessageCreateEvent.respondToInfoCommands(client: Kord) {
        if (message.author?.isBot == true) return
        if (message.content.length > 1 && message.content[0] == infoCommandPrefix) {
            val commandName = message.content.removePrefix(infoCommandPrefix.toString())
            val commandListForGuild = engine.getInfoCommands(forGuildId = message.getGuild().id)
            val command = commandListForGuild.find { it.commandName == commandName } ?: return
            message.channel.createMessage(command.commandText)
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.addInfoCommand(option: OptionData) {
        var commandName: String? = null
        var commandText: String? = null

        option.values.value?.forEach { commandArgument ->
            catchCastExceptions {
                when (commandArgument.name) {
                    commandToAddArgumentName -> commandName = commandArgument.value.toString()
                    commandTextArgumentName -> commandText = commandArgument.value.toString()
                }
            }
        }

        if (commandName == null || commandText == null) {
            ephemeralResponse(genericErrorMessage)
            return
        }

        return when (val addInfoCommandResult =
            engine.addInfoCommand(commandName!!, commandText!!, interaction.guildId)) {
            AddInfoCommandResult.Failure -> ephemeralResponse(genericErrorMessage)
            AddInfoCommandResult.CommandAlreadyExists -> ephemeralResponse(commandAlreadyExistsErrorMessage)
            is AddInfoCommandResult.Success -> {
                ephemeralResponse(commandSuccessFullyAddedMessage(addInfoCommandResult.command.commandName))
            }
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.editInfoCommand(option: OptionData) {
        var commandName: String? = null
        var commandText: String? = null

        option.values.value?.forEach { commandArgument ->
            catchCastExceptions {
                when (commandArgument.name) {
                    commandToEditArgumentName -> commandName = commandArgument.value.toString()
                    commandEditedTextArgumentName -> commandText = commandArgument.value.toString()
                }
            }
        }

        if (commandName == null || commandText == null) {
            ephemeralResponse(genericErrorMessage)
            return
        }

        return when (val editInfoCommandResult =
            engine.editInfoCommand(commandName!!, commandText!!, interaction.guildId)) {
            EditInfoCommandResult.Failure -> ephemeralResponse(genericErrorMessage)
            EditInfoCommandResult.CommandDoesNotExist -> ephemeralResponse(commandDoesNotExist)
            is EditInfoCommandResult.Success -> {
                ephemeralResponse(commandSuccessFullyEditedMessage(editInfoCommandResult.newCommand.commandName))
            }
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.removeInfoCommand(option: OptionData) {
        var commandName: String? = null

        option.values.value?.forEach { commandArgument ->
            catchCastExceptions {
                when (commandArgument.name) {
                    commandToRemoveArgumentName -> commandName = commandArgument.value.toString()
                }
            }
        }

        if (commandName == null) {
            ephemeralResponse(genericErrorMessage)
            return
        }

        return when (val removeInfoCommandResult = engine.removeInfoCommand(commandName!!, interaction.guildId)) {
            RemoveInfoCommandResult.Failure -> ephemeralResponse(genericErrorMessage)
            RemoveInfoCommandResult.CommandDoesNotExist -> ephemeralResponse(commandDoesNotExist)
            is RemoveInfoCommandResult.Success -> {
                ephemeralResponse(commandSuccessFullyRemovedMessage(removeInfoCommandResult.command.commandName))
            }
        }
    }


    private fun ChatInputCreateBuilder.manageInfoCommandsSubcommands() {
        defaultPermission = false

        subCommand(addInfoCommandName, addInfoCommandDescription) {
            string(commandToAddArgumentName, commandToAddArgumentDescription) { required = true }
            string(commandTextArgumentName, commandTextArgumentDescription) { required = true }
        }
        subCommand(editInfoCommandName, editInfoCommandDescription) {
            string(commandToEditArgumentName, commandToEditArgumentDescription) { required = true }
            string(commandEditedTextArgumentName, commandEditedTextArgumentDescription) { required = true }
        }
        subCommand(removeInfoCommandName, removeInfoCommandDescription) {
            string(commandToRemoveArgumentName, commandToRemoveArgumentDescription) { required = true }
        }
    }

    private suspend fun Kord.defineManagePermissions() {
        manageInfoCommands.forEach { command ->
            getGuild(command.guildId)?.let { guild ->
                PermissionsHelper.authorizeRoleForCommandInGuild(
                    role = manageInfoCommandsRole,
                    commandId = command.id,
                    guild = guild,
                    featureRolesContract = featureRolesContract,
                    client = this
                )
            }
        }
    }
}