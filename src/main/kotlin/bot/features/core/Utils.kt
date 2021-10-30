package bot.features.core

import dev.kord.core.Kord
import dev.kord.core.entity.application.GuildChatInputCommand
import dev.kord.core.entity.application.GuildMessageCommand
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import kotlinx.coroutines.flow.toList

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