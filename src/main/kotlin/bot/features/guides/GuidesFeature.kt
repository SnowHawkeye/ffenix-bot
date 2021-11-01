package bot.features.guides

import bot.features.Feature
import bot.features.core.addChatInputCommandForEveryGuild
import bot.features.core.addChatInputCommandResponse
import bot.features.core.data.FeatureDataContract
import bot.features.core.data.RequiresData
import bot.features.core.noChangeUpdate
import bot.features.core.permissions.FeatureRolesContract
import bot.features.core.permissions.NecessaryRole
import bot.features.core.permissions.PermissionsHelper
import bot.features.guides.data.*
import bot.features.guides.model.GuidesEngine
import bot.features.guides.subcommands.*
import dev.kord.core.Kord
import dev.kord.core.entity.application.GuildChatInputCommand
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.*

object GuidesFeature : Feature() {
    override val name: String = guidesFeatureName

    private val engine = GuidesEngine.instance()

    private lateinit var accessGuidesCommands: List<GuildChatInputCommand>
    private lateinit var editGuidesCommands: List<GuildChatInputCommand>

    override val featureDataContract: FeatureDataContract = RequiresData.guild(
        createNewData = { engine.makeInitialDataStructure() },
        updateExistingData = { data -> noChangeUpdate(data) },
    )

    private val editGuidesNecessaryRole =
        NecessaryRole(roleName = editGuidesNecessaryRoleName, color = editGuidesNecessaryRoleColor)

    override val featureRolesContract = FeatureRolesContract.RequiresRoles(
        necessaryRoles = setOf(editGuidesNecessaryRole)
    )

    override suspend fun Kord.addFeatureGlobalCommands() {}

    override suspend fun Kord.addFeatureGuildCommands() {
        accessGuidesCommands = addChatInputCommandForEveryGuild(
            name = accessGuidesCommandName,
            description = accessGuidesCommandDescription
        ) {
            defaultPermission = true
            getGuideCommand()
            getGuideCategoriesCommand()
        }

        editGuidesCommands = addChatInputCommandForEveryGuild(
            name = modifyGuidesCommandName,
            description = modifyGuidesCommandDescription,
        ) {
            defaultPermission = false
            addGuidesSubcommandGroup()
            editGuidesSubcommandGroup()
            removeGuidesSubcommandGroup()
        }

        defineEditPermissions()
    }

    override suspend fun Kord.addFeatureResponses() {
        on<GuildChatInputCommandInteractionCreateEvent> {
            addChatInputCommandResponse(accessGuidesCommands) {
                val options = interaction.data.data.options
                options.value?.forEach { option ->
                    when (option.name) {
                        getGuideCommandName ->
                            GetGuideSubcommand.getGuide(engine, option, interaction.guildId, this)
                        getGuideCategoriesCommandName ->
                            GetGuideCategoriesSubcommand.getGuideCategories(engine, interaction.guildId, this)
                    }
                }
            }

            addChatInputCommandResponse(editGuidesCommands) {
                val options = interaction.data.data.options
                options.value?.forEach { option ->
                    val guildId = interaction.guildId
                    when (option.name) {
                        addGuidesCommandName -> AddGuidesSubcommand.addGuides(engine, option, guildId, this)
                        editGuidesCommandName -> EditGuidesSubcommand.editGuides(engine, option, guildId, this)
                        removeGuidesCommandName -> RemoveGuidesSubcommand.removeGuides(engine, option, guildId, this)
                    }
                }
            }
        }

        on<SelectMenuInteractionCreateEvent> {
            GetGuideSubcommand.guideSelectMenuResponse(engine, this)
        }
    }

    private suspend fun Kord.defineEditPermissions() {
        editGuidesCommands.forEach { command ->
            getGuild(command.guildId)?.let { guild ->
                PermissionsHelper.authorizeRoleForCommandInGuild(
                    role = editGuidesNecessaryRole,
                    commandId = command.id,
                    guild = guild,
                    featureRolesContract = featureRolesContract,
                    client = this
                )
            }
        }
    }

