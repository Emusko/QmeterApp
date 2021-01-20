package com.example.paydaychallenge.di.base

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

open class BaseViewModel : ViewModel() {
    protected val subscriptions = CompositeDisposable()
}