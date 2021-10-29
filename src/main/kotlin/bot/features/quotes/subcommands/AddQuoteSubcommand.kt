package bot.features.quotes.subcommands

import bot.features.quotes.data.quoteAddFailureMessage
import bot.features.quotes.data.quoteAddSuccessMessage
import bot.features.quotes.data.quoteAuthorArgumentName
import bot.features.quotes.data.quoteTextArgumentName
import bot.features.quotes.model.QuotesEngine
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.cache.data.OptionData
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import java.time.Instant
import java.util.*

object AddQuoteSubcommand {

    suspend fun addQuote(
        engine: QuotesEngine,
        option: OptionData,
        interaction: GuildChatInputCommandInteractionCreateEvent
    ) {
        return interaction.addQuote(engine, option)
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.addQuote(
        engine: QuotesEngine,
        option: OptionData
    ) {
        var quoteText = ""
        var quoteAuthor = ""
        option.values.value?.forEach { commandArgument ->
            when (commandArgument.name) {
                quoteTextArgumentName -> quoteText = commandArgument.value.toString()
                quoteAuthorArgumentName -> quoteAuthor = commandArgument.value.toString()
            }
        }

        if (quoteAuthor.isNotEmpty() && quoteText.isNotEmpty()) {
            val attemptAddQuote = engine.addQuote(
                quoteText = quoteText,
                quoteAuthor = quoteAuthor,
                quoteDate = Date.from(Instant.now()),
                forGuildId = interaction.guildId
            )
            when (attemptAddQuote) {
                QuotesEngine.QuoteResult.Failure -> interaction.respondEphemeral {
                    content = quoteAddFailureMessage
                }
                is QuotesEngine.QuoteResult.Success -> {
                    interaction.channel.createMessage(attemptAddQuote.text)
                    interaction.respondEphemeral { content = quoteAddSuccessMessage }
                }
            }
        } else interaction.respondEphemeral { content = quoteAddFailureMessage }
    }
}