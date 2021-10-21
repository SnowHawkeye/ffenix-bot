package bot

import com.jessecorbett.diskord.bot.bot
import com.jessecorbett.diskord.bot.classicCommands

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
