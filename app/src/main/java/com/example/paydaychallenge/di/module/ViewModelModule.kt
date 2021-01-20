package com.example.paydaychallenge.di.module

import androidx.lifecycle.ViewModel
import com.example.paydaychallenge.di.ViewModelKey
import com.example.paydaychallenge.presentation.accounts.AccountListViewModel
import com.example.paydaychallenge.presentation.authentication.AuthenticateViewModel
import com.example.paydaychallenge.presentation.monthly_expense.MonthlyExpenseViewModel
import com.example.paydaychallenge.presentation.transactions.TransactionListActivity
import com.example.paydaychallenge.presentation.transactions.TransactionListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(AuthenticateViewModel::class)
    abstract fun bindMainViewModel(viewModel: AuthenticateViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TransactionListViewModel::class)
    abstract fun bindTransactionListViewModel(viewModel: TransactionListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AccountListViewModel::class)
    abstract fun bindAccountListViewModel(viewModel: AccountListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MonthlyExpenseViewModel::class)
    abstract fun bindMonthlyExpenseViewModel(viewModel: MonthlyExpenseViewModel): ViewModel
}