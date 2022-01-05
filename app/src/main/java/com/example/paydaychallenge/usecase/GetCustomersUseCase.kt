package com.example.paydaychallenge.usecase

import com.example.paydaychallenge.repository.Repository
import com.example.paydaychallenge.service.model.remote.request.AuthenticateRequestModel
import com.example.paydaychallenge.service.model.remote.response.AuthenticationResponseModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GetCustomersUseCase @Inject constructor(private val repository: Repository) {
    fun execute(
        username: String,
        password: String,
        onSuccess: (data: AuthenticationResponseModel) -> Unit,
        onError: (t: Throwable) -> Unit,
        subscriptions: CompositeDisposable
    ) {
        repository
            .getComponents(username, password)
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