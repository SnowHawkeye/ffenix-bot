package bot.remote.service.model

import com.google.gson.annotations.SerializedName

data class DownloadDataRequest(
    @SerializedName("path") val path: String
)
