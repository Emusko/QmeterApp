package com.technolink.qmeter.service

import com.technolink.qmeter.service.model.remote.request.AuthenticateRequestModel
import com.technolink.qmeter.service.model.remote.response.AuthenticationResponseModel
import com.technolink.qmeter.service.model.remote.response.GetWidgetsResponseModel
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*

interface ApiServices {
    @Headers("Connection: close")
    @POST("api/v1/template/device/login/")
    fun login(@Body authenticateRequestModel: AuthenticateRequestModel): Observable<AuthenticationResponseModel>

    @POST
    fun login(@Url url: String, @Body authenticateRequestModel: AuthenticateRequestModel): Observable<AuthenticationResponseModel>

    @Headers("Connection: close")
    @POST("api/v1/template/device/logout/")
    fun logout(@Body authenticateRequestModel: AuthenticateRequestModel): Observable<Response<Void>?>

    @POST
    fun logout(@Url url: String, @Body authenticateRequestModel: AuthenticateRequestModel): Observable<Response<Void>?>

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