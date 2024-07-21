package service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dto.Author
import dto.Comment
import dto.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PostsService {
    private val baseUrl = "http://localhost:9999"
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor(::println).apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun OkHttpClient.apiCall(url: String): Response {
        return suspendCoroutine { continuation ->
            Request.Builder()
                .url(url)
                .build()
                .let(::newCall)
                .enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        continuation.resume(response)
                    }

                    override fun onFailure(call: Call, e: IOException) {
                        continuation.resumeWithException(e)
                    }
                })
        }
    }

    suspend fun <T> makeRequest(url: String, client: OkHttpClient, typeToken: TypeToken<T>): T =
        withContext(Dispatchers.IO) {
            client.apiCall(url)
                .let { response ->
                    if (!response.isSuccessful) {
                        response.close()
                        throw RuntimeException(response.message)
                    }
                    val body = response.body ?: throw RuntimeException("response body is null")
                    gson.fromJson(body.string(), typeToken.type)
                }
        }

    suspend fun getPosts(): List<Post> =
        makeRequest("$baseUrl/api/posts", client, object : TypeToken<List<Post>>() {})

    suspend fun getComments(id: Long): List<Comment> =
        makeRequest("$baseUrl/api/posts/$id/comments", client, object : TypeToken<List<Comment>>() {})

    suspend fun getAuthor(id: Long): Author =
        makeRequest("$baseUrl/api/authors/$id", client, object : TypeToken<Author>() {})
}