package bot.features.scheduling.model.results

sealed class CancelExceptionalRaidResult {
    object Success : CancelExceptionalRaidResult()
    object Failure : CancelExceptionalRaidResult()
    object NothingToCancel : CancelExceptionalRaidResult()
    object IncorrectDate : CancelExceptionalRaidResult()
    object IncorrectTimezoneId : CancelExceptionalRaidResult()
    object IncorrectTimeFormat : CancelExceptionalRaidResult()
}
