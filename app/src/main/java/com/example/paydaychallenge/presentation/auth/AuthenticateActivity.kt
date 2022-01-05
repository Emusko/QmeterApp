package com.example.paydaychallenge.presentation.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import com.example.paydaychallenge.R
import com.example.paydaychallenge.databinding.ActivityAuthenticateBinding
import com.example.paydaychallenge.di.base.BaseActivity
import com.example.paydaychallenge.di.factory.ViewModelProviderFactory
import com.example.paydaychallenge.presentation.main.MainActivity
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

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