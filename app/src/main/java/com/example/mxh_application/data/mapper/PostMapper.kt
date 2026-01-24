package com.example.mxh_application.data.mapper

import com.example.mxh_application.data.local.entity.PostEntity
import com.example.mxh_application.data.local.entity.Reactions
import com.example.mxh_application.data.remote.dto.PostResponse

fun PostResponse.toEntity(): PostEntity {
    return PostEntity(
        id = id,
        title = title,
        body = body,
        tags = tags,
        reactions = Reactions(
            likes = reactions?.likes ?: 0,
            dislikes = reactions?.dislikes ?: 0
        ),
        views = views ?: 0,
        userId = userId,
        lastUpdated = System.currentTimeMillis()
    )
}

fun List<PostResponse>.toEntityList(): List<PostEntity> {
    return map { it.toEntity() }
}
