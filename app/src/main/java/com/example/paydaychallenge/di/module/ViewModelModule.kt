package com.example.paydaychallenge.di.module

import androidx.lifecycle.ViewModel
import com.example.paydaychallenge.di.ViewModelKey
import com.example.paydaychallenge.presentation.main.MainViewModel
import com.example.paydaychallenge.presentation.auth.AuthenticateViewModel
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