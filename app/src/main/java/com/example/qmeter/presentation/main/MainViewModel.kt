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
import com.example.qmeter.service.model.remote.response.AuthenticationResponseModel
import com.example.qmeter.usecase.GetCustomersUseCase
import com.example.qmeter.usecase.PostFeedbackUseCase
import com.google.android.material.textfield.TextInputEditText
import okhttp3.Credentials
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainViewModel @Inject constructor(
    private val getCustomersUseCase: GetCustomersUseCase,
    val sharedPreferences: SharedPreferences,
    private val postFeedbackUseCase: PostFeedbackUseCase
) : BaseViewModel() {
    val viewData: MutableLiveData<AuthenticationResponseModel> = MutableLiveData()

    val pageStateLiveData = MutableLiveData<Pair<Int, Boolean>>()

    private val requestList = arrayListOf<HashMap<String?, Any?>>()

    val requestModel = hashMapOf<String?, Any?>()

    val dataPost = MutableLiveData<Boolean>()

    fun addToQueue(){
        val request = requestModel.clone()
        requestList.add(request as HashMap<String?, Any?>)
        postFeedback()
    }
    fun getComponents(){
        getCustomersUseCase.execute(
            sharedPreferences.getString("username", "")?: "",
            sharedPreferences.getString("password", "")?: "",{
                    viewData.value = it
            }, {
                error.onNext(it.localizedMessage?: "")
            }, subscriptions)
    }
    init {
        executeNotificationTimer()
    }

    fun bindCustomerDataToRequest(
        layout: LinearLayoutCompat?,
        customerData: AuthenticationResponseModel.CustomerData?
    ) {
        val customerDataMap = hashMapOf<String, Any?>()
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
                        if (it is AuthenticationResponseModel.SelectOption)
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
                    customerDataMap[attr.name ?: ""] = layout.findViewById<RadioButton>(dataView.checkedRadioButtonId)?.text?.toString()
                }
            }
        }
        requestModel[CUSTOMER_TAG] = customerDataMap
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
        customFieldFeedbackComponent: AuthenticationResponseModel.CustomFieldFeedbackComponent?
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
                        if (it is AuthenticationResponseModel.SelectOption)
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
        sliData: AuthenticationResponseModel.SliData?,
        markPageData: ArrayList<AuthenticationResponseModel.MarkPageData>?
    ) {
        val feedBacks = arrayListOf<FeedbackRequestModel>()

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
        if (requestModel["feedbacks"] is ArrayList<*>){
            (requestModel["feedbacks"] as ArrayList<FeedbackRequestModel>).addAll(feedBacks)
        } else {
            requestModel["feedbacks"] = feedBacks
        }
    }


    fun bindCommentDataToRequest(
        layout: LinearLayoutCompat?,
        commentData: AuthenticationResponseModel.CommentData?
    ) {

        val commentMap = hashMapOf<String, Any?>()
        commentData?.attrs?.let {

            when (val dataView = layout?.findViewWithTag<View>(it.name)) {
                is TextInputEditText -> {
                    requestModel[COMMENT_TAG] = dataView.text.toString()
                }
            }
        }
    }
}