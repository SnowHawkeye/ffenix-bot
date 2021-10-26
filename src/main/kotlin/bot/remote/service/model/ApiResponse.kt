package bot.remote.service.model


sealed class ApiResponse {
    data class Failure(val code: Int, val message: String) : ApiResponse()
    data class Error(val exception: Exception) : ApiResponse()

    abstract class Success : ApiResponse() {
        data class WithData<T>(val data: T) : Success()
        object WithNoData : Success()
    }

}
