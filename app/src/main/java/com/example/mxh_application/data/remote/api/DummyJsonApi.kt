package com.example.mxh_application.data.remote.api

import com.example.mxh_application.data.remote.dto.CreatePostRequest
import com.example.mxh_application.data.remote.dto.PaginatedPostsResponse
import com.example.mxh_application.data.remote.dto.PaginatedUsersResponse
import com.example.mxh_application.data.remote.dto.PostResponse
import com.example.mxh_application.data.remote.dto.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DummyJsonApi {

    @GET("users")
    suspend fun getAllUsers(
        @Query("limit") limit: Int = 20,
        @Query("skip") skip: Int = 0
    ) : PaginatedUsersResponse
    
    @GET("users/{id}")
    suspend fun getUserById(
        @Path("id") userId: Int
    ): UserResponse
    
    @GET("users/search")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("skip") skip: Int = 0
    ): PaginatedUsersResponse
    
    @GET("posts")
    suspend fun getAllPosts(
        @Query("limit") limit: Int = 20,
        @Query("skip") skip: Int = 0
    ): PaginatedPostsResponse
    
    @GET("posts/{id}")
    suspend fun getPostById(
        @Path("id") postId: Int
    ): PostResponse
    
    @GET("posts/user/{userId}")
    suspend fun getPostsByUserId(
        @Path("userId") userId: Int,
        @Query("limit") limit: Int = 20,
        @Query("skip") skip: Int = 0
    ): PaginatedPostsResponse
    
    @GET("posts/search")
    suspend fun searchPosts(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("skip") skip: Int = 0
    ): PaginatedPostsResponse

    @POST("posts/add")
    suspend fun createPost(
        @Body request: CreatePostRequest
    ): PostResponse
}
