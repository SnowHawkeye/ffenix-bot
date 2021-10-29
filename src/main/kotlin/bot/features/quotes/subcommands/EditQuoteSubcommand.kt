package bot.features.quotes.subcommands

import bot.features.quotes.data.*
import bot.features.quotes.model.QuotesEngine
import dev.kord.common.entity.SubCommand
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.cache.data.OptionData
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import utils.logging.Log

object EditQuoteSubcommand {

    suspend fun editQuote(
        engine: QuotesEngine,
        option: OptionData,
        interaction: GuildChatInputCommandInteractionCreateEvent
    ) {
        return interaction.editQuote(engine, option)
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.editQuote(
        engine: QuotesEngine,
        option: OptionData
    ) {
        option.subCommands.value?.forEach { subCommand ->
            when (subCommand.name) {
                editQuoteTextCommandName -> editQuoteText(engine, subCommand)
                editQuoteAuthorCommandName -> editQuoteAuthor(engine, subCommand)
            }
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.editQuoteText(
        engine: QuotesEngine,
        subCommand: SubCommand
    ) {
        var quoteText = ""
        var quoteNumber = Int.MIN_VALUE
        subCommand.options.value?.forEach { commandArgument ->
            try {
                when (commandArgument.name) {
                    quoteTextArgumentName -> quoteText = commandArgument.value.toString()
                    quoteNumberArgumentName -> quoteNumber = (commandArgument.value as Double).toInt()
                }
            } catch (e: ClassCastException) {
                Log.error("An argument with a wrong type was provided.", "GetQuoteSubCommand")
                Log.error(e)
            }
        }

        if (quoteText.isNotEmpty() && quoteNumber != Int.MIN_VALUE) {
            when (val attemptEditQuote = engine.editQuoteText(quoteNumber, quoteText, interaction.guildId)) {
                QuotesEngine.QuoteResult.Failure -> interaction.respondEphemeral {
                    content = quoteEditFailureMessage
                }
                is QuotesEngine.QuoteResult.Success -> {
                    interaction.channel.createMessage(attemptEditQuote.text)
                    interaction.respondEphemeral { content = quoteEditTextSuccessMessage }
                }
            }
        } else interaction.respondEphemeral { content = quoteEditFailureMessage }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.editQuoteAuthor(
        engine: QuotesEngine,
        subCommand: SubCommand
    ) {
        var quoteAuthor = ""
        var quoteNumber = Int.MIN_VALUE
        subCommand.options.value?.forEach { commandArgument ->
            try {
                when (commandArgument.name) {
                    quoteAuthorArgumentName -> quoteAuthor = commandArgument.value.toString()
                    quoteNumberArgumentName -> quoteNumber = (commandArgument.value as Double).toInt()
                }
            } catch (e: ClassCastException) {
                Log.error("An argument with a wrong type was provided.", "GetQuoteSubCommand")
                Log.error(e)
            }
        }

        if (quoteAuthor.isNotEmpty() && quoteNumber != Int.MIN_VALUE) {
            when (val attemptEditQuote = engine.editQuoteAuthor(quoteNumber, quoteAuthor, interaction.guildId)) {
                QuotesEngine.QuoteResult.Failure -> interaction.respondEphemeral {
                    content = quoteEditFailureMessage
                }
                is QuotesEngine.QuoteResult.Success -> {
                    interaction.channel.createMessage(attemptEditQuote.text)
                    interaction.respondEphemeral { content = quoteEditAuthorSuccessMessage }
                }
            }
        } else interaction.respondEphemeral { content = quoteEditFailureMessage }
    }
}


