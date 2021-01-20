package com.example.paydaychallenge.usecase

import com.example.paydaychallenge.repository.Repository
import com.example.paydaychallenge.service.model.remote.request.AuthenticateRequestModel
import javax.inject.Inject

class AuthenticateUseCase @Inject constructor(private val repository: Repository){
    fun execute(email: String, password: String) = repository.authenticate(AuthenticateRequestModel(email, password))
}