package com.example.mxh_application.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mxh_application.data.local.converter.TagsConverter
import com.example.mxh_application.data.local.dao.PostDao
import com.example.mxh_application.data.local.dao.PostRemoteKeysDao
import com.example.mxh_application.data.local.dao.UserDao
import com.example.mxh_application.data.local.dao.UserRemoteKeysDao
import com.example.mxh_application.data.local.entity.PostEntity
import com.example.mxh_application.data.local.entity.PostRemoteKeysEntity
import com.example.mxh_application.data.local.entity.UserEntity
import com.example.mxh_application.data.local.entity.UserRemoteKeysEntity

@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        UserRemoteKeysEntity::class,
        PostRemoteKeysEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(TagsConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun userRemoteKeysDao(): UserRemoteKeysDao
    abstract fun postRemoteKeysDao(): PostRemoteKeysDao
}
