package bot.features.quotes.model

import bot.features.core.data.GuildId
import bot.features.quotes.data.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.random.Random

class QuotesEngine(
    private val repository: QuotesRepository,
    private val rng: Random = Random.Default,
    private val dateFormatter: SimpleDateFormat = SimpleDateFormat("d MMM yyyy", Locale.ENGLISH)
) {

    fun makeInitialDataStructure(): QuotesDataStructure {
        val initialQuote = Quote(
            quote = initialQuoteText,
            number = initialQuoteNumber,
            author = initialQuoteAuthor,
            date = initialQuoteDate
        )
        return QuotesDataStructure(listOf(initialQuote))
    }

    suspend fun getQuote(quoteNumber: Int, forGuildId: GuildId): QuoteResult {
        val quotes = repository.getExistingQuotes(forGuildId)
        return quotes.find { it.number == quoteNumber }?.let { quote ->
            QuoteResult.Success(quote.toText())
        } ?: QuoteResult.Failure

    }

    suspend fun getRandomQuote(forGuildId: GuildId): String {
        val quotes = repository.getExistingQuotes(forGuildId)
        return if (quotes.isEmpty()) {
            placeHolderQuote.toText()
        } else quotes.random(rng).toText()
    }

    suspend fun addQuote(
        quoteText: String,
        quoteAuthor: String,
        quoteDate: Date,
        forGuildId: GuildId
    ): QuoteResult {
        val quotes = repository.getExistingQuotes(guildId = forGuildId).toMutableList()
        val maxNumber = quotes.maxOfOrNull { it.number } ?: -1
        val quote = Quote(quote = quoteText, author = quoteAuthor, date = quoteDate, number = maxNumber + 1)
        quotes.add(quote)
        return when (repository.updateQuotes(quotes, forGuildId)) {
            QuotesRepository.EditQuoteUploadResult.Failure -> QuoteResult.Failure
            QuotesRepository.EditQuoteUploadResult.Success -> QuoteResult.Success(quote.toText())
        }
    }

    suspend fun removeQuote(
        quoteNumber: Int,
        forGuildId: GuildId,
    ): QuoteResult {
        val quotes = repository.getExistingQuotes(guildId = forGuildId).toMutableList()
        val quote = quotes.find { it.number == quoteNumber } ?: return QuoteResult.Failure
        return if (quotes.remove(quote)) {
            when (repository.updateQuotes(quotes, forGuildId)) {
                QuotesRepository.EditQuoteUploadResult.Failure -> QuoteResult.Failure
                QuotesRepository.EditQuoteUploadResult.Success -> QuoteResult.Success(quote.toText())
            }
        } else QuoteResult.Failure
    }

    suspend fun editQuoteText(
        quoteNumber: Int,
        newQuoteText: String,
        forGuildId: GuildId,
    ): QuoteResult {
        val quotes = repository.getExistingQuotes(guildId = forGuildId).toMutableList()
        val quote = quotes.find { it.number == quoteNumber } ?: return QuoteResult.Failure
        val newQuote = quote.copy(quote = newQuoteText)
        quotes.remove(quote)
        quotes.add(newQuote)

        return when (repository.updateQuotes(quotes, forGuildId)) {
            QuotesRepository.EditQuoteUploadResult.Failure -> QuoteResult.Failure
            QuotesRepository.EditQuoteUploadResult.Success -> QuoteResult.Success(newQuote.toText())
        }
    }

    suspend fun editQuoteAuthor(
        quoteNumber: Int,
        newQuoteAuthor: String,
        forGuildId: GuildId,
    ): QuoteResult {
        val quotes = repository.getExistingQuotes(guildId = forGuildId).toMutableList()
        val quote = quotes.find { it.number == quoteNumber } ?: return QuoteResult.Failure
        val newQuote = quote.copy(author = newQuoteAuthor)
        quotes.remove(quote)
        quotes.add(newQuote)

        return when (repository.updateQuotes(quotes, forGuildId)) {
            QuotesRepository.EditQuoteUploadResult.Failure -> QuoteResult.Failure
            QuotesRepository.EditQuoteUploadResult.Success -> QuoteResult.Success(newQuote.toText())
        }
    }

    sealed class QuoteResult {
        object Failure : QuoteResult()
        data class Success(val text: String) : QuoteResult()
    }

    private fun Quote.toText(): String {
        return "`Quote #$number:` \"*${quote}*\" - **$author** (${date.toText()})"
    }

    private fun Date.toText(): String {
        return dateFormatter.format(this)
    }

    private val placeHolderQuote = Quote(
        quote = "Sadly, no quote was found for this server...",
        number = -666,
        author = "FF-Enix bot",
        date = Date.from(Instant.ofEpochMilli(924904800000)),
    )

    companion object {
        fun instance() = QuotesEngine(repository = QuotesRepository())
    }
}
