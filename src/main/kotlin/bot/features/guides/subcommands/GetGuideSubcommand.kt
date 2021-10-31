package bot.features.guides.subcommands

import bot.features.core.catchCastExceptions
import bot.features.core.ephemeralResponse
import bot.features.core.typealiases.GuildId
import bot.features.guides.data.*
import bot.features.guides.model.GuideCategory
import bot.features.guides.model.GuidesEngine
import bot.features.guides.model.results.GuideCategoriesResult
import bot.features.guides.model.results.GuideCategoryResult
import bot.features.guides.model.results.GuideResult
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.cache.data.OptionData
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.rest.builder.message.create.actionRow

object GetGuideSubcommand {

    suspend fun getGuide(
        engine: GuidesEngine,
        option: OptionData,
        guildId: GuildId,
        interaction: InteractionCreateEvent
    ) {
        return interaction.getGuide(engine, option, guildId)
    }

    suspend fun guideSelectMenuResponse(
        engine: GuidesEngine,
        interaction: SelectMenuInteractionCreateEvent
    ) {
        return interaction.guideSelectMenuResponse(engine)
    }

    private suspend fun InteractionCreateEvent.getGuide(
        engine: GuidesEngine,
        option: OptionData,
        guildId: GuildId
    ) {
        var categoryName = ""
        var guideTitle: String? = null

        option.values.value?.forEach { commandArgument ->
            catchCastExceptions {
                when (commandArgument.name) {
                    categoryArgumentName -> categoryName = commandArgument.value.toString()
                    guideArgumentName -> guideTitle = commandArgument.value.toString()
                }
            }
        }

        if (guideTitle == null) {
            displayGuidesInCategory(engine, categoryName, guildId)
        } else {
            displaySingleGuide(engine, categoryName, guideTitle, guildId)
        }
    }

    private suspend fun InteractionCreateEvent.displayGuidesInCategory(
        engine: GuidesEngine,
        categoryName: String,
        guildId: GuildId
    ) {
        when (val attemptGetCategory = engine.getGuideCategory(categoryName, guildId)) {
            GuideCategoryResult.NoSuchCategory -> ephemeralResponse(noSuchCategoryErrorMessage)
            is GuideCategoryResult.Success -> {
                val guideTitles = attemptGetCategory.category.guides.map { it.title }
                makeSelectMenu(categoryName, guideTitles)
            }
        }
    }

    private suspend fun InteractionCreateEvent.makeSelectMenu(
        categoryName: String,
        guideTitles: List<String>
    ) {
        if (guideTitles.isEmpty()) ephemeralResponse(categoryEmptyErrorMessage)
        else {
            interaction.respondEphemeral {
                content = guideSelectMenuMessage(categoryName)
                actionRow {
                    selectMenu(categoryName + guideSelectMenuCustomId) {
                        guideTitles.forEach {
                            option(label = it, value = it)
                        }
                    }
                }
            }
        }


    }

    private suspend fun InteractionCreateEvent.displaySingleGuide(
        engine: GuidesEngine,
        categoryName: String,
        guideTitle: String?,
        guildId: GuildId
    ) {
        when (val attemptGetGuide = engine.getGuide(categoryName, guideTitle!!, guildId)) {
            GuideResult.NoSuchCategory -> ephemeralResponse(noSuchCategoryErrorMessage)
            GuideResult.NoSuchGuide -> ephemeralResponse(noSuchGuideErrorMessage)
            is GuideResult.Success -> GuidesCommon.displayGuide(
                attemptGetGuide.guide,
                attemptGetGuide.category,
                this
            )
        }
    }

    private suspend fun SelectMenuInteractionCreateEvent.guideSelectMenuResponse(engine: GuidesEngine) {
        interaction.data.guildId.value?.let { guildId ->
            when (val attemptGetCategories = engine.getGuideCategories(guildId)) {
                GuideCategoriesResult.Empty ->
                    interaction.respondEphemeral { content = genericFailureMessage }
                is GuideCategoriesResult.Success -> {
                    displayMatchingGuide(engine, attemptGetCategories.guideCategories, guildId)
                }
            }
        }
    }

    private suspend fun SelectMenuInteractionCreateEvent.displayMatchingGuide(
        engine: GuidesEngine,
        guideCategories: List<GuideCategory>,
        guildId: Snowflake
    ) {
        guideCategories.find { interaction.component?.customId == it.name + guideSelectMenuCustomId }
            ?.let {
                if (interaction.values.isNotEmpty()) {
                    val selectedGuideTitle = interaction.values[0]
                    when (val attemptGetGuide = engine.getGuide(it.name, selectedGuideTitle, guildId)) {
                        GuideResult.NoSuchCategory -> interaction.respondEphemeral {
                            content = noSuchCategoryErrorMessage
                        }
                        GuideResult.NoSuchGuide -> interaction.respondEphemeral { content = noSuchGuideErrorMessage }
                        is GuideResult.Success -> {
                            GuidesCommon.displayGuide(
                                guide = attemptGetGuide.guide,
                                guideCategory = attemptGetGuide.category,
                                this
                            )
                        }
                    }
                }
            }
    }
}