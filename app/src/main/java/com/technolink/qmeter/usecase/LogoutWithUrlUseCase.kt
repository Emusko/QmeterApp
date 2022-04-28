package com.technolink.qmeter.usecase

import com.technolink.qmeter.repository.Repository
import com.technolink.qmeter.service.model.remote.response.AuthenticationResponseModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import javax.inject.Inject

class LogoutWithUrlUseCase @Inject constructor(private val repository: Repository) {
    fun execute(
        username: String,
        password: String,
        url: String,
        onSuccess: (data: Response<Void>?) -> Unit,
        onError: (t: Throwable) -> Unit,
        subscriptions: CompositeDisposable
    ) {
        repository
            .logout("${url}/api/v1/template/device/logout/", username, password)
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