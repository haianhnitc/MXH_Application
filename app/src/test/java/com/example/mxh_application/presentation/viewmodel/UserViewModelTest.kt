package com.example.mxh_application.presentation.viewmodel

import com.example.mxh_application.data.local.entity.UserEntity
import com.example.mxh_application.data.local.entity.Hair
import com.example.mxh_application.data.repository.UserRepository
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: UserRepository
    private lateinit var viewModel: UserViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = mockk(relaxed = true)
        io.mockk.coEvery { repository.getAllUsersFromDb() } returns flowOf(emptyList())
        viewModel = UserViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty and not loading`() {
        val state = viewModel.userListState.value
        assertEquals(0, state.data.size)
        assertFalse(state.isLoading)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun `isSearching flag initialized to false`() {
        assertEquals(false, viewModel.isSearching.value)
    }

    @Test
    fun `userDetailState initialized with null user`() {
        val state = viewModel.userDetailState.value
        assertEquals(null, state.user)
        assertFalse(state.isLoading)
    }

    @Test
    fun `userListState has correct default values`() {
        val state = viewModel.userListState.value
        assertEquals(emptyList<UserEntity>(), state.data)
        assertEquals(null, state.error)
        assertEquals(true, state.hasMoreData)
    }
}


