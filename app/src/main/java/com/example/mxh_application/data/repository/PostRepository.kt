package com.example.mxh_application.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.example.mxh_application.data.local.AppDatabase
import com.example.mxh_application.data.local.dao.PostDao
import com.example.mxh_application.data.local.entity.PostEntity
import com.example.mxh_application.data.mapper.toEntity
import com.example.mxh_application.data.mapper.toEntityList
import com.example.mxh_application.data.remote.api.DummyJsonApi
import com.example.mxh_application.data.remote.dto.CreatePostRequest
import com.example.mxh_application.data.remote.mediator.PostRemoteMediator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val api: DummyJsonApi,
    private val postDao: PostDao,
    private val database: AppDatabase
) {
    
    // Lấy tất cả posts từ API và cache vào database
    fun fetchAndCachePosts(
        limit: Int = 20,
        skip: Int = 0
    ): Flow<Resource<List<PostEntity>>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = api.getAllPosts(limit, skip)
            val listPostEntities = response.posts.toEntityList()
            postDao.insertPosts(listPostEntities)
            
            emit(Resource.Success(listPostEntities))
        } catch (e: Exception) {
            emit(Resource.Error(
                message = e.localizedMessage ?: "Có lỗi xảy ra"
            ))
        }
    }

    fun fetchAndCachePostById(postId: Int): Flow<Resource<PostEntity>> = flow {
        emit(Resource.Loading())
        
        try {
            val postResponse = api.getPostById(postId)
            val postEntity = postResponse.toEntity()
            
            postDao.insertPost(postEntity)
            
            emit(Resource.Success(postEntity))
        } catch (e: Exception) {
            val cachedPost = postDao.getPostById(postId)
            if (cachedPost != null) {
                emit(Resource.Success(cachedPost))
            } else {
                emit(Resource.Error(e.localizedMessage ?: "Post không tồn tại"))
            }
        }
    }
    
    // Lấy posts của một user từ API
    fun fetchAndCachePostsByUserId(
        userId: Int,
        limit: Int = 20,
        skip: Int = 0
    ): Flow<Resource<List<PostEntity>>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = api.getPostsByUserId(userId, limit, skip)
            val postEntities = response.posts.toEntityList()
            
            postDao.insertPosts(postEntities)
            
            emit(Resource.Success(postEntities))
        } catch (e: Exception) {
            val localPosts = postDao.getPostsByUserOneTime(userId)
            emit(Resource.Success(localPosts))
        }
    }
    
    // Search posts - gọi API, fallback local nếu offline
    // Approach: Online search API (toàn bộ), Offline search local cache
    fun searchPosts(query: String): Flow<Resource<List<PostEntity>>> = flow {
        val trimmed = query.trim()
        emit(Resource.Loading())
        
        if (trimmed.isEmpty()) {
            emit(Resource.Success(emptyList()))
            return@flow
        }
        
        try {
            try {
                val apiResponse = api.searchPosts(trimmed, limit = 100, skip = 0)
                val apiResults = apiResponse.posts
                    .filter { post ->
                        val q = trimmed.lowercase()
                        post.title?.lowercase()?.contains(q) == true
                    }
                    .toEntityList()
                
                if (apiResults.isNotEmpty()) {
                    postDao.insertPosts(apiResults)
                }
                
                emit(Resource.Success(apiResults))
            } catch (apiError: Exception) {
                val localResults = postDao.searchPostsOneTime(trimmed)
                
                if (localResults.isNotEmpty()) {
                    emit(Resource.Success(localResults))
                } else {
                    emit(Resource.Error("Không tìm thấy kết quả"))
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Search failed"))
        }
    }
    
    // Lấy tất cả posts từ database (offline-first)
    fun getAllPostsFromDb(): Flow<List<PostEntity>> {
        return postDao.getAllPostsFlow()
    }
    
    // Lấy post theo ID từ database
    fun getPostByIdFromDb(postId: Int): Flow<PostEntity?> {
        return postDao.getPostByIdFlow(postId)
    }
    
    // Lấy posts của user từ database
    fun getPostsByUserFromDb(userId: Int): Flow<List<PostEntity>> {
        return postDao.getPostsByUserFlow(userId)
    }
    
    // phân trang post
    fun getPostsPagingSource(): PagingSource<Int, PostEntity> {
        return postDao.getAllPostsPaging()
    }
    
    // phân trang cho post của một user
    fun getPostsByUserPagingSource(userId: Int): PagingSource<Int, PostEntity> {
        return postDao.getPostsByUserPaging(userId)
    }

     // PagingData flow với RemoteMediator (posts tổng)
    @OptIn(ExperimentalPagingApi::class)
    fun getPostsPagingData(pageSize: Int = 20): Flow<PagingData<PostEntity>> {
        return Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            remoteMediator = PostRemoteMediator(api, database, pageSize),
            pagingSourceFactory = { postDao.getAllPostsPaging() }
        ).flow
    }

     // Tạo bài viết mới qua API, cache vào DB
    fun createPost(
        title: String,
        body: String,
        userId: Int,
        tags: List<String> = emptyList()
    ): Flow<Resource<PostEntity>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.createPost(
                CreatePostRequest(
                    title = title,
                    body = body,
                    userId = userId,
                    tags = tags
                )
            )
            val entity = response.toEntity()
            postDao.insertPost(entity)
            emit(Resource.Success(entity))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Không thể tạo bài viết"))
        }
    }
    
    // Xóa post
    suspend fun deletePost(postId: Int) {
        postDao.deletePostById(postId)
    }
    
    // Xóa tất cả posts của một user
    suspend fun deletePostsByUserId(userId: Int) {
        postDao.deletePostsByUserId(userId)
    }
    
    // Xóa tất cả posts 
    suspend fun clearAllPosts() {
        postDao.deleteAllPosts()
    }
    
    // Đếm số posts của một user
    suspend fun getPostCountByUserId(userId: Int): Int {
        return postDao.getPostCountByUser(userId)
    }
}
