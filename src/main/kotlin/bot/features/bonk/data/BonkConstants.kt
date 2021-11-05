package bot.features.bonk.data

import dev.kord.common.Color

const val bonkFeatureName = "bonk"

const val bonkedRoleName = "Horny ♡"
val bonkedRoleColor = Color(rgb = 0xA3174F)

const val bonkedStandingsSize = 5

// COMMANDS
const val bonkCommandName = "bonk"
const val bonkCommandDescription = "Bonk the given user"

const val jailCommandName = "hornyjail"
const val jailCommandDescription = "Get information about users who have been bonked"

const val jailStandingsCommandName = "standings"
const val jailStandingsCommandDescription = "Return the top bonked users"

const val jailScoreCommandName = "score"
const val jailScoreCommandDescription = "Return the number of bonks for the given user"

// ARGUMENTS
const val userToBonkArgumentName = "who"
const val userToBonkArgumentDescription = "The user to bonk"

const val bonkedUserArgumentName = "who"
const val bonkedUserArgumentDescription = "The user whose bonk count to get"

// MESSAGES
const val genericErrorMessage = "Sorry, something went wrong..."

fun bonkUserSuccessMessage(displayName: String, numberOfBonks: Int): String {
    return "\uD83D\uDD34 **HORNY USER DETECTED** \uD83D\uDD34\n" +
            "$displayName was bonked! They have been bonked $numberOfBonks " +
            "time${if (numberOfBonks > 1) "s" else ""}."
}

fun jailScoreResultMessage(displayName: String, score: Int): String {
    return "$displayName was bonked $score time${if (score > 1) "s" else ""}!"
}

const val jailStandingsErrorMessage = "It seems no one was bonked yet... " +
        "Use `/bonk` to bonk a user!"