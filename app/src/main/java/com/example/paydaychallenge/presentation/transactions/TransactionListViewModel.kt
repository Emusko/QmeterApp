package com.example.paydaychallenge.presentation.transactions

import com.example.paydaychallenge.di.base.BaseViewModel
import com.example.paydaychallenge.service.model.remote.response.GetTransactionResponseModel
import com.example.paydaychallenge.usecase.GetTransactionsUseCase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class TransactionListViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase
) : BaseViewModel(){
    val transactions = PublishSubject.create<ArrayList<GetTransactionResponseModel>>()

    fun getTransactions() {
        getTransactionsUseCase.execute().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({ transactionList ->
                if (transactionList != null) {
                    transactions.onNext(transactionList)
                }
            }, {
                it.printStackTrace()
            }, {

            }).addTo(subscriptions)
    }
}