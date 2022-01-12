package com.example.qmeter.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import com.example.qmeter.databinding.ActivityAuthenticateBinding
import com.example.qmeter.di.base.BaseActivity
import com.example.qmeter.di.factory.ViewModelProviderFactory
import com.example.qmeter.presentation.main.MainActivity
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class AuthenticateActivity : BaseActivity() {
    private lateinit var binding: ActivityAuthenticateBinding

    @Inject
    lateinit var factory: ViewModelProviderFactory

    private val username: String
    get() = binding.usernameEdittext.text.toString()

    private val password: String
    get() = binding.passwordEdittext.text.toString()

    private val viewModel: AuthenticateViewModel by viewModels { factory }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticateBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        setListeners()
    }

    private fun setListeners() {
        viewModel.viewData.observe(this){
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("pagesResponse", it)
            })
        }

        viewModel.error.subscribe {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }.addTo(subscriptions)

        binding.login.setOnClickListener {
            viewModel.getComponents(username, password)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptions.clear()
    }
}