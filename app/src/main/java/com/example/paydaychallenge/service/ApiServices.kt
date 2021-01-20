package com.example.paydaychallenge.service

import com.example.paydaychallenge.service.model.remote.request.AuthenticateRequestModel
import com.example.paydaychallenge.service.model.remote.response.AuthenticateResponseModel
import com.example.paydaychallenge.service.model.remote.response.GetAccountResponseModel
import com.example.paydaychallenge.service.model.remote.response.GetTransactionResponseModel
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiServices {
    @POST("authenticate")
    fun authenticate(@Body authenticateRequestModel: AuthenticateRequestModel): Observable<AuthenticateResponseModel>
    @GET("customers")
    fun getCustomers(): Observable<ArrayList<AuthenticateResponseModel>>
    @GET("accounts")
    fun getAccounts(): Observable<ArrayList<GetAccountResponseModel>>
    @GET("transactions")
    fun getTransactions(): Observable<ArrayList<GetTransactionResponseModel>>
}