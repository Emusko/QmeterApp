package com.example.qmeter.service

import com.example.qmeter.service.model.remote.request.AuthenticateRequestModel
import com.example.qmeter.service.model.remote.response.AuthenticationResponseModel
import com.example.qmeter.service.model.remote.response.GetWidgetsResponseModel
import io.reactivex.Observable
import retrofit2.http.*

interface ApiServices {
    @Headers("Connection: close")
    @POST("api/v1/template/device/login/")
    fun login(@Body authenticateRequestModel: AuthenticateRequestModel): Observable<AuthenticationResponseModel>

    @POST
    fun login(@Url url: String, @Body authenticateRequestModel: AuthenticateRequestModel): Observable<AuthenticationResponseModel>

    @Headers("Content-Type: application/json")
    @POST("api/v1/template/device/widget/")
    fun postFeedback(@Body body: ArrayList<HashMap<String?, Any?>>): Observable<AuthenticationResponseModel>

    @Headers("Content-Type: application/json")
    @POST
    fun postFeedback(@Url url: String, @Body body: ArrayList<HashMap<String?, Any?>>): Observable<AuthenticationResponseModel>

    @GET("api/v1/template/device/widget/")
    fun getWidgets(): Observable<GetWidgetsResponseModel>

    @GET
    fun getWidgets(@Url url: String): Observable<GetWidgetsResponseModel>
}