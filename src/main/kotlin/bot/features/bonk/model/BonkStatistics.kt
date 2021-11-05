package bot.features.bonk.model

import bot.features.core.typealiases.UserId
import com.google.gson.annotations.SerializedName

data class BonkStatistics(
    @SerializedName("bonkedUsers") val bonkedUsers: List<BonkedUser>
)

data class BonkedUser(
    @SerializedName("userId") val userId: UserId,
    @SerializedName("bonkNumber") val bonkNumber: Int,
)