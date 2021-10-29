package bot.features.quotes.subcommands

import bot.features.quotes.data.quoteNumberArgumentName
import bot.features.quotes.data.quoteRemoveFailureMessage
import bot.features.quotes.data.quoteRemoveMessageSuccess
import bot.features.quotes.model.QuotesEngine
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.cache.data.OptionData
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import utils.logging.Log

object RemoveQuoteSubCommand {

    suspend fun removeQuote(
        engine: QuotesEngine,
        option: OptionData,
        interaction: GuildChatInputCommandInteractionCreateEvent
    ) {
        return interaction.removeQuote(engine, option)
    }


    private suspend fun GuildChatInputCommandInteractionCreateEvent.removeQuote(
        engine: QuotesEngine,
        option: OptionData
    ) {
        var quoteNumber = Int.MIN_VALUE
        try {
            option.values.value?.forEach { commandArgument ->
                if (commandArgument.name == quoteNumberArgumentName)
                    quoteNumber = (commandArgument.value as Double).toInt()
            }
        } catch (e: ClassCastException) {
            Log.error("An argument with a wrong type was provided.", "RemoveQuoteSubCommand")
            Log.error(e)
        }

        when (val attemptRemoveQuote = engine.removeQuote(quoteNumber, interaction.guildId)) {
            QuotesEngine.QuoteResult.Failure -> interaction.respondEphemeral { content = quoteRemoveFailureMessage }
            is QuotesEngine.QuoteResult.Success -> {
                interaction.respondEphemeral { content = quoteRemoveMessageSuccess(attemptRemoveQuote.text) }
            }
        }

    }
}