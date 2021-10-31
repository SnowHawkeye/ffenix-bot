package bot.features.guides.model

import com.google.gson.annotations.SerializedName
import dev.kord.common.Color

data class GuideCategory(
    @SerializedName("name") val name: String,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("color") val color: Color? = null,
    @SerializedName("guides") val guides: List<Guide>,
)