package com.example.mxh_application.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mxh_application.data.local.entity.UserRemoteKeysEntity

@Dao
interface UserRemoteKeysDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKeys: List<UserRemoteKeysEntity>)
    
    @Query("SELECT * FROM user_remote_keys WHERE userId = :userId")
    suspend fun getRemoteKeyByUserId(userId: Int): UserRemoteKeysEntity?
    
    @Query("DELETE FROM user_remote_keys")
    suspend fun clearAll() : Int
    
}
