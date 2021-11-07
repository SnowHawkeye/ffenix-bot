package bot.features.help

import bot.features.Feature
import bot.features.core.addChatInputCommandForEveryGuild
import bot.features.core.addChatInputCommandResponse
import bot.features.help.data.*
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.application.GuildChatInputCommand
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.create.actionRow

object HelpFeature : Feature() {

    override val name = helpFeatureName

    private lateinit var helpCommands: List<GuildChatInputCommand>

    override suspend fun Kord.addFeatureGlobalCommands() {}

    override suspend fun Kord.addFeatureGuildCommands() {
        helpCommands = addChatInputCommandForEveryGuild(helpCommandName, helpCommandDescription)
    }


    override suspend fun Kord.addFeatureResponses() {
        on<GuildChatInputCommandInteractionCreateEvent> {
            addChatInputCommandResponse(helpCommands) {
                interaction.respondEphemeral {
                    content = helpMessageContent
                    actionRow {
                        linkButton(url = documentationLink) { label = documentationButtonLabel }
                        linkButton(url = repositoryLink) { label = repositoryLabel }
                    }
                }
            }
        }
    }
}