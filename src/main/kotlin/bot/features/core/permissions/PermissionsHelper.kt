package bot.features.core.permissions

import bot.features.core.typealiases.CommandId
import dev.kord.core.Kord
import dev.kord.core.entity.Guild
import kotlinx.coroutines.flow.toList

object PermissionsHelper {

    suspend fun authorizeRoleForCommandInGuild(
        role: NecessaryRole,
        commandId: CommandId,
        guild: Guild,
        featureRolesContract: FeatureRolesContract,
        client: Kord
    ) {
        val roleId = guild.roles.toList().find { it.name == role.roleName }?.id
        if (roleId != null) {
            client.editApplicationCommandPermissions(guild.id, commandId) { role(roleId) }
        } else {
            featureRolesContract.createNecessaryRoles(guild)
            val newAttemptRoleId = guild.roles.toList().find { it.name == role.roleName }?.id
            if (newAttemptRoleId != null) {
                client.editApplicationCommandPermissions(guild.id, commandId) { role(newAttemptRoleId) }
            }
        }

    }
}