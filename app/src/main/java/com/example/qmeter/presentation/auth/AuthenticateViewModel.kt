package com.example.qmeter.presentation.auth

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.example.qmeter.di.base.BaseViewModel
import com.example.qmeter.service.model.remote.response.GetWidgetsResponseModel
import com.example.qmeter.usecase.LoginUseCase
import com.example.qmeter.usecase.GetWidgetsUseCase
import com.example.qmeter.usecase.GetWidgetsWithUrlUseCase
import com.example.qmeter.usecase.LoginWithUrlUseCase
import javax.inject.Inject

class AuthenticateViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val loginWithUrlUseCase: LoginWithUrlUseCase,
    private val getWidgetsUseCase: GetWidgetsUseCase,
    private val getWidgetsWithUrlUseCase: GetWidgetsWithUrlUseCase,
    val sharedPreferences: SharedPreferences
) : BaseViewModel() {
    val viewData: MutableLiveData<GetWidgetsResponseModel> = MutableLiveData()
    fun getComponents(username: String, password: String) {

        if (sharedPreferences.getString("baseUrl", null).isNullOrEmpty())
            loginUseCase.execute(
                username,
                password, {
                    sharedPreferences.edit().putString("username", username).apply()
                    sharedPreferences.edit().putString("token", it.token).apply()
                    sharedPreferences.edit().putString("password", password).apply()
                    getWidgets()
                }, {
                    error.onNext(it.localizedMessage ?: "")
                }, subscriptions
            )
        else
            loginWithUrlUseCase.execute(
                username,
                password, sharedPreferences.getString("baseUrl", null)!!, {
                    sharedPreferences.edit().putString("username", username).apply()
                    sharedPreferences.edit().putString("token", it.token).apply()
                    sharedPreferences.edit().putString("password", password).apply()
                    getWidgets()
                }, {
                    error.onNext(it.localizedMessage ?: "")
                }, subscriptions
            )
    }

    fun saveBaseUrl(baseUrl: String?) {
        sharedPreferences.edit().putString("baseUrl", baseUrl).apply()
    }

    fun getWidgets() {
        if (sharedPreferences.getString("baseUrl", null).isNullOrEmpty())
            getWidgetsUseCase.execute(
                {
                    viewData.value = it
                },
                {
                    sharedPreferences.edit().clear().apply()
                    error.onNext(it.localizedMessage ?: "")
                },
                subscriptions
            )
        else
            getWidgetsWithUrlUseCase.execute(
                sharedPreferences.getString("baseUrl", null)!!,
                {
                    viewData.value = it
                },
                {
                    sharedPreferences.edit().clear().apply()
                    error.onNext(it.localizedMessage ?: "")
                },
                subscriptions
            )
    }
}