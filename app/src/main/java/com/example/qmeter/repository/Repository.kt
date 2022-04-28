package com.example.qmeter.repository

import com.example.qmeter.service.ApiServices
import com.example.qmeter.service.model.remote.request.AuthenticateRequestModel
import retrofit2.http.Url
import javax.inject.Inject

class Repository @Inject constructor(
    private val apiServices: ApiServices
) {
    fun login(username: String, password: String) = apiServices.login(AuthenticateRequestModel(username, password))
    fun login(url: String, username: String, password: String) = apiServices.login(url, AuthenticateRequestModel(username, password))
    fun postFeedback(body: ArrayList<HashMap<String?, Any?>>) = apiServices.postFeedback(body)
    fun postFeedback(url: String, body: ArrayList<HashMap<String?, Any?>>) = apiServices.postFeedback(url, body)
    fun getWidgets() = apiServices.getWidgets()
    fun getWidgets(url: String) = apiServices.getWidgets(url)
}