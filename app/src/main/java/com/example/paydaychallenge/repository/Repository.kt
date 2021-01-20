package com.example.paydaychallenge.repository

import com.example.paydaychallenge.service.ApiServices
import com.example.paydaychallenge.service.model.remote.request.AuthenticateRequestModel
import javax.inject.Inject

class Repository @Inject constructor(
    private val apiServices: ApiServices
) {
    fun authenticate(authenticateRequestModel: AuthenticateRequestModel) =
        apiServices.authenticate(authenticateRequestModel)

    fun getCustomers() = apiServices.getCustomers()

    fun getAccounts() = apiServices.getAccounts()

    fun getTransactions() = apiServices.getTransactions()
}