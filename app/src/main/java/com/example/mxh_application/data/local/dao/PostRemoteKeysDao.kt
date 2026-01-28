package com.example.mxh_application.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mxh_application.data.local.entity.PostRemoteKeysEntity

@Dao
interface PostRemoteKeysDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKeys: List<PostRemoteKeysEntity>)
    
    @Query("SELECT * FROM post_remote_keys WHERE postId = :postId")
    suspend fun getRemoteKeyByPostId(postId: Int): PostRemoteKeysEntity?
    
    @Query("DELETE FROM post_remote_keys")
    suspend fun clearAll() : Int

}
