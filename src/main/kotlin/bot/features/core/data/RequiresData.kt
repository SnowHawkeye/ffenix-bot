package bot.features.core.data

import bot.features.Feature
import dev.kord.core.entity.Guild

internal object RequiresData {

    /**
     * Builder for data contracts of features that require both guild and global data.
     */
    inline fun <reified GlobalData, reified GuildData> globalAndGuild(
        crossinline createNewGlobalData: () -> GlobalData,
        crossinline updateExistingGlobalData: (Any) -> GlobalData,
        crossinline createNewGuildData: () -> GuildData,
        crossinline updateExistingGuildData: (Any) -> GuildData,
    ): FeatureDataContract {
        return object : FeatureDataContract {
            override suspend fun initializeGlobalData(feature: Feature) {
                initializeGlobalData(feature, createNewGlobalData, updateExistingGlobalData)
            }

            override suspend fun initializeGuildData(feature: Feature, guild: Guild) {
                initializeGuildData(feature, guild, createNewGuildData, updateExistingGuildData)
            }

        }
    }

    /**
     * Builder for data contracts of features that only require global data.
     */
    inline fun <reified GlobalData> global(
        crossinline createNewData: () -> GlobalData,
        crossinline updateExistingData: (Any) -> GlobalData,
    ): FeatureDataContract {
        return object : FeatureDataContract {
            override suspend fun initializeGlobalData(feature: Feature) {
                initializeGlobalData(feature, createNewData, updateExistingData)
            }

            override suspend fun initializeGuildData(feature: Feature, guild: Guild) {}

        }
    }

    /**
     * Builder for data contracts of features that only require guild data.
     */
    inline fun <reified GuildData> guild(
        crossinline createNewData: () -> GuildData,
        crossinline updateExistingData: (Any) -> GuildData,
    ): FeatureDataContract {
        return object : FeatureDataContract {
            override suspend fun initializeGlobalData(feature: Feature) {}

            override suspend fun initializeGuildData(feature: Feature, guild: Guild) {
                initializeGuildData(feature, guild, createNewData, updateExistingData)
            }

        }
    }

    private suspend inline fun <reified GlobalData> initializeGlobalData(
        feature: Feature,
        createNewGlobalData: () -> GlobalData,
        updateExistingGlobalData: (Any) -> GlobalData
    ) {
        when (val existingData = FeatureDataManager.checkForExistingGlobalData<GlobalData>(feature)) {
            DataCheckResult.NoData -> {
                val data = createNewGlobalData()
                FeatureDataManager.createGlobalData(feature, data)
            }
            is DataCheckResult.ExistingData -> {
                val oldData = existingData.data
                val data = updateExistingGlobalData(oldData)
                FeatureDataManager.updateGlobalData(feature, data)
            }
        }
    }

    private suspend inline fun <reified GuildData> initializeGuildData(
        feature: Feature,
        guild: Guild,
        createNewGuildData: () -> GuildData,
        updateExistingGuildData: (Any) -> GuildData
    ) {
        when (val existingData = FeatureDataManager.checkForExistingGuildData<GuildData>(feature, guild.id)) {
            DataCheckResult.NoData -> {
                val data = createNewGuildData()
                FeatureDataManager.createGuildData(feature, data, guild.id)
            }
            is DataCheckResult.ExistingData -> {
                val oldData = existingData.data
                val data = updateExistingGuildData(oldData)
                FeatureDataManager.updateGuildData(feature, data, guild.id)
            }
        }
    }


}