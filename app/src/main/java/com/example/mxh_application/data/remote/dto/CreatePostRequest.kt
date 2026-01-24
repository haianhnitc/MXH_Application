package com.example.mxh_application.data.remote.dto

import com.google.gson.annotations.SerializedName

// request body để tạo post mới
data class CreatePostRequest(
    @SerializedName("title")
    val title: String,
    @SerializedName("body")
    val body: String,
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("tags")
    val tags: List<String> = emptyList()
    // chỗ này luôn cho tags là mảng rỗng để luôn truyền lên khi gọi API tạo post
)
