package bot.features.bonk.model

import bot.features.bonk.data.BonkDataStructure
import bot.features.bonk.data.BonkRepository
import bot.features.bonk.model.results.BonkStandingsResult
import bot.features.bonk.model.results.IncrementBonkResult
import bot.features.core.typealiases.GuildId
import bot.features.core.typealiases.UserId

class BonkEngine(
    private val repository: BonkRepository
) {
    fun makeInitialDataStructure(): BonkDataStructure {
        return BonkDataStructure(BonkStatistics(listOf()))
    }

    suspend fun incrementBonkCount(
        userId: UserId,
        forGuildId: GuildId,
    ): IncrementBonkResult {
        val bonkStatistics = repository.getBonkStatistics(forGuildId)
        val bonkedUser = bonkStatistics.bonkedUsers.find { it.userId == userId }
            ?: BonkedUser(userId, 0)
        val updatedBonkedUser = bonkedUser.copy(bonkNumber = bonkedUser.bonkNumber + 1)

        val updatedBonkedUsers = bonkStatistics.bonkedUsers.toMutableList().apply {
            remove(bonkedUser)
            add(updatedBonkedUser)
            sortBy { it.bonkNumber }
        }

        val updatedBonkStatistics = bonkStatistics.copy(bonkedUsers = updatedBonkedUsers)

        return when (repository.updateBonkStatistics(updatedBonkStatistics, forGuildId)) {
            BonkRepository.UploadBonkStatisticsResult.Failure -> IncrementBonkResult.Failure
            BonkRepository.UploadBonkStatisticsResult.Success -> IncrementBonkResult.Success(updatedBonkedUser)
        }
    }

    suspend fun getBonkStandings(
        numberOfUsers: Int,
        forGuildId: GuildId,
    ): BonkStandingsResult {
        val bonkStatistics = repository.getBonkStatistics(forGuildId)
        val mostBonkedUsers = bonkStatistics.bonkedUsers.sortedByDescending { it.bonkNumber }.take(numberOfUsers)

        if (mostBonkedUsers.isEmpty()) return BonkStandingsResult.NothingToDisplay
        return BonkStandingsResult.Success(mostBonkedUsers)
    }

    suspend fun getBonkScore(
        userId: UserId,
        forGuildId: GuildId,
    ): Int {
        val bonkStatistics = repository.getBonkStatistics(forGuildId)
        val bonkedUser = bonkStatistics.bonkedUsers.find { it.userId == userId }
            ?: BonkedUser(userId, 0)
        return bonkedUser.bonkNumber
    }

    companion object {
        fun instance() = BonkEngine(BonkRepository())
    }
}
