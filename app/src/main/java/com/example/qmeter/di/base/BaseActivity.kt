package com.example.qmeter.di.base

import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable

open class BaseActivity : DaggerAppCompatActivity() {
    protected val subscriptions = CompositeDisposable()
}