package com.example.qmeter.service.model.remote.response

data class GetTransactionResponseModel(
    val id: Int,
    val account_id: Int,
    val amount: Double,
    val vendor: String,
    val category: String,
    val date: String
)