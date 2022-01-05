package com.example.paydaychallenge.repository

import com.example.paydaychallenge.service.ApiServices
import com.example.paydaychallenge.service.model.remote.request.AuthenticateRequestModel
import javax.inject.Inject

class Repository @Inject constructor(
    private val apiServices: ApiServices
) {
    fun getComponents(username: String, password: String) = apiServices.getComponents(AuthenticateRequestModel(username, password))
}