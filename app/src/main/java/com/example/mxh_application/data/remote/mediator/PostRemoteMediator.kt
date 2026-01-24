package com.example.mxh_application.data.remote.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.mxh_application.data.local.AppDatabase
import com.example.mxh_application.data.local.entity.PostEntity
import com.example.mxh_application.data.local.entity.PostRemoteKeysEntity
import com.example.mxh_application.data.mapper.toEntityList
import com.example.mxh_application.data.remote.api.DummyJsonApi

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val api: DummyJsonApi,
    private val db: AppDatabase,
    private val pageSize: Int = 20
) : RemoteMediator<Int, PostEntity>() {

    private val postDao = db.postDao()
    private val remoteKeysDao = db.postRemoteKeysDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        return try {
            val limit = state.config.pageSize.coerceAtLeast(pageSize)
            val skip = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> {
                    val remoteKeys = getRemoteKeyForFirstItem(state)
                    val prevKey = remoteKeys?.prevKey
                    if (prevKey == null) return MediatorResult.Success(endOfPaginationReached = true)
                    prevKey
                }
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextKey = remoteKeys?.nextKey
                    if (nextKey == null) return MediatorResult.Success(endOfPaginationReached = true)
                    nextKey
                }
            }

            val apiResponse = api.getAllPosts(limit = limit, skip = skip)
            val posts = apiResponse.posts.toEntityList()
            val endOfPaginationReached = posts.isEmpty() || skip + posts.size >= apiResponse.total

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeysDao.clearAll()
                    postDao.clearAll()
                }
                val prevKey = if (skip == 0) null else (skip - limit).coerceAtLeast(0)
                val nextKey = if (endOfPaginationReached) null else skip + posts.size
                val keys = posts.map { post ->
                    PostRemoteKeysEntity(
                        postId = post.id,
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                }
                remoteKeysDao.insertAll(keys)
                postDao.insertPosts(posts)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, PostEntity>): PostRemoteKeysEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { post ->
            remoteKeysDao.getRemoteKeyByPostId(post.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, PostEntity>): PostRemoteKeysEntity? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { post ->
            remoteKeysDao.getRemoteKeyByPostId(post.id)
        }
    }
}
