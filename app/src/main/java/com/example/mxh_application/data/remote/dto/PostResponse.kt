package com.example.mxh_application.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PostResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("body")
    val body: String,
    
    @SerializedName("tags")
    val tags: List<String>,
    
    @SerializedName("reactions")
    val reactions: ReactionsResponse? = null,
    
    @SerializedName("views")
    val views: Int? = 0,
    
    @SerializedName("userId")
    val userId: Int
)

data class ReactionsResponse(
    @SerializedName("likes")
    val likes: Int,
    
    @SerializedName("dislikes")
    val dislikes: Int
)
