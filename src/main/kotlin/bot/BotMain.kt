package bot

import com.jessecorbett.diskord.bot.*

suspend fun main() {
    val token = System.getenv(FFENIX_BOT_TOKEN_KEY)
    bot(token) {
        classicCommands(commandPrefix = "!") {
            command("ping") {
                it.respond("pong")
            }
        }
    }
}
