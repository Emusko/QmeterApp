package com.example.qmeter.di

import android.app.Application
import com.example.qmeter.di.base.BaseApplication
import com.example.qmeter.di.module.ActivityModule
import com.example.qmeter.di.module.BaseModule
import com.example.qmeter.di.module.NetworkModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        BaseModule::class,
        NetworkModule::class,
        ActivityModule::class
    ]
)
interface AppComponent : AndroidInjector<BaseApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }
}