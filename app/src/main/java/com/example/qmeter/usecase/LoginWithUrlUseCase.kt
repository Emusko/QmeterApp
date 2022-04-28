package com.example.qmeter.usecase

import com.example.qmeter.repository.Repository
import com.example.qmeter.service.model.remote.response.AuthenticationResponseModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class LoginWithUrlUseCase @Inject constructor(private val repository: Repository) {
    fun execute(
        username: String,
        password: String,
        url: String,
        onSuccess: (data: AuthenticationResponseModel) -> Unit,
        onError: (t: Throwable) -> Unit,
        subscriptions: CompositeDisposable
    ) {
        repository
            .login("${url}/api/v1/template/device/login/", username, password)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onSuccess(it)
            }, {
                it.printStackTrace()
                onError(it)
            }, {

            }).addTo(subscriptions)
    }
}