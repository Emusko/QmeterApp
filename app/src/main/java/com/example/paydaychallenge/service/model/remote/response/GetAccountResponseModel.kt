package com.example.paydaychallenge.service.model.remote.response

data class GetAccountResponseModel(
    val id: Int,
    val customer_id: Int,
    val iban: String,
    val type: String,
    val date_created: String,
    val active: Boolean
)