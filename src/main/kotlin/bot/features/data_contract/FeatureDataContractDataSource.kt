package bot.features.data_contract

import bot.features.Feature
import bot.remote.CONFLICT_CODE
import bot.remote.service.StorageApiServiceWrapper
import bot.remote.service.model.ApiResponse
import bot.remote.service.model.ApiResponse.Error
import bot.remote.service.model.ApiResponse.Failure
import bot.remote.service.model.ApiResponse.Success
import bot.remote.service.model.CreateFolderRequest
import bot.remote.service.model.DownloadDataRequest
import bot.remote.service.model.UploadDataRequest
import bot.remote.service.model.UploadType
import utils.logging.Log

internal class FeatureDataContractDataSource(val service: StorageApiServiceWrapper) {

    suspend inline fun <reified GlobalData> checkForExistingGlobalData(feature: Feature): DataCheckResult {
        val request = DownloadDataRequest(StoragePathHelper.globalStorageDataPath(feature))
        val response = service.downloadData<GlobalData>(request)
        return handleDownloadResult(response)
    }

    suspend inline fun <reified GlobalData> createGlobalData(feature: Feature, data: GlobalData) {
        attemptCreateGlobalDataFolder(feature)
        uploadGlobalData(
            feature = feature,
            data = data,
            uploadType = UploadType.CREATE,
            successLog = "Global data successfully created for ${feature.name}."
        )
    }

    suspend inline fun <reified GlobalData> updateGlobalData(feature: Feature, data: GlobalData) {
        attemptCreateGlobalDataFolder(feature)
        uploadGlobalData(
            feature = feature,
            data = data,
            uploadType = UploadType.UPDATE,
            successLog = "Global data successfully updated for ${feature.name}."
        )
    }

    suspend inline fun <reified GuildData> checkForExistingGuildData(
        feature: Feature,
        guildId: GuildId
    ): DataCheckResult {
        val request = DownloadDataRequest(StoragePathHelper.guildStorageDataPath(feature, guildId))
        val response = service.downloadData<GuildData>(request)
        return handleDownloadResult(response)
    }

    suspend inline fun <reified GuildData> createGuildData(feature: Feature, data: GuildData, guildId: GuildId) {
        attemptCreateGuildDataFolder(feature, guildId)
        uploadGuildData(
            feature = feature,
            data = data,
            guildId = guildId,
            uploadType = UploadType.CREATE,
            successLog = "Guild data successfully created for ${feature.name} and guild of id $guildId."
        )
    }

    suspend inline fun <reified GuildData> updateGuildData(feature: Feature, data: GuildData, guildId: GuildId) {
        attemptCreateGuildDataFolder(feature, guildId)
        uploadGuildData(
            feature = feature,
            data = data,
            guildId = guildId,
            uploadType = UploadType.UPDATE,
            successLog = "Guild data successfully updated for ${feature.name} and guild of id $guildId."
        )
    }

    private fun handleDownloadResult(response: ApiResponse): DataCheckResult {
        return when (response) {
            is Error -> throw response.exception
            is Failure -> {
                if (response.code == CONFLICT_CODE) DataCheckResult.NoData else throw IllegalStateException()
            }
            is Success.WithData<*> -> DataCheckResult.ExistingData(response.data!!)
            is Success.WithNoData -> throw IllegalStateException("Data was requested but successful response had no data.")
            is Success -> throw IllegalStateException("Data was requested but successful response had no data.")
        }
    }

    private suspend fun attemptCreateGlobalDataFolder(feature: Feature) {
        val createFolderRequest = CreateFolderRequest(StoragePathHelper.globalStorageRepositoryPath(feature))
        handleFolderCreationResponse(
            createFolderRequest = createFolderRequest,
            successLog = "Global data repository created for feature ${feature.name}."
        )
    }

    private suspend fun attemptCreateGuildDataFolder(feature: Feature, guildId: GuildId) {
        val createFolderRequest = CreateFolderRequest(StoragePathHelper.guildStorageRepositoryPath(feature, guildId))
        handleFolderCreationResponse(
            createFolderRequest = createFolderRequest,
            successLog = "Guild data repository created for feature ${feature.name} and guild of id $guildId."
        )
    }

    private suspend fun handleFolderCreationResponse(
        createFolderRequest: CreateFolderRequest,
        successLog: String,
    ) {
        when (val createFolderResponse = service.createFolder(createFolderRequest)) {
            is Error -> throw createFolderResponse.exception
            is Failure -> {
                if (createFolderResponse.code != CONFLICT_CODE)
                    throw IllegalStateException("HTTP error: ${createFolderResponse.code}")
            }
            is Success -> {
                Log.info(successLog)
            }
        }
    }

    private suspend inline fun <reified GlobalData> uploadGlobalData(
        feature: Feature,
        data: GlobalData,
        uploadType: UploadType,
        successLog: String
    ) {
        val uploadDataRequest = UploadDataRequest(
            path = StoragePathHelper.globalStorageDataPath(feature),
            uploadType = uploadType
        )
        when (val uploadDataResponse = service.uploadData(data, uploadDataRequest)) {
            is Error -> throw uploadDataResponse.exception
            is Failure -> throw IllegalStateException("HTTP error: ${uploadDataResponse.code}")
            is Success -> Log.info(successLog)
        }
    }

    private suspend inline fun <reified GuildData> uploadGuildData(
        feature: Feature,
        data: GuildData,
        guildId: GuildId,
        uploadType: UploadType,
        successLog: String
    ) {
        val uploadDataRequest = UploadDataRequest(
            path = StoragePathHelper.guildStorageDataPath(feature, guildId),
            uploadType = uploadType
        )
        when (val uploadDataResponse = service.uploadData(data, uploadDataRequest)) {
            is Error -> throw uploadDataResponse.exception
            is Failure -> throw IllegalStateException("HTTP error: ${uploadDataResponse.code}")
            is Success -> Log.info(successLog)
        }
    }


}

