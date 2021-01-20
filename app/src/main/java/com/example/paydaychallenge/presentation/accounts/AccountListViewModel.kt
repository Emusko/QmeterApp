package com.example.paydaychallenge.presentation.accounts

import com.example.paydaychallenge.di.base.BaseViewModel
import com.example.paydaychallenge.service.model.remote.response.GetAccountResponseModel
import com.example.paydaychallenge.usecase.GetAccountsUseCase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class AccountListViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase
) : BaseViewModel(){
    val accounts = PublishSubject.create<ArrayList<GetAccountResponseModel>>()

    fun getTransactions() {
        getAccountsUseCase.execute().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({ accountsList ->
                if (accountsList != null) {
                    accounts.onNext(accountsList)
                }
            }, {
                it.printStackTrace()
            }, {

            }).addTo(subscriptions)
    }
}