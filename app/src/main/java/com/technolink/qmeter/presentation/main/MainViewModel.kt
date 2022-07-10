package com.technolink.qmeter.presentation.main

import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.forEach
import androidx.lifecycle.MutableLiveData
import com.technolink.qmeter.di.base.BaseViewModel
import com.technolink.qmeter.service.model.remote.request.FeedbackRequestModel
import com.technolink.qmeter.service.model.remote.response.GetWidgetsResponseModel
import com.google.android.material.textfield.TextInputEditText
import com.technolink.qmeter.usecase.*
import retrofit2.HttpException
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainViewModel @Inject constructor(
    val sharedPreferences: SharedPreferences,
    private val logoutUseCase: LogoutUseCase,
    private val logoutWithUrlUseCase: LogoutWithUrlUseCase,
    private val getWidgetsUseCase: GetWidgetsUseCase,
    private val getWidgetsWithUrlUseCase: GetWidgetsWithUrlUseCase,
    private val postFeedbackUseCase: PostFeedbackUseCase,
    private val postFeedbackCustomUrlUseCase: PostFeedbackUrlUseCase
) : BaseViewModel() {
    val viewData: MutableLiveData<GetWidgetsResponseModel> = MutableLiveData()

    var lastRetrievedData: GetWidgetsResponseModel? = GetWidgetsResponseModel()

    val customerDataMap = hashMapOf<String, Any?>()

    val pageStateLiveData = MutableLiveData<Pair<Int, Boolean>>()

    private val requestList = arrayListOf<HashMap<String?, Any?>>()

    val requestModel = hashMapOf<String?, Any?>()
    val feedBacks = arrayListOf<FeedbackRequestModel>()

    val dataPost = MutableLiveData<Boolean>()

    val logoutLive = MutableLiveData<Boolean>()

    val unauthorizedError = MutableLiveData<Boolean>()

    fun addToQueue() {
        requestModel[CUSTOMER_TAG] = customerDataMap.clone()
        val request = requestModel.clone()
        requestList.add(request as HashMap<String?, Any?>)
        postFeedback()
        customerDataMap.clear()
        feedBacks.clear()
    }

    fun logout() {
        if (sharedPreferences.getString("baseUrl", null).isNullOrEmpty())
            logoutUseCase.execute(
                sharedPreferences.getString("username", "")!!,
                sharedPreferences.getString("password", "")!!,
                {
                    logoutLive.postValue(true)
                },
                {
                    if (it is HttpException) {
                        if (it.code() == 204) {
                            sharedPreferences.edit().putString("username", null).apply()
                            sharedPreferences.edit().putString("password", null).apply()
                            unauthorizedError.postValue(true)
                        }
                    }
                },
                subscriptions
            )
        else
            logoutWithUrlUseCase.execute(
                sharedPreferences.getString("baseUrl", null)!!,
                sharedPreferences.getString("username", "")!!,
                sharedPreferences.getString("password", "")!!,
                {
                    logoutLive.postValue(true)
                },
                {
                    if (it is HttpException) {
                        if (it.code() == 401) {
                            sharedPreferences.edit().putString("username", null).apply()
                            sharedPreferences.edit().putString("password", null).apply()
                            unauthorizedError.postValue(true)
                        }
                    }
                },
                subscriptions
            )
    }

    fun getWidgets() {

        if (sharedPreferences.getString("baseUrl", null).isNullOrEmpty())
            getWidgetsUseCase.execute(
                {
                    viewData.value = it
                },
                {
                    if (it is HttpException) {
                        if (it.code() == 401) {
                            sharedPreferences.edit().putString("username", null).apply()
                            sharedPreferences.edit().putString("password", null).apply()
                            unauthorizedError.postValue(true)
                        }
                    } else {
                        if (lastRetrievedData?.generalSettings?.is_kiosk_mode == true)
                            viewData.value = lastRetrievedData
                    }
                },
                subscriptions
            )
        else
            getWidgetsWithUrlUseCase.execute(
                sharedPreferences.getString("baseUrl", null)!!,
                {
                    viewData.value = it
                },
                {
                    if (it is HttpException) {
                        if (it.code() == 401) {
                            sharedPreferences.edit().putString("username", null).apply()
                            sharedPreferences.edit().putString("password", null).apply()
                            unauthorizedError.postValue(true)
                        }
                    } else {
                        if (lastRetrievedData?.generalSettings?.is_kiosk_mode == true)
                            viewData.value = lastRetrievedData
                    }
                },
                subscriptions
            )
    }

    init {
        executeNotificationTimer()
    }

    fun bindCustomerDataToRequest(
        layout: LinearLayoutCompat?,
        customerData: GetWidgetsResponseModel.CustomerData?
    ) {
        customerData?.attrs?.forEach { attr ->

            when (val dataView = layout?.findViewWithTag<View>(attr.name)) {
                is TextInputEditText -> {
                    dataView.text?.toString()?.let {
                        if (it.isNotEmpty()) {
                            if (attr.name == "phone_number") {
                                if (it != "994")
                                    customerDataMap[attr.name ?: ""] = it
                            }else {
                                customerDataMap[attr.name ?: ""] = it
                            }
                        }
                    }
                }
                is AppCompatSpinner -> {
                    dataView.selectedItem?.let {
                        if (it is GetWidgetsResponseModel.SelectOption && it.id != "placeholder")
                            customerDataMap[attr.name ?: ""] = it.id
                    }
                }
                is LinearLayoutCompat -> {
                    val checkedList = arrayListOf<Int?>()
                    dataView.forEach {
                        if (it is AppCompatCheckBox && it.isChecked) {
                            checkedList.add(it.tag?.toString()?.toInt())
                        }
                    }
                    if (!checkedList.isNullOrEmpty())
                        customerDataMap[attr.name ?: ""] = checkedList
                }
                is RadioGroup -> {
                    customerDataMap[attr.name ?: ""] =
                        layout.findViewById<RadioButton>(dataView.checkedRadioButtonId)?.text?.toString()
                }
            }
        }
    }

    private fun executeNotificationTimer() {

        val handler = Handler(Looper.getMainLooper())
        val timer = Timer()
        val doAsynchronousTask: TimerTask = object : TimerTask() {
            override fun run() {
                handler.post {
                    try {
                        postFeedback()
                    } catch (e: Exception) {
                    }
                }
            }
        }
        timer.schedule(doAsynchronousTask, 0, 60000)
    }

    fun postFeedback() {
        requestList.forEach { request ->

            if (sharedPreferences.getString("baseUrl", null).isNullOrEmpty())
                postFeedbackUseCase
                    .execute(
                        arrayListOf(request),
                        {
                            requestList.remove(request)
                        },
                        {
                        },
                        subscriptions
                    )
            else
                postFeedbackCustomUrlUseCase
                    .execute(
                        "${
                            sharedPreferences.getString(
                                "baseUrl",
                                null
                            )!!
                        }/api/v1/template/device/widget/",
                        arrayListOf(request),
                        {
                            requestList.remove(request)
                        },
                        {
                        },
                        subscriptions
                    )
        }
    }

    fun bindCustomFieldFeedbackDataToRequest(
        layout: LinearLayoutCompat?,
        customFieldFeedbackComponent: GetWidgetsResponseModel.CustomFieldFeedbackComponent?
    ) {
        customFieldFeedbackComponent?.attrs?.forEach { attr ->

            when (val dataView = layout?.findViewWithTag<View>(attr.name)) {
                is TextInputEditText -> {
                    dataView.text?.toString()?.let {
                        if (it.isNotEmpty()) {
                            if (attr.name == "phone_number") {
                                if (it != "994")
                                    requestModel[attr.name ?: ""] = it
                            } else {
                                requestModel[attr.name ?: ""] = it
                            }
                        }
                    }
                }
                is AppCompatSpinner -> {
                    dataView.selectedItem?.let {
                        if (it is GetWidgetsResponseModel.SelectOption && it.id != "placeholder")
                            requestModel[attr.name ?: ""] = it.id
                    }
                }
                is LinearLayoutCompat -> {
                    val checkedList = arrayListOf<Int?>()
                    dataView.forEach {
                        if (it is AppCompatCheckBox && it.isChecked) {
                            checkedList.add(it.text?.toString()?.toInt())
                        }
                    }
                    if (!checkedList.isNullOrEmpty())
                        requestModel[attr.name ?: ""] = checkedList
                }
            }
        }
    }

    fun bindSliDataToRequest(
        language: String,
        pageIndex: Int,
        sliData: GetWidgetsResponseModel.SliData?,
        markPageData: ArrayList<GetWidgetsResponseModel.MarkPageData>?
    ) {

        sliData?.attrs?.service?.forEach { service ->
            service.rateOptions.filter { it.selected == true }.forEach { rateOptions ->
                val feedback = FeedbackRequestModel()
                feedback.rate = rateOptions.name
                feedback.serviceId = service.id
                feedback.markPage = rateOptions.markpageId
                feedback.page = pageIndex.toString()
                rateOptions.markpageIdx.let {
                    val markPage =
                        markPageData?.filter { it.idx == rateOptions.markpageIdx }?.firstOrNull()
                    val selectedMarks = markPage?.marks?.filter { it.selected == true }
                    selectedMarks?.forEach {
                        val mark =
                            FeedbackRequestModel.Mark(it.id, it.name?.filter { it.key == language })
                        feedback.marks?.add(mark)
                    }
                }
                feedBacks.add(feedback)
            }
        }
        if (requestModel["feedbacks"] is ArrayList<*>) {
            (requestModel["feedbacks"] as ArrayList<FeedbackRequestModel>).addAll(feedBacks)
        } else {
            requestModel["feedbacks"] = feedBacks.clone()
        }
    }


    fun bindCommentDataToRequest(
        layout: LinearLayoutCompat?,
        commentData: GetWidgetsResponseModel.CommentData?
    ) {

        commentData?.attrs?.let {

            when (val dataView = layout?.findViewWithTag<View>(it.name)) {
                is TextInputEditText -> {
                    requestModel["comment"] = dataView.text.toString()
                }
            }
        }
    }
}