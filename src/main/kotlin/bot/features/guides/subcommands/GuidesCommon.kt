package bot.features.guides.subcommands

import bot.features.guides.data.guideDisplayMessage
import bot.features.guides.model.Guide
import bot.features.guides.model.GuideCategory
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.message.create.embed

object GuidesCommon {

    fun String.formatDescription(): String = this.replace("\\n", "\n")

    suspend fun displayGuide(
        guide: Guide,
        guideCategory: GuideCategory,
        interaction: InteractionCreateEvent
    ) {
        return interaction.displayGuide(guide, guideCategory)
    }

    private suspend fun InteractionCreateEvent.displayGuide(
        guide: Guide,
        guideCategory: GuideCategory
    ) {
        interaction.respondPublic {
            content = guideDisplayMessage(guide.title)
            embed {
                title = guide.title
                description = guide.description
                url = guide.link
                timestamp = guide.timestamp
                image = guide.imageUrl
                color = guideCategory.color
                author { name = guideCategory.name }
                footer { text = guide.author }
                if (guideCategory.thumbnailUrl != null) thumbnail { url = guideCategory.thumbnailUrl }
            }
        }
    }
}


