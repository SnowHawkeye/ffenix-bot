package bot.features.bonk.model.results

import bot.features.bonk.model.BonkedUser

sealed class IncrementBonkResult {
    object Failure : IncrementBonkResult()
    data class Success(val bonkedUser: BonkedUser) : IncrementBonkResult()
}
