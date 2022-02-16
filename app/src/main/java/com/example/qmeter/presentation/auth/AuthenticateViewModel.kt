package com.example.qmeter.presentation.auth

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.example.qmeter.di.base.BaseViewModel
import com.example.qmeter.service.model.remote.response.AuthenticationResponseModel
import com.example.qmeter.service.model.remote.response.GetWidgetsResponseModel
import com.example.qmeter.usecase.GetCustomersUseCase
import com.example.qmeter.usecase.GetWidgetsUseCase
import javax.inject.Inject

class AuthenticateViewModel @Inject constructor(
    private val getCustomersUseCase: GetCustomersUseCase,
    private val getWidgetsUseCase: GetWidgetsUseCase,
    val sharedPreferences: SharedPreferences
): BaseViewModel() {
    val viewData: MutableLiveData<GetWidgetsResponseModel> = MutableLiveData()
    fun getComponents(username: String, password: String){
        getCustomersUseCase.execute(
            username,
            password,{
                sharedPreferences.edit().putString("username", username).apply()
                sharedPreferences.edit().putString("token", it.token).apply()
                sharedPreferences.edit().putString("password", password).apply()
                getWidgets()
            }, {
               error.onNext(it.localizedMessage?: "")
            }, subscriptions)
    }

    private fun getWidgets(){
        getWidgetsUseCase.execute(
            {
                viewData.value = it
            },
            {

            },
            subscriptions
        )
    }
}