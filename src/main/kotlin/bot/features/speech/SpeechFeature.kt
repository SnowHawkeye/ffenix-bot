package bot.features.speech

import bot.features.Feature
import bot.features.core.*
import bot.features.core.data.FeatureDataContract
import bot.features.core.data.RequiresData
import bot.features.speech.data.*
import bot.features.speech.model.SpeechEngine
import bot.features.speech.model.results.SetUwuModeResult
import dev.kord.core.Kord
import dev.kord.core.behavior.edit
import dev.kord.core.entity.application.GuildChatInputCommand
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.boolean

object SpeechFeature : Feature() {
    override val name: String = speechFeatureName
    private val engine = SpeechEngine.instance()
    private lateinit var uwuSpeechCommands: List<GuildChatInputCommand>

    override val featureDataContract: FeatureDataContract = RequiresData.guild(
        createNewData = { engine.makeInitialDataStructure() },
        updateExistingData = { data -> noChangeUpdate(data) }
    )

    override suspend fun Kord.addFeatureGlobalCommands() {}

    override suspend fun Kord.addFeatureGuildCommands() {
        uwuSpeechCommands = addChatInputCommandForEveryGuild(
            name = uwuSpeechCommandName,
            description = uwuSpeechCommandDescription
        ) {
            boolean(uwuSpeechActivateArgumentName, uwuSpeechActivateArgumentDescription) { required = true }
        }
    }


    override suspend fun Kord.addFeatureResponses() {
        on<GuildChatInputCommandInteractionCreateEvent> {
            setUwuSpeechPattern()
        }


        on<MessageCreateEvent> {
            editMessageToMatchPattern()
        }
    }

    private suspend fun MessageCreateEvent.editMessageToMatchPattern() {
        if (engine.isUwuModeActivated(message.getGuild().id)) {
            if (message.author == null)
                message.edit {
                    content = message.content.replace('r', 'w')
                }
        }
    }

    private suspend fun GuildChatInputCommandInteractionCreateEvent.setUwuSpeechPattern() {
        addChatInputCommandResponse(uwuSpeechCommands) { option ->
            if (option.value.value?.name == uwuSpeechActivateArgumentName) {
                option.value.value?.let {
                    var activate = false
                    catchCastExceptions {
                        if (it.name == uwuSpeechActivateArgumentName)
                            activate = it.value as Boolean
                    }

                    when (engine.setUwuMode(activate, interaction.guildId)) {
                        SetUwuModeResult.Failure -> ephemeralResponse(genericErrorMessage)
                        SetUwuModeResult.Success -> {
                            if (activate) publicResponse(updateSpeechPatternActivateSuccessMessage)
                            else ephemeralResponse(updateSpeechPatternDeactivateSuccessMessage)
                        }
                    }
                }
            }
        }
    }
}