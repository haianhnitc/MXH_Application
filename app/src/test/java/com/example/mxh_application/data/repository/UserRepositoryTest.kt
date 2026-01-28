package com.example.mxh_application.data.repository

import com.example.mxh_application.data.local.dao.UserDao
import com.example.mxh_application.data.local.entity.UserEntity
import com.example.mxh_application.data.local.entity.Hair
import com.example.mxh_application.data.local.entity.Address
import com.example.mxh_application.data.local.entity.Company
import com.example.mxh_application.data.remote.api.DummyJsonApi
import com.example.mxh_application.data.remote.dto.PaginatedUsersResponse
import com.example.mxh_application.data.remote.dto.UserResponse
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

class UserRepositoryTest {

    private lateinit var api: DummyJsonApi
    private lateinit var dao: FakeUserDao
    private lateinit var repository: UserRepository

    @Before
    fun setup() {
        api = mockk()
        dao = FakeUserDao()
        repository = UserRepository(api, dao, database = mockk(relaxed = true))
    }

    @Test
    fun `searchUsers filters by first and last name from API`() = runTest {
        val userA = UserResponse(
            id = 1,
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            phone = "123",
            username = "john",
            image = "",
            age = 30,
            gender = "male",
            address = null,
            company = null,
            bloodGroup = "",
            height = 180.0,
            weight = 75.0,
            hair = null
        )
        val userB = UserResponse(
            id = 2,
            firstName = "Jane",
            lastName = "Johnson",
            email = "jane@example.com",
            phone = "456",
            username = "jane",
            image = "",
            age = 28,
            gender = "female",
            address = null,
            company = null,
            bloodGroup = "",
            height = 165.0,
            weight = 60.0,
            hair = null
        )
        coEvery { api.searchUsers("john", any(), any()) } returns PaginatedUsersResponse(
            users = listOf(userA, userB), total = 2, skip = 0, limit = 100
        )

        val emissions = repository.searchUsers("john").toList()

        assertTrue(emissions.size >= 2)
        assertTrue(emissions[1] is Resource.Success)
        val data = (emissions[1] as Resource.Success).data!!
        assertTrue(data.isNotEmpty())
        assertEquals("John", data.first().firstName)
    }

    @Test
    fun `searchUsers falls back to local when API fails`() = runTest {
        val local = UserEntity(
            id = 99,
            firstName = "Offline",
            lastName = "User",
            email = "offline@example.com",
            phone = "999",
            username = "offline",
            image = "",
            age = 25,
            gender = "male",
            address = null,
            company = null,
            bloodGroup = "",
            height = 170.0,
            weight = 70.0,
            postCount = 5,
            hair = null
        )
        dao.insertUser(local)
        coEvery { api.searchUsers(any(), any(), any()) } throws RuntimeException("network down")

        val emissions = repository.searchUsers("offline").toList()

        assertTrue(emissions[1] is Resource.Success)
        val data = (emissions[1] as Resource.Success).data!!
        assertEquals(1, data.size)
        assertEquals(99, data.first().id)
    }

    @Test
    fun `fetchAndCacheUsers caches users from API`() = runTest {
        val user1 = UserResponse(
            id = 1,
            firstName = "Alice",
            lastName = "Smith",
            email = "alice@example.com",
            phone = "111",
            username = "alice",
            image = "",
            age = 30,
            gender = "female",
            address = null,
            company = null,
            bloodGroup = "",
            height = 165.0,
            weight = 60.0,
            hair = null
        )
        val user2 = UserResponse(
            id = 2,
            firstName = "Bob",
            lastName = "Jones",
            email = "bob@example.com",
            phone = "222",
            username = "bob",
            image = "",
            age = 35,
            gender = "male",
            address = null,
            company = null,
            bloodGroup = "",
            height = 180.0,
            weight = 80.0,
            hair = null
        )
        coEvery { api.getAllUsers(20, 0) } returns PaginatedUsersResponse(
            users = listOf(user1, user2), total = 2, skip = 0, limit = 20
        )

        val emissions = repository.fetchAndCacheUsers(20, 0).toList()

        assertTrue(emissions[1] is Resource.Success)
        val data = (emissions[1] as Resource.Success).data!!
        assertEquals(2, data.size)
        assertEquals(2, dao.getUserCount())
        coVerify { api.getAllUsers(20, 0) }
    }

    @Test
    fun `fetchAndCacheUsers falls back to cache when API fails`() = runTest {
        val cachedUser = UserEntity(
            id = 99,
            firstName = "Cached",
            lastName = "User",
            email = "cached@example.com",
            phone = "999",
            username = "cached",
            image = "",
            age = 25,
            gender = "male",
            address = null,
            company = null,
            bloodGroup = "",
            height = 170.0,
            weight = 70.0,
            postCount = 3,
            hair = null
        )
        dao.insertUser(cachedUser)
        coEvery { api.getAllUsers(20, 0) } throws RuntimeException("network down")

        val emissions = repository.fetchAndCacheUsers(20, 0).toList()

        // Should emit Loading, then Success with cached data
        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Success)
        val data = (emissions[1] as Resource.Success).data!!
        assertEquals(1, data.size)
        assertEquals(99, data.first().id)
    }

    @Test
    fun `fetchAndCacheUsers returns error when API fails and cache is empty`() = runTest {
        coEvery { api.getAllUsers(20, 0) } throws RuntimeException("network down")

        val emissions = repository.fetchAndCacheUsers(20, 0).toList()

        // Should emit Loading, then Error
        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Error)
        assertEquals("network down", (emissions[1] as Resource.Error).message)
    }

    /**
     * Minimal in-memory UserDao for unit tests.
     */
    private class FakeUserDao : UserDao {
        private val storage = mutableListOf<UserEntity>()
        private val flow = MutableStateFlow<List<UserEntity>>(emptyList())

        private fun upsert(user: UserEntity) {
            storage.removeAll { it.id == user.id }
            storage.add(user)
            flow.value = storage.sortedBy { it.id }
        }

//        override fun getAllUsersPaging() = throw UnsupportedOperationException() // commented: unused
        override fun getAllUsersFlow() = flow
        override suspend fun getAllUsersOneTime() = storage.sortedBy { it.id }
        override suspend fun getUserById(userId: Int) = storage.firstOrNull { it.id == userId }
//        override fun getUserByIdFlow(userId: Int) = throw UnsupportedOperationException() // commented: unused
//        override fun searchUsers(query: String) = throw UnsupportedOperationException() // commented: unused
        override suspend fun searchUsersOneTime(query: String) = storage.filter {
            val q = query.lowercase()
            (it.firstName?.lowercase()?.contains(q) == true) || (it.lastName?.lowercase()?.contains(q) == true)
        }
//        override suspend fun getUserWithPosts(userId: Int) = throw UnsupportedOperationException() // commented: unused
//        override fun getUserWithPostsFlow(userId: Int) = throw UnsupportedOperationException() // commented: unused
        override suspend fun insertUser(user: UserEntity) = upsert(user)
        override suspend fun insertUsers(users: List<UserEntity>) { users.forEach { upsert(it) } }
//        override suspend fun updatePostCount(userId: Int, count: Int) {} // commented: unused
        override suspend fun deleteUserById(userId: Int) { storage.removeAll { it.id == userId }; flow.value = storage }
        override suspend fun clearAll() = storage.clear().let { 0 }
        override suspend fun deleteAllUsers() = clearAll()
        override suspend fun getUserCount() = storage.size
    }
}

