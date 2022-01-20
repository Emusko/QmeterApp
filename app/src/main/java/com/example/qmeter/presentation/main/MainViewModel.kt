package com.example.qmeter.presentation.main

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.example.qmeter.di.base.BaseViewModel
import com.example.qmeter.service.model.remote.response.AuthenticationResponseModel
import javax.inject.Inject

class MainViewModel @Inject constructor(
): BaseViewModel() {
    val viewData: MutableLiveData<AuthenticationResponseModel> = MutableLiveData()

    val pageStateLiveData = MutableLiveData<Int>()

    val submitButton = ObservableBoolean()

    val requestModel = hashMapOf<String, Any>()

}