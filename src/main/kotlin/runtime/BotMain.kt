package runtime

import bot.FFEnixBot
import bot.features.bonk.BonkFeature
import bot.features.guides.GuidesFeature
import bot.features.help.HelpFeature
import bot.features.info.InfoFeature
import bot.features.poll.PollFeature
import bot.features.quotes.QuotesFeature
import bot.features.scheduling.SchedulingFeature
import bot.features.speech.SpeechFeature
import dev.kord.core.Kord

suspend fun main() {

    val token = System.getenv(FFENIX_BOT_TOKEN_KEY)
    val client = Kord(token)
    FFEnixBot(
        client = client, features = setOf(
            HelpFeature,
            PollFeature,
            SpeechFeature,
            GuidesFeature,
            QuotesFeature,
            SchedulingFeature,
            BonkFeature,
            InfoFeature,
        )
    ).start()
}