package bot.features.scheduling.model.results

sealed class AddExceptionalRaidResult {
    object Success : AddExceptionalRaidResult()
    object Failure : AddExceptionalRaidResult()
    object RaidAlreadyExists : AddExceptionalRaidResult()
    object DateIsInThePast : AddExceptionalRaidResult()
    object IncorrectDate : AddExceptionalRaidResult()
    object IncorrectTimezoneId : AddExceptionalRaidResult()
    object IncorrectTimeFormat : AddExceptionalRaidResult()
}
