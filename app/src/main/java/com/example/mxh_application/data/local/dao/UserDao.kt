package com.example.mxh_application.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.mxh_application.data.local.entity.UserEntity
import com.example.mxh_application.data.local.relation.UserWithPosts
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
     // Lấy tất cả users với Paging 3
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsersPaging(): PagingSource<Int, UserEntity>
    
     // Lấy tất cả users dạng Flow
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>
    
     // Lấy user theo id
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?
    
     // Lấy user theo id dạng Flow
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdFlow(userId: Int): Flow<UserEntity?>
    
     // Tìm kiếm users theo firstName hoặc lastName (PagingSource)
    @Query("""
        SELECT * FROM users 
        WHERE firstName LIKE '%' || :query || '%' 
        OR lastName LIKE '%' || :query || '%'
        ORDER BY firstName ASC
    """)
    fun searchUsers(query: String): PagingSource<Int, UserEntity>

     // Tìm kiếm users một lần - chỉ theo firstName và lastName (offline fallback)
    @Query("""
        SELECT * FROM users 
        WHERE firstName LIKE '%' || :query || '%' 
        OR lastName LIKE '%' || :query || '%'
        ORDER BY firstName ASC
    """)
    suspend fun searchUsersOneTime(query: String): List<UserEntity>
    
     // Lấy user kèm theo posts của họ
    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserWithPosts(userId: Int): UserWithPosts?
    
     // Lấy user kèm theo posts dạng Flow
    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserWithPostsFlow(userId: Int): Flow<UserWithPosts?>
    
     // Insert một user
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
     // Insert danh sách users
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
    
     // Cập nhật số lượng posts của user
    @Query("UPDATE users SET postCount = :count WHERE id = :userId")
    suspend fun updatePostCount(userId: Int, count: Int)
    
    // Xóa user theo id
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Int)

    // Xóa tất cả users
    @Query("DELETE FROM users")
    suspend fun clearAll() : Int

    // Alias cho clearAll để tương thích repository cũ
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers() : Int
    
     // Đếm số lượng users
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
