package bot.features.guides.data

import bot.features.guides.model.GuideCategory
import com.google.gson.annotations.SerializedName

data class GuidesDataStructure(
    @SerializedName("guideCategories") val guideCategories: List<GuideCategory>
)
