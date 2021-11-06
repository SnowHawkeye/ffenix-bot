package bot.features.info.data

import dev.kord.common.Color

const val infoFeatureName = "info"

const val editInfoCommandsRoleName = "Herald"
val editInfoCommandsRoleColor = Color(0xE99D82)

const val infoCommandPrefix = '!'

// COMMANDS

const val infoCommandName = "info"
const val infoCommandDescription = "Manage info commands"

const val addInfoCommandName = "add"
const val addInfoCommandDescription = "Add an info command with the given text"

const val editInfoCommandName = "edit"
const val editInfoCommandDescription = "Edit an info command by replacing its text"

const val removeInfoCommandName = "remove"
const val removeInfoCommandDescription = "Remove an existing info command"


// ARGUMENTS

const val commandToAddArgumentName = "commandname"
const val commandToAddArgumentDescription = "The name of the info command to add"

const val commandTextArgumentName = "commandtext"
const val commandTextArgumentDescription = "The text that will be displayed in response to the command"

const val commandToEditArgumentName = "commandname"
const val commandToEditArgumentDescription = "The name of the info command to edit"

const val commandEditedTextArgumentName = "newtext"
const val commandEditedTextArgumentDescription = "The new text to display in response to the command"

const val commandToRemoveArgumentName = "commandname"
const val commandToRemoveArgumentDescription = "The name of the info command to remove"


// MESSAGES

const val genericErrorMessage = "Sorry, something went wrong..."
const val commandAlreadyExistsErrorMessage = "This command already exists! Use `/info edit` to change its text."
const val commandDoesNotExist = "This command does not exist! Use `/info add` to add this command."

fun commandSuccessFullyAddedMessage(commandName: String) =
    "The command was successfully added! Use $infoCommandPrefix$commandName to see the result."

fun commandSuccessFullyEditedMessage(commandName: String) =
    "The command was successfully edited! Use $infoCommandPrefix$commandName to see the result."

fun commandSuccessFullyRemovedMessage(commandName: String) =
    "The $commandName command was successfully removed!"
