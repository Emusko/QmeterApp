package com.technolink.qmeter.presentation.auth

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import com.technolink.qmeter.di.base.BaseActivity
import com.technolink.qmeter.di.factory.ViewModelProviderFactory
import com.technolink.qmeter.presentation.main.MainActivity
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.set_domain_view.view.*
import technolink.qmeter.R
import technolink.qmeter.databinding.ActivityAuthenticateBinding
import javax.inject.Inject

class AuthenticateActivity : BaseActivity() {
    private lateinit var binding: ActivityAuthenticateBinding

    @Inject
    lateinit var factory: ViewModelProviderFactory

    private val username: String?
        get() = binding.usernameEdittext.text?.toString()?.trim()

    private val password: String?
        get() = binding.passwordEdittext.text?.toString()?.trim()

    private val viewModel: AuthenticateViewModel by viewModels { factory }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticateBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        checkUser()

        setListeners()
    }

    private fun checkUser() {
        if (!viewModel.sharedPreferences.getString("token", "").isNullOrEmpty()) {
            viewModel.getWidgets()
        } else {
            binding.progressBar.visibility = View.GONE
            binding.credentialContainer.visibility = View.VISIBLE
        }
    }

    private fun setListeners() {
        viewModel.viewData.observe(this) {
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("pagesResponse", it)
            })

            binding.progressBar.visibility = View.GONE
            binding.credentialContainer.visibility = View.VISIBLE
        }

        viewModel.error.subscribe {
            binding.progressBar.visibility = View.GONE
            binding.credentialContainer.visibility = View.VISIBLE
//            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }.addTo(subscriptions)

        binding.login.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.credentialContainer.visibility = View.GONE
            viewModel.getComponents(username ?: "", password ?: "")
        }
        binding.customUrl.setOnClickListener {
            popSetUrl()
        }
    }

    private fun popSetUrl() {
        val dialog = AlertDialog.Builder(this)
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.set_domain_view, null)
        dialog.setView(dialogView)
        val alertDialog = dialog.create()
        dialogView.base_url.setText(viewModel.sharedPreferences.getString("baseUrl", null))
        dialogView.set_url.setOnClickListener {
            viewModel.saveBaseUrl(dialogView.base_url.text.toString())
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptions.clear()

        binding.progressBar.visibility = View.GONE
        binding.credentialContainer.visibility = View.VISIBLE
    }
}