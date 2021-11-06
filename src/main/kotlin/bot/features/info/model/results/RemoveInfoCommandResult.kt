package bot.features.info.model.results

import bot.features.info.model.InfoCommand

sealed class RemoveInfoCommandResult {
    object Failure : RemoveInfoCommandResult()
    object CommandDoesNotExist : RemoveInfoCommandResult()
    data class Success(val command: InfoCommand) : RemoveInfoCommandResult()
}
