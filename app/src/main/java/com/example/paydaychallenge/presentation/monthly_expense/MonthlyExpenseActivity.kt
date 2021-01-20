package com.example.paydaychallenge.presentation.monthly_expense

import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.paydaychallenge.R
import com.example.paydaychallenge.di.base.BaseActivity
import com.example.paydaychallenge.di.factory.ViewModelProviderFactory
import com.example.paydaychallenge.presentation.transactions.TransactionsAdapter
import com.example.paydaychallenge.service.model.remote.response.GetTransactionResponseModel
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_monthly_expense.*
import kotlinx.android.synthetic.main.date_pick_dialog_view.view.*
import java.util.*
import javax.inject.Inject


class MonthlyExpenseActivity : BaseActivity() {
    @Inject
    lateinit var factory: ViewModelProviderFactory

    lateinit var viewModel: MonthlyExpenseViewModel

    lateinit var adapter: TransactionsAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly_expense)

        viewModel =
            ViewModelProvider(this, factory)[MonthlyExpenseViewModel::class.java]

        setLinearLayoutManager()

        setListeners()
    }

    private fun setLinearLayoutManager() {
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = linearLayoutManager
    }

    private fun setListeners() {
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.filter?.filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    viewModel.getTransactions()
                } else {
                    adapter.filter?.filter(newText)
                }
                return true
            }

        })
        viewModel.getTransactions()
        viewModel.transactions.subscribe {
            if (it != null) {
                adapter = TransactionsAdapter(it, this)
                recyclerView.adapter = adapter
            }
        }.addTo(subscriptions)
        searchButton.setOnClickListener {
            openCustomDatePickingDialog()
        }
    }

    private fun openCustomDatePickingDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.select_searched_month)
        val view = layoutInflater
            .inflate(R.layout.date_pick_dialog_view, null)
        val monthAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.months)
        )
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        view.monthSpinner.adapter = monthAdapter
        val yearAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.years)
        )
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        view.yearSpinner.adapter = yearAdapter
        builder.setView(view)

        builder.setCancelable(false)
        builder.setPositiveButton(
            R.string.search
        ) { _, _ ->
            adapter = TransactionsAdapter(
                viewModel.getTransactions(
                    view.yearSpinner.selectedItem.toString().toInt(),
                    view.monthSpinner.selectedItemPosition + 1,
                    (viewModel.transactions.value as ArrayList<GetTransactionResponseModel>)),
                    this
                )
                        recyclerView . adapter = adapter
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()

    }
}