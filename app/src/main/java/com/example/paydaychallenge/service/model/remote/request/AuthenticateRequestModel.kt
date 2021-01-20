package com.example.paydaychallenge.service.model.remote.request

data class AuthenticateRequestModel(
    val email: String,
    val password: String
)