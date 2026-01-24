package com.example.mxh_application.data.remote.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.mxh_application.data.local.AppDatabase
import com.example.mxh_application.data.local.entity.UserEntity
import com.example.mxh_application.data.local.entity.UserRemoteKeysEntity
import com.example.mxh_application.data.mapper.toEntityList
import com.example.mxh_application.data.remote.api.DummyJsonApi

@OptIn(ExperimentalPagingApi::class)
class UserRemoteMediator(
    private val api: DummyJsonApi,
    private val db: AppDatabase,
    private val pageSize: Int = 20
) : RemoteMediator<Int, UserEntity>() {

    private val userDao = db.userDao()
    private val remoteKeysDao = db.userRemoteKeysDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, UserEntity>
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

            val apiResponse = api.getAllUsers(limit = limit, skip = skip)
            val users = apiResponse.users.toEntityList()
            val endOfPaginationReached = users.isEmpty() || skip + users.size >= apiResponse.total

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeysDao.clearAll()
                    userDao.clearAll()
                }
                val prevKey = if (skip == 0) null else (skip - limit).coerceAtLeast(0)
                val nextKey = if (endOfPaginationReached) null else skip + users.size
                val keys = users.map { user ->
                    UserRemoteKeysEntity(
                        userId = user.id,
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                }
                remoteKeysDao.insertAll(keys)
                userDao.insertUsers(users)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, UserEntity>): UserRemoteKeysEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { user ->
            remoteKeysDao.getRemoteKeyByUserId(user.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, UserEntity>): UserRemoteKeysEntity? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { user ->
            remoteKeysDao.getRemoteKeyByUserId(user.id)
        }
    }
}
