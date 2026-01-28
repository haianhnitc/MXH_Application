package com.example.mxh_application.presentation.screens.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mxh_application.presentation.components.EmptyState
import com.example.mxh_application.presentation.components.ErrorMessage
import com.example.mxh_application.presentation.components.LoadingIndicator
import com.example.mxh_application.presentation.components.PostListItem
import com.example.mxh_application.presentation.viewmodel.PostViewModel
import com.example.mxh_application.presentation.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    userId: Int,
    onBackClick: () -> Unit,
    onPostClick: (Int) -> Unit,
    userViewModel: UserViewModel = hiltViewModel(),
    postViewModel: PostViewModel = hiltViewModel()
) {
    val userState by userViewModel.userDetailState.collectAsState()
    val postState by postViewModel.postListState.collectAsState()
    
    LaunchedEffect(userId) {
        userViewModel.loadUserDetail(userId)
        postViewModel.loadPostsByUser(userId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông tin User") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        when {
            userState.isLoading -> {
                LoadingIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            
            userState.error != null -> {
                ErrorMessage(
                    message = userState.error ?: "Có lỗi xảy ra",
                    onRetry = { userViewModel.loadUserDetail(userId) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            
            userState.user != null -> {
                val user = userState.user!!
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = user.image,
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "${user.firstName} ${user.lastName}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Text(
                                    text = "@${user.username}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                InfoRow(label = "Email", value = user.email)
                                InfoRow(label = "Phone", value = user.phone)
                                
                                user.birthDate?.let {
                                    InfoRow(label = "Ngày sinh", value = it)
                                }
                                
                                InfoRow(label = "Giới tính", value = user.gender)
                                InfoRow(label = "Tuổi", value = user.age.toString())
                                
                                user.address?.let { addr ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    addr.address?.let { InfoRow(label = "Địa chỉ", value = it) }
                                    addr.city?.let { InfoRow(label = "Thành phố", value = it) }
                                    addr.state?.let { InfoRow(label = "Tỉnh/Bang", value = it) }
                                    addr.country?.let { InfoRow(label = "Quốc gia", value = it) }
                                    addr.postalCode?.let { InfoRow(label = "Mã bưu chính", value = it) }
                                }
                                
                                user.company?.let { comp ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    comp.name?.let { InfoRow(label = "Công ty", value = it) }
                                    comp.title?.let { InfoRow(label = "Chức vụ", value = it) }
                                    comp.department?.let { InfoRow(label = "Phòng ban", value = it) }
                                }
                                
                                user.university?.let { InfoRow(label = "Đại học", value = it) }
                            }
                        }
                    }
                    
                    item {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = "Bài viết của ${user.firstName}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            val countLabel = when {
                                postState.totalCount != null -> "${postState.totalCount} bài viết"
                                else -> "${postState.data.size} bài viết"
                            }
                            Text(
                                text = countLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    when {
                        postState.isLoading -> {
                            item {
                                LoadingIndicator()
                            }
                        }
                        
                        postState.error != null && postState.data.isEmpty() -> {
                            item {
                                ErrorMessage(
                                    message = postState.error ?: "Không thể tải posts",
                                    onRetry = { postViewModel.loadPostsByUser(userId) }
                                )
                            }
                        }
                        
                        postState.data.isEmpty() -> {
                            item {
                                EmptyState(message = "User này chưa có bài viết nào")
                            }
                        }
                        
                        else -> {
                            itemsIndexed(
                                items = postState.data,
                                key = { _, post -> post.id }
                            ) { index, post ->
                                if (index >= postState.data.lastIndex - 2 &&
                                    postState.hasMoreData &&
                                    !postState.isLoadingMore &&
                                    !postState.isLoading
                                ) {
                                    LaunchedEffect(key1 = index, key2 = postState.data.size) {
                                        postViewModel.loadMorePosts()
                                    }
                                }
                                PostListItem(
                                    post = post,
                                    onClick = { onPostClick(post.id) }
                                )
                            }
                            item {
                                if (postState.isLoadingMore) {
                                    LoadingIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
