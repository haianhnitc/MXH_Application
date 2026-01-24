package com.example.mxh_application.presentation.viewmodel

import com.example.mxh_application.data.local.entity.PostEntity
import com.example.mxh_application.data.local.entity.Reactions
import com.example.mxh_application.data.repository.PostRepository
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
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PostViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: PostRepository
    private lateinit var viewModel: PostViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = mockk(relaxed = true)
        // Mock empty DB flow để init không crash
        io.mockk.coEvery { repository.getAllPostsFromDb() } returns flowOf(emptyList())
        viewModel = PostViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty and not loading`() {
        val state = viewModel.postListState.value
        assertEquals(0, state.data.size)
        assertFalse(state.isLoading)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun `isSearching flag initialized to false`() {
        assertEquals(false, viewModel.isSearching.value)
    }

    @Test
    fun `postDetailState initialized with null post`() {
        val state = viewModel.postDetailState.value
        assertEquals(null, state.post)
        assertFalse(state.isLoading)
    }

    @Test
    fun `postListState has correct default values`() {
        val state = viewModel.postListState.value
        assertEquals(emptyList<PostEntity>(), state.data)
        assertEquals(null, state.error)
        assertEquals(true, state.hasMoreData)
    }
}

