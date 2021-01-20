package com.example.paydaychallenge.usecase

import com.example.paydaychallenge.repository.Repository
import com.example.paydaychallenge.service.model.remote.request.AuthenticateRequestModel
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(private val repository: Repository){
    fun execute() = repository.getTransactions()
}