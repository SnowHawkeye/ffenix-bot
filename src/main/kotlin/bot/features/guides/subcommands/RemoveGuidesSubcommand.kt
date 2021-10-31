package bot.features.guides.subcommands

import bot.features.core.catchCastExceptions
import bot.features.core.ephemeralResponse
import bot.features.core.typealiases.GuildId
import bot.features.guides.data.*
import bot.features.guides.model.GuidesEngine
import bot.features.guides.model.results.RemoveGuideCategoryResult
import bot.features.guides.model.results.RemoveGuideResult
import dev.kord.common.entity.SubCommand
import dev.kord.core.cache.data.OptionData
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent

object RemoveGuidesSubcommand {

    suspend fun removeGuides(
        engine: GuidesEngine,
        option: OptionData,
        guildId: GuildId,
        interaction: GuildChatInputCommandInteractionCreateEvent
    ) {
        return interaction.removeGuides(engine, option, guildId)
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.removeGuides(
        engine: GuidesEngine,
        option: OptionData,
        guildId: GuildId,
    ) {
        option.subCommands.value?.forEach { subCommand ->
            when (subCommand.name) {
                removeCategoryCommandName -> removeCategory(engine, subCommand, guildId)
                removeGuideCommandName -> removeGuide(engine, subCommand, guildId)
            }
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.removeCategory(
        engine: GuidesEngine,
        subCommand: SubCommand,
        guildId: GuildId,
    ) {
        var categoryName = ""

        subCommand.options.value?.forEach { commandArgument ->
            catchCastExceptions {
                when (commandArgument.name) {
                    categoryNameArgumentName -> categoryName = commandArgument.value.toString()
                }
            }
        }

        val attemptAddCategory = engine.removeCategory(
            categoryName = categoryName,
            forGuildId = guildId,
        )

        when (attemptAddCategory) {
            RemoveGuideCategoryResult.CategoryNotEmpty -> ephemeralResponse(categoryNotEmptyErrorMessage)
            RemoveGuideCategoryResult.Failure -> ephemeralResponse(genericFailureMessage)
            RemoveGuideCategoryResult.NoSuchCategory -> ephemeralResponse(noSuchCategoryErrorMessage)
            is RemoveGuideCategoryResult.Success -> ephemeralResponse(
                categorySuccessfullyRemovedMessage(
                    attemptAddCategory.category.name
                )
            )
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.removeGuide(
        engine: GuidesEngine,
        subCommand: SubCommand,
        guildId: GuildId,
    ) {
        var categoryName = ""
        var guideTitle = ""

        subCommand.options.value?.forEach { commandArgument ->
            catchCastExceptions {
                when (commandArgument.name) {
                    categoryNameArgumentName -> categoryName = commandArgument.value.toString()
                    guideTitleArgumentName -> guideTitle = commandArgument.value.toString()
                }
            }
        }

        val attemptAddCategory = engine.removeGuide(
            categoryName = categoryName,
            title = guideTitle,
            forGuildId = guildId,
        )

        when (attemptAddCategory) {
            RemoveGuideResult.Failure -> ephemeralResponse(genericFailureMessage)
            RemoveGuideResult.NoSuchCategory -> ephemeralResponse(noSuchCategoryErrorMessage)
            RemoveGuideResult.NoSuchGuide -> ephemeralResponse(noSuchGuideErrorMessage)
            is RemoveGuideResult.Success -> ephemeralResponse(guideSuccessfullyRemovedMessage(guideTitle, categoryName))
        }
    }


}
