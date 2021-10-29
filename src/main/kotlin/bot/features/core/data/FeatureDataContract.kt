package bot.features.core.data

import bot.features.Feature
import dev.kord.core.entity.Guild

/**
 * Abstraction representing the kind of data required by a feature.
 * Its implementations are able to initialize said data.
 */
interface FeatureDataContract {
    suspend fun initializeGlobalData(feature: Feature)
    suspend fun initializeGuildData(feature: Feature, guild: Guild)

    object RequiresNoData : FeatureDataContract {
        override suspend fun initializeGlobalData(feature: Feature) {}
        override suspend fun initializeGuildData(feature: Feature, guild: Guild) {}
    }
}
