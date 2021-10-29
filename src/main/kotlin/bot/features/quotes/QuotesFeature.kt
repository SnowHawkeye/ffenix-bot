package bot.features.quotes

import bot.features.Feature
import bot.features.core.addChatInputCommandForEveryGuild
import bot.features.core.data.FeatureDataContract
import bot.features.core.data.RequiresData
import bot.features.core.noChangeUpdate
import bot.features.quotes.data.*
import bot.features.quotes.model.QuotesEngine
import bot.features.quotes.subcommands.*
import dev.kord.core.Kord
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.*

object QuotesFeature : Feature() {
    override val name: String = quotesFeatureName

    private val engine = QuotesEngine.instance()

    override val featureDataContract: FeatureDataContract = RequiresData.guild(
        createNewData = { engine.makeInitialDataStructure() },
        updateExistingData = { data -> noChangeUpdate(data) }
    )

    override suspend fun Kord.addFeatureGlobalCommands() {}

    override suspend fun Kord.addFeatureGuildCommands() {
        addChatInputCommandForEveryGuild(
            name = quotesCommandName,
            description = quotesCommandDescription,
        ) {
            getQuoteCommand()
            getRandomQuoteCommand()
            addQuoteCommand()
            editQuoteCommandGroup()
            removeQuoteCommand()
        }
    }

    override suspend fun Kord.addFeatureResponses() {
        on<GuildChatInputCommandInteractionCreateEvent> {
            val options = interaction.data.data.options
            options.value?.forEach { option ->
                when (option.name) {
                    getQuoteCommandName -> GetQuoteSubCommand.getQuote(engine, option, this)
                    getRandomQuoteCommandName -> GetRandomQuoteSubcommand.getRandomQuote(engine, this)
                    addQuoteCommandName -> AddQuoteSubcommand.addQuote(engine, option, this)
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