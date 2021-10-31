package bot.features.quotes

import bot.features.Feature
import bot.features.core.addChatInputCommandForEveryGuild
import bot.features.core.data.FeatureDataContract
import bot.features.core.data.RequiresData
import bot.features.core.noChangeUpdate
import bot.features.core.permissions.FeatureRolesContract
import bot.features.core.permissions.NecessaryRole
import bot.features.core.permissions.PermissionsHelper
import bot.features.quotes.data.*
import bot.features.quotes.model.QuotesEngine
import bot.features.quotes.subcommands.*
import dev.kord.core.Kord
import dev.kord.core.entity.application.GuildChatInputCommand
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.*

object QuotesFeature : Feature() {
    override val name: String = quotesFeatureName

    private val engine = QuotesEngine.instance()

    private lateinit var accessQuotesCommands: List<GuildChatInputCommand>
    private lateinit var editQuotesCommands: List<GuildChatInputCommand>

    override val featureDataContract: FeatureDataContract = RequiresData.guild(
        createNewData = { engine.makeInitialDataStructure() },
        updateExistingData = { data -> noChangeUpdate(data) }
    )

    private val editQuoteNecessaryRole =
        NecessaryRole(roleName = editQuoteNecessaryRoleName, color = editQuoteNecessaryRoleColor)

    override val featureRolesContract = FeatureRolesContract.RequiresRoles(
        necessaryRoles = setOf(editQuoteNecessaryRole)
    )

    override suspend fun Kord.addFeatureGlobalCommands() {}

    override suspend fun Kord.addFeatureGuildCommands() {
        accessQuotesCommands = addChatInputCommandForEveryGuild(
            name = accessQuotesCommandName,
            description = accessQuotesCommandDescription,
        ) {
            defaultPermission = true
            getQuoteCommand()
            getRandomQuoteCommand()
            addQuoteCommand()
        }

        editQuotesCommands = addChatInputCommandForEveryGuild(
            name = modifyQuotesCommandName,
            description = modifyQuotesCommandDescription,
        ) {
            defaultPermission = false
            editQuoteCommandGroup()
            removeQuoteCommand()
        }

        defineEditPermissions()
    }

    override suspend fun Kord.addFeatureResponses() {
        on<GuildChatInputCommandInteractionCreateEvent> {
            accessQuotesResponses()
            editQuotesResponses()
        }
    }

    private suspend fun Kord.defineEditPermissions() {
        editQuotesCommands.forEach { command ->
            getGuild(command.guildId)?.let { guild ->
                PermissionsHelper.authorizeRoleForCommandInGuild(
                    role = editQuoteNecessaryRole,
                    commandId = command.id,
                    guild = guild,
                    featureRolesContract = featureRolesContract,
                    client = this
                )
            }
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.accessQuotesResponses() {
        if (accessQuotesCommands.find { it.id == interaction.invokedCommandId } != null) {
            val options = interaction.data.data.options
            options.value?.forEach { option ->
                when (option.name) {
                    getQuoteCommandName -> GetQuoteSubCommand.getQuote(engine, option, this)
                    getRandomQuoteCommandName -> GetRandomQuoteSubcommand.getRandomQuote(engine, this)
                    addQuoteCommandName -> AddQuoteSubcommand.addQuote(engine, option, this)
                }
            }
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.editQuotesResponses() {
        if (editQuotesCommands.find { it.id == interaction.invokedCommandId } != null) {
            val options = interaction.data.data.options
            options.value?.forEach { option ->
                when (option.name) {
                    removeQuoteCommandName -> RemoveQuoteSubCommand.removeQuote(engine, option, this)
                    editQuoteCommandGroupName -> EditQuoteSubcommand.editQuote(engine, option, this)
                }
            }
        }
    }

    private fun ChatInputCreateBuilder.getQuoteCommand() {
        subCommand(getQuoteCommandName, getQuoteCommandDescription) {
            quoteNumberArgument()
        }
    }

    private fun ChatInputCreateBuilder.getRandomQuoteCommand() {
        subCommand(getRandomQuoteCommandName, getRandomQuoteCommandDescription)
    }

    private fun ChatInputCreateBuilder.addQuoteCommand() {
        subCommand(addQuoteCommandName, addQuoteCommandDescription) {
            quoteTextArgument()
            quoteAuthorArgument()
        }
    }

    private fun ChatInputCreateBuilder.editQuoteCommandGroup() {
        group(editQuoteCommandGroupName, editQuoteCommandGroupDescription) {
            subCommand(editQuoteTextCommandName, editQuoteTextCommandDescription) {
                quoteNumberArgument()
                quoteTextArgument()
            }
            subCommand(editQuoteAuthorCommandName, editQuoteAuthorCommandDescription) {
                quoteNumberArgument()
                quoteAuthorArgument()
            }
        }
    }

    private fun ChatInputCreateBuilder.removeQuoteCommand() {
        subCommand(removeQuoteCommandName, removeQuoteCommandDescription) {
            quoteNumberArgument()
        }
    }

    private fun SubCommandBuilder.quoteNumberArgument() =
        number(quoteNumberArgumentName, quoteNumberArgumentDescription) { required = true }

    private fun SubCommandBuilder.quoteTextArgument() =
        string(quoteTextArgumentName, quoteTextArgumentDescription) { required = true }

    private fun SubCommandBuilder.quoteAuthorArgument() =
        string(quoteAuthorArgumentName, quoteAuthorArgumentDescription) { required = true }

}