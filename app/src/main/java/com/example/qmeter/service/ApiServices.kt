package com.example.qmeter.service

import com.example.qmeter.service.model.remote.request.AuthenticateRequestModel
import com.example.qmeter.service.model.remote.response.AuthenticationResponseModel
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiServices {
    @Headers("Connection: close")
    @POST("api/v1/template/device/login/")
    fun getComponents(@Body authenticateRequestModel: AuthenticateRequestModel): Observable<AuthenticationResponseModel>
    @POST("api/v1/template/device/widget/")
    fun postFeedback(@Body body: ArrayList<HashMap<String?, Any?>>): Observable<AuthenticationResponseModel>
}