package bot.features.info.model.results

import bot.features.info.model.InfoCommand

sealed class EditInfoCommandResult {
    object Failure : EditInfoCommandResult()
    object CommandDoesNotExist : EditInfoCommandResult()
    data class Success(val oldCommand: InfoCommand, val newCommand: InfoCommand) : EditInfoCommandResult()
}
