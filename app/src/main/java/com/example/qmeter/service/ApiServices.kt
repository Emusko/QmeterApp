package com.example.qmeter.service

import com.example.qmeter.service.model.remote.request.AuthenticateRequestModel
import com.example.qmeter.service.model.remote.response.AuthenticationResponseModel
import com.example.qmeter.service.model.remote.response.GetWidgetsResponseModel
import io.reactivex.Observable
import retrofit2.http.*

interface ApiServices {
    @Headers("Connection: close")
    @POST("api/v1/template/device/login/")
    fun getComponents(@Body authenticateRequestModel: AuthenticateRequestModel): Observable<AuthenticationResponseModel>
    @Headers("Content-Type: application/json")
    @POST("api/v1/template/device/widget/")
    fun postFeedback(@Body body: ArrayList<HashMap<String?, Any?>>): Observable<AuthenticationResponseModel>
    @GET("api/v1/template/device/widget/")
    fun getWidgets(): Observable<GetWidgetsResponseModel>
}