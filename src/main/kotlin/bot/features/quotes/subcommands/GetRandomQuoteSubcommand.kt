package bot.features.quotes.subcommands

import bot.features.quotes.model.QuotesEngine
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent

object GetRandomQuoteSubcommand {
    suspend fun getRandomQuote(engine: QuotesEngine, interaction: GuildChatInputCommandInteractionCreateEvent) {
        return interaction.getRandomQuote(engine)
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.getRandomQuote(engine: QuotesEngine) {
        interaction.respondPublic {
            content = engine.getRandomQuote(interaction.guildId)
        }
    }
}