package com.example.paydaychallenge.presentation.authentication

import com.example.paydaychallenge.di.base.BaseViewModel
import com.example.paydaychallenge.service.model.remote.response.AuthenticateResponseModel
import com.example.paydaychallenge.usecase.GetCustomersUseCase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class AuthenticateViewModel @Inject constructor(
    private val getCustomersUseCase: GetCustomersUseCase
) : BaseViewModel() {
    val authentication = PublishSubject.create<AuthenticateResponseModel>()

    fun authenticateUser(email: String, password: String) {
        getCustomersUseCase.execute().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({ users ->
                if (users != null) {
                    for (i in 0 until users.size) {
                        if (users[i].email == email && users[i].password == password) {
                            authentication.onNext(users[i])
                            break
                        }
                    }
                }
            }, {
                it.printStackTrace()
            }, {

            }).addTo(subscriptions)
    }
}