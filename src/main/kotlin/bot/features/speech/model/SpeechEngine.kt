package bot.features.speech.model

import bot.features.core.typealiases.GuildId
import bot.features.speech.data.SpeechDataStructure
import bot.features.speech.data.SpeechRepository
import bot.features.speech.model.results.SetUwuModeResult

class SpeechEngine(
    private val repository: SpeechRepository,
) {

    fun makeInitialDataStructure(): SpeechDataStructure {
        return SpeechDataStructure(uwuMode = false)
    }

    suspend fun isUwuModeActivated(guildId: GuildId): Boolean {
        return repository.getUwuMode(guildId)
    }

    suspend fun setUwuMode(activate: Boolean, guildId: GuildId): SetUwuModeResult {
        return when (repository.setUwuMode(activate, guildId)) {
            SpeechRepository.UpdateSpeechModeResult.Failure -> SetUwuModeResult.Failure
            SpeechRepository.UpdateSpeechModeResult.Success -> SetUwuModeResult.Success
        }
    }

    companion object {
        fun instance() = SpeechEngine(SpeechRepository())
    }
}