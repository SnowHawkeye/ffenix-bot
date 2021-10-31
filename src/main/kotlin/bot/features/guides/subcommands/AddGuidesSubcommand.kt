package bot.features.guides.subcommands

import bot.features.core.catchCastExceptions
import bot.features.core.ephemeralResponse
import bot.features.core.typealiases.GuildId
import bot.features.guides.data.*
import bot.features.guides.model.GuidesEngine
import bot.features.guides.model.results.AddGuideCategoryResult
import bot.features.guides.model.results.AddGuideResult
import bot.features.guides.subcommands.GuidesCommon.formatDescription
import dev.kord.common.entity.SubCommand
import dev.kord.core.cache.data.OptionData
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import kotlinx.datetime.Clock

object AddGuidesSubcommand {

    suspend fun addGuides(
        engine: GuidesEngine,
        option: OptionData,
        guildId: GuildId,
        interaction: GuildChatInputCommandInteractionCreateEvent
    ) {
        return interaction.addGuides(engine, option, guildId)
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.addGuides(
        engine: GuidesEngine,
        option: OptionData,
        guildId: GuildId
    ) {
        option.subCommands.value?.forEach { subCommand ->
            when (subCommand.name) {
                addCategoryCommandName -> addCategory(engine, subCommand, guildId)
                addGuideCommandName -> addGuide(engine, subCommand, guildId)
            }
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.addCategory(
        engine: GuidesEngine,
        subCommand: SubCommand,
        guildId: GuildId,
    ) {
        var categoryName = ""
        var thumbnailUrl: String? = null
        var colorHex: Int? = null

        subCommand.options.value?.forEach { commandArgument ->
            catchCastExceptions {
                when (commandArgument.name) {
                    categoryNameArgumentName -> categoryName = commandArgument.value.toString()
                    categoryThumbnailArgumentName -> thumbnailUrl = commandArgument.value.toString()
                    categoryColorArgumentName -> colorHex = (commandArgument.value as Long).toInt()
                }
            }
        }

        val attemptAddCategory = engine.addCategory(
            categoryName = categoryName,
            forGuildId = guildId,
            thumbnailUrl = thumbnailUrl,
            colorHex = colorHex
        )

        when (attemptAddCategory) {
            AddGuideCategoryResult.CategoryAlreadyExists -> ephemeralResponse(categoryAlreadyExistsErrorMessage)
            AddGuideCategoryResult.Failure -> ephemeralResponse(genericFailureMessage)
            is AddGuideCategoryResult.Success -> ephemeralResponse(categorySuccessfullyAddedMessage(attemptAddCategory.category.name))
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.addGuide(
        engine: GuidesEngine,
        subCommand: SubCommand,
        guildId: GuildId
    ) {
        var categoryName = ""
        var title = ""
        var description: String? = null
        var link: String? = null
        var imageUrl: String? = null
        val author = interaction.member.displayName
        val timestamp = Clock.System.now()

        subCommand.options.value?.forEach { commandArgument ->
            catchCastExceptions {
                when (commandArgument.name) {
                    categoryNameArgumentName -> categoryName = commandArgument.value.toString()
                    guideTitleArgumentName -> title = commandArgument.value.toString()
                    guideDescriptionArgumentName -> description = commandArgument.value.toString().formatDescription()
                    guideLinkArgumentName -> link = commandArgument.value.toString()
                    guideImageArgumentName -> imageUrl = commandArgument.value.toString()
                }
            }
        }

        val attemptAddGuide = engine.addGuide(
            categoryName = categoryName,
            title = title,
            description = description,
            link = link,
            imageUrl = imageUrl,
            author = author,
            timestamp = timestamp,
            forGuildId = guildId
        )

        when (attemptAddGuide) {
            AddGuideResult.Failure -> ephemeralResponse(genericFailureMessage)
            AddGuideResult.GuideAlreadyExists -> ephemeralResponse(guideAlreadyExistsErrorMessage)
            AddGuideResult.NoSuchCategory -> ephemeralResponse(noSuchCategoryErrorMessage)
            is AddGuideResult.Success -> GuidesCommon.displayGuide(
                attemptAddGuide.guide,
                attemptAddGuide.category,
                this
            )
        }

    }
}