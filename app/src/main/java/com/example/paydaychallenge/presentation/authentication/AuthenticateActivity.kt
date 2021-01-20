package com.example.paydaychallenge.presentation.authentication

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.paydaychallenge.R
import com.example.paydaychallenge.di.base.BaseActivity
import com.example.paydaychallenge.di.factory.ViewModelProviderFactory
import com.example.paydaychallenge.presentation.main.MainActivity
import com.example.paydaychallenge.utils.PayDayDialog
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_authenticate.*
import javax.inject.Inject

class AuthenticateActivity : BaseActivity() {

    @Inject
    lateinit var factory: ViewModelProviderFactory

    lateinit var viewModel: AuthenticateViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authenticate)
        viewModel =
            ViewModelProvider(this, factory)[AuthenticateViewModel::class.java]

        setListeners()
    }

    private fun setListeners() {
        login.setOnClickListener {
            if (email.text.toString().trim().isNotEmpty() && password.text.toString().trim()
                    .isNotEmpty()
            ) {
                if (password.text.toString().trim().length >= 6) {
                    viewModel.authenticateUser(email.text.toString(), password.text.toString())
                } else {
                    PayDayDialog().getPayDayDialog(this, resources.getString(R.string.error_occured), resources.getString(R.string.password_too_small))
                }
            } else {

                PayDayDialog().getPayDayDialog(this, resources.getString(R.string.error_occured), resources.getString(R.string.fill_email_or_password))
            }
        }
        viewModel.authentication.subscribe {
            if (it != null) {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }.addTo(subscriptions)
    }
}
