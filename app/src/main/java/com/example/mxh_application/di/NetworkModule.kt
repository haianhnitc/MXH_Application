package com.example.mxh_application.di

import com.example.mxh_application.data.remote.api.DummyJsonApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private const val BASE_URL = "https://dummyjson.com/"
    
    @Singleton
    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient() // cho phép JSON format không chuẩn
            .create()
    }
    
    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }) // Interceptor thêm vào để hiện log chi tiết khi gọi API phục vụ debug
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(
        gson: Gson,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // có logging của http request
            .addConverterFactory(GsonConverterFactory.create(gson)) 
            .build()
    }
    

    @Singleton
    @Provides
    fun provideDummyJsonApi(retrofit: Retrofit): DummyJsonApi {
        return retrofit.create(DummyJsonApi::class.java)
    }
}
