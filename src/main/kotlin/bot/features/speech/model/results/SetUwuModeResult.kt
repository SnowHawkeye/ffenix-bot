package bot.features.speech.model.results

sealed class SetUwuModeResult {
    object Success : SetUwuModeResult()
    object Failure : SetUwuModeResult()
}
