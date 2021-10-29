package bot.remote.service

import bot.remote.*
import bot.remote.service.model.CreateFolderRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface StorageApiService {

    @POST(CREATE_FOLDER_PATH)
    @Headers(JSON_CONTENT_TYPE_HEADER)
    suspend fun createFolder(
        @Body createFolderRequest: CreateFolderRequest
    ): Response<*>

    @POST(DOWNLOAD_FILE_PATH)
    suspend fun <T> downloadFile(
        @Header(ARGUMENT_HEADER_KEY) downloadFileRequest: String
    ): Response<T>

    @POST(UPLOAD_FILE_PATH)
    @Headers(TEXT_CONTENT_TYPE_HEADER)
    suspend fun uploadFile(
        @Header(ARGUMENT_HEADER_KEY) uploadFileRequest: String,
        @Body file: Any
    ): Response<*>
}