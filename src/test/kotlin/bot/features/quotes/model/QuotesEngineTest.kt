package bot.features.quotes.model

import bot.features.quotes.data.*
import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import utils.logging.Log
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.random.Random

@ExperimentalCoroutinesApi
internal class QuotesEngineTest {

    @Test
    fun `Should return default data structure`() {
        // GIVEN
        val engine = QuotesEngine.instance()
        val quote = Quote(
            quote = initialQuoteText,
            number = initialQuoteNumber,
            author = initialQuoteAuthor,
            date = initialQuoteDate
        )

        val expected = QuotesDataStructure(listOf(quote))

        // WHEN
        val result = engine.makeInitialDataStructure()

        // THEN
        assertEquals(result, expected)
        Log.info(result)
    }

    @Mock
    private val mockRepository: QuotesRepository = Mockito.mock(QuotesRepository::class.java)

    @Mock
    private val dateFormatter: SimpleDateFormat = Mockito.mock(SimpleDateFormat::class.java)

    @Test
    fun `Should return random quote with proper format`() = runBlockingTest {
        // GIVEN
        val random = Random(1)
        val engine = QuotesEngine(mockRepository, random, dateFormatter)
        val guildId = Snowflake(1)

        val dateInMillis = 924904800000 // 24 apr 1999
        val date = Date.from(Instant.ofEpochMilli(dateInMillis))
        val quote1 = Quote(quote = "My quote.", number = 0, author = "Hadrien", date = date)
        val quote2 = Quote(quote = "My quote 2.", number = 1, author = "Hadrien", date = date)
        val quotes = listOf(quote1, quote2)

        val expected = "`Quote #0:` \"*My quote.*\" - **Hadrien** (date)"

        Mockito.`when`(dateFormatter.format(date)).thenReturn("date")
        Mockito.`when`(mockRepository.getExistingQuotes(guildId)).thenReturn(quotes)

        // WHEN
        val result = engine.getRandomQuote(guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return placeholder quote`() = runBlockingTest {
        // GIVEN
        val engine = QuotesEngine(mockRepository, dateFormatter = dateFormatter)
        val date = Date.from(Instant.ofEpochMilli(924904800000))
        val guildId = Snowflake(1)
        val expected = "`Quote #-666:` \"*Sadly, no quote was found for this server...*\" - **FF-Enix bot** (date)"
        Mockito.`when`(dateFormatter.format(date)).thenReturn("date")
        Mockito.`when`(mockRepository.getExistingQuotes(guildId)).thenReturn(listOf())

        // WHEN
        val result = engine.getRandomQuote(guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success when quote was successfully added`() = runBlockingTest {
        // GIVEN
        val engine = QuotesEngine(mockRepository, dateFormatter = dateFormatter)
        val guildId = Snowflake(1)

        val dateInMillis = 924904800000 // 24 apr 1999
        val date = Date.from(Instant.ofEpochMilli(dateInMillis))

        val quote1 = Quote(quote = "My quote.", number = 0, author = "Hadrien", date = date)
        val quote2 = Quote(quote = "My quote 2.", number = 3, author = "Hadrien", date = date)
        val quotes = listOf(quote1, quote2)

        val quoteAuthor = "A"
        val quoteText = "B"
        val expectedNumber = 4
        val expectedQuote = Quote(quoteText, expectedNumber, quoteAuthor, date)

        val expectedUpdatedQuotes = listOf(quote1, quote2, expectedQuote)
        val expectedMessage = "`Quote #4:` \"*B*\" - **A** (date)"
        val expected = QuotesEngine.QuoteResult.Success(expectedMessage)

        Mockito.`when`(dateFormatter.format(date)).thenReturn("date")
        Mockito.`when`(mockRepository.getExistingQuotes(guildId)).thenReturn(quotes)
        Mockito.`when`(mockRepository.updateQuotes(expectedUpdatedQuotes, guildId))
            .thenReturn(QuotesRepository.EditQuoteUploadResult.Success)

        // WHEN
        val result = engine.addQuote(quoteText, quoteAuthor, date, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when quote was failed to be added`() = runBlockingTest {
        // GIVEN
        val engine = QuotesEngine(mockRepository, dateFormatter = dateFormatter)
        val guildId = Snowflake(1)

        val dateInMillis = 924904800000 // 24 apr 1999
        val date = Date.from(Instant.ofEpochMilli(dateInMillis))

        val quoteAuthor = "A"
        val quoteText = "B"
        val expectedNumber = 0
        val expectedQuote = Quote(quoteText, expectedNumber, quoteAuthor, date)
        val expected = QuotesEngine.QuoteResult.Failure

        Mockito.`when`(dateFormatter.format(date)).thenReturn("date")
        Mockito.`when`(mockRepository.getExistingQuotes(guildId)).thenReturn(listOf())
        Mockito.`when`(mockRepository.updateQuotes(listOf(expectedQuote), guildId))
            .thenReturn(QuotesRepository.EditQuoteUploadResult.Failure)

        // WHEN
        val result = engine.addQuote(quoteText, quoteAuthor, date, guildId)

        // THEN
        assertEquals(expected, result)
    }


    @Test
    fun `Should return quote with designated number`() = runBlockingTest {
        // GIVEN
        val engine = QuotesEngine(mockRepository, dateFormatter = dateFormatter)
        val guildId = Snowflake(1)

        val dateInMillis = 924904800000 // 24 apr 1999
        val date = Date.from(Instant.ofEpochMilli(dateInMillis))

        val targetQuoteNumber = 3
        val quote1 = Quote(quote = "B", number = targetQuoteNumber, author = "A", date = date)
        val quote2 = Quote(quote = "My quote 2.", number = 0, author = "Hadrien", date = date)
        val quotes = listOf(quote1, quote2)

        val expectedMessage = "`Quote #3:` \"*B*\" - **A** (date)"
        val expected = QuotesEngine.QuoteResult.Success(expectedMessage)

        Mockito.`when`(dateFormatter.format(date)).thenReturn("date")
        Mockito.`when`(mockRepository.getExistingQuotes(guildId)).thenReturn(quotes)

        // WHEN
        val result = engine.getQuote(targetQuoteNumber, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when quote with designated number does not exist`() = runBlockingTest {
        // GIVEN
        val engine = QuotesEngine(mockRepository)
        val guildId = Snowflake(1)

        val targetQuoteNumber = 3
        val quotes = listOf<Quote>()

        val expected = QuotesEngine.QuoteResult.Failure
        Mockito.`when`(mockRepository.getExistingQuotes(guildId)).thenReturn(quotes)

        // WHEN
        val result = engine.getQuote(targetQuoteNumber, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should remove quote and return success`() = runBlockingTest {
        // GIVEN
        val engine = QuotesEngine(mockRepository, dateFormatter = dateFormatter)
        val guildId = Snowflake(1)

        val dateInMillis = 924904800000 // 24 apr 1999
        val date = Date.from(Instant.ofEpochMilli(dateInMillis))

        val quote1 = Quote(quote = "A", number = 0, author = "B", date = date)
        val quote2 = Quote(quote = "My quote 2.", number = 3, author = "Hadrien", date = date)
        val quotes = listOf(quote1, quote2)

        val targetNumber = 0
        val expectedUpdatedQuotes = listOf(quote2)
        val expectedMessage = "`Quote #0:` \"*A*\" - **B** (date)"
        val expected = QuotesEngine.QuoteResult.Success(expectedMessage)

        Mockito.`when`(dateFormatter.format(date)).thenReturn("date")
        Mockito.`when`(mockRepository.getExistingQuotes(guildId)).thenReturn(quotes)
        Mockito.`when`(mockRepository.updateQuotes(expectedUpdatedQuotes, guildId))
            .thenReturn(QuotesRepository.EditQuoteUploadResult.Success)

        // WHEN
        val result = engine.removeQuote(targetNumber, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when the quote to remove does not exist`() = runBlockingTest {
        // GIVEN
        val engine = QuotesEngine(mockRepository, dateFormatter = dateFormatter)
        val guildId = Snowflake(1)

        val dateInMillis = 924904800000 // 24 apr 1999
        val date = Date.from(Instant.ofEpochMilli(dateInMillis))

        val quote1 = Quote(quote = "A", number = 1, author = "B", date = date)
        val quote2 = Quote(quote = "My quote 2.", number = 3, author = "Hadrien", date = date)
        val quotes = listOf(quote1, quote2)

        val targetNumber = 0
        val expected = QuotesEngine.QuoteResult.Failure

        Mockito.`when`(dateFormatter.format(date)).thenReturn("date")
        Mockito.`when`(mockRepository.getExistingQuotes(guildId)).thenReturn(quotes)

        // WHEN
        val result = engine.removeQuote(targetNumber, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when the quote list failed to be updated`() = runBlockingTest {
        // GIVEN
        val engine = QuotesEngine(mockRepository, dateFormatter = dateFormatter)
        val guildId = Snowflake(1)

        val dateInMillis = 924904800000 // 24 apr 1999
        val date = Date.from(Instant.ofEpochMilli(dateInMillis))

        val quote1 = Quote(quote = "A", number = 0, author = "B", date = date)
        val quote2 = Quote(quote = "My quote 2.", number = 3, author = "Hadrien", date = date)
        val quotes = listOf(quote1, quote2)

        val targetNumber = 0
        val expectedUpdatedQuotes = listOf(quote2)
        val expected = QuotesEngine.QuoteResult.Failure

        Mockito.`when`(dateFormatter.format(date)).thenReturn("date")
        Mockito.`when`(mockRepository.getExistingQuotes(guildId)).thenReturn(quotes)
        Mockito.`when`(mockRepository.updateQuotes(expectedUpdatedQuotes, guildId))
            .thenReturn(QuotesRepository.EditQuoteUploadResult.Failure)

        // WHEN
        val result = engine.removeQuote(targetNumber, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return success when quote author was successfully edited`() = runBlockingTest {
        // GIVEN
        val engine = QuotesEngine(mockRepository, dateFormatter = dateFormatter)
        val guildId = Snowflake(1)

        val dateInMillis = 924904800000 // 24 apr 1999
        val date = Date.from(Instant.ofEpochMilli(dateInMillis))

        val quote1 = Quote(quote = "B", number = 0, author = "A", date = date)
        val quote2 = Quote(quote = "My quote 2.", number = 3, author = "Hadrien", date = date)
        val quotes = listOf(quote1, quote2)

        val newAuthor = "AAA"
        val targetNumber = 0
        val expectedUpdatedQuote = Quote(quote = "B", number = 0, author = "AAA", date = date)

        val expectedUpdatedQuotes = listOf(quote2, expectedUpdatedQuote)
        val expectedMessage = "`Quote #0:` \"*B*\" - **AAA** (date)"
        val expected = QuotesEngine.QuoteResult.Success(expectedMessage)

        Mockito.`when`(dateFormatter.format(date)).thenReturn("date")
        Mockito.`when`(mockRepository.getExistingQuotes(guildId)).thenReturn(quotes)
        Mockito.`when`(mockRepository.updateQuotes(expectedUpdatedQuotes, guildId))
            .thenReturn(QuotesRepository.EditQuoteUploadResult.Success)

        // WHEN
        val result = engine.editQuoteAuthor(targetNumber, newAuthor, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when quote could not be found when trying to edit its author`() = runBlockingTest {
        // GIVEN
        val engine = QuotesEngine(mockRepository, dateFormatter = dateFormatter)
        val guildId = Snowflake(1)

        val dateInMillis = 924904800000 // 24 apr 1999
        val date = Date.from(Instant.ofEpochMilli(dateInMillis))

        val quote1 = Quote(quote = "B", number = 1, author = "A", date = date)
        val quote2 = Quote(quote = "My quote 2.", number = 3, author = "Hadrien", date = date)
        val quotes = listOf(quote1, quote2)

        val newAuthor = "AAA"
        val targetNumber = 0
        val expectedUpdatedQuote = Quote(quote = "B", number = 0, author = "AAA", date = date)

        val expectedUpdatedQuotes = listOf(quote2, expectedUpdatedQuote)
        val expected = QuotesEngine.QuoteResult.Failure

        Mockito.`when`(dateFormatter.format(date)).thenReturn("date")
        Mockito.`when`(mockRepository.getExistingQuotes(guildId)).thenReturn(quotes)
        Mockito.`when`(mockRepository.updateQuotes(expectedUpdatedQuotes, guildId))
            .thenReturn(QuotesRepository.EditQuoteUploadResult.Success)

        // WHEN
        val result = engine.editQuoteAuthor(targetNumber, newAuthor, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when quote could not be uploaded when trying to edit its author`() = runBlockingTest {
        // GIVEN
        val engine = QuotesEngine(mockRepository, dateFormatter = dateFormatter)
        val guildId = Snowflake(1)

        val dateInMillis = 924904800000 // 24 apr 1999
        val date = Date.from(Instant.ofEpochMilli(dateInMillis))

        val quote1 = Quote(quote = "B", number = 1, author = "A", date = date)
        val quote2 = Quote(quote = "My quote 2.", number = 3, author = "Hadrien", date = date)
        val quotes = listOf(quote1, quote2)

        val newAuthor = "AAA"
        val targetNumber = 0
        val expectedUpdatedQuote = Quote(quote = "B", number = 0, author = "AAA", date = date)

        val expectedUpdatedQuotes = listOf(quote2, expectedUpdatedQuote)
        val expected = QuotesEngine.QuoteResult.Failure

        Mockito.`when`(dateFormatter.format(date)).thenReturn("date")
        Mockito.`when`(mockRepository.getExistingQuotes(guildId)).thenReturn(quotes)
        Mockito.`when`(mockRepository.updateQuotes(expectedUpdatedQuotes, guildId))
            .thenReturn(QuotesRepository.EditQuoteUploadResult.Failure)

        // WHEN
        val result = engine.editQuoteAuthor(targetNumber, newAuthor, guildId)

        // THEN
        assertEquals(expected, result)
    }


    @Test
    fun `Should return success when quote text was successfully edited`() = runBlockingTest {
        // GIVEN
        val engine = QuotesEngine(mockRepository, dateFormatter = dateFormatter)
        val guildId = Snowflake(1)

        val dateInMillis = 924904800000 // 24 apr 1999
        val date = Date.from(Instant.ofEpochMilli(dateInMillis))

        val quote1 = Quote(quote = "B", number = 0, author = "A", date = date)
        val quote2 = Quote(quote = "My quote 2.", number = 3, author = "Hadrien", date = date)
        val quotes = listOf(quote1, quote2)

        val newText = "BBB"
        val targetNumber = 0
        val expectedUpdatedQuote = Quote(quote = "BBB", number = 0, author = "A", date = date)

        val expectedUpdatedQuotes = listOf(quote2, expectedUpdatedQuote)
        val expectedMessage = "`Quote #0:` \"*BBB*\" - **A** (date)"
        val expected = QuotesEngine.QuoteResult.Success(expectedMessage)

        Mockito.`when`(dateFormatter.format(date)).thenReturn("date")
        Mockito.`when`(mockRepository.getExistingQuotes(guildId)).thenReturn(quotes)
        Mockito.`when`(mockRepository.updateQuotes(expectedUpdatedQuotes, guildId))
            .thenReturn(QuotesRepository.EditQuoteUploadResult.Success)

        // WHEN
        val result = engine.editQuoteText(targetNumber, newText, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when quote could not be found when trying to edit its text`() = runBlockingTest {
        // GIVEN
        val engine = QuotesEngine(mockRepository, dateFormatter = dateFormatter)
        val guildId = Snowflake(1)

        val dateInMillis = 924904800000 // 24 apr 1999
        val date = Date.from(Instant.ofEpochMilli(dateInMillis))

        val quote1 = Quote(quote = "B", number = 1, author = "A", date = date)
        val quote2 = Quote(quote = "My quote 2.", number = 3, author = "Hadrien", date = date)
        val quotes = listOf(quote1, quote2)

        val newText = "BBB"
        val targetNumber = 0
        val expectedUpdatedQuote = Quote(quote = "BBB", number = 0, author = "A", date = date)

        val expectedUpdatedQuotes = listOf(quote2, expectedUpdatedQuote)
        val expected = QuotesEngine.QuoteResult.Failure

        Mockito.`when`(dateFormatter.format(date)).thenReturn("date")
        Mockito.`when`(mockRepository.getExistingQuotes(guildId)).thenReturn(quotes)
        Mockito.`when`(mockRepository.updateQuotes(expectedUpdatedQuotes, guildId))
            .thenReturn(QuotesRepository.EditQuoteUploadResult.Success)

        // WHEN
        val result = engine.editQuoteAuthor(targetNumber, newText, guildId)

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `Should return failure when quote could not be uploaded when trying to edit its text`() = runBlockingTest {
        // GIVEN
        val engine = QuotesEngine(mockRepository, dateFormatter = dateFormatter)
        val guildId = Snowflake(1)

        val dateInMillis = 924904800000 // 24 apr 1999
        val date = Date.from(Instant.ofEpochMilli(dateInMillis))

        val quote1 = Quote(quote = "B", number = 1, author = "A", date = date)
        val quote2 = Quote(quote = "My quote 2.", number = 3, author = "Hadrien", date = date)
        val quotes = listOf(quote1, quote2)

        val newText = "BBB"
        val targetNumber = 0
        val expectedUpdatedQuote = Quote(quote = "B", number = 0, author = "AAA", date = date)

        val expectedUpdatedQuotes = listOf(quote2, expectedUpdatedQuote)
        val expected = QuotesEngine.QuoteResult.Failure

        Mockito.`when`(dateFormatter.format(date)).thenReturn("date")
        Mockito.`when`(mockRepository.getExistingQuotes(guildId)).thenReturn(quotes)
        Mockito.`when`(mockRepository.updateQuotes(expectedUpdatedQuotes, guildId))
            .thenReturn(QuotesRepository.EditQuoteUploadResult.Failure)

        // WHEN
        val result = engine.editQuoteAuthor(targetNumber, newText, guildId)

        // THEN
        assertEquals(expected, result)
    }

}