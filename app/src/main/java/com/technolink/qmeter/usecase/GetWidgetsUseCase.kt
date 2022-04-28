package com.technolink.qmeter.usecase

import com.technolink.qmeter.repository.Repository
import com.technolink.qmeter.service.model.remote.response.GetWidgetsResponseModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GetWidgetsUseCase @Inject constructor(private val repository: Repository) {
    fun execute(
        onSuccess: (data: GetWidgetsResponseModel) -> Unit,
        onError: (t: Throwable) -> Unit,
        subscriptions: CompositeDisposable
    ) {
        repository
            .getWidgets()
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