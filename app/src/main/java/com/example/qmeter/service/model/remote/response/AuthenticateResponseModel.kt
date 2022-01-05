package com.example.qmeter.service.model.remote.response

import com.google.gson.annotations.SerializedName

data class AuthenticateResponseModel(
    val id: Int,
    @SerializedName("First Name")
    val firstName: String,
    @SerializedName("Last Name")
    val lastName: String,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("dob")
    val birthday: String,
    @SerializedName("phone")
    val phone: String
)