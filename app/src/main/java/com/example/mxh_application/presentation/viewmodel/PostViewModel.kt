package com.example.mxh_application.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import com.example.mxh_application.data.local.entity.PostEntity
import com.example.mxh_application.data.repository.PostRepository
import com.example.mxh_application.data.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    data class PostListUiState(
        val data: List<PostEntity> = emptyList(),
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val hasMoreData: Boolean = true,
        val error: String? = null,
        val totalCount: Int? = null
    )

    data class PostDetailUiState(
        val post: PostEntity? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _postListState = MutableStateFlow(PostListUiState())
    val postListState: StateFlow<PostListUiState> = _postListState.asStateFlow()

    private val _postDetailState = MutableStateFlow(PostDetailUiState())
    val postDetailState: StateFlow<PostDetailUiState> = _postDetailState.asStateFlow()

    private var postsObserverJob: Job? = null
    private var searchJob: Job? = null
    private var currentUserId: Int? = null
    private var currentSkip: Int = 0
    private val pageSize: Int = 20
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        observePosts(userId = null)
    }

    private fun observePosts(userId: Int?) {
        postsObserverJob?.cancel()
        postsObserverJob = viewModelScope.launch {
            val flow = if (userId != null) {
                postRepository.getPostsByUserFromDb(userId)
            } else {
                postRepository.getAllPostsFromDb()
            }
            combine(flow, _isSearching) { posts, searching -> posts to searching }
                .collect { (posts, searching) ->
                    if (!searching) {
                        _postListState.update { it.copy(data = posts) }
                    }
                }
        }
    }


    fun refreshPosts(limit: Int = 20, skip: Int = 0) {
        viewModelScope.launch {
            _isSearching.value = false
            currentUserId = null
            observePosts(userId = null)
            currentSkip = 0
            _postListState.update { it.copy(hasMoreData = true, totalCount = null) }
            
            postRepository.clearAllPosts()
            
            postRepository.fetchAndCachePosts(limit, skip).collect { result ->
                when (result) {
                    is Resource.Loading -> _postListState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> {
                        _postListState.update { 
                            it.copy(
                                isLoading = false,
                                error = null,
                                hasMoreData = result.data?.size == limit,
                                totalCount = result.data?.size
                            ) 
                        }
                    }
                    is Resource.Error -> _postListState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }


    fun loadMorePosts() {
        val state = _postListState.value
        // Không load nếu đang loading hoặc hết data
        if (state.isLoadingMore || state.isLoading || !state.hasMoreData) return

        viewModelScope.launch {
            _postListState.update { it.copy(isLoadingMore = true) }
            val nextSkip = currentSkip + pageSize
            val targetUserId = currentUserId
            val flow = if (targetUserId != null) {
                postRepository.fetchAndCachePostsByUserId(
                    userId = targetUserId,
                    limit = pageSize,
                    skip = nextSkip
                )
            } else {
                postRepository.fetchAndCachePosts(limit = pageSize, skip = nextSkip)
            }

            flow.collect { result ->
                when (result) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        val fetchedCount = result.data?.size ?: 0
                        currentSkip = nextSkip
                        val totalForUser = if (targetUserId != null) {
                            postRepository.getPostCountByUserId(targetUserId)
                        } else null
                        _postListState.update {
                            it.copy(
                                isLoadingMore = false,
                                hasMoreData = fetchedCount == pageSize,
                                error = null,
                                totalCount = totalForUser ?: it.totalCount
                            )
                        }
                    }
                    is Resource.Error -> {
                        _postListState.update { 
                            it.copy(isLoadingMore = false, error = result.message) 
                        }
                    }
                }
            }
        }
    }

    fun loadPostsByUser(userId: Int, limit: Int = pageSize, skip: Int = 0) {
        viewModelScope.launch {
            _isSearching.value = false
            currentUserId = userId
            observePosts(userId)
            currentSkip = 0
            _postListState.update { it.copy(hasMoreData = true, totalCount = null) }  // Reset flag
            postRepository.fetchAndCachePostsByUserId(userId, limit, skip).collect { result ->
                when (result) {
                    is Resource.Loading -> _postListState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> {
                        val fetchedCount = result.data?.size ?: 0
                        val totalForUser = postRepository.getPostCountByUserId(userId)
                        _postListState.update { it.copy(isLoading = false, data = result.data.orEmpty(), error = null, hasMoreData = fetchedCount == limit, totalCount = totalForUser) }
                    }
                    is Resource.Error -> _postListState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun searchPosts(query: String) {
        val trimmed = query.trim()

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            currentUserId = null
            observePosts(userId = null)
            currentSkip = 0
            _postListState.update { it.copy(hasMoreData = true, totalCount = null) }  // Reset flag

            if (trimmed.isEmpty()) {
                _isSearching.value = false
                refreshPosts(limit = pageSize, skip = 0)
                return@launch
            }

            _isSearching.value = true
            postRepository.searchPosts(trimmed).collect { result ->
                when (result) {
                    is Resource.Loading -> _postListState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> _postListState.update { it.copy(isLoading = false, data = result.data.orEmpty(), error = null) }
                    is Resource.Error -> _postListState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun debounceSearch(query: String) {
        val trimmed = query.trim()

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            if (trimmed.isNotEmpty()) {
                _isSearching.value = true
                searchPosts(trimmed)
            } else {
                _isSearching.value = false
                refreshPosts(limit = pageSize, skip = 0)
            }
        }
    }

    fun loadPostDetail(postId: Int) {
        viewModelScope.launch {
            postRepository.fetchAndCachePostById(postId).collect { result ->
                when (result) {
                    is Resource.Loading -> _postDetailState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> _postDetailState.update { it.copy(isLoading = false, post = result.data, error = null) }
                    is Resource.Error -> _postDetailState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }
    
    suspend fun createPost(
        title: String,
        body: String,
        userId: Int,
        tags: List<String> = emptyList()
    ) {
        postRepository.createPost(title, body, userId, tags).collect { result ->
            when (result) {
                is Resource.Loading -> {}
                is Resource.Success -> {
                    // Đã insert vào DB trong Repository -> Flow observeLocalPosts() sẽ tự cập nhật UI.
                    // Không gọi refreshPosts() để tránh clear DB làm mất post vừa tạo trên DummyJson.
                    // sao chỗ này không thêm thông báo đã tạo post thành công lên UI vậy
                }
                is Resource.Error -> {
                    throw Exception(result.message)
                }
            }
        }
    }
}
