package com.technolink.qmeter.di.module

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides

@Module
class BaseModule {

    @Provides
    fun provideSharedPreference(application: Application): SharedPreferences =
        application.applicationContext.getSharedPreferences("qmeter.app", Context.MODE_PRIVATE)
}