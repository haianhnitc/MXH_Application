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
    
    // Lấy tất cả posts với Paging 3
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPostsPaging(): PagingSource<Int, PostEntity>
    
     // Lấy tất cả posts dạng Flow
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun getAllPostsFlow(): Flow<List<PostEntity>>
    
     // Lấy post theo id
    @Query("SELECT * FROM posts WHERE id = :postId")
    suspend fun getPostById(postId: Int): PostEntity?
    
     // Lấy post theo id dạng Flow
    @Query("SELECT * FROM posts WHERE id = :postId")
    fun getPostByIdFlow(postId: Int): Flow<PostEntity?>
    
     // Lấy tất cả posts của một user với Paging 3
    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPostsByUserPaging(userId: Int): PagingSource<Int, PostEntity>
    
    // Lấy tất cả posts của một user dạng Flow
    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPostsByUserFlow(userId: Int): Flow<List<PostEntity>>

    // Lấy tất cả posts của một user (one-shot)
    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getPostsByUserOneTime(userId: Int): List<PostEntity>
    
     // Tìm kiếm posts theo tiêu đề (PagingSource)
    @Query("""
        SELECT * FROM posts 
        WHERE title LIKE '%' || :query || '%' 
        ORDER BY createdAt DESC
    """)
    fun searchPosts(query: String): PagingSource<Int, PostEntity>

     // Tìm kiếm posts một lần - chỉ theo title (offline fallback)
    @Query("""
        SELECT * FROM posts 
        WHERE title LIKE '%' || :query || '%' 
        ORDER BY createdAt DESC
    """)
    suspend fun searchPostsOneTime(query: String): List<PostEntity>
    
     // Lấy posts theo tag
    @Query("""
        SELECT * FROM posts 
        WHERE tags LIKE '%' || :tag || '%'
        ORDER BY createdAt DESC
    """)
    fun getPostsByTag(tag: String): PagingSource<Int, PostEntity>
    
     // Insert một post
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)
    
     // Insert danh sách posts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)
    
    // Xóa post theo id
    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePostById(postId: Int) : Int

    // Xóa tất cả posts
    @Query("DELETE FROM posts")
    suspend fun clearAll() : Int

    // Xóa tất cả posts của một user
    @Query("DELETE FROM posts WHERE userId = :userId")
    suspend fun clearPostsByUser(userId: Int)

    // Alias cho clearAll để tương thích repository cũ
    @Query("DELETE FROM posts")
    suspend fun deleteAllPosts() : Int

    // Alias cho clearPostsByUser
    @Query("DELETE FROM posts WHERE userId = :userId")
    suspend fun deletePostsByUserId(userId: Int) : Int
    
     // Đếm số lượng posts
    @Query("SELECT COUNT(*) FROM posts")
    suspend fun getPostCount(): Int
    
     // Đếm số lượng posts của một user
    @Query("SELECT COUNT(*) FROM posts WHERE userId = :userId")
    suspend fun getPostCountByUser(userId: Int): Int
}
