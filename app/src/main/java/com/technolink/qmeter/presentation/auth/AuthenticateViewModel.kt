package com.technolink.qmeter.presentation.auth

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.technolink.qmeter.di.base.BaseViewModel
import com.technolink.qmeter.service.model.remote.response.GetWidgetsResponseModel
import com.technolink.qmeter.usecase.LoginUseCase
import com.technolink.qmeter.usecase.GetWidgetsUseCase
import com.technolink.qmeter.usecase.GetWidgetsWithUrlUseCase
import com.technolink.qmeter.usecase.LoginWithUrlUseCase
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
                    sharedPreferences.edit().putString("username", null).apply()
                    sharedPreferences.edit().putString("token", null).apply()
                    sharedPreferences.edit().putString("password", null).apply()
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
                    sharedPreferences.edit().putString("username", null).apply()
                    sharedPreferences.edit().putString("token", null).apply()
                    sharedPreferences.edit().putString("password", null).apply()
                    error.onNext(it.localizedMessage ?: "")
                },
                subscriptions
            )
    }
}