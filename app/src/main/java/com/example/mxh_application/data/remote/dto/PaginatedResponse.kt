package com.example.mxh_application.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PaginatedUsersResponse(
    @SerializedName("users")
    val users: List<UserResponse>,
    
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("skip")
    val skip: Int,
    
    @SerializedName("limit")
    val limit: Int
)

data class PaginatedPostsResponse(
    @SerializedName("posts")
    val posts: List<PostResponse>,
    
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("skip")
    val skip: Int,
    
    @SerializedName("limit")
    val limit: Int
)
