package bot.features.poll.model

import kotlinx.datetime.Instant

data class Poll(
    val question: String,
    val maxAnswers: Int = Int.MAX_VALUE,
    val author: String,
    val timestamp: Instant,
    val thumbnailUrl: String,
    val options: List<PollOption>
)

data class PollOption(
    val emojiUnicode: String,
    val option: String
)
