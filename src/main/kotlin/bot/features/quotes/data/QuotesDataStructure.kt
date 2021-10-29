package bot.features.quotes.data

import bot.features.quotes.model.Quote
import com.google.gson.annotations.SerializedName

data class QuotesDataStructure(
    @SerializedName("quotes") val quotes: List<Quote>
)
