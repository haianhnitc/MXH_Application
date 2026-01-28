package com.example.mxh_application.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mxh_application.data.local.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    // Lấy tất cả các post
    @Query("SELECT * FROM posts ORDER BY id ASC")
    fun getAllPosts(): List<PostEntity>
    
    // Lấy tất cả posts với Paging 3
    @Query("SELECT * FROM posts ORDER BY id ASC")
    fun getAllPostsPaging(): PagingSource<Int, PostEntity>
    
     // Lấy tất cả posts dạng Flow
    @Query("SELECT * FROM posts ORDER BY id ASC")
    fun getAllPostsFlow(): Flow<List<PostEntity>>
    
     // Lấy post theo id
    @Query("SELECT * FROM posts WHERE id = :postId")
    suspend fun getPostById(postId: Int): PostEntity?
    
     // Lấy post theo id dạng Flow
    @Query("SELECT * FROM posts WHERE id = :postId")
    fun getPostByIdFlow(postId: Int): Flow<PostEntity?>
    
    // Lấy tất cả posts của một user dạng Flow
    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY id ASC")
    fun getPostsByUserFlow(userId: Int): Flow<List<PostEntity>>

    // Lấy tất cả posts của một user (one-shot)
    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY id ASC")
    suspend fun getPostsByUserOneTime(userId: Int): List<PostEntity>
    
     // Tìm kiếm posts một lần - chỉ theo title (offline fallback)
    @Query("""
        SELECT * FROM posts 
        WHERE title LIKE '%' || :query || '%' 
        ORDER BY id ASC
    """)
    suspend fun searchPostsOneTime(query: String): List<PostEntity>
    
     // Insert một post
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)
    
     // Insert danh sách posts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    // Xóa tất cả posts
    @Query("DELETE FROM posts")
    suspend fun clearAll() : Int

    // Alias cho clearAll để tương thích repository cũ
    @Query("DELETE FROM posts")
    suspend fun deleteAllPosts() : Int

     // Đếm số lượng posts
    @Query("SELECT COUNT(*) FROM posts")
    suspend fun getPostCount(): Int
    
     // Đếm số lượng posts của một user
    @Query("SELECT COUNT(*) FROM posts WHERE userId = :userId")
    suspend fun getPostCountByUser(userId: Int): Int
}
