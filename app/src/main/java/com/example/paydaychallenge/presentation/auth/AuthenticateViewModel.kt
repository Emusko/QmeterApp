package com.example.paydaychallenge.presentation.auth

import androidx.lifecycle.MutableLiveData
import com.example.paydaychallenge.di.base.BaseViewModel
import com.example.paydaychallenge.service.model.remote.response.AuthenticationResponseModel
import com.example.paydaychallenge.usecase.GetCustomersUseCase
import javax.inject.Inject

class AuthenticateViewModel @Inject constructor(
    private val getCustomersUseCase: GetCustomersUseCase
): BaseViewModel() {
    val viewData: MutableLiveData<AuthenticationResponseModel> = MutableLiveData()
    fun getComponents(username: String, password: String){
        getCustomersUseCase.execute(
            username,
            password,{
                viewData.value = it
            }, {
               error.onNext(it.localizedMessage?: "")
            }, subscriptions)
    }
}