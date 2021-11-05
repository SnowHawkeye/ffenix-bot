package bot.features.bonk.data

import bot.features.bonk.BonkFeature
import bot.features.bonk.model.BonkStatistics
import bot.features.core.data.DataCheckResult
import bot.features.core.data.FeatureDataManager
import bot.features.core.typealiases.GuildId
import utils.logging.Log

class BonkRepository {

    suspend fun getBonkStatistics(guildId: GuildId): BonkStatistics {
        val result = FeatureDataManager.checkForExistingGuildData<BonkDataStructure>(
            feature = BonkFeature,
            guildId = guildId
        )

        return when (result) {
            is DataCheckResult.ExistingData -> {
                if (result.data is BonkDataStructure) result.data.bonkStatistics
                else {
                    Log.error("Invalid data type found for bonk feature in guild repository: $guildId.")
                    BonkStatistics(listOf())
                }
            }
            DataCheckResult.NoData -> BonkStatistics(listOf())
        }
    }

    suspend fun updateBonkStatistics(
        updatedBonkStatistics: BonkStatistics,
        guildId: GuildId
    ): UploadBonkStatisticsResult {
        return try {
            FeatureDataManager.updateGuildData(
                data = BonkDataStructure(updatedBonkStatistics),
                feature = BonkFeature,
                guildId = guildId
            )
            UploadBonkStatisticsResult.Success
        } catch (e: Exception) {
            Log.error("Caught exception while updating bonk statistics: $e")
            UploadBonkStatisticsResult.Failure
        }
    }

    sealed class UploadBonkStatisticsResult {
        object Failure : UploadBonkStatisticsResult()
        object Success : UploadBonkStatisticsResult()
    }


}