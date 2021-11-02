package bot.features.scheduling.model.results

sealed class RevertDefaultRaidCancellationResult {
    object Failure : RevertDefaultRaidCancellationResult()
    object Success : RevertDefaultRaidCancellationResult()
    object NothingToRevert : RevertDefaultRaidCancellationResult()
    object IncorrectDate : RevertDefaultRaidCancellationResult()
    object IncorrectTimezoneId : RevertDefaultRaidCancellationResult()
    object IncorrectTimeFormat : RevertDefaultRaidCancellationResult()
}
