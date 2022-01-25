package com.example.qmeter.di.module

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.qmeter.BuildConfig
import com.example.qmeter.di.base.BaseApplication
import com.example.qmeter.service.ApiServices
import com.example.qmeter.utils.encodeCredentials
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


@Module
class NetworkModule {
    @Provides
    fun providesRetrofitInstance(interceptor: Interceptor, loggingInterceptor: HttpLoggingInterceptor): Retrofit {

        val client = OkHttpClient.Builder().addInterceptor(interceptor).addInterceptor(loggingInterceptor).build()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }
    @Provides
    fun providesService(retrofit: Retrofit): ApiServices =
        retrofit.create(ApiServices::class.java)


    @Provides
    fun providesInterceptor(sharedPreferences: SharedPreferences): Interceptor {
        return Interceptor {
            val request: Request = it.request()
            val authenticatedRequest: Request = request.newBuilder()
                .header("Authorization", "Basic ${sharedPreferences.encodeCredentials()}").build()
            it.proceed(authenticatedRequest)
        }
    }

    @Provides
    fun providesLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }
}