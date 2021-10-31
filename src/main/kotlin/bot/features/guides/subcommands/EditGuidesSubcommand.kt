package bot.features.guides.subcommands

import bot.features.core.catchCastExceptions
import bot.features.core.ephemeralResponse
import bot.features.core.typealiases.GuildId
import bot.features.guides.data.*
import bot.features.guides.model.GuidesEngine
import bot.features.guides.model.results.EditGuideCategoryResult
import bot.features.guides.model.results.EditGuideResult
import bot.features.guides.subcommands.GuidesCommon.formatDescription
import dev.kord.common.entity.SubCommand
import dev.kord.core.cache.data.OptionData
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent

object EditGuidesSubcommand {

    suspend fun editGuides(
        engine: GuidesEngine,
        option: OptionData,
        guildId: GuildId,
        interaction: GuildChatInputCommandInteractionCreateEvent
    ) {
        return interaction.editGuides(engine, option, guildId)
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.editGuides(
        engine: GuidesEngine,
        option: OptionData,
        guildId: GuildId
    ) {
        option.subCommands.value?.forEach { subCommand ->
            when (subCommand.name) {
                editCategoryCommandName -> editCategory(engine, subCommand, guildId)
                editGuideCommandName -> editGuide(engine, subCommand, guildId)
            }
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.editCategory(
        engine: GuidesEngine,
        subCommand: SubCommand,
        guildId: GuildId,
    ) {
        var categoryName = ""
        var newCategoryName: String? = null
        var thumbnailUrl: String? = null
        var colorHex: Int? = null

        subCommand.options.value?.forEach { commandArgument ->
            catchCastExceptions {
                when (commandArgument.name) {
                    categoryNameArgumentName -> categoryName = commandArgument.value.toString()
                    categoryNewNameArgumentName -> newCategoryName = commandArgument.value.toString()
                    categoryThumbnailArgumentName -> thumbnailUrl = commandArgument.value.toString()
                    categoryColorArgumentName -> colorHex = (commandArgument.value as Long).toInt()
                }
            }
        }

        val attemptEditCategory = engine.editCategory(
            categoryName = categoryName,
            forGuildId = guildId,
            newCategoryName = newCategoryName,
            thumbnailUrl = thumbnailUrl,
            colorHex = colorHex
        )

        when (attemptEditCategory) {
            EditGuideCategoryResult.Failure -> ephemeralResponse(genericFailureMessage)
            EditGuideCategoryResult.NoSuchCategory -> ephemeralResponse(noSuchCategoryErrorMessage)
            EditGuideCategoryResult.CategoryAlreadyExists -> ephemeralResponse(categoryAlreadyExistsErrorMessage)
            is EditGuideCategoryResult.Success -> ephemeralResponse(
                categorySuccessfullyEditedMessage(
                    attemptEditCategory.guideCategory.name
                )
            )
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.editGuide(
        engine: GuidesEngine,
        subCommand: SubCommand,
        guildId: GuildId
    ) {
        var categoryName = ""
        var title = ""
        var newTitle: String? = null
        var description: String? = null
        var link: String? = null
        var imageUrl: String? = null

        subCommand.options.value?.forEach { commandArgument ->
            catchCastExceptions {
                when (commandArgument.name) {
                    categoryNameArgumentName -> categoryName = commandArgument.value.toString()
                    guideTitleArgumentName -> title = commandArgument.value.toString()
                    guideNewTitleArgumentName -> newTitle = commandArgument.value.toString()
                    guideDescriptionArgumentName -> description = commandArgument.value.toString().formatDescription()
                    guideLinkArgumentName -> link = commandArgument.value.toString()
                    guideImageArgumentName -> imageUrl = commandArgument.value.toString()
                }
            }
        }

        val attemptEditGuide = engine.editGuide(
            categoryName = categoryName,
            title = title,
            newTitle = newTitle,
            description = description,
            link = link,
            imageUrl = imageUrl,
            forGuildId = guildId
        )

        when (attemptEditGuide) {
            EditGuideResult.Failure -> ephemeralResponse(genericFailureMessage)
            EditGuideResult.NoSuchCategory -> ephemeralResponse(noSuchCategoryErrorMessage)
            EditGuideResult.NoSuchGuide -> ephemeralResponse(noSuchGuideErrorMessage)
            EditGuideResult.GuideAlreadyExists -> ephemeralResponse(guideAlreadyExistsErrorMessage)
            is EditGuideResult.Success -> GuidesCommon.displayGuide(
                attemptEditGuide.guide,
                attemptEditGuide.category,
                this
            )
        }

    }
}