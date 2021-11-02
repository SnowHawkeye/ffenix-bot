package bot.features.scheduling.data

import bot.features.scheduling.model.Schedule
import com.google.gson.annotations.SerializedName

data class SchedulingDataStructure(
    @SerializedName("schedule") val schedule: Schedule
)