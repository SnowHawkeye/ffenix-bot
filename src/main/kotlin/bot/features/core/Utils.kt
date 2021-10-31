package bot.features.core

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.cache.data.OptionData
import dev.kord.core.entity.application.GuildChatInputCommand
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import kotlinx.coroutines.flow.toList
import utils.logging.Log

inline fun <reified Data> noChangeUpdate(any: Any): Data {
    if (any is Data) return any
    else throw IllegalStateException("Unexpected data type for ${Data::class.java.name}: ${any::class.java.name}.")
}

suspend fun Kord.addChatInputCommandForEveryGuild(
    name: String,
    description: String,
    builder: ChatInputCreateBuilder.() -> Unit = {}
): List<GuildChatInputCommand> {
    return guilds.toList().map { guild ->
        createGuildChatInputCommand(
            guildId = guild.id,
            name = name,
            description = description,
            builder = builder
        )
    }
}

suspend fun GuildChatInputCommandInteractionCreateEvent.addChatInputCommandResponse(
    commands: List<GuildChatInputCommand>,
    howToProcessOptions: suspend (OptionData) -> Unit
) {
    val invokedCommand = commands.find { it.id == interaction.invokedCommandId }
    if (invokedCommand != null) {
        val options = interaction.data.data.options
        options.value?.forEach { option -> howToProcessOptions(option) }
    }
}

suspend fun catchCastExceptions(block: () -> Unit) {
    try {
        block()
    } catch (e: ClassCastException) {
        Log.error("An argument with a wrong type was provided.")
        Log.error(e)
    }

}

suspend fun InteractionCreateEvent.ephemeralResponse(message: String) {
    interaction.respondEphemeral { content = message }
}