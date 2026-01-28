package com.example.mxh_application.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.example.mxh_application.data.local.AppDatabase
import com.example.mxh_application.data.local.dao.UserDao
import com.example.mxh_application.data.local.entity.UserEntity
import com.example.mxh_application.data.local.relation.UserWithPosts
import com.example.mxh_application.data.mapper.toEntity
import com.example.mxh_application.data.mapper.toEntityList
import com.example.mxh_application.data.remote.api.DummyJsonApi
import com.example.mxh_application.data.remote.mediator.UserRemoteMediator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserRepository @Inject constructor(
    private val api: DummyJsonApi,
    private val userDao: UserDao,
    private val database: AppDatabase
) {
    
     // Lấy tất cả users từ API và cache vào database
    fun fetchAndCacheUsers(
        limit: Int = 20,
        skip: Int = 0
    ): Flow<Resource<List<UserEntity>>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = api.getAllUsers(limit, skip)
            val userEntities = response.users.toEntityList()
            userDao.insertUsers(userEntities)

            emit(Resource.Success(userEntities))
        } catch (e: Exception) {
            val cachedUsers = userDao.getAllUsersOneTime()
            if(cachedUsers.isNotEmpty()) {
                emit(Resource.Success(cachedUsers))
            } else {
                emit(Resource.Error(
                    message = e.localizedMessage ?: "Có lỗi xảy ra",
                    data = null
                ))
            }
        }
    }
    
    // Lấy user theo ID từ API và cache
    fun fetchAndCacheUserById(userId: Int): Flow<Resource<UserEntity>> = flow {
        emit(Resource.Loading())
        
        try {
            val userResponse = api.getUserById(userId)
            val userEntity = userResponse.toEntity()
            userDao.insertUser(userEntity)
            
            emit(Resource.Success(userEntity))
        } catch (e: Exception) {
            val cachedUser = userDao.getUserById(userId)
            if (cachedUser != null) {
                emit(Resource.Success(cachedUser))
            } else {
                emit(Resource.Error(e.localizedMessage ?: "User không tồn tại"))
            }
        }
    }
    
    // Search users - gọi API, fallback local nếu offline
    fun searchUsers(query: String): Flow<Resource<List<UserEntity>>> = flow {
        val trimmed = query.trim()
        emit(Resource.Loading())
        
        if (trimmed.isEmpty()) {
            emit(Resource.Success(emptyList()))
            return@flow
        }
        
        try {
            try {
                val apiResponse = api.searchUsers(trimmed, limit = 100, skip = 0)
                val apiResults = apiResponse.users
                    .filter { user ->
                        val q = trimmed.lowercase()
                        (user.firstName?.lowercase()?.contains(q) == true) ||
                        (user.lastName?.lowercase()?.contains(q) == true)
                    }
                    .toEntityList()
                
                if (apiResults.isNotEmpty()) {
                    userDao.insertUsers(apiResults)
                }
                
                emit(Resource.Success(apiResults))
            } catch (apiError: Exception) {
                val localResults = userDao.searchUsersOneTime(trimmed)
                
                if (localResults.isNotEmpty()) {
                    emit(Resource.Success(localResults))
                } else {
                    emit(Resource.Error("Không tìm thấy kết quả"))
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Có lỗi khi tìm kiếm, vui lòng thử lại"))
        }
    }
    
    // Lấy tất cả users từ database (offline-first)
    fun getAllUsersFromDb(): Flow<List<UserEntity>> {
        return userDao.getAllUsersFlow()
    }
    
    // Xóa tất cả users (clear cache)
    suspend fun clearAllUsers() {
        userDao.deleteAllUsers()
    }
}
