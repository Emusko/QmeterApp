package com.example.paydaychallenge.presentation.transactions

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.paydaychallenge.R
import com.example.paydaychallenge.di.base.BaseActivity
import com.example.paydaychallenge.di.factory.ViewModelProviderFactory
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_transaction_list.*
import javax.inject.Inject

class TransactionListActivity : BaseActivity() {
    @Inject
    lateinit var viewModelProviderFactory: ViewModelProviderFactory

    lateinit var viewModel: TransactionListViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_list)

        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[TransactionListViewModel::class.java]

        setLinearLayoutManager()

        setListeners()
    }

    private fun setLinearLayoutManager() {

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = linearLayoutManager
    }

    private fun setListeners() {
        viewModel.getTransactions()
        viewModel.transactions.subscribe {
            if (it != null) {
                it.sortByDescending { transaction -> transaction.date }
                recyclerView.adapter = TransactionsAdapter(it, this)
            }
        }.addTo(subscriptions)
    }
}