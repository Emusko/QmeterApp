package com.example.paydaychallenge.presentation.main

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.paydaychallenge.di.base.BaseViewModel
import com.example.paydaychallenge.service.model.remote.response.AuthenticationResponseModel
import com.example.paydaychallenge.usecase.GetCustomersUseCase
import javax.inject.Inject

class MainViewModel @Inject constructor(
): BaseViewModel() {
    val viewData: MutableLiveData<AuthenticationResponseModel> = MutableLiveData()

    val pageStateLiveData = MutableLiveData<Int>()

    val submitButton = ObservableBoolean()

}