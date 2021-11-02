package bot.features.scheduling.model.results

import bot.features.scheduling.model.DefaultRaidSchedule

sealed class AddDefaultRaidResult {
    data class Success(val defaultRaidSchedule: DefaultRaidSchedule) : AddDefaultRaidResult()
    object Failure : AddDefaultRaidResult()
    object IncorrectTimezoneId : AddDefaultRaidResult()
    object IncorrectTimeFormat : AddDefaultRaidResult()
    object RaidAlreadyExists : AddDefaultRaidResult()
}
