package com.example.paydaychallenge.di.module

import com.example.paydaychallenge.di.base.BaseApplication
import dagger.Module
import dagger.Provides

@Module
class BaseModule {
    @Provides
    fun provideActivity() = BaseApplication()
}