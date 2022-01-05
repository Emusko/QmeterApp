package com.example.qmeter.service

import com.example.qmeter.service.model.remote.request.AuthenticateRequestModel
import com.example.qmeter.service.model.remote.response.AuthenticationResponseModel
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiServices {
    @POST("api/v1/template/device/login/")
    fun getComponents(@Body authenticateRequestModel: AuthenticateRequestModel): Observable<AuthenticationResponseModel>
}