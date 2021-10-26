package bot.remote.client

import bot.remote.AUTHORIZATION_HEADER_KEY
import bot.remote.DROPBOX_API_TOKEN_KEY
import bot.remote.EMPTY_BASE_URL
import bot.remote.service.StorageApiService
import bot.remote.service.StorageApiServiceWrapper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

internal object StorageApiClient {

    fun service() = StorageApiServiceWrapper(apiService, gson)

    private val gson: Gson by lazy { GsonBuilder().setLenient().create() }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            addInterceptor(loggingInterceptor())
            addInterceptor(defaultHeadersInterceptor())
        }.build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(EMPTY_BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(EnumConverterFactory())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    private val apiService: StorageApiService = retrofit.create(StorageApiService::class.java)

    private fun defaultHeadersInterceptor() = Interceptor { chain ->
        val authorizationHeader = "Bearer ${System.getenv(DROPBOX_API_TOKEN_KEY)}"
        val request = chain.request()
            .newBuilder()
            .addHeader(AUTHORIZATION_HEADER_KEY, authorizationHeader).build()
        chain.proceed(request)
    }

    private fun loggingInterceptor() =
        HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
            redactHeader(AUTHORIZATION_HEADER_KEY)
        }
}