    private fun ChatInputCreateBuilder.getGuideCommand() {
        subCommand(getGuideCommandName, getGuideCommandDescription) {
            categoryArgument(true)
            guideArgument(false)
        }
    }

    private fun ChatInputCreateBuilder.getGuideCategoriesCommand() {
        subCommand(getGuideCategoriesCommandName, getGuideCategoriesCommandDescription)
    }

    private fun ChatInputCreateBuilder.addGuidesSubcommandGroup() {
        group(addGuidesCommandName, addGuidesCommandDescription) {
            subCommand(addCategoryCommandName, addCategoryCommandDescription) {
                categoryNameArgument(true)
                categoryThumbnailArgument(false)
                categoryColorArgument(false)
            }
            subCommand(addGuideCommandName, addGuideCommandDescription) {
                categoryNameArgument(true)
                guideTitleArgument(true)
                guideDescriptionArgument(false)
                guideLinkArgument(false)
                guideImageArgument(false)
            }
        }
    }

    private fun ChatInputCreateBuilder.editGuidesSubcommandGroup() {
        group(editGuidesCommandName, editGuidesCommandDescription) {
            subCommand(editCategoryCommandName, editCategoryCommandDescription) {
                categoryNameArgument(true)
                categoryNewNameArgument(false)
                categoryThumbnailArgument(false)
                categoryColorArgument(false)
            }
            subCommand(editGuideCommandName, editGuideCommandDescription) {
                categoryNameArgument(true)
                guideTitleArgument(true)
                guideNewTitleArgument(false)
                guideDescriptionArgument(false)
                guideLinkArgument(false)
                guideImageArgument(false)
            }
        }
    }

    private fun ChatInputCreateBuilder.removeGuidesSubcommandGroup() {
        group(removeGuidesCommandName, removeGuidesCommandDescription) {
            subCommand(removeCategoryCommandName, removeCategoryCommandDescription) {
                categoryNameArgument(true)
            }
            subCommand(removeGuideCommandName, removeGuideCommandDescription) {
                categoryNameArgument(true)
                guideTitleArgument(true)
            }
        }
    }

    private fun SubCommandBuilder.categoryArgument(isRequired: Boolean) =
        string(categoryArgumentName, categoryArgumentDescription) { required = isRequired }

    private fun SubCommandBuilder.guideArgument(isRequired: Boolean) =
        string(guideArgumentName, guideArgumentDescription) { required = isRequired }

    private fun SubCommandBuilder.categoryNameArgument(isRequired: Boolean) =
        string(categoryNameArgumentName, categoryNameArgumentDescription) { required = isRequired }

    private fun SubCommandBuilder.categoryNewNameArgument(isRequired: Boolean) =
        string(categoryNewNameArgumentName, categoryNewNameArgumentDescription) { required = isRequired }

    private fun SubCommandBuilder.categoryThumbnailArgument(isRequired: Boolean) =
        string(categoryThumbnailArgumentName, categoryThumbnailArgumentDescription) { required = isRequired }

    private fun SubCommandBuilder.categoryColorArgument(isRequired: Boolean) =
        int(categoryColorArgumentName, categoryColorArgumentDescription) { required = isRequired }

    private fun SubCommandBuilder.guideTitleArgument(isRequired: Boolean) =
        string(guideTitleArgumentName, guideTitleArgumentDescription) { required = isRequired }

    private fun SubCommandBuilder.guideNewTitleArgument(isRequired: Boolean) =
        string(guideNewTitleArgumentName, guideNewTitleArgumentDescription) { required = isRequired }

    private fun SubCommandBuilder.guideDescriptionArgument(isRequired: Boolean) =
        string(guideDescriptionArgumentName, guideDescriptionArgumentDescription) { required = isRequired }

    private fun SubCommandBuilder.guideLinkArgument(isRequired: Boolean) =
        string(guideLinkArgumentName, guideLinkArgumentDescription) { required = isRequired }

    private fun SubCommandBuilder.guideImageArgument(isRequired: Boolean) =
        string(guideImageArgumentName, guideImageArgumentDescription) { required = isRequired }
}