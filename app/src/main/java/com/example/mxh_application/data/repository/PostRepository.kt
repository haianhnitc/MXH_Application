package com.example.mxh_application.data.repository

import com.example.mxh_application.data.local.AppDatabase
import com.example.mxh_application.data.local.dao.PostDao
import com.example.mxh_application.data.local.entity.PostEntity
import com.example.mxh_application.data.mapper.toEntity
import com.example.mxh_application.data.mapper.toEntityList
import com.example.mxh_application.data.remote.api.DummyJsonApi
import com.example.mxh_application.data.remote.dto.CreatePostRequest
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
            val cachedListPost = postDao.getAllPosts()
            if(cachedListPost.isNotEmpty()) {
                emit(Resource.Success(cachedListPost))
            } else {
                emit(Resource.Error(
                    message = e.localizedMessage ?: "Có lỗi xảy ra"
                ))
            }
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
            if(localPosts.isNotEmpty()) {
                emit(Resource.Success(localPosts))
            } else {
                emit(Resource.Error(message = e.localizedMessage ?: "Có lỗi xảy ra"))
            }
        }
    }
    
    // Search posts - gọi API, fallback local nếu offline
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
    
    // Lấy posts của user từ database
    fun getPostsByUserFromDb(userId: Int): Flow<List<PostEntity>> {
        return postDao.getPostsByUserFlow(userId)
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
    
    // Xóa tất cả posts
    suspend fun clearAllPosts() {
        postDao.deleteAllPosts()
    }
    
    // Đếm số posts của một user
    suspend fun getPostCountByUserId(userId: Int): Int {
        return postDao.getPostCountByUser(userId)
    }
}
