package com.example.paydaychallenge.di.module

import com.example.paydaychallenge.presentation.main.MainActivity
import com.example.paydaychallenge.presentation.auth.AuthenticateActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {
    @ContributesAndroidInjector(modules = [ViewModelModule::class])
    abstract fun contributeMainActivity(): MainActivity
    @ContributesAndroidInjector(modules = [ViewModelModule::class])
    abstract fun contributeAuthenticateActivity(): AuthenticateActivity
}