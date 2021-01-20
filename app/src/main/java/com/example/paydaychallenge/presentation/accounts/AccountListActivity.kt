package com.example.paydaychallenge.presentation.accounts

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.paydaychallenge.R
import com.example.paydaychallenge.di.base.BaseActivity
import com.example.paydaychallenge.di.factory.ViewModelProviderFactory
import com.example.paydaychallenge.presentation.transactions.TransactionListActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_transaction_list.*
import javax.inject.Inject

class AccountListActivity : BaseActivity() {
    @Inject
    lateinit var viewModelProviderFactory: ViewModelProviderFactory

    lateinit var viewModel: AccountListViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_list)

        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[AccountListViewModel::class.java]

        setLinearLayoutManager()

        setListeners()
    }

    private fun setLinearLayoutManager() {

        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = linearLayoutManager
    }

    private fun setListeners() {
        viewModel.getTransactions()
        viewModel.accounts.subscribe {
            if (it != null) {
                recyclerView.adapter = AccountsAdapter(it) {
                    startActivity(Intent(this, TransactionListActivity::class.java))
                }
            }
        }.addTo(subscriptions)
    }
}