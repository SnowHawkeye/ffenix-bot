package bot.features.poll.model.results

import bot.features.poll.model.Poll

sealed class PollCreationResult {
    object NotEnoughOptions : PollCreationResult()
    object TooManyOptions : PollCreationResult()
    object InvalidMaxAnswers : PollCreationResult()
    data class PollCreated(val poll: Poll) : PollCreationResult()
}