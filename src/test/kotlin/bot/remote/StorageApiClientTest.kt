package bot.remote

import bot.remote.client.StorageApiClient
import bot.remote.service.JsonAllNotNull
import bot.remote.service.model.ApiResponse.*
import bot.remote.service.model.CreateFolderRequest
import bot.remote.service.model.DownloadDataRequest
import bot.remote.service.model.UploadDataRequest
import bot.remote.service.model.UploadType
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.runBlocking
import utils.logging.Log

/**
 * This is not a proper unit test class, but can be used to test api calls.
 */
internal class StorageApiClientTest

fun main() = runBlocking {
    downloadFileApiCallTest()
}

suspend fun createFolderApiCallTest() {
    val apiService = StorageApiClient.service()
    when (val response = apiService.createFolder(CreateFolderRequest(path = "/dev/test/testApiCall"))) {
        is Error -> Log.error(response.exception)
        is Failure -> Log.error(response.code)
        is Success -> Log.info(response)
    }
}

@JsonAllNotNull
data class KeyValue(
    @SerializedName("key") val key: String,
)

suspend fun downloadFileApiCallTest() {
    val apiService = StorageApiClient.service()
    val request = DownloadDataRequest(path = "/dev/test/testApiCall/testDownload.json")

    when (val response = apiService.downloadData<KeyValue>(request)) {
        is Error -> Log.error(response.exception)
        is Failure -> Log.error(response.code)
        is Success.WithData<*> -> {
            if (response.data is KeyValue) Log.success(response.data)
            else Log.error("Incorrect data type")
        }
    }
}

suspend fun createFileApiCallTest() {
    val apiService = StorageApiClient.service()
    val request = UploadDataRequest(
        path = "/dev/test/testApiCall/testUpload.json",
        uploadType = UploadType.CREATE
    )

    val data = KeyValue(key = "value")

    when (val response = apiService.uploadData(data, request)) {
        is Error -> Log.error(response.exception)
        is Failure -> Log.error(response.code)
        is Success -> Log.success("Upload successful!")
    }
}




