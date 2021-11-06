package bot.features.info.data

import bot.features.info.model.InfoCommand
import com.google.gson.annotations.SerializedName

data class InfoDataStructure(
    @SerializedName("commands") val commands: List<InfoCommand>
)