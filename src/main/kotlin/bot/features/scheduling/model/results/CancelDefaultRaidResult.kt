package bot.features.scheduling.model.results

sealed class CancelDefaultRaidResult {
    object Success : CancelDefaultRaidResult()
    object Failure : CancelDefaultRaidResult()
    object NoRaidPlanned : CancelDefaultRaidResult()
    object RaidAlreadyCancelled : CancelDefaultRaidResult()
    object DateIsInThePast : CancelDefaultRaidResult()
    object IncorrectDate : CancelDefaultRaidResult()
    object IncorrectTimezoneId : CancelDefaultRaidResult()
    object IncorrectTimeFormat : CancelDefaultRaidResult()
}
