package bot.features.core.permissions

import dev.kord.core.behavior.createRole
import dev.kord.core.entity.Guild
import kotlinx.coroutines.flow.toList
import utils.logging.Log

/**
 * Represents the roles required by the bot's features.
 */
sealed class FeatureRolesContract {
    /**
     * Creates the roles necessary a feature.
     */
    abstract suspend fun createNecessaryRoles(guild: Guild)

    /**
     * Roles contract for features that do not require any roles.
     */
    object RequiresNoRoles : FeatureRolesContract() {
        override suspend fun createNecessaryRoles(guild: Guild) {}
    }

    /**
     * Roles contract for features that require certain roles.
     * Roles are checked and created based on name only.
     * If a role does not already exist, it will be created if the bot has the permission to do so.
     * If not, feature-specific roles will have to be created by hand.
     */
    data class RequiresRoles(val necessaryRoles: Set<NecessaryRole>) : FeatureRolesContract() {
        override suspend fun createNecessaryRoles(guild: Guild) {
            necessaryRoles.forEach { necessaryRole ->
                if (guild.roles.toList().all { role -> role.name != necessaryRole.roleName }) {
                    guild.createRole {
                        name = necessaryRole.roleName
                        color = necessaryRole.color
                    }
                    Log.info("Attempted to create role ${necessaryRole.roleName} in guild ${guild.name} (ID: ${guild.id})")
                }
            }
        }
    }
}