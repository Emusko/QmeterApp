package com.example.qmeter.di.module

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.qmeter.di.base.BaseApplication
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class BaseModule {

    @Provides
    fun provideSharedPreference(application: Application): SharedPreferences =
        application.applicationContext.getSharedPreferences("qmeter.app", Context.MODE_PRIVATE)
}