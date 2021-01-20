package com.example.paydaychallenge.presentation.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.paydaychallenge.R
import com.example.paydaychallenge.presentation.accounts.AccountListActivity
import com.example.paydaychallenge.presentation.monthly_expense.MonthlyExpenseActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        accounts.setOnClickListener {
            startActivity(Intent(this, AccountListActivity::class.java))
        }
        monthly.setOnClickListener {
            startActivity(Intent(this, MonthlyExpenseActivity::class.java))
        }
    }
}