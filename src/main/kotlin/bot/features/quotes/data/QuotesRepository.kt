package bot.features.quotes.data

import bot.features.core.data.DataCheckResult
import bot.features.core.data.FeatureDataManager
import bot.features.core.data.GuildId
import bot.features.quotes.QuotesFeature
import bot.features.quotes.model.Quote
import utils.logging.Log

class QuotesRepository {

    suspend fun getExistingQuotes(guildId: GuildId): List<Quote> {
        val result = FeatureDataManager.checkForExistingGuildData<QuotesDataStructure>(
            feature = QuotesFeature,
            guildId = guildId
        )
        return when (result) {
            is DataCheckResult.ExistingData -> {
                if (result.data is QuotesDataStructure) result.data.quotes
                else {
                    Log.error("Invalid data type found for quotes feature in guild repository: $guildId.")
                    listOf()
                }
            }
            DataCheckResult.NoData -> listOf()
        }
    }

    suspend fun updateQuotes(updatedQuotes: List<Quote>, guildId: GuildId): EditQuoteUploadResult {
        return try {
            FeatureDataManager.updateGuildData(
                data = QuotesDataStructure(quotes = updatedQuotes),
                feature = QuotesFeature,
                guildId = guildId
            )
            EditQuoteUploadResult.Success
        } catch (e: Exception) {
            Log.error("Caught exception while updating quotes: $e")
            EditQuoteUploadResult.Failure
        }

    }

    sealed class EditQuoteUploadResult {
        object Failure : EditQuoteUploadResult()
        object Success : EditQuoteUploadResult()
    }
}
