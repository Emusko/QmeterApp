package com.technolink.qmeter.service.model.remote.request

import com.google.gson.annotations.SerializedName

data class FeedbackRequestModel(
    @SerializedName("mark_page") var markPage: Int? = null,
    @SerializedName("conditionType") var conditionType: String? = null,
    @SerializedName("rate") var rate: String? = null,
    @SerializedName("page") var page: String? = null,
    @SerializedName("service_id") var serviceId: Int? = null,
    @SerializedName("marks") var marks: ArrayList<Mark>? = arrayListOf(),
){
    data class Mark(
        val id: Int? = null,
        val name: Map<String, String>? = hashMapOf()
    )
}