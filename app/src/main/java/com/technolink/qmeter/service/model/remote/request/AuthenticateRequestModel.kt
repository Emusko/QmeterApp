package com.technolink.qmeter.service.model.remote.request

data class AuthenticateRequestModel(
    val username: String,
    val password: String
)