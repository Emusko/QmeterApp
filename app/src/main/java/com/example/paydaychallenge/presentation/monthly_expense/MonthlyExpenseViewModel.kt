package com.example.paydaychallenge.presentation.monthly_expense

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.paydaychallenge.di.base.BaseViewModel
import com.example.paydaychallenge.service.model.remote.response.GetTransactionResponseModel
import com.example.paydaychallenge.usecase.GetTransactionsUseCase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class MonthlyExpenseViewModel @Inject constructor(
    private val transactionsUseCase: GetTransactionsUseCase
) : BaseViewModel(){
    val transactions = BehaviorSubject.create<ArrayList<GetTransactionResponseModel>>()

    fun getTransactions() {
        transactionsUseCase.execute().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({ transactionList ->
                if (transactionList != null) {
                    transactions.onNext(transactionList)
                }
            }, {
                it.printStackTrace()
            }, {

            }).addTo(subscriptions)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getTransactions(year: Int, month: Int, transactions: ArrayList<GetTransactionResponseModel>): ArrayList<GetTransactionResponseModel> {
        val newList = arrayListOf<GetTransactionResponseModel>()
        transactions.forEach {
            val string = it.date.removeSuffix("Z")
            val formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = LocalDate.parse(string, formatter)

            Log.i("Date Year", "YEAR IS ${date.year}, Month IS ${date.monthValue}")
            if (date?.year == year && date.monthValue == month){
                newList.add(it)
            }
        }
        return newList
    }
}