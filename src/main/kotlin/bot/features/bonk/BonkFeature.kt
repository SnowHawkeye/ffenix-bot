package bot.features.bonk

import bot.features.Feature
import bot.features.bonk.data.*
import bot.features.bonk.model.BonkEngine
import bot.features.bonk.model.BonkedUser
import bot.features.bonk.model.results.BonkStandingsResult
import bot.features.bonk.model.results.IncrementBonkResult
import bot.features.core.*
import bot.features.core.data.FeatureDataContract
import bot.features.core.data.RequiresData
import bot.features.core.permissions.FeatureRolesContract
import bot.features.core.permissions.NecessaryRole
import bot.features.core.typealiases.UserId
import dev.kord.core.Kord
import dev.kord.core.behavior.createRole
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.cache.data.OptionData
import dev.kord.core.entity.Guild
import dev.kord.core.entity.application.GuildChatInputCommand
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.subCommand
import dev.kord.rest.builder.interaction.user
import kotlinx.coroutines.flow.toList

object BonkFeature : Feature() {
    override val name: String = bonkFeatureName

    private val engine = BonkEngine.instance()

    private lateinit var bonkCommands: List<GuildChatInputCommand>
    private lateinit var jailCommands: List<GuildChatInputCommand>

    override val featureDataContract: FeatureDataContract = RequiresData.guild(
        createNewData = { engine.makeInitialDataStructure() },
        updateExistingData = { data -> noChangeUpdate(data) }
    )

    private val bonkedRole = NecessaryRole(bonkedRoleName, bonkedRoleColor, hoist = true)
    override val featureRolesContract = FeatureRolesContract.RequiresRoles(setOf(bonkedRole))

    override suspend fun Kord.addFeatureGlobalCommands() {}

    override suspend fun Kord.addFeatureGuildCommands() {
        bonkCommands = addChatInputCommandForEveryGuild(
            name = bonkCommandName,
            description = bonkCommandDescription
        ) {
            user(userToBonkArgumentName, userToBonkArgumentDescription) { required = true }
        }

        jailCommands = addChatInputCommandForEveryGuild(
            name = jailCommandName,
            description = jailCommandDescription,
        ) {
            subCommand(jailStandingsCommandName, jailStandingsCommandDescription)
            subCommand(jailScoreCommandName, jailScoreCommandDescription) {
                user(bonkedUserArgumentName, bonkedUserArgumentDescription) { required = true }
            }
        }
    }


    override suspend fun Kord.addFeatureResponses() {
        on<GuildChatInputCommandInteractionCreateEvent> {
            addChatInputCommandResponse(bonkCommands) { bonkResponses() }
            addChatInputCommandResponse(jailCommands) { jailResponses() }
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.bonkResponses() {
        val options = interaction.data.data.options

        // Get arguments
        var userId: UserId? = null
        options.value?.forEach { option ->
            option.value.value?.let { commandArgument ->
                catchCastExceptions {
                    if (commandArgument.name == userToBonkArgumentName) {
                        userId = commandArgument.value as UserId
                    }
                }
            }
        }

        if (userId == null) {
            ephemeralResponse(genericErrorMessage)
            return
        }

        val displayName = interaction.getGuild().getMember(userId!!).displayName

        // Send message
        when (val bonkUserResult = engine.incrementBonkCount(userId!!, interaction.guildId)) {
            IncrementBonkResult.Failure -> ephemeralResponse(genericErrorMessage)
            is IncrementBonkResult.Success ->
                publicResponse(bonkUserSuccessMessage(displayName, bonkUserResult.bonkedUser.bonkNumber))
        }

        // Attribute role
        val guild = interaction.getGuild()
        val role = guild.roles.toList().find { it.name == bonkedRoleName }
            ?: guild.createRole {
                name = bonkedRoleName
                color = bonkedRoleColor
                hoist = true
            }
        interaction.getGuild().getMember(userId!!).addRole(role.id)
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.jailResponses() {
        val options = interaction.data.data.options
        options.value?.forEach { option ->
            when (option.name) {
                jailStandingsCommandName -> jailStandings()
                jailScoreCommandName -> jailScore(option)
            }
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.jailScore(option: OptionData) {
        var userId: UserId? = null

        option.values.value?.forEach { commandArgument ->
            catchCastExceptions {
                if (commandArgument.name == bonkedUserArgumentName) userId = commandArgument.value as UserId
            }
        }

        if (userId == null) {
            ephemeralResponse(genericErrorMessage)
            return
        }

        val displayName = interaction.getGuild().getMember(userId!!).displayName

        val score = engine.getBonkScore(userId!!, interaction.guildId)
        publicResponse(jailScoreResultMessage(displayName, score))
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.jailStandings() {
        when (val getStandingsResult = engine.getBonkStandings(bonkedStandingsSize, interaction.guildId)) {
            BonkStandingsResult.NothingToDisplay -> ephemeralResponse(jailStandingsErrorMessage)
            is BonkStandingsResult.Success -> interaction.respondPublic {
                content = makeStandings(getStandingsResult.bonkedUsers, interaction.getGuild())
            }
        }
    }

    private suspend fun makeStandings(bonkedUsers: List<BonkedUser>, guild: Guild): String {
        var message = "Here are the current bonk champions!\n\n"

        bonkedUsers.forEachIndexed { index, bonkedUser ->
            val memberDisplayName = guild.getMember(bonkedUser.userId).displayName
            val memberNumberOfBonks = bonkedUser.bonkNumber
            message += if (index == 0) {
                "▷ **$memberDisplayName is the horniest of them all with $memberNumberOfBonks bonk${if (memberNumberOfBonks > 1) "s" else ""}!**\n"
            } else {
                "▷ $memberDisplayName ($memberNumberOfBonks bonk${if (memberNumberOfBonks > 1) "s" else ""})\n"
            }
        }

        return message
    }
}