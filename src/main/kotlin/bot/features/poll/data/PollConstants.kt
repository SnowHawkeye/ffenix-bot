package bot.features.poll.data

const val pollFeatureName = "poll"
const val maxPollOptionsNumber = 25
const val minPollOptionsNumber = 2

// COMMANDS

const val pollCommandName = "poll"
const val pollCommandDescription = "Create a simple emoji-based poll"

const val pollQuestionArgumentName = "question"
const val pollQuestionArgumentDescription = "The subject of this poll"

const val pollOptionsArgumentName = "options"
const val pollOptionsArgumentDescription =
    "The options for the poll (max $maxPollOptionsNumber). Options must be separated by a slash"

const val pollMaxAnswersArgumentName = "maxanswers"
const val pollMaxAnswersArgumentDescription = "The maximum number of answers a user can give"

const val pollThumbnailUrlArgumentName = "thumbnail"
const val pollThumbnailUrlArgumentDescription = "The URL of a thumbnail for this poll"

// MESSAGES

const val genericErrorMessage = "Sorry, an error has occurred..."
const val pollInvalidMaxAnswersErrorMessage =
    "Sorry, your poll is invalid... The number of max answers must be 0 or higher."
const val pollNotEnoughOptionsErrorMessage = "Please specify at least two options for your poll."
const val pollTooManyOptionsErrorMessage = "Please specify less than $maxPollOptionsNumber options for your poll."

const val pollDisplayMessage = "A new poll was started!"
const val pollSuccessfullyCreatedMessage = "Your poll was successfully created!"

// EMBED
const val pollDefaultThumbnailUrl =
    "https://cdn.discordapp.com/attachments/904320277842915348/904706418295308328/icon-256x256.png"

const val pollEmbedTitle = "Poll"
fun pollEmbedDescription(maxAnswers: Int): String {
    return if (maxAnswers == Int.MAX_VALUE) "You can pick as many options as you want."
    else "You can pick $maxAnswers option${if (maxAnswers > 1) "s" else ""}."
}


// REACTIONS

val reactionEmojisUnicode = listOf(
    "\uD83D\uDE91",
    "\uD83D\uDC2C",
    "\uD83D\uDC51",
    "\uD83D\uDCD6",
    "\uD83D\uDCD9",
    "\uD83D\uDC25",
    "\uD83C\uDFD3",
    "\uD83D\uDC36",
    "\uD83D\uDC8E",
    "\uD83C\uDF34",
    "\uD83C\uDF84",
    "\uD83C\uDF55",
    "\uD83C\uDF33",
    "\uD83D\uDE82",
    "\uD83C\uDF51",
    "\uD83D\uDD25",
    "\uD83D\uDD11",
    "\uD83D\uDC07",
    "\uD83C\uDF4E",
    "\uD83D\uDC33",
    "\uD83C\uDF81",
    "\uD83C\uDF6D",
    "\uD83D\uDC80",
    "\uD83C\uDF54",
    "\uD83D\uDC14",
    "\uD83C\uDF77",
    "\uD83C\uDF08",
    "\uD83C\uDFA8",
    "\uD83C\uDF6A",
    "\uD83C\uDF42",
)