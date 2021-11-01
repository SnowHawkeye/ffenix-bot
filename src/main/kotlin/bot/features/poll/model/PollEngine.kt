package bot.features.poll.model

import bot.features.poll.data.maxPollOptionsNumber
import bot.features.poll.data.minPollOptionsNumber
import bot.features.poll.data.pollDefaultThumbnailUrl
import bot.features.poll.data.reactionEmojisUnicode
import bot.features.poll.model.results.PollCreationResult
import dev.kord.core.entity.ReactionEmoji
import kotlinx.datetime.Instant
import kotlin.random.Random

class PollEngine(
    private val rng: Random = Random.Default,
) {

    fun makePoll(
        question: String,
        options: String,
        author: String,
        timestamp: Instant,
        maxAnswers: Int? = null,
        thumbnailUrl: String? = null,
    ): PollCreationResult {
        val stringPollOptions = options.parseOptions()
        if (stringPollOptions.size > maxPollOptionsNumber) return PollCreationResult.TooManyOptions
        if (stringPollOptions.size < minPollOptionsNumber) return PollCreationResult.NotEnoughOptions
        if (maxAnswers != null && maxAnswers <= 0) return PollCreationResult.InvalidMaxAnswers
        val pollMaxAnswers = maxAnswers ?: Int.MAX_VALUE
        val pollThumbnailUrl = thumbnailUrl ?: pollDefaultThumbnailUrl

        val pollOptions = stringPollOptions.map {
            PollOption(emojiUnicode = reactionEmojisUnicode.random(rng), option = it)
        }

        val poll = Poll(
            question = question,
            maxAnswers = pollMaxAnswers,
            author = author,
            timestamp = timestamp,
            thumbnailUrl = pollThumbnailUrl,
            options = pollOptions
        )

        return PollCreationResult.PollCreated(poll)
    }

    private fun String.parseOptions(): List<String> {
        val options = split('/').toMutableList()
        val toRemove = mutableListOf<String>()
        options.forEach { if (it.isEmpty()) toRemove.add(it) }
        options.removeAll(toRemove)
        return options
    }

    companion object {
        fun instance() = PollEngine()
    }

}