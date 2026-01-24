package com.example.mxh_application.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "post_remote_keys")
data class PostRemoteKeysEntity(
    @PrimaryKey
    val postId: Int,
    val prevKey: Int?,
    val nextKey: Int?,
    val lastUpdated: Long = System.currentTimeMillis()
)
