package bot.features.scheduling.data

import bot.features.core.data.DataCheckResult
import bot.features.core.data.FeatureDataManager
import bot.features.core.typealiases.GuildId
import bot.features.scheduling.SchedulingFeature
import bot.features.scheduling.model.DefaultRaidSchedule
import bot.features.scheduling.model.Schedule
import utils.logging.Log

class SchedulingRepository {

    suspend fun getSchedule(guildId: GuildId): Schedule {
        val result = FeatureDataManager.checkForExistingGuildData<SchedulingDataStructure>(
            feature = SchedulingFeature,
            guildId = guildId
        )

        val defaultRaidSchedule = DefaultRaidSchedule(listOf(), defaultTimezone)
        val schedule = Schedule(defaultRaidSchedule, listOf(), listOf(), listOf())

        return when (result) {
            is DataCheckResult.ExistingData -> {
                if (result.data is SchedulingDataStructure) result.data.schedule
                else {
                    Log.error("Invalid data type found for guides feature in guild repository: $guildId.")
                    schedule
                }
            }
            DataCheckResult.NoData -> schedule
        }
    }

    suspend fun updateSchedule(
        updatedSchedule: Schedule,
        guildId: GuildId,
    ): UploadScheduleResult {
        return try {
            FeatureDataManager.updateGuildData(
                data = SchedulingDataStructure(schedule = updatedSchedule),
                feature = SchedulingFeature,
                guildId = guildId
            )
            UploadScheduleResult.Success
        } catch (e: Exception) {
            Log.error("Caught exception while updating schedule: $e")
            UploadScheduleResult.Failure
        }
    }


    sealed class UploadScheduleResult {
        object Failure : UploadScheduleResult()
        object Success : UploadScheduleResult()
    }
}