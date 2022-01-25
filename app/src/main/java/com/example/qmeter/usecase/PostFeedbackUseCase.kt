package com.example.qmeter.usecase

import com.example.qmeter.repository.Repository
import com.example.qmeter.service.model.remote.response.AuthenticationResponseModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class PostFeedbackUseCase @Inject constructor(private val repository: Repository) {
    fun execute(
        body: ArrayList<HashMap<String?, Any?>>,
        onSuccess: (data: AuthenticationResponseModel) -> Unit,
        onError: (t: Throwable) -> Unit,
        subscriptions: CompositeDisposable
    ) {
        repository
            .postFeedback(body)
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