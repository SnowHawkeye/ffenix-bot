package bot.features.info.data

import bot.features.core.data.DataCheckResult
import bot.features.core.data.FeatureDataManager
import bot.features.core.typealiases.GuildId
import bot.features.info.InfoFeature
import bot.features.info.model.InfoCommand
import utils.logging.Log

class InfoRepository {

    suspend fun getInfoCommands(guildId: GuildId): List<InfoCommand> {
        val result = FeatureDataManager.checkForExistingGuildData<InfoDataStructure>(
            feature = InfoFeature,
            guildId = guildId
        )

        return when (result) {
            is DataCheckResult.ExistingData -> {
                if (result.data is InfoDataStructure) result.data.commands
                else {
                    Log.error("Invalid data type found for info feature in guild repository: $guildId.")
                    listOf()
                }
            }
            DataCheckResult.NoData -> listOf()
        }
    }

    suspend fun updateInfoCommands(
        updatedInfoCommands: List<InfoCommand>,
        guildId: GuildId
    ): UploadInfoCommandsResult {
        return try {
            FeatureDataManager.updateGuildData(
                data = InfoDataStructure(updatedInfoCommands),
                feature = InfoFeature,
                guildId = guildId
            )
            UploadInfoCommandsResult.Success
        } catch (e: Exception) {
            Log.error("Caught exception while updating info commands: $e")
            UploadInfoCommandsResult.Failure
        }
    }

    sealed class UploadInfoCommandsResult {
        object Failure : UploadInfoCommandsResult()
        object Success : UploadInfoCommandsResult()
    }
}

