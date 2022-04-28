package com.technolink.qmeter.di.module

import androidx.lifecycle.ViewModel
import com.technolink.qmeter.di.ViewModelKey
import com.technolink.qmeter.presentation.main.MainViewModel
import com.technolink.qmeter.presentation.auth.AuthenticateViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(viewModel: MainViewModel): ViewModel
    @Binds
    @IntoMap
    @ViewModelKey(AuthenticateViewModel::class)
    abstract fun bindAuthenticateViewModel(viewModel: AuthenticateViewModel): ViewModel
}