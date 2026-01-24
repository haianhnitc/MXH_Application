package com.example.mxh_application.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.mxh_application.data.local.converter.TagsConverter

@Entity(
    tableName = "posts",
    indices = [Index(value = ["userId"])]
)
data class PostEntity(
    @PrimaryKey
    val id: Int,
    
    val title: String,
    val body: String,

    // chỗ này không cần thêm @TypeConverters(TagsConverter::class) vì đã khai báo ở AppDatabaseModule
    val tags: List<String>,
    
    @Embedded(prefix = "reactions_")
    val reactions: Reactions,
    
    val views: Int,
    val userId: Int,
    
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)


data class Reactions(
    val likes: Int = 0,
    val dislikes: Int = 0
)
