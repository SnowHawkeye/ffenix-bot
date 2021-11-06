package bot.features.info.model

import com.google.gson.annotations.SerializedName

data class InfoCommand(
    @SerializedName("commandName") val commandName: String,
    @SerializedName("commandText") val commandText: String
)