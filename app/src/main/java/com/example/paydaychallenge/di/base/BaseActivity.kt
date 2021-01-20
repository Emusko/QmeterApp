package com.example.paydaychallenge.di.base

import androidx.lifecycle.ViewModel
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable

open class BaseActivity : DaggerAppCompatActivity() {
    protected val subscriptions = CompositeDisposable()
}