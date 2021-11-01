package bot.features.poll.model

import bot.features.poll.data.maxPollOptionsNumber
import bot.features.poll.data.pollDefaultThumbnailUrl
import bot.features.poll.model.results.PollCreationResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class PollEngineTest {

    @Test
    fun `Should return error when poll is created with an invalid number of max answers`() = runBlockingTest {
        // GIVEN
        val engine = PollEngine.instance()

        val question = "?"
        val options = "a/b"
        val author = "me"
        val timestamp = Instant.fromEpochMilliseconds(924904800000)
        val maxAnswers = 0
        val thumbnailUrl = null

        val expected = PollCreationResult.InvalidMaxAnswers

        // WHEN
        val result = engine.makePoll(
            question = question,
            options = options,
            author = author,
            timestamp = timestamp,
            maxAnswers = maxAnswers,
            thumbnailUrl = thumbnailUrl
        )

        // THEN
        assertEquals(expected, result)
    }


    @Test
    fun `Should return error when poll is created without enough options`() = runBlockingTest {
        // GIVEN
        val engine = PollEngine.instance()

        val question = "?"
        val options = "aaa///"
        val author = "me"
        val timestamp = Instant.fromEpochMilliseconds(924904800000)
        val maxAnswers = null
        val thumbnailUrl = null

        val expected = PollCreationResult.NotEnoughOptions

        // WHEN
        val result = engine.makePoll(
            question = question,
            options = options,
            author = author,
            timestamp = timestamp,
            maxAnswers = maxAnswers,
            thumbnailUrl = thumbnailUrl
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return error when poll is created with too many options`() = runBlockingTest {
        // GIVEN
        val engine = PollEngine.instance()

        val question = "?"
        val options = "a/".repeat(maxPollOptionsNumber + 1)
        val author = "me"
        val timestamp = Instant.fromEpochMilliseconds(924904800000)
        val maxAnswers = null
        val thumbnailUrl = null

        val expected = PollCreationResult.TooManyOptions

        // WHEN
        val result = engine.makePoll(
            question = question,
            options = options,
            author = author,
            timestamp = timestamp,
            maxAnswers = maxAnswers,
            thumbnailUrl = thumbnailUrl
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return poll with default thumbnail url and number of max answers`() = runBlockingTest {
        // GIVEN
        val rng = Random(1)
        val engine = PollEngine(rng)

        val question = "?"
        val options = "a/b"
        val author = "me"
        val timestamp = Instant.fromEpochMilliseconds(924904800000)
        val maxAnswers = null
        val thumbnailUrl = null

        val expected = PollCreationResult.PollCreated(
            Poll(
                question = question,
                author = author,
                thumbnailUrl = pollDefaultThumbnailUrl,
                maxAnswers = Int.MAX_VALUE,
                timestamp = timestamp,
                options = listOf(
                    PollOption("\uD83D\uDD25", "a"),
                    PollOption("\uD83C\uDFD3", "b"),
                )
            )
        )

        // WHEN
        val result = engine.makePoll(
            question = question,
            options = options,
            author = author,
            timestamp = timestamp,
            maxAnswers = maxAnswers,
            thumbnailUrl = thumbnailUrl
        )

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return poll with custom thumbnail url and number of max answers`() = runBlockingTest {
        // GIVEN
        val rng = Random(1)
        val engine = PollEngine(rng)

        val question = "?"
        val options = "a/b"
        val author = "me"
        val timestamp = Instant.fromEpochMilliseconds(924904800000)
        val maxAnswers = 3
        val thumbnailUrl = "thumbnailUrl"

        val expected = PollCreationResult.PollCreated(
            Poll(
                question = question,
                author = author,
                thumbnailUrl = thumbnailUrl,
                maxAnswers = maxAnswers,
                timestamp = timestamp,
                options = listOf(
                    PollOption("\uD83D\uDD25", "a"),
                    PollOption("\uD83C\uDFD3", "b"),
                )
            )
        )

        // WHEN
        val result = engine.makePoll(
            question = question,
            options = options,
            author = author,
            timestamp = timestamp,
            maxAnswers = maxAnswers,
            thumbnailUrl = thumbnailUrl
        )

        // THEN
        assertEquals(expected, result)
    }
}