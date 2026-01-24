package com.example.mxh_application.presentation.screens.posts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import com.example.mxh_application.presentation.components.EmptyState
import com.example.mxh_application.presentation.components.ErrorMessage
import com.example.mxh_application.presentation.components.LoadingIndicator
import com.example.mxh_application.presentation.components.PostListItem
import com.example.mxh_application.presentation.components.SearchBar
import com.example.mxh_application.presentation.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(
    onPostClick: (Int) -> Unit,
    onCreatePostClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: PostViewModel = hiltViewModel()
) {
    val uiState by viewModel.postListState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val isSearching by viewModel.isSearching.collectAsState()
    val listState = rememberLazyListState()
    var forceScrollToTop by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        if (uiState.data.isEmpty() && !uiState.isLoading) {
            viewModel.refreshPosts()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh sách Posts") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreatePostClick
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tạo post mới"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Hiển thị ${uiState.data.size} posts",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            SearchBar(
                query = searchQuery,
                onQueryChange = { query ->
                    searchQuery = query
                    viewModel.debounceSearch(query)
                },
                onSearch = {
                    if (searchQuery.isNotEmpty()) {
                        viewModel.searchPosts(searchQuery)
                    }
                },
                placeholder = "Tìm kiếm post..."
            )
            
            PullToRefreshBox(
                isRefreshing = uiState.isLoading && !uiState.isLoadingMore,
                onRefresh = {
                    forceScrollToTop = true
                    if (isSearching && searchQuery.isNotEmpty()) {
                        viewModel.searchPosts(searchQuery)
                    } else {
                        viewModel.refreshPosts()
                    }
                }
            ) {
                when {
                    uiState.isLoading && uiState.data.isEmpty() -> {
                        LoadingIndicator(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    uiState.error != null && uiState.data.isEmpty() -> {
                        ErrorMessage(
                            message = uiState.error ?: "Có lỗi xảy ra",
                            onRetry = {
                                if (isSearching && searchQuery.isNotEmpty()) {
                                    viewModel.searchPosts(searchQuery)
                                } else {
                                    viewModel.refreshPosts()
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    uiState.data.isEmpty() -> {
                        EmptyState(
                            message = if (isSearching) {
                                "Không tìm thấy post nào"
                            } else {
                                "Chưa có post nào"
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            state = listState
                        ) {
                            items(
                                items = uiState.data,
                                key = { post -> post.id }
                            ) { post ->
                                PostListItem(
                                    post = post,
                                    onClick = { onPostClick(post.id) }
                                )
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        uiState.isLoadingMore -> {
                                            LoadingIndicator()
                                        }
                                        !uiState.hasMoreData && uiState.data.isNotEmpty() -> {
                                            Text(
                                                text = "Đã hiển thị hết posts",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        LaunchedEffect(listState, isSearching, uiState.hasMoreData) {
                            snapshotFlow {
                                Pair(
                                    listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1,
                                    listState.isScrollInProgress
                                )
                            }
                                .filter { (lastIndex, isScrolling) -> lastIndex >= 0 && isScrolling }
                                .distinctUntilChanged()
                                .collect { (lastIndex, _) ->
                                    val total = uiState.data.size
                                    if (!isSearching && total > 0 && lastIndex >= total - 3 && uiState.hasMoreData && !uiState.isLoadingMore && !uiState.isLoading) {
                                        viewModel.loadMorePosts()
                                    }
                                }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState.data.size, uiState.isLoading, uiState.isLoadingMore, forceScrollToTop) {
        if (forceScrollToTop && !uiState.isLoading && !uiState.isLoadingMore && uiState.data.isNotEmpty()) {
            listState.scrollToItem(0)
            forceScrollToTop = false
        }
    }

    LaunchedEffect(isSearching, uiState.data.size) {
        if (isSearching && !uiState.isLoading && uiState.data.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }
}
