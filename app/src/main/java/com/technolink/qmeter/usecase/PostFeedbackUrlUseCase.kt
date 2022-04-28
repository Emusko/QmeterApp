package com.technolink.qmeter.usecase

import com.technolink.qmeter.repository.Repository
import com.technolink.qmeter.service.model.remote.response.AuthenticationResponseModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class PostFeedbackUrlUseCase @Inject constructor(private val repository: Repository) {
    fun execute(
        url: String,
        body: ArrayList<HashMap<String?, Any?>>,
        onSuccess: (data: AuthenticationResponseModel) -> Unit,
        onError: (t: Throwable) -> Unit,
        subscriptions: CompositeDisposable
    ) {
        repository
            .postFeedback(url, body)
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