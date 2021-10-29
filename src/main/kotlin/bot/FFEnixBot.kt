package bot

import bot.features.Feature
import dev.kord.core.Kord
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
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
        setup()
        login()
    }

    /**
     * Sets up the bot and adds the given [features].
     */
    private suspend fun setup() {
        deleteExistingCommands()
        // Received both on startup and when joining a new guild
        client.on<GuildCreateEvent> {
            features.withEach {
                addGuildCommands(client)
                initializeGuildData(guild)
            }
        }
        features.withEach {
            addGlobalCommands(client)
            addResponses(client)
            initializeGlobalData()
        }
    }

    private suspend fun deleteExistingCommands() = with(client) {
        guilds.collect { guild ->
            getGuildApplicationCommands(guild.id).toList().forEach { it.delete() }
        }
        globalCommands.toList().forEach { it.delete() }
    }
}
