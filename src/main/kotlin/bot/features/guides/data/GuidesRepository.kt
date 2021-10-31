package bot.features.guides.data

import bot.features.core.data.DataCheckResult
import bot.features.core.data.FeatureDataManager
import bot.features.core.typealiases.GuildId
import bot.features.guides.GuidesFeature
import bot.features.guides.model.GuideCategory
import utils.logging.Log

class GuidesRepository {

    suspend fun getGuideCategories(guildId: GuildId): List<GuideCategory> {
        val result = FeatureDataManager.checkForExistingGuildData<GuidesDataStructure>(
            feature = GuidesFeature,
            guildId = guildId
        )
        return when (result) {
            is DataCheckResult.ExistingData -> {
                if (result.data is GuidesDataStructure) result.data.guideCategories
                else {
                    Log.error("Invalid data type found for guides feature in guild repository: $guildId.")
                    listOf()
                }
            }
            DataCheckResult.NoData -> listOf()
        }
    }

    suspend fun updateGuideCategories(
        updatedGuideCategories: List<GuideCategory>,
        guildId: GuildId
    ): UploadGuidesResult {
        return try {
            FeatureDataManager.updateGuildData(
                data = GuidesDataStructure(guideCategories = updatedGuideCategories),
                feature = GuidesFeature,
                guildId = guildId
            )
            UploadGuidesResult.Success
        } catch (e: Exception) {
            Log.error("Caught exception while updating guides: $e")
            UploadGuidesResult.Failure
        }
    }

    sealed class UploadGuidesResult {
        object Failure : UploadGuidesResult()
        object Success : UploadGuidesResult()
    }

}