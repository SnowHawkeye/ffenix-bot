package bot.remote.service.model

import com.google.gson.annotations.SerializedName

data class CreateFolderRequest(
    @SerializedName("path") val path: String
) {
    @SerializedName("autorename")
    val autorename = false
}