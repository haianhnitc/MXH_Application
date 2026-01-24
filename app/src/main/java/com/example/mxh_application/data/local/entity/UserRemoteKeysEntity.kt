package com.example.mxh_application.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_remote_keys")
data class UserRemoteKeysEntity(
    @PrimaryKey
    val userId: Int,
    val prevKey: Int?,
    val nextKey: Int?,
    val lastUpdated: Long = System.currentTimeMillis()
)
