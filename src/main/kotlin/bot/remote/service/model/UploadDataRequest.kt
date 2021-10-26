package bot.remote.service.model

import bot.remote.service.JsonAllNotNull
import com.google.gson.annotations.SerializedName

@JsonAllNotNull
data class UploadDataRequest(
    @SerializedName("path") val path: String,
    @Transient val uploadType: UploadType
) {
    @SerializedName("mode")
    val mode: String = when (uploadType) {
        UploadType.CREATE -> "add"
        UploadType.UPDATE -> "overwrite"
    }

    @SerializedName("autorename")
    val autorename: Boolean = false

    @SerializedName("mute")
    val mute: Boolean = false

    @SerializedName("strict_conflict")
    val strictConflict: Boolean = false

}

enum class UploadType { CREATE, UPDATE }
