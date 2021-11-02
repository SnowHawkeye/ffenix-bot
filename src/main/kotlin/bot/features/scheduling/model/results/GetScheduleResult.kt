package bot.features.scheduling.model.results

import bot.features.scheduling.model.DefaultRaidSchedule
import bot.features.scheduling.model.ScheduleUIModel

sealed class GetScheduleResult {
    object IncorrectNumberOfRaids : GetScheduleResult()
    object IncorrectDate : GetScheduleResult()
    object DateIsInThePast : GetScheduleResult()
    object NothingToDisplay : GetScheduleResult()
    data class DefaultSchedule(val defaultRaidSchedule: DefaultRaidSchedule) : GetScheduleResult()
    data class UpcomingSchedule(val uiModel: ScheduleUIModel) : GetScheduleResult()
}
