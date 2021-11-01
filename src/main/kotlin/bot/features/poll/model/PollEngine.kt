package bot.features.poll.model

import bot.features.poll.data.maxPollOptionsNumber
import bot.features.poll.data.minPollOptionsNumber
import bot.features.poll.data.pollDefaultThumbnailUrl
import bot.features.poll.data.reactionEmojisUnicode
import bot.features.poll.model.results.PollCreationResult
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


        val emojisToMap = reactionEmojisUnicode.pickElementsAtRandom(stringPollOptions.size, rng)
        val optionsToEmojis = stringPollOptions.zip(emojisToMap)

        val pollOptions = optionsToEmojis.map { PollOption(option = it.first, emojiUnicode = it.second) }

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

    private fun <T> List<T>.pickElementsAtRandom(n: Int, rng: Random): List<T> {
        if (n >= size) return this
        if (n <= 0) return listOf()
        val poolOfElements = this.toMutableList()
        val elementsToReturn = mutableListOf<T>()
        repeat(n) {
            val newElement = poolOfElements.random(rng)
            elementsToReturn.add(newElement)
            poolOfElements.remove(newElement)
        }
        return elementsToReturn
    }

    companion object {
        fun instance() = PollEngine()
    }

}