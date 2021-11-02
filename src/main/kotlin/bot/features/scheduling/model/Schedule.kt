package bot.features.scheduling.model

import bot.features.core.typealiases.UserId
import com.google.gson.annotations.SerializedName
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class Schedule(
    @SerializedName("defaultRaidSchedule") val defaultRaidSchedule: DefaultRaidSchedule,
    @SerializedName("cancelledDefaultRaids") val cancelledDefaultRaids: List<CancelledDefaultRaid>,
    @SerializedName("exceptionalRaids") val exceptionalRaids: List<ExceptionalRaid>,
    @SerializedName("absences") val absences: List<Absence>
)

data class DefaultRaidSchedule(
    @SerializedName("defaultRaids") val defaultRaids: List<DefaultRaid>,
    @SerializedName("defaultTimezoneId") val defaultTimezoneId: String
)

data class DefaultRaid(
    @SerializedName("dayOfWeek") val dayOfWeek: DayOfWeek,
    @SerializedName("hoursUTC") val hoursUTC: Int,
    @SerializedName("minutesUTC") val minutesUTC: Int,
    @SerializedName("comment") val comment: String?,
)

data class ExceptionalRaid(
    @SerializedName("timestamp") val timestamp: Instant,
    @SerializedName("comment") val comment: String?,
)

data class CancelledDefaultRaid(
    @SerializedName("timestamp") val timestamp: Instant,
    @SerializedName("comment") val comment: String?
)

data class Absence(
    @SerializedName("date") val date: LocalDate,
    @SerializedName("userId") val userId: UserId,
    @SerializedName("comment") val comment: String?,
)