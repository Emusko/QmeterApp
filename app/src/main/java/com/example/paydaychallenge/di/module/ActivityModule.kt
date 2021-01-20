package com.example.paydaychallenge.di.module

import com.example.paydaychallenge.presentation.accounts.AccountListActivity
import com.example.paydaychallenge.presentation.authentication.AuthenticateActivity
import com.example.paydaychallenge.presentation.monthly_expense.MonthlyExpenseActivity
import com.example.paydaychallenge.presentation.transactions.TransactionListActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {
    @ContributesAndroidInjector(modules = [ViewModelModule::class])
    abstract fun authenticateActivity(): AuthenticateActivity

    @ContributesAndroidInjector(modules = [ViewModelModule::class])
    abstract fun transactionListActivity(): TransactionListActivity

    @ContributesAndroidInjector(modules = [ViewModelModule::class])
    abstract fun accountListActivity(): AccountListActivity

    @ContributesAndroidInjector(modules = [ViewModelModule::class])
    abstract fun monthlyExpenseActivity(): MonthlyExpenseActivity
}