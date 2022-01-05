package com.example.qmeter.di.base

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

open class BaseViewModel : ViewModel() {
    protected val subscriptions = CompositeDisposable()
    val error = PublishSubject.create<String>()
}