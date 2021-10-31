package bot.features.guides.model

import com.google.gson.annotations.SerializedName
import kotlinx.datetime.Instant

data class Guide(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("link") val link: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("author") val author: String,
    @SerializedName("timestamp") val timestamp: Instant,
)
