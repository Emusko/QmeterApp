package com.example.paydaychallenge.di.base

import androidx.lifecycle.ViewModel
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

open class BaseActivity : DaggerAppCompatActivity() {
    protected val subscriptions = CompositeDisposable()
}