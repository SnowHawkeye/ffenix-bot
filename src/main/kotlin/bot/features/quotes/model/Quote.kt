package bot.features.quotes.model

import bot.remote.service.JsonAllNotNull
import com.google.gson.annotations.SerializedName
import java.util.*

@JsonAllNotNull
data class Quote(
    @SerializedName("quote") val quote: String,
    @SerializedName("number") val number: Int,
    @SerializedName("author") val author: String,
    @SerializedName("date") val date: Date
)
