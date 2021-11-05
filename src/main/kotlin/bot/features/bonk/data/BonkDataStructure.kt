package bot.features.bonk.data

import bot.features.bonk.model.BonkStatistics
import com.google.gson.annotations.SerializedName

data class BonkDataStructure(@SerializedName("bonkStatistics") val bonkStatistics: BonkStatistics)