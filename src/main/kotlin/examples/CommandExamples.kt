package examples

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.acknowledgePublicUpdateMessage
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.application.GuildChatInputCommand
import dev.kord.core.entity.application.GuildMessageCommand
import dev.kord.core.entity.application.GuildUserCommand
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.GuildMessageCommandInteractionCreateEvent
import dev.kord.core.event.interaction.GuildUserCommandInteractionCreateEvent
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.boolean
import dev.kord.rest.builder.message.create.actionRow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import runtime.FFENIX_BOT_TOKEN_KEY

/**
 * These examples are not well-made, but can be a good reference for future use of the Kord API.
 */

suspend fun main() {
    val token = System.getenv(FFENIX_BOT_TOKEN_KEY)
    val client = Kord(token)

    val guildIds = client.guilds.map { it.id }

    /**
     * "Brutal" way to save a reference to the commands
     */
    var messageCommand1: GuildMessageCommand? = null
    var messageCommand2: GuildMessageCommand? = null
    var userCommand: GuildUserCommand? = null
    var slashCommand: GuildChatInputCommand? = null

    /**
     * COMMAND CREATION
     * Delete existing commands, create new commands and save a reference.
     * WARNING : Here we're only saving the id of the command in the last guild.
     * This code sample does not work if the bot works with several guilds.
     */
    guildIds.collect { guildId ->
        // Create commands
        deleteGuildApplicationCommands(client, guildId)
        messageCommand1 = createTestMenuMessageCommand(client, guildId)
        messageCommand2 = createSelectMessageCommand(client, guildId)
        slashCommand = createExampleSlashCommand(client, guildId)
        userCommand = createExampleUserCommand(client, guildId)

        // Set permissions
        setCommandPermissions(client, guildId, userCommand)
    }

    /**
     * COMMAND EXECUTION
     * Receive interactions and respond accordingly
     *
     * WARNING : The implementation below does not work if the bot works with different guilds.
     * (We're considering only command ids that don't seem to depend on the guild).
     */

    // Message commands
    client.on<GuildMessageCommandInteractionCreateEvent> {
        when (interaction.invokedCommandId) {
            messageCommand1!!.id -> {
                selectMenuExample()
            }
            messageCommand2!!.id -> {
                messageSelectExample()
            }
        }
    }

    // User commands
    client.on<GuildUserCommandInteractionCreateEvent> {
        if (interaction.invokedCommandId == userCommand!!.id) {
            userDisplayExample()
        }
    }

    // Chat input commands
    client.on<GuildChatInputCommandInteractionCreateEvent> {
        if (interaction.command.rootId == slashCommand!!.id) {
            // Get argument value
            val displayMessage: Boolean = interaction.command.booleans["display"]!!
            if (displayMessage) {
                componentsAndFollowupExample()
            } else {
                interaction.respondEphemeral { content = "Will not display then..." }
            }
        }
    }

    // Button interactions
    client.on<GuildButtonInteractionCreateEvent> {
        if (interaction.component?.customId == "followUpTrigger") {
            followupExample()
        }
    }

    // Select menu interactions
    client.on<SelectMenuInteractionCreateEvent> {
        if (interaction.component?.customId == "mySelectMenu") {
            selectMenuInteractionExample()
        }
    }

    client.login()
}

private suspend fun SelectMenuInteractionCreateEvent.selectMenuInteractionExample() {
    val selectedOptions = interaction.values
    interaction.acknowledgePublicUpdateMessage {
        content = "These are the options you chose: $selectedOptions"
    }
}

private suspend fun GuildButtonInteractionCreateEvent.followupExample() {
    interaction.acknowledgePublicUpdateMessage { // edits the message
        content = "FollowUp"
        actionRow {
            linkButton(url = "https://twitch.tv/the_happy_hob") { label = "BBB" }
        } // with this you could chain followup messages
    }
}

private suspend fun GuildChatInputCommandInteractionCreateEvent.componentsAndFollowupExample() {
    interaction.respondEphemeral {
        content = "Google link" // !!! It seems is necessary in a message
        actionRow {
            linkButton(url = "https://google.com") {
                label = "AAA"
            }
            interactionButton(style = ButtonStyle.Primary, customId = "followUpTrigger") {
                label = "Press this to send followup"
            }
        }
    }
}

private suspend fun GuildUserCommandInteractionCreateEvent.userDisplayExample() {
    val selectedUser = interaction.getTarget()
    interaction.respondEphemeral { content = "This is the user you selected: ${selectedUser.username}." }
}

private suspend fun GuildMessageCommandInteractionCreateEvent.messageSelectExample() {
    val selectedMessage = interaction.getTarget().content
    interaction.respondEphemeral { content = "This is the message you selected: $selectedMessage." }
}

private suspend fun GuildMessageCommandInteractionCreateEvent.selectMenuExample() {
    interaction.respondPublic {
        content = "Select options below."
        actionRow {
            selectMenu("mySelectMenu") {
                allowedValues = 2..3 // to choose a min/max amount of options to choose
                option(label = "Cats", value = "cats") { description = "Sneaky animals." }
                option(label = "Dogs", value = "dogs") { description = "Nice animals." }
                option(label = "Mice", value = "mice") { description = "Endangered animals." }
                option(label = "Crocodiles", value = "crocodiles") { description = "Dangerous animals." }
                option(label = "Humans", value = "humans") {
                    emoji = DiscordPartialEmoji(Snowflake(625891303795982337))
                }
            }
        }
    }
}

@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
@OptIn(FlowPreview::class)
private suspend fun setCommandPermissions(
    client: Kord,
    guildId: Snowflake,
    userCommand: GuildUserCommand?
) {
    // Edit permissions for commands
    val authorizedRoleName = "Printer"
    val authorizedRolesIds = client.guilds.flatMapConcat { guild ->
        val map = guild.roles.mapNotNull { role -> if (role.name == authorizedRoleName) role.id else null }
        map
    }
    client.editApplicationCommandPermissions(guildId = guildId, commandId = userCommand!!.id) {
        authorizedRolesIds.collect { role(id = it) } // allow all users with the specified role
    }
}

private suspend fun createExampleUserCommand(client: Kord, guildId: Snowflake): GuildUserCommand {
    return client.createGuildUserCommand(guildId = guildId, name = "Print user's name") {
        defaultPermission = false
    }
}

private suspend fun createExampleSlashCommand(client: Kord, guildId: Snowflake): GuildChatInputCommand {
    return client.createGuildChatInputCommand(
        guildId = guildId,
        name = "display",
        description = "Displays an example message."
    ) {
        // group() lets you define a subcommand group
        // subCommand() lets you define a subcommand
        boolean("display", "Display message?") { required = true }
    }
}

private suspend fun createSelectMessageCommand(
    client: Kord,
    guildId: Snowflake
) = client.createGuildMessageCommand(guildId = guildId, name = "Select message")

private suspend fun createTestMenuMessageCommand(
    client: Kord,
    guildId: Snowflake
) = client.createGuildMessageCommand(guildId = guildId, name = "Test menu")

suspend fun deleteGuildApplicationCommands(
    client: Kord,
    guildId: Snowflake
) {
    val guildApplicationCommandIds = client.getGuildApplicationCommands(guildId).map { it.id }
    guildApplicationCommandIds.collect { commandId ->
        client.getGuildApplicationCommand(guildId, commandId).delete()
    }
}
