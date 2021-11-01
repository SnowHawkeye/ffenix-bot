package runtime

import bot.FFEnixBot
import bot.features.guides.GuidesFeature
import bot.features.quotes.QuotesFeature
import bot.features.speech.SpeechFeature
import dev.kord.core.Kord

suspend fun main() {

    val token = System.getenv(FFENIX_BOT_TOKEN_KEY)
    val client = Kord(token)
    FFEnixBot(
        client = client, features = setOf(
            QuotesFeature,
            GuidesFeature,
            SpeechFeature,
        )
    ).start()
}
