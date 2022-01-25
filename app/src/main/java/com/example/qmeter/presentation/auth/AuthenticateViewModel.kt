package com.example.qmeter.presentation.auth

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.example.qmeter.di.base.BaseViewModel
import com.example.qmeter.service.model.remote.response.AuthenticationResponseModel
import com.example.qmeter.usecase.GetCustomersUseCase
import javax.inject.Inject

class AuthenticateViewModel @Inject constructor(
    private val getCustomersUseCase: GetCustomersUseCase,
    val sharedPreferences: SharedPreferences
): BaseViewModel() {
    val viewData: MutableLiveData<AuthenticationResponseModel> = MutableLiveData()
    fun getComponents(username: String, password: String){
        getCustomersUseCase.execute(
            username,
            password,{
                sharedPreferences.edit().putString("username", username).apply()
                sharedPreferences.edit().putString("password", password).apply()
                viewData.value = it
            }, {
               error.onNext(it.localizedMessage?: "")
            }, subscriptions)
    }
}