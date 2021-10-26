package bot.features

import bot.features.data_contract.FeatureDataContract
import dev.kord.core.Kord
import dev.kord.core.entity.Guild

/**
 * Abstraction for a bot feature.
 */
abstract class Feature {
    /**
     * This property must be overridden for a feature to store data.
     * Using this ensures that data is initialized when it needs to be.
     */
    protected open val featureDataContract: FeatureDataContract = FeatureDataContract.RequiresNoData
    open val name: String = this.javaClass.name

    /**
     * Adds functionality to the given Kord instance.
     */
    suspend fun addTo(receiver: Kord) {
        receiver.addFeature()
    }

    /**
     * Initializes the data necessary to this feature.
     */
    suspend fun initializeGlobalData() = featureDataContract.initializeGlobalData(this)
    suspend fun initializeGuildData(guild: Guild) = featureDataContract.initializeGuildData(this, guild)

    protected abstract suspend fun Kord.addFeature()
}
