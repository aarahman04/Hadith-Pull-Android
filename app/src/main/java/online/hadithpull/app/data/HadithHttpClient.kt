package online.hadithpull.app.data

import java.util.concurrent.TimeUnit
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.coroutines.executeAsync

/** Result of one HTTP attempt against the hadithapi.com endpoint. */
sealed interface HttpOutcome {
    data class Ok(val item: HadithDto?) : HttpOutcome
    data object Rejected : HttpOutcome // 401/403
    data object Busy : HttpOutcome // 429
    data object NotOk : HttpOutcome // any other non-2xx
}

/**
 * §1.1: GET https://hadithapi.com/api/hadiths?apiKey=…&book=…&hadithNumber=…
 * 15s connect/read timeouts, no cache, no logging interceptor (the key is in the query string).
 */
class HadithHttpClient(
    private val apiKey: String,
    private val baseUrl: String = "https://hadithapi.com",
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build(),
) {
    suspend fun fetch(slug: String, number: Int): HttpOutcome {
        val url = baseUrl.toHttpUrl().newBuilder()
            .addPathSegments("api/hadiths")
            .addQueryParameter("apiKey", apiKey)
            .addQueryParameter("book", slug)
            .addQueryParameter("hadithNumber", number.toString())
            .build()
        val request = Request.Builder().url(url).get().build()
        val response = client.newCall(request).executeAsync()
        return response.use {
            when (it.code) {
                200 -> HttpOutcome.Ok(parseHadithItem(it.body.string()))
                401, 403 -> HttpOutcome.Rejected
                429 -> HttpOutcome.Busy
                else -> HttpOutcome.NotOk
            }
        }
    }
}
