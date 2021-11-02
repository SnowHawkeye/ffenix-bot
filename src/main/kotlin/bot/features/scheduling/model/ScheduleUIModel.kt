package bot.features.scheduling.model

import bot.features.core.typealiases.UserId
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

data class ScheduleUIModel(
    val timezoneId: String,
    val upcomingEvents: List<UpcomingEvent>
)

sealed class UpcomingEvent(val referenceTimestamp: Instant) {
    data class DefaultRaidEvent(val timestamp: Instant, val comment: String?, val cancellation: UpcomingCancellation) :
        UpcomingEvent(timestamp)

    data class ExceptionalRaidEvent(val timestamp: Instant, val comment: String?) : UpcomingEvent(timestamp)
    data class AbsenceEvent(val date: LocalDate, val userId: UserId, val comment: String?) :
        UpcomingEvent(date.atStartOfDayIn(TimeZone.UTC))

    data class UpcomingCancellation(val isCancelled: Boolean, val comment: String?)
}

