package bot.features.info.model

import bot.features.core.typealiases.GuildId
import bot.features.info.data.InfoDataStructure
import bot.features.info.data.InfoRepository
import bot.features.info.model.results.AddInfoCommandResult
import bot.features.info.model.results.EditInfoCommandResult
import bot.features.info.model.results.RemoveInfoCommandResult

class InfoEngine(val repository: InfoRepository) {

    fun makeInitialDataStructure(): InfoDataStructure {
        return InfoDataStructure(listOf())
    }

    suspend fun getInfoCommands(
        forGuildId: GuildId,
    ): List<InfoCommand> {
        return repository.getInfoCommands(forGuildId)
    }

    suspend fun addInfoCommand(
        commandName: String,
        commandText: String,
        forGuildId: GuildId,
    ): AddInfoCommandResult {
        val existingCommands = repository.getInfoCommands(forGuildId)
        if (existingCommands.any { it.commandName == commandName }) return AddInfoCommandResult.CommandAlreadyExists

        val addedCommand = InfoCommand(commandName, commandText)
        val updatedCommands = existingCommands.toMutableList().apply { add(addedCommand) }

        return when (repository.updateInfoCommands(updatedCommands, forGuildId)) {
            InfoRepository.UploadInfoCommandsResult.Failure -> AddInfoCommandResult.Failure
            InfoRepository.UploadInfoCommandsResult.Success -> AddInfoCommandResult.Success(addedCommand)
        }
    }

    suspend fun editInfoCommand(
        commandName: String,
        newCommandText: String,
        forGuildId: GuildId,
    ): EditInfoCommandResult {
        val existingCommands = repository.getInfoCommands(forGuildId)
        val commandToEdit = existingCommands.find { it.commandName == commandName }
            ?: return EditInfoCommandResult.CommandDoesNotExist

        val editedCommand = InfoCommand(commandName, newCommandText)
        val updatedCommands = existingCommands.toMutableList().apply {
            remove(commandToEdit)
            add(editedCommand)
        }

        return when (repository.updateInfoCommands(updatedCommands, forGuildId)) {
            InfoRepository.UploadInfoCommandsResult.Failure -> EditInfoCommandResult.Failure
            InfoRepository.UploadInfoCommandsResult.Success -> EditInfoCommandResult.Success(
                commandToEdit,
                editedCommand
            )
        }
    }

    suspend fun removeInfoCommand(
        commandName: String,
        forGuildId: GuildId,
    ): RemoveInfoCommandResult {
        val existingCommands = repository.getInfoCommands(forGuildId)
        val commandToRemove = existingCommands.find { it.commandName == commandName }
            ?: return RemoveInfoCommandResult.CommandDoesNotExist

        val updatedCommands = existingCommands.toMutableList().apply {
            remove(commandToRemove)
        }

        return when (repository.updateInfoCommands(updatedCommands, forGuildId)) {
            InfoRepository.UploadInfoCommandsResult.Failure -> RemoveInfoCommandResult.Failure
            InfoRepository.UploadInfoCommandsResult.Success -> RemoveInfoCommandResult.Success(commandToRemove)
        }
    }

    companion object {
        fun instance() = InfoEngine(InfoRepository())
    }
}