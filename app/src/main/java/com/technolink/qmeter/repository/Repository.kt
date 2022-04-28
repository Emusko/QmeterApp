package com.technolink.qmeter.repository

import com.technolink.qmeter.service.ApiServices
import com.technolink.qmeter.service.model.remote.request.AuthenticateRequestModel
import javax.inject.Inject

class Repository @Inject constructor(
    private val apiServices: ApiServices
) {
    fun getComponents(username: String, password: String) = apiServices.getComponents(AuthenticateRequestModel(username, password))
    fun postFeedback(body: ArrayList<HashMap<String?, Any?>>) = apiServices.postFeedback(body)
    fun postFeedback(url: String, body: ArrayList<HashMap<String?, Any?>>) = apiServices.postFeedback(url, body)
    fun getWidgets() = apiServices.getWidgets()
}