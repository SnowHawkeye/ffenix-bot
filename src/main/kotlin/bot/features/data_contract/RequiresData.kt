package bot.features.data_contract

import bot.features.Feature
import dev.kord.core.entity.Guild

internal object RequiresData {

    /**
     * Builder for data contracts of features that require both guild and global data.
     */
    inline fun <reified GlobalData, reified GuildData> globalAndGuild(
        dataSource: FeatureDataContractDataSource,
        crossinline createNewGlobalData: () -> GlobalData,
        crossinline updateExistingGlobalData: (Any) -> GlobalData,
        crossinline createNewGuildData: () -> GuildData,
        crossinline updateExistingGuildData: (Any) -> GuildData,
    ): FeatureDataContract {
        return object : FeatureDataContract {
            override suspend fun initializeGlobalData(feature: Feature) {
                initializeGlobalData(dataSource, feature, createNewGlobalData, updateExistingGlobalData)
            }

            override suspend fun initializeGuildData(feature: Feature, guild: Guild) {
                initializeGuildData(dataSource, feature, guild, createNewGuildData, updateExistingGuildData)
            }

        }
    }

    /**
     * Builder for data contracts of features that only require global data.
     */
    inline fun <reified GlobalData> global(
        dataSource: FeatureDataContractDataSource,
        crossinline createNewData: () -> GlobalData,
        crossinline updateExistingData: (Any) -> GlobalData,
    ): FeatureDataContract {
        return object : FeatureDataContract {
            override suspend fun initializeGlobalData(feature: Feature) {
                initializeGlobalData(dataSource, feature, createNewData, updateExistingData)
            }

            override suspend fun initializeGuildData(feature: Feature, guild: Guild) {}

        }
    }

    /**
     * Builder for data contracts of features that only require guild data.
     */
    inline fun <reified GuildData> guild(
        dataSource: FeatureDataContractDataSource,
        crossinline createNewData: () -> GuildData,
        crossinline updateExistingData: (Any) -> GuildData,
    ): FeatureDataContract {
        return object : FeatureDataContract {
            override suspend fun initializeGlobalData(feature: Feature) {}

            override suspend fun initializeGuildData(feature: Feature, guild: Guild) {
                initializeGuildData(dataSource, feature, guild, createNewData, updateExistingData)
            }

        }
    }

    private suspend inline fun <reified GlobalData> initializeGlobalData(
        dataSource: FeatureDataContractDataSource,
        feature: Feature,
        createNewGlobalData: () -> GlobalData,
        updateExistingGlobalData: (Any) -> GlobalData
    ) {
        when (val existingData = dataSource.checkForExistingGlobalData<GlobalData>(feature)) {
            DataCheckResult.NoData -> {
                val data = createNewGlobalData()
                dataSource.createGlobalData(feature, data)
            }
            is DataCheckResult.ExistingData -> {
                val data = updateExistingGlobalData(existingData)
                dataSource.updateGlobalData(feature, data)
            }
        }
    }

    private suspend inline fun <reified GuildData> initializeGuildData(
        dataSource: FeatureDataContractDataSource,
        feature: Feature,
        guild: Guild,
        createNewGuildData: () -> GuildData,
        updateExistingGuildData: (Any) -> GuildData
    ) {
        when (val existingData = dataSource.checkForExistingGuildData<GuildData>(feature, guild.id)) {
            DataCheckResult.NoData -> {
                val data = createNewGuildData()
                dataSource.createGuildData(feature, data, guild.id)
            }
            is DataCheckResult.ExistingData -> {
                val data = updateExistingGuildData(existingData)
                dataSource.updateGuildData(feature, data, guild.id)
            }
        }
    }


}