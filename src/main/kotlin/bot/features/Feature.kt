package bot.features

import bot.features.core.data.FeatureDataContract
import bot.features.core.permissions.FeatureRolesContract
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
    protected open val featureRolesContract: FeatureRolesContract = FeatureRolesContract.RequiresNoRoles

    open val name: String = this.javaClass.name

    /**
     * Adds functionality to the given Kord instance.
     */
    suspend fun addGuildCommands(receiver: Kord) {
        receiver.addFeatureGuildCommands()
    }

    suspend fun addGlobalCommands(receiver: Kord) {
        receiver.addFeatureGlobalCommands()
    }

    suspend fun addResponses(receiver: Kord) {
        receiver.addFeatureResponses()
    }

    /**
     * Initializes the data necessary to this feature.
     */
    suspend fun initializeGlobalData() = featureDataContract.initializeGlobalData(this)
    suspend fun initializeGuildData(guild: Guild) = featureDataContract.initializeGuildData(this, guild)

    protected abstract suspend fun Kord.addFeatureGuildCommands()
    protected abstract suspend fun Kord.addFeatureGlobalCommands()
    protected abstract suspend fun Kord.addFeatureResponses()
}
