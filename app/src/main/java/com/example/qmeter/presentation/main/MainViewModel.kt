package com.example.qmeter.presentation.main

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
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.example.qmeter.di.base.BaseViewModel
import com.example.qmeter.service.model.remote.request.FeedbackRequestModel
import com.example.qmeter.service.model.remote.response.GetWidgetsResponseModel
import com.example.qmeter.usecase.GetCustomersUseCase
import com.example.qmeter.usecase.GetWidgetsUseCase
import com.example.qmeter.usecase.PostFeedbackUseCase
import com.google.android.material.textfield.TextInputEditText
import okhttp3.Credentials
import retrofit2.HttpException
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainViewModel @Inject constructor(
    val sharedPreferences: SharedPreferences,
    private val getWidgetsUseCase: GetWidgetsUseCase,
    private val postFeedbackUseCase: PostFeedbackUseCase
) : BaseViewModel() {
    val viewData: MutableLiveData<GetWidgetsResponseModel> = MutableLiveData()

    val customerDataMap = hashMapOf<String, Any?>()

    val pageStateLiveData = MutableLiveData<Pair<Int, Boolean>>()

    private val requestList = arrayListOf<HashMap<String?, Any?>>()

    val requestModel = hashMapOf<String?, Any?>()
    val feedBacks = arrayListOf<FeedbackRequestModel>()

    val dataPost = MutableLiveData<Boolean>()

    val unauthorizedError = MutableLiveData<Boolean>()

    fun addToQueue() {
        requestModel[CUSTOMER_TAG] = customerDataMap.clone()
        val request = requestModel.clone()
        requestList.add(request as HashMap<String?, Any?>)
        postFeedback()
        customerDataMap.clear()
        feedBacks.clear()
    }

    fun getWidgets() {
        getWidgetsUseCase.execute(
            {
                viewData.value = it
            },
            {
                if (it is HttpException) {
                    if (it.code() == 401) {
                        sharedPreferences.edit().clear().apply()
                        unauthorizedError.postValue(true)
                    }
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
                        if (it.isNotEmpty())
                            customerDataMap[attr.name ?: ""] = it
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
                        if (it.isNotEmpty())
                            requestModel[attr.name ?: ""] = it
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
                    requestModel[COMMENT_TAG] = dataView.text.toString()
                }
            }
        }
    }
}