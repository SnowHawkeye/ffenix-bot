package bot.features.scheduling.model.results

sealed class RemoveDefaultRaidResult {
    object Failure : RemoveDefaultRaidResult()
    object Success : RemoveDefaultRaidResult()
    object RaidDoesNotExist : RemoveDefaultRaidResult()
    object IncorrectTimezoneId : RemoveDefaultRaidResult()
    object IncorrectTimeFormat : RemoveDefaultRaidResult()
}
