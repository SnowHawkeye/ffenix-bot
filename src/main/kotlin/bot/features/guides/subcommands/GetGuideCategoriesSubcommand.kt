package bot.features.guides.subcommands

import bot.features.core.ephemeralResponse
import bot.features.core.typealiases.GuildId
import bot.features.guides.data.guideCategoriesDisplayMessage
import bot.features.guides.data.noCategoriesFoundErrorMessage
import bot.features.guides.model.GuideCategory
import bot.features.guides.model.GuidesEngine
import bot.features.guides.model.results.GuideCategoriesResult
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent

object GetGuideCategoriesSubcommand {

    suspend fun getGuideCategories(
        engine: GuidesEngine,
        guildId: GuildId,
        interaction: GuildChatInputCommandInteractionCreateEvent
    ) {
        return interaction.getGuideCategories(engine, guildId)
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.getGuideCategories(
        engine: GuidesEngine,
        guildId: GuildId,
    ) {
        when (val attemptGetGuideCategories = engine.getGuideCategories(guildId)) {
            GuideCategoriesResult.Empty -> ephemeralResponse(noCategoriesFoundErrorMessage)
            is GuideCategoriesResult.Success -> interaction.respondPublic {
                content = guideCategoriesDisplayMessage + attemptGetGuideCategories.guideCategories.toMessage()
            }
        }

    }

    private fun List<GuideCategory>.toMessage(): String {
        var message = ""
        forEach {
            // ▷ $categoryName ($numberOfGuides guide(s))
            message += "▷ ${it.name} (${it.guides.size} guide${if (it.guides.size > 1) "s" else ""})\n"
        }
        return message
    }

}
