package bot.features.core

sealed class DataCheckResult {
    object NoData : DataCheckResult()
    data class ExistingData(val data: Any) : DataCheckResult()
}