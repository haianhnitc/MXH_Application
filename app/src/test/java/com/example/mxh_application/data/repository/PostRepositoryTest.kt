package com.example.mxh_application.data.repository

import com.example.mxh_application.data.local.dao.PostDao
import com.example.mxh_application.data.local.entity.PostEntity
import com.example.mxh_application.data.local.entity.Reactions
import com.example.mxh_application.data.remote.api.DummyJsonApi
import com.example.mxh_application.data.remote.dto.PaginatedPostsResponse
import com.example.mxh_application.data.remote.dto.PostResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PostRepositoryTest {

    private lateinit var api: DummyJsonApi
    private lateinit var dao: FakePostDao
    private lateinit var repository: PostRepository

    @Before
    fun setup() {
        api = mockk()
        dao = FakePostDao()
        repository = PostRepository(api, dao, database = mockk(relaxed = true))
    }

    @Test
    fun `searchPosts returns API results and caches them`() = runTest {
        val postA = PostResponse(id = 1, title = "Hello world", body = "body", tags = emptyList(), userId = 10)
        val postB = PostResponse(id = 2, title = "Other", body = "body", tags = emptyList(), userId = 11)
        coEvery { api.searchPosts("hello", any(), any()) } returns PaginatedPostsResponse(
            posts = listOf(postA, postB), total = 2, skip = 0, limit = 100
        )

        val emissions = repository.searchPosts("hello").toList()

        assertTrue(emissions[1] is Resource.Success)
        val data = (emissions[1] as Resource.Success).data!!
        assertEquals(1, data.size)
        assertEquals(1, data.first().id)
        // cached into dao
        assertEquals(1, dao.searchPostsOneTime("hello").size)
    }

    @Test
    fun `searchPosts falls back to local when API fails`() = runTest {
        val local = PostEntity(
            id = 99,
            title = "Offline title",
            body = "body",
            tags = listOf("tag"),
            reactions = Reactions(),
            views = 0,
            userId = 1
        )
        dao.insertPost(local)
        coEvery { api.searchPosts(any(), any(), any()) } throws RuntimeException("network down")

        val emissions = repository.searchPosts("offline").toList()

        assertTrue(emissions[1] is Resource.Success)
        val data = (emissions[1] as Resource.Success).data!!
        assertEquals(1, data.size)
        assertEquals(99, data.first().id)
    }

    @Test
    fun `createPost inserts into dao and returns success`() = runTest {
        val postResponse = PostResponse(
            id = 5,
            title = "New Post",
            body = "Body",
            tags = listOf("t"),
            reactions = null,
            views = 0,
            userId = 7
        )
        coEvery { api.createPost(any()) } returns postResponse

        val emissions = repository.createPost(
            title = "New Post",
            body = "Body",
            userId = 7,
            tags = listOf("t")
        ).toList()

        assertTrue(emissions[1] is Resource.Success)
        val data = (emissions[1] as Resource.Success).data!!
        assertEquals(5, data.id)
        assertEquals(1, dao.searchPostsOneTime("New Post").size)
        coVerify { api.createPost(any()) }
    }

    @Test
    fun `fetchAndCachePostsByUserId fetches and caches user posts`() = runTest {
        val post1 = PostResponse(id = 10, title = "User Post 1", body = "body", tags = emptyList(), reactions = null, views = 5, userId = 2)
        val post2 = PostResponse(id = 11, title = "User Post 2", body = "body", tags = emptyList(), reactions = null, views = 3, userId = 2)
        coEvery { api.getPostsByUserId(2, 20, 0) } returns PaginatedPostsResponse(
            posts = listOf(post1, post2), total = 2, skip = 0, limit = 20
        )

        val emissions = repository.fetchAndCachePostsByUserId(2, 20, 0).toList()

        assertTrue(emissions[1] is Resource.Success)
        val data = (emissions[1] as Resource.Success).data!!
        assertEquals(2, data.size)
        assertEquals(2, dao.getPostsByUserOneTime(2).size)
    }

    /**
     * Minimal in-memory PostDao for unit tests.
     */
    private class FakePostDao : PostDao {
        private val storage = mutableListOf<PostEntity>()
        private val flow = MutableStateFlow<List<PostEntity>>(emptyList())

        private fun upsert(post: PostEntity) {
            storage.removeAll { it.id == post.id }
            storage.add(post)
            flow.value = storage.sortedByDescending { it.createdAt }
        }

        override fun getAllPostsPaging() = throw UnsupportedOperationException()
        override fun getAllPostsFlow() = flow
        override suspend fun getPostById(postId: Int) = storage.firstOrNull { it.id == postId }
        override fun getPostByIdFlow(postId: Int) = throw UnsupportedOperationException()
        override fun getPostsByUserPaging(userId: Int) = throw UnsupportedOperationException()
        override fun getPostsByUserFlow(userId: Int) = throw UnsupportedOperationException()
        override suspend fun getPostsByUserOneTime(userId: Int) = storage.filter { it.userId == userId }
        override fun searchPosts(query: String) = throw UnsupportedOperationException()
        override suspend fun searchPostsOneTime(query: String) = storage.filter { it.title.contains(query, ignoreCase = true) }
        override fun getPostsByTag(tag: String) = throw UnsupportedOperationException()
        override suspend fun insertPost(post: PostEntity) = upsert(post)
        override suspend fun insertPosts(posts: List<PostEntity>) { posts.forEach { upsert(it) } }
        override suspend fun deletePostById(postId: Int) = storage.removeAll { it.id == postId }.let { 1 }
        override suspend fun clearAll() = storage.clear().let { 0 }
        override suspend fun clearPostsByUser(userId: Int) { storage.removeAll { it.userId == userId }; flow.value = storage }
        override suspend fun deleteAllPosts() = clearAll()
        override suspend fun deletePostsByUserId(userId: Int): Int { clearPostsByUser(userId); return 0 }
        override suspend fun getPostCount() = storage.size
        override suspend fun getPostCountByUser(userId: Int) = storage.count { it.userId == userId }
    }
}
