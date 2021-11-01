package bot.features.speech.data

import bot.features.core.data.DataCheckResult
import bot.features.core.data.FeatureDataManager
import bot.features.core.typealiases.GuildId
import bot.features.speech.SpeechFeature
import utils.logging.Log

class SpeechRepository {

    suspend fun getUwuMode(guildId: GuildId): Boolean {
        val result = FeatureDataManager.checkForExistingGuildData<SpeechDataStructure>(
            feature = SpeechFeature,
            guildId = guildId
        )

        return when (result) {
            is DataCheckResult.ExistingData -> {
                if (result.data is SpeechDataStructure) result.data.uwuMode
                else {
                    Log.error("Invalid data type found for ${SpeechFeature.name} feature in guild repository: $guildId.")
                    false
                }
            }
            DataCheckResult.NoData -> false
        }
    }

    suspend fun setUwuMode(activate: Boolean, guildId: GuildId): UpdateSpeechModeResult {
        return try {
            FeatureDataManager.updateGuildData(
                data = SpeechDataStructure(uwuMode = activate),
                feature = SpeechFeature,
                guildId = guildId
            )
            UpdateSpeechModeResult.Success
        } catch (e: Exception) {
            Log.error("Caught exception while updating speech mode: $e")
            UpdateSpeechModeResult.Failure
        }
    }

    sealed class UpdateSpeechModeResult {
        object Failure : UpdateSpeechModeResult()
        object Success : UpdateSpeechModeResult()
    }
}


