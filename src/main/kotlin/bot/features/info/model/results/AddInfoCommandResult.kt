package bot.features.info.model.results

import bot.features.info.model.InfoCommand

sealed class AddInfoCommandResult {
    object Failure : AddInfoCommandResult()
    object CommandAlreadyExists: AddInfoCommandResult()
    data class Success(val command: InfoCommand) : AddInfoCommandResult()
}
