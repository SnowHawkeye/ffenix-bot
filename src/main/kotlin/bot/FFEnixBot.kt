package bot

import bot.features.Feature
import dev.kord.core.Kord
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.on
import utils.withEach

/**
 * The main class representing a configurable bot.
 */
class FFEnixBot(
    private val client: Kord,
    private val features: Set<Feature>,
) {

    /**
     * Starts the bot's execution.
     */
    suspend fun start() = with(client) {
        setupDataStructure()
        login()
    }

    /**
     * Sets up the bot's data structure and adds the given [features].
     */
    private suspend fun setupDataStructure() {
        features.withEach {
            initializeGlobalData()
            addTo(client)
        }

        // Received both on startup and when joining a new guild
        client.on<GuildCreateEvent> { features.withEach { initializeGuildData(guild) } }
    }


}
