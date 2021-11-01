package bot.features.poll

import bot.features.Feature
import bot.features.core.addChatInputCommandForEveryGuild
import bot.features.core.addChatInputCommandResponse
import bot.features.core.catchCastExceptions
import bot.features.core.ephemeralResponse
import bot.features.core.typealiases.MessageId
import bot.features.poll.data.*
import bot.features.poll.model.Poll
import bot.features.poll.model.PollEngine
import bot.features.poll.model.results.PollCreationResult
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.application.GuildChatInputCommand
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.int
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

object PollFeature : Feature() {
    override val name: String = pollFeatureName
    private lateinit var pollCommands: List<GuildChatInputCommand>

    private var currentlyKnownPolls: MutableMap<MessageId, Poll> = mutableMapOf()

    private val engine = PollEngine.instance()

    override suspend fun Kord.addFeatureGlobalCommands() {}

    override suspend fun Kord.addFeatureGuildCommands() {
        pollCommands = addChatInputCommandForEveryGuild(
            name = pollCommandName,
            description = pollCommandDescription
        ) {
            string(pollQuestionArgumentName, pollQuestionArgumentDescription) { required = true }
            string(pollOptionsArgumentName, pollOptionsArgumentDescription) { required = true }
            int(pollMaxAnswersArgumentName, pollMaxAnswersArgumentDescription) { required = false }
            string(pollThumbnailUrlArgumentName, pollThumbnailUrlArgumentDescription) { required = false }
        }
    }


    override suspend fun Kord.addFeatureResponses() {
        pollResponse()
        managePollReactions()
    }

    private fun Kord.pollResponse() {
        on<GuildChatInputCommandInteractionCreateEvent> {
            var question: String? = null
            var pollOptions: String? = null
            var maxAnswers: Int? = null
            var thumbnailUrl: String? = null
            val pollTimestamp = Clock.System.now()
            val author = interaction.member.displayName

            addChatInputCommandResponse(pollCommands) {
                val options = interaction.data.data.options
                options.value?.forEach { option ->
                    option.value.value?.let { commandArgument ->
                        catchCastExceptions {
                            when (commandArgument.name) {
                                pollQuestionArgumentName -> question = commandArgument.value.toString()
                                pollOptionsArgumentName -> pollOptions = commandArgument.value.toString()
                                pollMaxAnswersArgumentName -> maxAnswers = (commandArgument.value as Long).toInt()
                                pollThumbnailUrlArgumentName -> thumbnailUrl = commandArgument.value.toString()
                            }
                        }
                    }
                }
                makePoll(question, pollOptions, author, pollTimestamp, maxAnswers, thumbnailUrl)
            }
        }
    }

    private suspend fun InteractionCreateEvent.makePoll(
        question: String?,
        options: String?,
        author: String,
        pollTimestamp: Instant,
        maxAnswers: Int?,
        thumbnailUrl: String?,
    ) {
        if (question == null || options == null) {
            ephemeralResponse(genericErrorMessage)
        } else {
            val attemptCreatePoll = engine.makePoll(
                question = question,
                options = options,
                author = author,
                timestamp = pollTimestamp,
                maxAnswers = maxAnswers,
                thumbnailUrl = thumbnailUrl
            )

            when (attemptCreatePoll) {
                PollCreationResult.InvalidMaxAnswers -> ephemeralResponse(pollInvalidMaxAnswersErrorMessage)
                PollCreationResult.NotEnoughOptions -> ephemeralResponse(pollNotEnoughOptionsErrorMessage)
                PollCreationResult.TooManyOptions -> ephemeralResponse(pollTooManyOptionsErrorMessage)
                is PollCreationResult.PollCreated -> {
                    val poll = attemptCreatePoll.poll
                    makePollEmbed(poll)
                }
            }
        }
    }


    private suspend fun InteractionCreateEvent.makePollEmbed(poll: Poll) {
        val channel = interaction.channel
        ephemeralResponse(pollSuccessfullyCreatedMessage)

        val pollMessage = channel.createMessage {
            content = pollDisplayMessage
            embed {
                author { name = pollEmbedTitle }
                thumbnail { url = poll.thumbnailUrl }
                title = poll.question
                description = pollEmbedDescription(poll.maxAnswers)
                poll.options.forEach {
                    field { name = it.emojiUnicode; value = it.option; inline = true }
                }
                timestamp = poll.timestamp
                footer { text = poll.author }
            }
        }

        val reactions = poll.options.map { ReactionEmoji.Unicode(it.emojiUnicode) }
        reactions.forEach { pollMessage.addReaction(it) }
        currentlyKnownPolls[pollMessage.id] = poll
    }

    private fun Kord.managePollReactions() {
        on<ReactionAddEvent> {
            if (this.user == this@managePollReactions.getSelf()) return@on
            val correspondingPoll = currentlyKnownPolls[this.message.id]
            if (correspondingPoll != null) {
                var numberOfReactionsForThisUser = 0
                val message = getMessage()
                message.reactions.forEach {
                    val allReactors = message.getReactors(it.emoji)
                    if (allReactors.toList().contains(this.user)) numberOfReactionsForThisUser++
                }
                if (numberOfReactionsForThisUser > correspondingPoll.maxAnswers)
                    message.deleteReaction(this.userId, emoji)
            }
        }
    }
}