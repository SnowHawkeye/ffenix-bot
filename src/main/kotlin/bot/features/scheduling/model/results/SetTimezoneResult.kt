package bot.features.scheduling.model.results

sealed class SetTimezoneResult {
    object Success : SetTimezoneResult()
    object Failure : SetTimezoneResult()
    object IncorrectTimezoneId : SetTimezoneResult()
}
