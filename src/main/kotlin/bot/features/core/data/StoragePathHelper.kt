package bot.features.core.data

import bot.features.Feature
import bot.features.core.typealiases.GuildId

/**
 * Regroups utility functions to centralize the path writing.
 */
object StoragePathHelper {
    private fun formatFeatureName(feature: Feature) = feature.name.lowercase()
    private fun featureDataFileName(feature: Feature) = "${formatFeatureName(feature)}.json"

    fun globalStorageDataPath(feature: Feature): String {
        return globalStorageRepositoryPath(feature) + featureDataFileName(feature)
    }

    fun globalStorageRepositoryPath(feature: Feature): String {
        val featureName = formatFeatureName(feature)
        return "/data/global/$featureName/"
    }

    fun guildStorageDataPath(feature: Feature, guildId: GuildId): String {
        return guildStorageRepositoryPath(feature, guildId) + featureDataFileName(feature)
    }

    fun guildStorageRepositoryPath(feature: Feature, guildId: GuildId): String {
        val featureName = formatFeatureName(feature)
        return "/data/guild/${guildId.asString}/$featureName/"
    }
}