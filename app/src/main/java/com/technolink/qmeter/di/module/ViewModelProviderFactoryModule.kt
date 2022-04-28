package com.technolink.qmeter.di.module

import androidx.lifecycle.ViewModelProvider
import com.technolink.qmeter.di.factory.ViewModelProviderFactory
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelProviderFactoryModule {
    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelProviderFactory): ViewModelProvider.Factory
}