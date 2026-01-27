package com.example.mxh_application.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import com.example.mxh_application.data.local.entity.UserEntity
import com.example.mxh_application.data.repository.Resource
import com.example.mxh_application.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job


@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    data class UserListUiState(
        val data: List<UserEntity> = emptyList(),
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val hasMoreData: Boolean = true,
        val error: String? = null
    )

    data class UserDetailUiState(
        val user: UserEntity? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _userListState = MutableStateFlow(UserListUiState())
    val userListState: StateFlow<UserListUiState> = _userListState.asStateFlow()

    private val _userDetailState = MutableStateFlow(UserDetailUiState())
    val userDetailState: StateFlow<UserDetailUiState> = _userDetailState.asStateFlow()

    private var currentSkip: Int = 0
    private val pageSize: Int = 20
    private var searchJob: Job? = null
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        observeLocalUsers()
    }

    private fun observeLocalUsers() {
        viewModelScope.launch {
            combine(
                userRepository.getAllUsersFromDb(),
                _isSearching
            ) { users, searching -> users to searching }
                .collect { (users, searching) ->
                    if (!searching) {
                        _userListState.update { it.copy(data = users) }
                    }
                }
        }
    }

    fun refreshUsers(limit: Int = 20, skip: Int = 0) {
        viewModelScope.launch {
            currentSkip = 0
            _userListState.update { it.copy(hasMoreData = true) }
            _isSearching.value = false
            
            userRepository.clearAllUsers()
            
            userRepository.fetchAndCacheUsers(limit, skip).collect { result ->
                when (result) {
                    is Resource.Loading -> _userListState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> {
                        _userListState.update { 
                            it.copy(
                                isLoading = false, 
                                error = null,
                                hasMoreData = result.data?.size == limit
                            ) 
                        }
                    }
                    is Resource.Error -> _userListState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }


    fun loadMoreUsers() {
        val state = _userListState.value
        // Không load nếu đang loading hoặc hết data
        if (state.isLoadingMore || state.isLoading || !state.hasMoreData) return

        viewModelScope.launch {
            _userListState.update { it.copy(isLoadingMore = true) }
            val nextSkip = currentSkip + pageSize
            userRepository.fetchAndCacheUsers(limit = pageSize, skip = nextSkip).collect { result ->
                when (result) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        val fetchedCount = result.data?.size ?: 0
                        currentSkip = nextSkip
                        _userListState.update { 
                            it.copy(
                                isLoadingMore = false,
                                hasMoreData = fetchedCount == pageSize,
                                error = null
                            ) 
                        }
                    }
                    is Resource.Error -> {
                        _userListState.update { 
                            it.copy(isLoadingMore = false, error = result.message) 
                        }
                    }
                }
            }
        }
    }

    private fun launchSearch(trimmed: String, debounceMs: Long = 0L) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (debounceMs > 0) delay(debounceMs)

            currentSkip = 0
            _userListState.update { it.copy(hasMoreData = true) }

            if (trimmed.isEmpty()) {
                _isSearching.value = false
                refreshUsers(limit = pageSize, skip = 0)
                return@launch
            }

            _isSearching.value = true
            userRepository.searchUsers(trimmed).collect { result ->
                when (result) {
                    is Resource.Loading -> _userListState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> _userListState.update { it.copy(isLoading = false, data = result.data.orEmpty(), error = null) }
                    is Resource.Error -> _userListState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun searchUsers(query: String) {
        launchSearch(query.trim(), debounceMs = 0L)
    }

    fun debounceSearch(query: String) {
        launchSearch(query.trim(), debounceMs = 500L)
    }

    fun loadUserDetail(userId: Int) {
        viewModelScope.launch {
            userRepository.fetchAndCacheUserById(userId).collect { result ->
                when (result) {
                    is Resource.Loading -> _userDetailState.update { it.copy(isLoading = true, error = null) }
                    is Resource.Success -> _userDetailState.update { it.copy(isLoading = false, user = result.data, error = null) }
                    is Resource.Error -> _userDetailState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }
}
