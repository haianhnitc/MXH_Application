package com.example.mxh_application.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.mxh_application.data.local.entity.PostEntity
import com.example.mxh_application.data.local.entity.UserEntity

// query lấy hết post của 1 user ra
data class UserWithPosts(
    @Embedded
    val user: UserEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val posts: List<PostEntity>
)
