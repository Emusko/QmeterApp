package com.example.paydaychallenge.presentation.authentication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.example.paydaychallenge.R
import com.example.paydaychallenge.di.base.BaseActivity
import com.example.paydaychallenge.di.factory.ViewModelProviderFactory
import com.example.paydaychallenge.presentation.main.MainActivity
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
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(R.string.error_occured)
                    builder.setMessage(R.string.password_too_small)
                    builder.setPositiveButton(
                        R.string.ok
                    ) { _, _ ->

                    }

                    builder.setCancelable(false)
                    builder.create().show()
                }
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.error_occured)
                builder.setMessage(R.string.fill_email_or_password)
                builder.setPositiveButton(
                    R.string.ok
                ) { _, _ ->

                }

                builder.setCancelable(false)
                builder.create().show()
            }
        }
        viewModel.authentication.subscribe {
            if (it != null) {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }.addTo(subscriptions)
    }
}
