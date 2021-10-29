package bot.features.quotes.subcommands

import bot.features.quotes.data.quoteGetFailureMessage
import bot.features.quotes.data.quoteNumberArgumentName
import bot.features.quotes.model.QuotesEngine
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.cache.data.OptionData
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import utils.logging.Log

object GetQuoteSubCommand {

    suspend fun getQuote(
        engine: QuotesEngine,
        option: OptionData,
        interaction: GuildChatInputCommandInteractionCreateEvent
    ) {
        return interaction.getQuote(engine, option)
    }


    private suspend fun GuildChatInputCommandInteractionCreateEvent.getQuote(
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
            Log.error("An argument with a wrong type was provided.", "GetQuoteSubCommand")
            Log.error(e)
        }

        when (val attemptGetQuote = engine.getQuote(quoteNumber, interaction.guildId)) {
            QuotesEngine.QuoteResult.Failure -> interaction.respondEphemeral { content = quoteGetFailureMessage }
            is QuotesEngine.QuoteResult.Success -> interaction.respondPublic { content = attemptGetQuote.text }
        }

    }
}