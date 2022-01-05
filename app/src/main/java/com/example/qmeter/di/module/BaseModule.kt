package com.example.qmeter.di.module

import com.example.qmeter.di.base.BaseApplication
import dagger.Module
import dagger.Provides

@Module
class BaseModule {
    @Provides
    fun provideActivity() = BaseApplication()
}