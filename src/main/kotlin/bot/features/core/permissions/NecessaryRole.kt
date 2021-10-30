package bot.features.core.permissions

import dev.kord.common.Color

data class NecessaryRole(
    val roleName: String,
    val color: Color? = null
)
