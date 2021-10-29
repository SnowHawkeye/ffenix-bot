package bot.features.core.data

sealed class DataCheckResult {
    object NoData : DataCheckResult()
    data class ExistingData(val data: Any) : DataCheckResult()
}