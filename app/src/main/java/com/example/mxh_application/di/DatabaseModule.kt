package com.example.mxh_application.di

import android.content.Context
import androidx.room.Room
import com.example.mxh_application.data.local.AppDatabase
import com.example.mxh_application.data.local.dao.PostDao
import com.example.mxh_application.data.local.dao.PostRemoteKeysDao
import com.example.mxh_application.data.local.dao.UserDao
import com.example.mxh_application.data.local.dao.UserRemoteKeysDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    private const val DATABASE_NAME = "mxh_database"

    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        )
            // .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun providePostDao(database: AppDatabase): PostDao {
        return database.postDao()
    }

    @Provides
    fun provideUserRemoteKeysDao(database: AppDatabase): UserRemoteKeysDao {
        return database.userRemoteKeysDao()
    }

    @Provides
    fun providePostRemoteKeysDao(database: AppDatabase): PostRemoteKeysDao {
        return database.postRemoteKeysDao()
    }
}
