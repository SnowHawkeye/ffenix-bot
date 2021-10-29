package bot.remote.service

import bot.remote.service.model.ApiResponse
import bot.remote.service.model.ApiResponse.*
import bot.remote.service.model.CreateFolderRequest
import bot.remote.service.model.DownloadDataRequest
import bot.remote.service.model.UploadDataRequest
import com.google.gson.Gson
import retrofit2.Response

/**
 * Exposes functions that use objects instead of strings.
 * This is necessary because the Dropbox API requires JSON in some of its headers,
 * and retrofit does not allow header conversion.
 */
internal class StorageApiServiceWrapper(
    private val apiService: StorageApiService,
    private val gson: Gson,
) {

    suspend fun createFolder(createFolderRequest: CreateFolderRequest): ApiResponse {
        return getApiResponse(
            call = { apiService.createFolder(createFolderRequest) },
            successResponse = { Success.WithNoData }
        )
    }

    suspend inline fun <reified T> downloadData(downloadDataRequest: DownloadDataRequest): ApiResponse {
        val convertedRequest = gson.toJson(downloadDataRequest)
        return getApiResponse(
            call = { apiService.downloadFile<T>(convertedRequest) },
            successResponse = { response -> Success.WithData<T>(validateDeserialization(parseObject(response.body()))) }
        )
    }

    suspend inline fun <reified T> uploadData(data: T, uploadDataRequest: UploadDataRequest): ApiResponse {
        val convertedRequest = gson.toJson(uploadDataRequest)
        val convertedData = gson.toJson(data)
        return getApiResponse(
            call = { apiService.uploadFile(convertedRequest, convertedData) },
            successResponse = { Success.WithNoData }
        )
    }


    private inline fun <reified T> parseObject(src: Any?): T {
        val clean = removeQuotesAndUnescape(src.toString())
        return gson.fromJson(clean, T::class.java)
    }

    /**
     * Credits to [this post](https://stackoverflow.com/questions/28418662/expected-begin-object-but-was-string-at-line-1-column-1).
     */
    private fun removeQuotesAndUnescape(uncleanJson: String) =
        uncleanJson.replace("^\"|\"$".toRegex(), "")


    private suspend fun <T> getApiResponse(
        call: suspend () -> Response<T>,
        successResponse: (Response<T>) -> Success
    ): ApiResponse {
        val response = call()
        return try {
            if (response.isSuccessful) {
                try {
                    successResponse(response)
                } catch (e: Exception) {
                    Error(e)
                }
            } else Failure(response.code(), response.message())
        } catch (e: Exception) {
            Error(e)
        }
    }
}