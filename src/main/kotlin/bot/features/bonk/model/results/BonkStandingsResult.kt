package bot.features.bonk.model.results

import bot.features.bonk.model.BonkedUser

sealed class BonkStandingsResult {
    object NothingToDisplay : BonkStandingsResult()
    data class Success(val bonkedUsers: List<BonkedUser>) : BonkStandingsResult()
}
