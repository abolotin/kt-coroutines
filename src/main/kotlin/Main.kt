import dto.CommentFullFilled
import dto.PostFullFilled
import kotlinx.coroutines.*
import service.PostsService
import kotlin.coroutines.EmptyCoroutineContext

fun main(args: Array<String>): Unit = runBlocking {
    val postsService = PostsService();

    with(CoroutineScope(EmptyCoroutineContext)) {
        launch {
            try {
                val posts = postsService.getPosts()
                    .map { post ->
                        async {
                            PostFullFilled(
                                post = post,
                                comments = postsService.getComments(post.id)
                                    .map { comment ->
                                            CommentFullFilled(
                                                comment = comment,
                                                author = postsService.getAuthor(comment.authorId)
                                            )
                                    },
                                author = postsService.getAuthor(post.authorId)
                            )
                        }
                    }.awaitAll()
                println(posts)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.join()
    }
}