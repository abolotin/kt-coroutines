package dto

data class PostFullFilled(
    val post: Post,
    val comments: List<CommentFullFilled>? = null,
    val author: Author? = null
)
