package bot.features.data_contract

sealed class DataCheckResult {
    object NoData : DataCheckResult()
    data class ExistingData(val data: Any) : DataCheckResult()
}