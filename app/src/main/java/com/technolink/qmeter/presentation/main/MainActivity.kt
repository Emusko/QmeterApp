package com.technolink.qmeter.presentation.main

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.*
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.italic
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import com.bumptech.glide.Glide
import technolink.qmeter.R
import com.technolink.qmeter.di.base.BaseActivity
import com.technolink.qmeter.di.factory.ViewModelProviderFactory
import com.technolink.qmeter.presentation.auth.AuthenticateActivity
import com.technolink.qmeter.service.model.remote.response.GetWidgetsResponseModel
import com.technolink.qmeter.utils.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import technolink.qmeter.databinding.*
import java.util.*
import javax.inject.Inject


const val SLI_TAG = "sli_data"
const val COMMENT_TAG = "comment_data"
const val CUSTOMER_TAG = "customer_data"

const val CUSTOM_FEEDBACK_TAG = "custom_field_feedback_component"

class MainActivity : BaseActivity() {
    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private val viewModel: MainViewModel by viewModels { factory }

    private val responseModel by lazy { (intent.getSerializableExtra("pagesResponse") as? GetWidgetsResponseModel) }
    private val pages by lazy { responseModel?.pages ?: arrayListOf() }

    private lateinit var binding: ActivityMainBinding

    private val pageViews = hashMapOf<Int, LinearLayoutCompat?>()
    private var languageIsActive = true

    private var languageContainer: PageLanguageContainerLayoutBinding? = null

    private var sliCondition = hashMapOf<String?, String?>()

    private var condition: GetWidgetsResponseModel.ConditionOverallData? = null

    private var finalPageCondition: GetWidgetsResponseModel.Reaction? = null

    private var finalPageSli: String? = ""

    private var language = "en"

    private var mCountDownTimer: CountDownTimer? = null

    private var exitCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        viewModel.lastRetrievedData = responseModel
        setContentView(binding.root)

        setPageView()

        setListener()
    }

    private fun hideKeyboard(){
        binding.root.let {
            WindowInsetsControllerCompat(
                window,
                it
            ).hide(WindowInsetsCompat.Type.ime())
        }
    }

    private fun setPageView() {
        binding.qmeterAppLogo.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.back.visibility =
                View.GONE
            binding.next.visibility =
                View.GONE
            binding.submit.visibility =
                View.GONE
            viewModel.getWidgets()
            binding.container.removeAllViews()
            pageViews.clear()
//            getLanguageFromUser()
            condition = null
            finalPageCondition = null
        }
        viewModel.logoutLive.observe(this) {
            sharedPreferences.edit().remove("username").apply()
            sharedPreferences.edit().remove("token").apply()
            sharedPreferences.edit().remove("password").apply()
            startActivity(Intent(this, AuthenticateActivity::class.java))
            finish()
        }
        binding.exitDummy.setOnClickListener {
            exitCounter++
            if (exitCounter == 3) {
                exitCounter = 0
                popUpExitDialog()
            }
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        Glide
            .with(this)
            .load(responseModel?.generalSettings?.logo_url)
            .into(binding.logo)
    }

    private fun setListener() {

        getLanguageFromUser()

        viewModel.viewData.observe(this) {
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("pagesResponse", it)
            })
            finish()
        }

        viewModel.unauthorizedError.observe(this) {
            startActivity(Intent(this, AuthenticateActivity::class.java))
            finish()
        }

        viewModel.pageStateLiveData.observe(this) {
            val pageIndex = it.first
            val back = it.second
            languageContainer?.root?.visibility = View.GONE
            pageViews[pageIndex]?.visibility = View.VISIBLE
            pageViews[pageIndex + 1]?.visibility = View.GONE
            pageViews[pageIndex - 1]?.visibility = View.GONE
            val handler = Handler(Looper.getMainLooper())

            mCountDownTimer?.cancel()
            pages[pageIndex]?.properties?.timeout?.let {
                if (it.time!! > 0 && it.enable == true) {
                    mCountDownTimer = object : CountDownTimer(it.time.toLong(), 10L) {
                        override fun onTick(p0: Long) {

                        }

                        override fun onFinish() {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.back.visibility =
                                View.GONE
                            binding.next.visibility =
                                View.GONE
                            binding.submit.visibility =
                                View.GONE
                            viewModel.getWidgets()
                            binding.container.removeAllViews()
                            pageViews.clear()
                            condition = null
                            finalPageCondition = null
                        }
                    }
                }
                mCountDownTimer?.start()
            }
            binding.next.setTextColor(pages[pageIndex]?.properties?.nextButtonTxtColor.getColor())
            binding.next.text = responseModel?.generalSettings?.next_button_text!![language]
            binding.next.background?.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    pages[pageIndex]?.properties?.nextButtonBgColor.getColor() ?: 0,
                    BlendModeCompat.SRC_ATOP
                )

            binding.back.setTextColor(pages[pageIndex]?.properties?.backButtonTxtColor.getColor())
            binding.back.text = responseModel?.generalSettings?.back_button_text!![language]
            binding.back.background?.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    pages[pageIndex]?.properties?.backButtonBgColor.getColor(),
                    BlendModeCompat.SRC_ATOP
                )


            binding.submit.setTextColor(pages[pageIndex]?.properties?.submitButtonTxtColor.getColor())
            binding.submit.text = responseModel?.generalSettings?.submit_button_text!![language]
            binding.submit.background?.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    pages[pageIndex]?.properties?.submitButtonBgColor.getColor(),
                    BlendModeCompat.SRC_ATOP
                )

            binding.motherLayout.setBackgroundColor(responseModel?.pages!![pageIndex]?.properties?.pageBg?.getColor()!!)
            window.statusBarColor =
                responseModel?.pages!![pageIndex]?.properties?.pageHeader?.getColor()!!
            binding.qmeterAppLogo.setBackgroundColor(responseModel?.pages!![pageIndex]?.properties?.pageHeader?.getColor()!!)
            binding.exitDummy.setBackgroundColor(responseModel?.pages!![pageIndex]?.properties?.pageHeader?.getColor()!!)

            if (sliCondition.isNotEmpty()) {
                var sliFeedBacks = ""
                sliCondition.forEach {
                    sliFeedBacks += it.value
                    finalPageSli += it.value
                }
                if (sliFeedBacks.contains("unacceptable") || sliFeedBacks.contains("bad")) {
                    condition = pages[pageIndex]?.condition?.overall?.negative
                } else if (sliFeedBacks.contains("excellent") || sliFeedBacks.contains("good")) {
                    condition = pages[pageIndex]?.condition?.overall?.positive
                } else if (sliFeedBacks.contains("neutral")) {
                    condition = pages[pageIndex]?.condition?.overall?.neutral
                }

            }

            condition?.let { reaction ->
                reaction.commentData?.let {
                    sliCondition.clear()
                    if (it) {
                        binding.container.findViewWithTag<LinearLayoutCompat>(COMMENT_TAG).visibility =
                            View.VISIBLE
                    } else {
                        binding.container.findViewWithTag<LinearLayoutCompat>(COMMENT_TAG).visibility =
                            View.GONE
                    }
                }
                reaction.customerData?.let {
                    sliCondition.clear()
                    if (it) {
                        binding.container.findViewWithTag<LinearLayoutCompat>(CUSTOMER_TAG).visibility =
                            View.VISIBLE
                    } else {
                        binding.container.findViewWithTag<LinearLayoutCompat>(CUSTOMER_TAG).visibility =
                            View.GONE
                    }
                }
                reaction.customFields?.let {
                    sliCondition.clear()
                    if (it) {
                        binding.container.findViewWithTag<LinearLayoutCompat>(CUSTOM_FEEDBACK_TAG)?.visibility =
                            View.VISIBLE
                    } else {
                        binding.container.findViewWithTag<LinearLayoutCompat>(CUSTOM_FEEDBACK_TAG)?.visibility =
                            View.GONE
                    }
                }
            }

            var skipCounter = 0
            pageViews[pageIndex]?.forEach { page ->
                if (page.visibility == View.VISIBLE) {
                    skipCounter++
                }
            }
            if (skipCounter == 0) {
                if (back) {
                    viewModel.pageStateLiveData.value =
                        Pair(viewModel.pageStateLiveData.value?.first?.minus(1) ?: 0, true)
                } else {
                    viewModel.pageStateLiveData.value =
                        Pair(viewModel.pageStateLiveData.value?.first?.plus(1) ?: 0, false)
                }
            }

//            writeDataToRequestModel(pageViews[pageIndex-1])
            when {
                pageIndex < 0 -> {
                    viewModel.pageStateLiveData.value = Pair(0, false)
                }
                pageIndex >= pages.size -> {
                    viewModel.pageStateLiveData.value = Pair(pages.size - 1, true)
                }
                pageIndex < pages.size -> {
                    if ((pages[pageIndex]?.properties?.isBackButtonEnabled!!)
                        || (pageIndex == 0 && responseModel?.languagePage?.languages?.size!! >= 1)
                    ) {
                        binding.back.visibility =
                            View.VISIBLE
                    } else {
                        binding.back.visibility =
                            View.GONE
                    }
                    if (pages[pageIndex]?.properties?.isNextButtonEnabled!!) {
                        binding.next.visibility =
                            View.VISIBLE
                    } else {
                        binding.next.visibility =
                            View.GONE
                    }
                    if (pages.size - 1 == pageIndex) {
                        binding.submit.visibility = View.VISIBLE
                        binding.next.visibility = View.GONE
                    } else {
                        binding.next.visibility = View.VISIBLE
                        binding.submit.visibility = View.GONE
                    }
                }
            }
            if (pageIndex == 0 && (responseModel?.languagePage?.languages.isNullOrEmpty() || responseModel?.languagePage?.languages?.size == 1))
                binding.back.visibility = View.GONE

            if (responseModel?.pages!![viewModel.pageStateLiveData.value?.first!!]!!.makePages()
                    .filter { it != null }.size == 1
                && (responseModel?.pages!![viewModel.pageStateLiveData.value?.first!!]!!.makePages()
                    .filter { it != null }
                    .firstOrNull() is GetWidgetsResponseModel.SliData)
                &&(responseModel?.pages!![viewModel.pageStateLiveData.value?.first!!]!!.makePages()
                    .filter { it != null }
                    .firstOrNull() as?GetWidgetsResponseModel.SliData )?.attrs?.service?.size == 1
            ) {
                binding.next.visibility = View.GONE
            }
        }

        binding.next.setOnClickListener {
            mCountDownTimer?.cancel()
            if (pages.size - 1 != viewModel.pageStateLiveData.value?.first) {
                val page = pages[viewModel.pageStateLiveData.value!!.first]
                page?.makePages()?.forEach { pageComponent ->
                    when (pageComponent) {
                        is GetWidgetsResponseModel.SliData -> {
                            pageComponent.attrs?.service?.forEach {
                                val filteredList = it.rateOptions.filter { it.selected == false }
                                if (it.required == true && filteredList.size == 5) {
                                    return@setOnClickListener
                                }
                            }
                        }
                        is GetWidgetsResponseModel.CustomerData -> {
                            pageComponent.attrs.forEach { attr ->
                                val dataView =
                                    binding.container.findViewWithTag<View>(attr.name)

                                if (dataView is TextInputEditText
                                    && attr.name == "email"
                                    && dataView.text?.toString()?.isNotEmpty() == true
                                    && dataView.text?.toString()?.isEmailValid() == false
                                ) {
                                    dataView.requestFocus()
                                    dataView.error =
                                        getString(R.string.email_field_error_message)
                                    return@setOnClickListener
                                }
                                if (
                                    (dataView is TextInputEditText
                                            && attr.name == "phone_number"
                                            && attr.required == true
                                            && dataView.text?.toString()?.trim()?.length !in 9..15)
                                ) {
                                    dataView.requestFocus()
                                    dataView.error =
                                        getString(R.string.phone_number_field_error_message)
                                    return@setOnClickListener
                                } else if (
                                    attr.name == "phone_number" &&
                                    attr.required == false
                                    && (dataView as TextInputEditText).text?.toString()?.trim() != "994"
                                    && dataView.text?.toString()?.trim()?.length !in 9..15){
                                    dataView.requestFocus()
                                    dataView.error =
                                        getString(R.string.phone_number_field_error_message)
                                    return@setOnClickListener
                                }
                                if (attr.required == true) {
                                    when (dataView) {
                                        is TextInputEditText -> {
                                            dataView.text?.toString()?.let {
                                                if (it.isEmpty()) {
                                                    dataView.requestFocus()
                                                    dataView.error =
                                                        getString(R.string.field_error_message)
                                                    return@setOnClickListener
                                                }
                                            }
                                        }
                                        is AppCompatSpinner -> {
                                            dataView.selectedItem?.toString()?.let {
                                                if (it.isEmpty()) {
                                                    return@setOnClickListener
                                                }
                                            }
                                        }
                                        is LinearLayoutCompat -> {
                                            val checkedList = arrayListOf<Int?>()
                                            dataView.forEach {
                                                if (it is AppCompatCheckBox && it.isChecked) {
                                                    checkedList.add(it.text?.toString()?.toInt())
                                                }
                                            }
                                            if (checkedList.isNullOrEmpty()) {
                                                return@setOnClickListener
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        is GetWidgetsResponseModel.CustomFieldFeedbackComponent -> {
                            pageComponent.attrs?.forEach { attr ->
                                if (attr.required == true) {
                                    when (val dataView =
                                        binding.container.findViewWithTag<View>(attr.name)) {
                                        is TextInputEditText -> {
                                            dataView.text?.toString()?.let {
                                                if (it.isEmpty()) {
                                                    dataView.requestFocus()
                                                    dataView.error =
                                                        getString(R.string.field_error_message)
                                                    return@setOnClickListener
                                                }
                                            }
                                        }
                                        is AppCompatSpinner -> {
                                            dataView.selectedItem?.toString()?.let {
                                                if (it.isEmpty()) {
                                                    return@setOnClickListener
                                                }
                                            }
                                        }
                                        is LinearLayoutCompat -> {
                                            val checkedList = arrayListOf<Int?>()
                                            dataView.forEach {
                                                if (it is AppCompatCheckBox && it.isChecked) {
                                                    checkedList.add(it.text?.toString()?.toInt())
                                                }
                                            }
                                            if (checkedList.isNullOrEmpty()) {
                                                return@setOnClickListener
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        is GetWidgetsResponseModel.CommentData -> {
                            pageComponent.attrs?.let { attr ->
                                if (attr.required == true) {
                                    when (val dataView =
                                        binding.container.findViewWithTag<View>(attr.name)) {
                                        is TextInputEditText -> {
                                            dataView.text?.toString()?.let {
                                                if (it.isEmpty()) {
                                                    dataView.requestFocus()
                                                    dataView.error =
                                                        getString(R.string.field_error_message)
                                                    return@setOnClickListener
                                                }
                                            }
                                        }
                                        is AppCompatSpinner -> {
                                            dataView.selectedItem?.toString()?.let {
                                                if (it.isEmpty()) {

                                                    return@setOnClickListener
                                                }
                                            }
                                        }
                                        is LinearLayoutCompat -> {
                                            val checkedList = arrayListOf<Int?>()
                                            dataView.forEach {
                                                if (it is AppCompatCheckBox && it.isChecked) {
                                                    checkedList.add(it.text?.toString()?.toInt())
                                                }
                                            }
                                            if (checkedList.isNullOrEmpty()) {
                                                return@setOnClickListener
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                viewModel.pageStateLiveData.value =
                    Pair(viewModel.pageStateLiveData.value?.first?.plus(1) ?: 0, false)
            }
        }
        binding.back.setOnClickListener {
            mCountDownTimer?.cancel()
            if (viewModel.pageStateLiveData.value != null && viewModel.pageStateLiveData.value?.first != 0) {
                viewModel.pageStateLiveData.value =
                    Pair(viewModel.pageStateLiveData.value?.first?.minus(1) ?: 0, true)
            } else {
                binding.progressBar.visibility = View.VISIBLE
                binding.back.visibility =
                    View.GONE
                binding.next.visibility =
                    View.GONE
                binding.submit.visibility =
                    View.GONE
                viewModel.getWidgets()
                binding.container.removeAllViews()
                pageViews.clear()
                condition = null
                finalPageCondition = null
            }
        }
        binding.submit.setOnClickListener {

            pages[viewModel.pageStateLiveData.value?.first!!]?.makePages()?.forEach { pageComponent ->
                when (pageComponent) {
                    is GetWidgetsResponseModel.SliData -> {
                        pageComponent.attrs?.service?.forEach {
                            val filteredList = it.rateOptions.filter { it.selected == false }
                            if (it.required == true && filteredList.size == 5) {
                                return@setOnClickListener
                            }
                        }
                    }
                    is GetWidgetsResponseModel.CustomerData -> {
                        pageComponent.attrs.forEach { attr ->
                            val dataView =
                                binding.container.findViewWithTag<View>(attr.name)

                            if (dataView is TextInputEditText
                                && attr.name == "email"
                                && dataView.text?.toString()?.isNotEmpty() == true
                                && dataView.text?.toString()?.isEmailValid() == false
                            ) {
                                dataView.requestFocus()
                                dataView.error =
                                    getString(R.string.email_field_error_message)
                                return@setOnClickListener
                            }
                            if (
                                (dataView is TextInputEditText
                                && attr.name == "phone_number"
                                && attr.required == true
                                && dataView.text?.toString()?.trim()?.length !in 9..15)
                            ) {
                                dataView.requestFocus()
                                dataView.error =
                                    getString(R.string.phone_number_field_error_message)
                                return@setOnClickListener
                            } else if (
                                attr.name == "phone_number" &&
                                attr.required == false
                                && (dataView as TextInputEditText).text?.toString()?.trim() != "994"
                                && dataView.text?.toString()?.trim()?.length !in 9..15){
                                dataView.requestFocus()
                                dataView.error =
                                    getString(R.string.phone_number_field_error_message)
                                return@setOnClickListener
                            }
                            if (attr.required == true) {
                                when (dataView) {
                                    is TextInputEditText -> {
                                        dataView.text?.toString()?.let {
                                            if (it.isEmpty()) {
                                                dataView.requestFocus()
                                                dataView.error =
                                                    getString(R.string.field_error_message)
                                                return@setOnClickListener
                                            }
                                        }
                                    }
                                    is AppCompatSpinner -> {
                                        dataView.selectedItem?.toString()?.let {
                                            if (it.isEmpty()) {
                                                return@setOnClickListener
                                            }
                                        }
                                    }
                                    is LinearLayoutCompat -> {
                                        val checkedList = arrayListOf<Int?>()
                                        dataView.forEach {
                                            if (it is AppCompatCheckBox && it.isChecked) {
                                                checkedList.add(it.text?.toString()?.toInt())
                                            }
                                        }
                                        if (checkedList.isNullOrEmpty()) {
                                            return@setOnClickListener
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is GetWidgetsResponseModel.CustomFieldFeedbackComponent -> {
                        pageComponent.attrs?.forEach { attr ->
                            if (attr.required == true) {
                                when (val dataView =
                                    binding.container.findViewWithTag<View>(attr.name)) {
                                    is TextInputEditText -> {
                                        dataView.text?.toString()?.let {
                                            if (it.isEmpty()) {
                                                dataView.requestFocus()
                                                dataView.error =
                                                    getString(R.string.field_error_message)
                                                return@setOnClickListener
                                            }
                                        }
                                    }
                                    is AppCompatSpinner -> {
                                        dataView.selectedItem?.toString()?.let {
                                            if (it.isEmpty()) {
                                                return@setOnClickListener
                                            }
                                        }
                                    }
                                    is LinearLayoutCompat -> {
                                        val checkedList = arrayListOf<Int?>()
                                        dataView.forEach {
                                            if (it is AppCompatCheckBox && it.isChecked) {
                                                checkedList.add(it.text?.toString()?.toInt())
                                            }
                                        }
                                        if (checkedList.isNullOrEmpty()) {
                                            return@setOnClickListener
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is GetWidgetsResponseModel.CommentData -> {
                        pageComponent.attrs?.let { attr ->
                            if (attr.required == true) {
                                when (val dataView =
                                    binding.container.findViewWithTag<View>(attr.name)) {
                                    is TextInputEditText -> {
                                        dataView.text?.toString()?.let {
                                            if (it.isEmpty()) {
                                                dataView.requestFocus()
                                                dataView.error =
                                                    getString(R.string.field_error_message)
                                                return@setOnClickListener
                                            }
                                        }
                                    }
                                    is AppCompatSpinner -> {
                                        dataView.selectedItem?.toString()?.let {
                                            if (it.isEmpty()) {

                                                return@setOnClickListener
                                            }
                                        }
                                    }
                                    is LinearLayoutCompat -> {
                                        val checkedList = arrayListOf<Int?>()
                                        dataView.forEach {
                                            if (it is AppCompatCheckBox && it.isChecked) {
                                                checkedList.add(it.text?.toString()?.toInt())
                                            }
                                        }
                                        if (checkedList.isNullOrEmpty()) {
                                            return@setOnClickListener
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            mCountDownTimer?.cancel()
            val response = responseModel?.copy()
            pageViews.forEach { page ->
                page.value?.forEach { pageView ->
                    when (pageView.tag as? String) {
                        COMMENT_TAG -> {
                            viewModel.bindCommentDataToRequest(
                                pageView as? LinearLayoutCompat,
                                response?.pages!![page.key]?.commentData
                            )
                        }
                        CUSTOMER_TAG -> {
                            viewModel.bindCustomerDataToRequest(
                                pageView as? LinearLayoutCompat,
                                response?.pages!![page.key]?.customerData
                            )
                        }
                        CUSTOM_FEEDBACK_TAG -> {
                            viewModel.bindCustomFieldFeedbackDataToRequest(
                                pageView as? LinearLayoutCompat,
                                response?.pages!![page.key]?.customFieldFeedbackComponent
                            )
                        }
                        SLI_TAG -> {
                            viewModel.bindSliDataToRequest(
                                language,
                                page.key,
                                response?.pages!![page.key]?.sliData,
                                response?.markPageData
                            )
                        }

                    }
                }
            }
            binding.submit.visibility = View.GONE
            binding.back.visibility = View.GONE
            binding.container.removeAllViews()
            viewModel.addToQueue()
            viewModel.dataPost.postValue(true)
        }

        viewModel.dataPost.observe(this) {
            var smileIndicator = ""
            finalPageSli?.let {
                if (it.contains("unacceptable") || it.contains("bad")) {
                    smileIndicator = "negative"
                    finalPageCondition = responseModel?.finalPageData?.negative
                } else if (it.contains("neutral")) {
                    smileIndicator = "neutral"
                    finalPageCondition = responseModel?.finalPageData?.positive
                } else if (it.contains("excellent") || it.contains("good")) {
                    smileIndicator = "positive"
                    finalPageCondition = responseModel?.finalPageData?.neutral
                }
            }
            finalPageSli = ""
            val finalPageData = responseModel?.finalPageData
            finalPageData?.pageBg?.let {
                binding.motherLayout.setBackgroundColor(it.getColor())
            }
            if (finalPageData == null) {
                binding.progressBar.visibility = View.VISIBLE
                binding.back.visibility =
                    View.GONE
                binding.next.visibility =
                    View.GONE
                binding.submit.visibility =
                    View.GONE
                viewModel.getWidgets()
                binding.container.removeAllViews()
                pageViews.clear()
                condition = null
                finalPageCondition = null
            }
            val title =
                InputFromUserSliFontViewFinalPageBinding.bind(
                    LayoutInflater.from(this)
                        .inflate(
                            R.layout.input_from_user_sli_font_view_final_page,
                            binding.container,
                            false
                        )
                )
                    .apply {
                        if (finalPageCondition != null) {
                            this.smileView.text =
                                smileIndicator.resolveFinalIconFromAwesome()
                            this.smileView.setBackgroundColor(
                                finalPageCondition!!.textBgColor.getColor()
                            )
                            this.smileView.setTextColor(finalPageCondition!!.textColor.getColor())
                            this.smileView.apply {
                                when (responseModel?.languagePage?.properties?.smileySize) {
                                    "S" -> {
                                        this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100f)
                                    }
                                    "M" -> {
                                        this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 130f)
                                    }
                                    "L" -> {
                                        this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 150f)
                                    }
                                }
                            }
                        } else {

                            this.smileView.text = "A"
                            this.smileView.apply {
                                when (responseModel?.languagePage?.properties?.smileySize) {
                                    "S" -> {
                                        this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100f)
                                    }
                                    "M" -> {
                                        this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 130f)
                                    }
                                    "L" -> {
                                        this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 150f)
                                    }
                                }
                            }
                        }
                    }
            val finalText =
                InputFromUserSliViewBinding.bind(
                    LayoutInflater.from(this)
                        .inflate(
                            R.layout.input_from_user_sli_view,
                            binding.container,
                            false
                        )
                ).apply {
                    finalPageCondition?.let {

                        this.textView.text =
                            it.text!![language]
                        this.textView.setBackgroundColor(it.textBgColor.getColor())
                        this.textView.setTextColor(it.textColor.getColor())
                        this.textView.setDynamicSize(it.textSize)
                    }
                }
            binding.container.addView(title.root)
            binding.container.addView(finalText.root)
            val handler = Handler(Looper.getMainLooper())
            if (it) {
                responseModel?.pages?.forEach {
                    it?.makePages()?.forEach {
                        if (it is GetWidgetsResponseModel.SliData) {
                            it.attrs?.service?.forEach {
                                it.rateOptions.forEach {
                                    it.selected = false
                                }
                            }
                        }
                    }
                }
                responseModel?.markPageData?.forEach {
                    it.marks.forEach {
                        it.selected = false
                    }
                }
                responseModel?.finalPageData?.timeout?.let {
                    if (it.time!! > 0 && it.enable == true) {
                        handler
                            .postDelayed({
                                binding.progressBar.visibility = View.VISIBLE
                                binding.back.visibility =
                                    View.GONE
                                binding.next.visibility =
                                    View.GONE
                                binding.submit.visibility =
                                    View.GONE
                                viewModel.getWidgets()
                                binding.container.removeAllViews()
                                pageViews.clear()
                                condition = null
                                finalPageCondition = null
                            }, it.time.toLong())
                    }
                }

            }
        }
        viewModel.requestModel.clear()
    }

    private fun initializeViews() {
        pages.forEachIndexed { index, page ->
            val pageLayout = PageLinearLayoutBinding.bind(
                LayoutInflater.from(this)
                    .inflate(
                        R.layout.page_linear_layout,
                        binding.container,
                        false
                    )
            )
            val title = ComponentTitleViewBinding.bind(
                LayoutInflater.from(this)
                    .inflate(
                        R.layout.component_title_view,
                        binding.container,
                        false
                    )
            )

            title.apply {
                this.componentTitle.setDynamicSize(page?.properties?.pageTitleSize)
                this.componentTitle.text = page?.properties?.pageTitle!![language]
                this.componentTitle.setTextColor(page.properties.pageTitleTextColor.getColor() ?: 0)
                root.background?.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        page.properties.pageTitleBgColor.getColor() ?: 0,
                        BlendModeCompat.SRC_ATOP
                    )
            }
            if (page?.properties?.isPageTitle == true)
                pageLayout.root.addView(title.root)

            page?.makePages()?.sortedBy { it?.position }?.forEach { pageComponent ->
                when (pageComponent) {
                    is GetWidgetsResponseModel.CommentData -> {
                        val view = populateCommentView(pageComponent)
                        view.addTopMargin(24f, this)
                        pageLayout.root.addView(view)
                    }
                    is GetWidgetsResponseModel.SliData -> {
                        val view = populateSliView(pageComponent)
                        view.addTopMargin(24f, this)
                        pageLayout.root.addView(view)
                    }
                    is GetWidgetsResponseModel.CustomerData -> {
                        val view = populateCustomerView(pageComponent)
                        view.addTopMargin(24f, this)
                        pageLayout.root.addView(view)
                    }
                    is GetWidgetsResponseModel.CustomFieldFeedbackComponent -> {
                        val view = populateCustomFeedbackView(pageComponent)
                        view.addTopMargin(24f, this)
                        pageLayout.root.addView(view)
                    }
                }

            }
            pageLayout.root.visibility = View.GONE
            pageViews[index] = pageLayout.root
            binding.container.addView(pageViews[index])
        }
    }

    override fun onResume() {
        super.onResume()
        mCountDownTimer?.cancel()
        viewModel.pageStateLiveData.value?.let {

            responseModel?.pages!![viewModel.pageStateLiveData.value?.first!!]?.properties?.timeout?.let {
                if (it.enable == true && it.time!! > 0) {
                    mCountDownTimer = object : CountDownTimer(it.time.toLong(), 10L) {
                        override fun onTick(p0: Long) {

                        }

                        override fun onFinish() {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.back.visibility =
                                View.GONE
                            binding.next.visibility =
                                View.GONE
                            binding.submit.visibility =
                                View.GONE
                            viewModel.getWidgets()
                            binding.container.removeAllViews()
                            pageViews.clear()
                            condition = null
                            finalPageCondition = null
                        }

                    }

                    mCountDownTimer?.start()
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        Log.i("TOUCH", "SCREEN REACTING")
        hideKeyboard()
        mCountDownTimer?.cancel()
        viewModel.pageStateLiveData.value?.let {

            responseModel?.pages!![viewModel.pageStateLiveData.value?.first!!]?.properties?.timeout?.let {

                if (it.enable == true && it.time!! > 0) {
                    mCountDownTimer = object : CountDownTimer(it.time.toLong(), 10L) {
                        override fun onTick(p0: Long) {

                        }

                        override fun onFinish() {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.back.visibility =
                                View.GONE
                            binding.next.visibility =
                                View.GONE
                            binding.submit.visibility =
                                View.GONE
                            viewModel.getWidgets()
                            binding.container.removeAllViews()
                            pageViews.clear()
                            condition = null
                            finalPageCondition = null
                        }

                    }

                    mCountDownTimer?.start()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onBackPressed() {
        if (viewModel.pageStateLiveData.value != null && viewModel.pageStateLiveData.value?.first != 0) {
            viewModel.pageStateLiveData.value =
                Pair(viewModel.pageStateLiveData.value?.first?.minus(1) ?: 0, true)
        } else {
            pageViews.forEach { (_, linearLayoutCompat) ->
                linearLayoutCompat?.visibility = View.GONE
            }
            binding.next.visibility = View.GONE
            binding.back.visibility = View.GONE
            binding.submit.visibility = View.GONE
            getLanguageFromUser()
        }
//        if (viewModel.pageStateLiveData.value != null && viewModel.pageStateLiveData.value?.first != 0) {
//            viewModel.pageStateLiveData.value =
//                Pair(viewModel.pageStateLiveData.value?.first?.minus(1) ?: 0, false)
//        } else {
//            if (languageIsActive) {
//                super.onBackPressed()
//            } else {
//                getLanguageFromUser()
//            }
//        }
    }

    private fun populateCustomerView(pageComponent: GetWidgetsResponseModel.CustomerData): LinearLayoutCompat? {

        val container = PageLinearLayoutBinding.bind(
            LayoutInflater.from(this)
                .inflate(
                    R.layout.page_linear_layout,
                    binding.container,
                    false
                )
        )

        val attrs = pageComponent.attrs

        val title = TitleViewBinding.bind(
            LayoutInflater.from(this)
                .inflate(
                    R.layout.title_view,
                    binding.container,
                    false
                )
        )

        title.root.text = pageComponent.component_title!![language]

        title.root.setTextColor(pageComponent.component_title_text_color.getColor())
        title.root.background.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                pageComponent.component_title_bg_color.getColor(),
                BlendModeCompat.SRC_ATOP
            )
        title.root.setDynamicSize(pageComponent.component_title_size)
        if (pageComponent.is_component_title == true)
            container.root.addView(title.root)

        attrs.sortBy { it.position }
        attrs.forEach {
            when (it.type) {
                "text" -> {
                    val view = InputFromUserViewBinding.bind(
                        LayoutInflater.from(this)
                            .inflate(
                                R.layout.input_from_user_view,
                                binding.container,
                                false
                            )
                    )
                        .apply {
                            this.passwordEt.setDynamicSize(it.label_text_size)
                            this.passwordEt.tag = it.name
                            this.passwordEt.hint = it.placeholder!![language] ?: ""
                            if (it.name == "email") {
                                this.loginInputLayout.setStartIconDrawable(R.drawable.ic_email)
                            } else if (it.name == "full_name"){
                                this.loginInputLayout.setStartIconDrawable(R.drawable.ic_full_name)
                            }
                        }
                    container.root.addView(view.root)
                }
                "date" -> {
                    val view = InputFromUserViewBinding.bind(
                        LayoutInflater.from(this)
                            .inflate(
                                R.layout.input_from_user_view,
                                binding.container,
                                false
                            )
                    )
                        .apply {
                            this.passwordEt.tag = it.name
                            this.passwordEt.hint = it.placeholder!![language] ?: ""
                            this.passwordEt.setDynamicSize(it.label_text_size)
                            this.passwordEt.setOnClickListener {
                                val newCalendar = Calendar.getInstance()
                                DatePickerDialog(
                                    this@MainActivity,
                                    R.style.DialogTheme,
                                    { _, year, monthOfYear, dayOfMonth ->
                                        this.passwordEt.setText(
                                            getString(
                                                R.string.date_picker_format,
                                                String.format(
                                                    "%02d", dayOfMonth
                                                ),
                                                String.format(
                                                    "%02d", (monthOfYear + 1)
                                                ),
                                                year.toString()
                                            )
                                        )
                                    },
                                    newCalendar[Calendar.YEAR],
                                    newCalendar[Calendar.MONTH],
                                    newCalendar[Calendar.DAY_OF_MONTH]
                                ).show()
                            }
                        }
                    container.root.addView(view.root)
                }
                "number" -> {
                    val view = NumberInputFromUserViewBinding.bind(
                        LayoutInflater.from(this)
                            .inflate(
                                R.layout.number_input_from_user_view,
                                binding.container,
                                false
                            )
                    )
                        .apply {
                            numberField.tag = it.name
                            numberField.hint = it.placeholder!![language] ?: ""
                            numberField.setDynamicSize(it.label_text_size)
                            numberField.setText(it.prefix)
                            numberField.setSelection(it.prefix?.trim()?.length?: 0)
                            if (it.name == "phone_number"){
                                this.loginInputLayout.setStartIconDrawable(R.drawable.ic_phone_number)
                            }
//                            Selection.setSelection(editText.text, editText.text?.length ?: 0)
//
//                            this.passwordEt.addTextChangedListener(object : TextWatcher {
//                                override fun onTextChanged(
//                                    s: CharSequence,
//                                    start: Int,
//                                    before: Int,
//                                    count: Int
//                                ) {
//                                }
//
//                                override fun beforeTextChanged(
//                                    s: CharSequence, start: Int, count: Int,
//                                    after: Int
//                                ) {
//                                }
//
//                                override fun afterTextChanged(s: Editable) {
//                                    if (!s.toString().startsWith(it.prefix ?: "")) {
//                                        editText.setText(it.prefix ?: "")
//                                        Selection.setSelection(
//                                            editText.text,
//                                            editText.text?.length ?: 0
//                                        )
//                                    }
//                                }
//                            })
                        }
                    container.root.addView(view.root)
                }
                "select" -> {
                    if (it.select_design == null || it.select_design.value == "dropdown") {
                        val view = SelectDropdownViewBinding.bind(
                            LayoutInflater.from(this)
                                .inflate(
                                    R.layout.select_dropdown_view,
                                    binding.container,
                                    false
                                )
                        )
                            .apply {
                                val spinnerArrayAdapter = SingleSelectAdapter(
                                    language,
                                    this@MainActivity,
                                    R.layout.single_select_view,
                                    android.R.layout.simple_spinner_item,
                                    it.label_text_size,
                                    mutableListOf<GetWidgetsResponseModel.SelectOption?>().apply {
                                        val placeHolderOption = hashMapOf<String, String>()
                                        placeHolderOption[language] = it.placeholder!![language]!!
                                        val placeHolder = GetWidgetsResponseModel.SelectOption(
                                            id = "placeholder",
                                            option = placeHolderOption
                                        )
                                        add(placeHolder)
                                        it.select?.forEach { selectOption ->
                                            this.add(selectOption)
                                        }
                                    }
                                )

                                this.root.adapter = spinnerArrayAdapter
                                this.root.tag = it.name
                            }
                        container.root.addView(view.root)
                    } else if (it.select_design.value == "radio_button") {
                        val containerView = RadioGroupContainerViewBinding.bind(
                            LayoutInflater.from(this)
                                .inflate(
                                    R.layout.radio_group_container_view,
                                    binding.container,
                                    false
                                )
                        )
                        it.select?.forEach { selectOption ->
                            val checkBox = AppCompatRadioButton(this@MainActivity)
                                .apply {
                                    this.text = selectOption.option!![language]
                                }
                            containerView.radioGroup.addView(checkBox)
                        }
                        containerView.titleTextView.text = it.placeholder!![language] ?: ""
                        containerView.titleTextView.setDynamicSize(it.label_text_size)
                        containerView.titleTextView.setTextColor(it.label_text_color.getColor())
                        containerView.titleTextView.background?.colorFilter =
                            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                it.label_bg_color.getColor(),
                                BlendModeCompat.SRC_ATOP
                            )
                        containerView.radioGroup.tag = it.name
                        container.root.addView(containerView.root)
                    }
                }
                "multi-select" -> {
                    val containerView = MultiSelectContainerViewBinding.bind(
                        LayoutInflater.from(this)
                            .inflate(
                                R.layout.multi_select_container_view,
                                binding.container,
                                false
                            )
                    )

                    containerView.titleTextView.text = it.placeholder!![language]
                    containerView.titleTextView.setDynamicSize(it.label_text_size)
                    containerView.titleTextView.setTextColor(it.label_text_color.getColor())
                    containerView.titleTextView.background?.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            it.label_bg_color.getColor(),
                            BlendModeCompat.SRC_ATOP
                        )
//                    containerView.title_text_view.setBackgroundColor(it.label_bg_color.getColor())
                    it.select?.forEach { selectOption ->
                        val checkBox = MultiSelectCheckboxBinding.bind(
                            LayoutInflater.from(this)
                                .inflate(
                                    R.layout.multi_select_checkbox,
                                    binding.container,
                                    false
                                )
                        )
                        checkBox.root.text = selectOption.option!![language] ?: ""
                        checkBox.root.tag = selectOption.id

                        containerView.root.addView(checkBox.root)
                    }

                    containerView.root.tag = it.name
                    container.root.addView(containerView.root)
                }
            }
        }
        container.root.tag = CUSTOMER_TAG
        return container.root
    }

    private fun populateCustomFeedbackView(pageComponent: GetWidgetsResponseModel.CustomFieldFeedbackComponent): LinearLayoutCompat? {

        val container = PageLinearLayoutBinding.bind(
            LayoutInflater.from(this)
                .inflate(
                    R.layout.page_linear_layout,
                    binding.container,
                    false
                )
        )

        val attrs = pageComponent.attrs

        attrs?.sortBy { it.position }
        attrs?.forEach {
            when (it.type) {
                "text" -> {
                    val view = InputFromUserViewBinding.bind(
                        LayoutInflater.from(this)
                            .inflate(
                                R.layout.input_from_user_view,
                                binding.container,
                                false
                            )
                    )
                        .apply {
                            this.passwordEt.tag = it.name
                            this.passwordEt.hint = it.placeholder!![language] ?: ""
                        }
                    container.root.addView(view.root)

                }
                "number" -> {
                    val view = NumberInputFromUserViewBinding.bind(
                        LayoutInflater.from(this)
                            .inflate(
                                R.layout.number_input_from_user_view,
                                binding.container,
                                false
                            )
                    )
                        .apply {
                            numberField.tag = it.name
                            numberField.hint = it.placeholder!![language] ?: ""
                            numberField.setDynamicSize(it.label_text_size)
                            numberField.setText(it.prefix)
                            numberField.setSelection(it.prefix?.trim()?.length?: 0)
                            if (it.name == "phone_number"){
                                this.loginInputLayout.setStartIconDrawable(R.drawable.ic_phone_number)
                            }
//                            Selection.setSelection(editText.text, editText.text?.length ?: 0)
//
//                            this.passwordEt.addTextChangedListener(object : TextWatcher {
//                                override fun onTextChanged(
//                                    s: CharSequence,
//                                    start: Int,
//                                    before: Int,
//                                    count: Int
//                                ) {
//                                }
//
//                                override fun beforeTextChanged(
//                                    s: CharSequence, start: Int, count: Int,
//                                    after: Int
//                                ) {
//                                }
//
//                                override fun afterTextChanged(s: Editable) {
//                                    if (!s.toString().startsWith(it.prefix ?: "")) {
//                                        editText.setText(it.prefix ?: "")
//                                        Selection.setSelection(
//                                            editText.text,
//                                            editText.text?.length ?: 0
//                                        )
//                                    }
//                                }
//                            })
                        }
                    container.root.addView(view.root)
                }
                "date" -> {
                    val view = InputFromUserViewBinding.bind(
                        LayoutInflater.from(this)
                            .inflate(
                                R.layout.input_from_user_view,
                                binding.container,
                                false
                            )
                    )
                        .apply {
                            this.passwordEt.tag = it.name
                            this.passwordEt.hint = it.placeholder!![language] ?: ""
                            this.passwordEt.setOnClickListener { _ ->
                                val newCalendar = Calendar.getInstance()
                                DatePickerDialog(
                                    this@MainActivity,
                                    R.style.DialogTheme,
                                    { view, year, monthOfYear, dayOfMonth ->
                                        this.passwordEt.setText(
                                            getString(
                                                R.string.date_picker_format,
                                                String.format(
                                                    "%02d", dayOfMonth
                                                ),
                                                String.format(
                                                    "%02d", (monthOfYear + 1)
                                                ),
                                                year.toString()
                                            )
                                        )
                                    },
                                    newCalendar[Calendar.YEAR],
                                    newCalendar[Calendar.MONTH],
                                    newCalendar[Calendar.DAY_OF_MONTH]
                                ).show()
                            }
                        }
                    container.root.addView(view.root)
                }
                "select" -> {
                    if (it.select_design == null || it.select_design.value == "dropdown") {
                        val view = SelectDropdownViewBinding.bind(
                            LayoutInflater.from(this)
                                .inflate(
                                    R.layout.select_dropdown_view,
                                    binding.container,
                                    false
                                )
                        )
                            .apply {
                                val spinnerArrayAdapter = SingleSelectAdapter(
                                    language,
                                    this@MainActivity,
                                    R.layout.single_select_view,
                                    android.R.layout.simple_spinner_item,
                                    it.label_text_size,
                                    mutableListOf<GetWidgetsResponseModel.SelectOption?>().apply {
                                        val placeHolderOption = hashMapOf<String, String>()
                                        placeHolderOption[language] = it.placeholder!![language]!!
                                        val placeHolder = GetWidgetsResponseModel.SelectOption(
                                            id = "placeholder",
                                            option = placeHolderOption
                                        )
                                        add(placeHolder)
                                        it.select?.forEach { selectOption ->
                                            this.add(selectOption)
                                        }
                                    }
                                )

                                this.root.adapter = spinnerArrayAdapter
                                this.root.tag = it.name
                            }
                        container.root.addView(view.root)
                    } else if (it.select_design.value == "radio_button") {
                        val containerView = RadioGroupContainerViewBinding.bind(
                            LayoutInflater.from(this)
                                .inflate(
                                    R.layout.radio_group_container_view,
                                    binding.container,
                                    false
                                )
                        )
                        it.select?.forEach { selectOption ->
                            val checkBox = AppCompatRadioButton(this@MainActivity)
                                .apply {
                                    this.text = selectOption.option!![language]
                                }
                            containerView.radioGroup.addView(checkBox)
                        }
                        containerView.titleTextView.text = it.placeholder!![language] ?: ""
                        containerView.titleTextView.setDynamicSize(it.label_text_size)
                        containerView.titleTextView.setTextColor(it.label_text_color.getColor())
                        containerView.titleTextView.background?.colorFilter =
                            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                it.label_bg_color.getColor(),
                                BlendModeCompat.SRC_ATOP
                            )
                        containerView.radioGroup.tag = it.name
                        container.root.addView(containerView.root)
                    }
                }
                "multi-select" -> {
                    val containerView = MultiSelectContainerViewBinding.bind(
                        LayoutInflater.from(this)
                            .inflate(
                                R.layout.multi_select_container_view,
                                binding.container,
                                false
                            )
                    )

                    containerView.titleTextView.text = it.placeholder!![language]
                    containerView.titleTextView.setDynamicSize(it.label_text_size)
                    containerView.titleTextView.setTextColor(it.label_text_color.getColor())
                    containerView.titleTextView.background?.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            it.label_bg_color.getColor(),
                            BlendModeCompat.SRC_ATOP
                        )
//                    containerView.title_text_view.setBackgroundColor(it.label_bg_color.getColor())
                    it.select?.forEach { selectOption ->
                        val checkBox = MultiSelectCheckboxBinding.bind(
                            LayoutInflater.from(this)
                                .inflate(
                                    R.layout.multi_select_checkbox,
                                    binding.container,
                                    false
                                )
                        )
                        checkBox.root.text = selectOption.option!![language] ?: ""
                        checkBox.root.tag = selectOption.id

                        containerView.root.addView(checkBox.root)
                    }

                    containerView.root.tag = it.name
                    container.root.addView(containerView.root)
                }
            }
        }
        container.root.tag = CUSTOM_FEEDBACK_TAG
        return container.root
    }

    private fun getLanguageFromUser() {
        val timer = Timer()
        binding.languageImageBackground.visibility = View.VISIBLE
        binding.languageImageBackgroundDimmer.visibility = View.VISIBLE
        binding.next.visibility = View.GONE
        binding.back.visibility = View.GONE
        binding.submit.visibility = View.GONE
        binding.container.removeView(languageContainer?.root)
        languageContainer = PageLanguageContainerLayoutBinding.bind(
            LayoutInflater.from(this)
                .inflate(
                    R.layout.page_language_container_layout,
                    binding.container,
                    false
                )
        )

        Glide.with(this).load(responseModel?.languagePage?.properties?.backgroundImageUrl)
            .into(binding.languageImageBackground)
        binding.languageImageBackgroundDimmer.alpha =
            responseModel?.languagePage?.properties?.dimmerOpacity?.toFloat() ?: 0.0f
        binding.languageImageBackgroundDimmer.setBackgroundColor(
            responseModel?.languagePage?.properties?.dimmerColor?.getColor() ?: 0
        )
        if (responseModel?.languagePage != null) {
            languageIsActive = true
            binding.logo.visibility = View.GONE
            val languages = responseModel?.languagePage?.languages
            val title = InputFromUserSliViewBinding.bind(
                LayoutInflater.from(this)
                    .inflate(
                        R.layout.input_from_user_sli_view,
                        binding.container,
                        false
                    )
            )

            title.apply {
                this.textView.text = languages?.firstOrNull()?.title ?: ""
                this.textView.setTextColor(responseModel?.languagePage?.properties?.titleColor.getColor())
                this.textView.setDynamicSize(responseModel?.languagePage?.properties?.animatedTitleSize)
                root.background = null
            }
            if (responseModel?.languagePage?.properties?.backgroundImageUrl.isNullOrEmpty())
                binding.motherLayout.setBackgroundColor(responseModel?.languagePage?.properties?.pageBackground?.getColor()!!)
            if (languages?.size?.compareTo(1) == 0 || languages.isNullOrEmpty()) {
                timer.cancel()
                languageIsActive = false
                binding.languageImageBackground.visibility = View.GONE
                binding.languageImageBackgroundDimmer.visibility = View.GONE
                language = languages?.firstOrNull()?.langCode ?: "en"
                viewModel.requestModel["language"] = language
                binding.logo.visibility = View.VISIBLE
                initializeViews()
                viewModel.pageStateLiveData.value = Pair(0, false)
            } else {
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        runOnUiThread {

                            val index =
                                languages.indexOfFirst { it.title == title.textView.text.toString() }
                            val newIndex = if (index == languages.size.minus(1)) {
                                0
                            } else {
                                index.plus(1)
                            }
                            title.textView.text = languages[newIndex].title
                        }
                    }

                }, 0L, 2000L)
                val smileView = InputFromUserSliFontViewFinalPageBinding.bind(
                    LayoutInflater.from(this)
                        .inflate(
                            R.layout.input_from_user_sli_font_view_final_page,
                            binding.container,
                            false
                        )
                )
                smileView.smileView.apply {
                    when (responseModel?.languagePage?.properties?.smileySize) {
                        "S" -> {
                            this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                        }
                        "M" -> {
                            this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 52f)
                        }
                        "L" -> {
                            this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 82f)
                        }
                    }
                    text = "A"
                    setTextColor(responseModel?.languagePage?.properties?.smileyColor?.getColor()!!)
                }
                responseModel?.languagePage?.properties?.showSmile?.let {
                    if (it)
                        languageContainer?.languageContainer?.addView(smileView.root)
                }
                languageContainer?.languageContainer?.addView(title.root)
                languages.forEach { languageModel ->

                    val linearLayout = ChooseLanguageViewBinding.bind(
                        LayoutInflater.from(this)
                            .inflate(
                                R.layout.choose_language_view,
                                binding.container,
                                false
                            )
                    )

                    linearLayout.apply {
//                            backgroundTintList = ColorStateList.valueOf(responseModel?.languagePage?.properties?.languageLabelBgColor?.getColor()!!)
                        val backgroundDrawable = ContextCompat.getDrawable(
                            this@MainActivity,
                            R.drawable.rounded_input_language_background_8
                        )
                        val newBackground = (backgroundDrawable as? GradientDrawable)
                        newBackground?.setColor(responseModel?.languagePage?.properties?.languageLabelBgColor?.getColor()!!)
                        newBackground?.setStroke(
                            5,
                            responseModel?.languagePage?.properties?.languageLabelBorderColor?.getColor()!!
                        )
                        root.background = newBackground
                        var typeFace = ""
                        if (responseModel?.languagePage?.properties?.languageLabelStyle?.contains(
                                "b"
                            ) == true
                        ) {
                            typeFace += "b"
                        }
                        if (responseModel?.languagePage?.properties?.languageLabelStyle?.contains(
                                "u"
                            ) == true
                        ) {
                            textView.paintFlags =
                                textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                        }
                        if (responseModel?.languagePage?.properties?.languageLabelStyle?.contains(
                                "i"
                            ) == true
                        ) {
                            typeFace += "i"
                        }
                        val formattedString = if (typeFace == "bi") {
                            buildSpannedString {
                                bold { italic { append(languageModel.label) } }
                            }
                        } else if (typeFace == "i") {
                            buildSpannedString {
                                italic { append(languageModel.label) }
                            }
                        } else if (typeFace == "b") {
                            buildSpannedString {
                                bold { append(languageModel.label) }
                            }
                        } else {
                            languageModel.label
                        }
                        if (responseModel?.languagePage?.properties?.showLabels == false)
                            textView.visibility = View.GONE
                        textView.text = formattedString
                        textView.setTextColor(languageModel.labelColor.getColor())
                        textView.setDynamicSize(responseModel?.languagePage?.properties?.languageLabelSize)
                        root.setOnClickListener {
                            timer.cancel()
                            languageIsActive = false
                            binding.languageImageBackgroundDimmer.visibility = View.GONE
                            binding.languageImageBackground.visibility = View.GONE
                            language = languageModel.langCode ?: "en"
                            binding.logo.visibility = View.VISIBLE
                            viewModel.requestModel["language"] = language
                            initializeViews()
                            viewModel.pageStateLiveData.value = Pair(0, false)
                        }
                    }
                    linearLayout.flagImage.loadSvgOrOther(languageModel.flagUrl)
                    responseModel?.languagePage?.properties?.showFlags?.let {
                        if (it)
                            linearLayout.flagImageContainer.visibility = View.VISIBLE
                    }


                    languageContainer?.languageContainer?.addView(linearLayout.root)
                }


            }
            binding.container.addView(languageContainer?.root)
        } else {
            binding.languageImageBackgroundDimmer.visibility = View.GONE
            binding.languageImageBackground.visibility = View.GONE
            timer.cancel()
            languageIsActive = false
            language = "en"
            binding.logo.visibility = View.VISIBLE
            viewModel.pageStateLiveData.value = Pair(0, false)
        }
    }

    private fun populateCommentView(commentData: GetWidgetsResponseModel.CommentData): LinearLayoutCompat? {
        val container = PageLinearLayoutBinding.bind(
            LayoutInflater.from(this)
                .inflate(
                    R.layout.page_linear_layout,
                    binding.container,
                    false
                )
        )

        val view = CommentBoxInputBinding.bind(
            LayoutInflater.from(this)
                .inflate(
                    R.layout.comment_box_input,
                    binding.container,
                    false
                )
        )
        val componentTitle = InputFromUserSliViewBinding.bind(
            LayoutInflater.from(this)
                .inflate(
                    R.layout.input_from_user_sli_view,
                    binding.container,
                    false
                )
        )
            .apply {
                this.textView.setDynamicSize(commentData.componentTitleSize)
                this.textView.text = commentData.componentTitle!![language] ?: ""
                this.textView.setTextColor(commentData.componentTitleTextColor.getColor())
                root.background?.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        commentData.componentTitleBgColor.getColor(),
                        BlendModeCompat.SRC_ATOP
                    )
            }
        val title = InputFromUserSliViewBinding.bind(
            LayoutInflater.from(this)
                .inflate(
                    R.layout.input_from_user_sli_view,
                    binding.container,
                    false
                )
        )
            .apply {
                this.textView.setDynamicSize(commentData.attrs?.label_text_size)
                this.textView.text = commentData.attrs?.label!![language] ?: ""
                this.textView.setTextColor(commentData.attrs.label_text_color.getColor())
                root.background?.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        commentData.componentTitleBgColor.getColor(),
                        BlendModeCompat.SRC_ATOP
                    )
            }
        view.root.minimumHeight =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200.0f, resources.displayMetrics)
                .toInt()
        view.comment.tag = commentData.attrs?.name
        view.comment.setDynamicSize(commentData.attrs?.label_text_size)
        view.comment.setTextColor(commentData.attrs?.label_text_color.getColor())
        view.comment.hint = commentData.attrs?.placeholder!![language] ?: ""
        view.comment.background.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                commentData.attrs.textarea_bg_color?.getColor() ?: 0,
                BlendModeCompat.SRC_ATOP
            )

        container.root.tag = COMMENT_TAG
        if (commentData.isComponentTitle == true)
            container.root.addView(componentTitle.root)
        container.root.addView(title.root)
        container.root.addView(view.root)

        return container.root
    }

    private fun populateSliView(sliData: GetWidgetsResponseModel.SliData): LinearLayoutCompat? {

        val container = PageLinearLayoutBinding.bind(
            LayoutInflater.from(this)
                .inflate(
                    R.layout.page_linear_layout,
                    binding.container,
                    false
                )
        )

        sliData.componentTitle!![language]?.let {
            val title = InputFromUserSliViewBinding.bind(
                LayoutInflater.from(this)
                    .inflate(
                        R.layout.input_from_user_sli_view,
                        binding.container,
                        false
                    )
            ).apply {
                this.textView.text = it
                this.textView.setTextColor(sliData.componentTitleTextColor.getColor())
                root.background?.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        sliData.componentTitleBgColor.getColor(),
                        BlendModeCompat.SRC_ATOP
                    )
                textView.setDynamicSize(sliData.componentTitleSize)
            }
            container.root.addView(title.root)
        }


        sliData.attrs?.service?.sortedBy { it.position }?.forEach { service ->

            val sliComponentLayout = SliPageLinearLayoutBinding.bind(
                LayoutInflater.from(this)
                    .inflate(R.layout.sli_page_linear_layout, binding.container, false)
            )
            sliComponentLayout.root.background?.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    service.bgColor.getColor(),
                    BlendModeCompat.SRC_ATOP
                )
            val linearLayout = LinearLayoutViewBinding.bind(
                LayoutInflater.from(this)
                    .inflate(
                        R.layout.linear_layout_view,
                        binding.container,
                        false
                    )
            )

            val serviceTitle = SliTitleViewBinding.bind(
                LayoutInflater.from(this)
                    .inflate(
                        R.layout.sli_title_view,
                        binding.container,
                        false
                    )
            )
                .apply {
                    this.sliText.text = service.name!![language] ?: ""
                    this.sliText.setDynamicSize(sliData.attrs.serviceNameSize)
                    this.sliText.setTextColor(service.textColor.getColor())
                }

            service.rateOptions.forEachIndexed { index, rateOption ->
                val view = InputFromUserSliFontViewBinding.bind(
                    LayoutInflater.from(this)
                        .inflate(
                            R.layout.input_from_user_sli_font_view,
                            binding.container,
                            false
                        )
                )
                    .apply {
                        root.background?.colorFilter =
                            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                service.rateBgColor.getColor(),
                                BlendModeCompat.SRC_ATOP
                            )
                    }
                view.textView.apply {
                    text = rateOption.name?.resolveIconFromAwesome()
                    setTextColor(service.rateIconColor.getColor())
                    view.sliText.setTextColor(service.rateIconColor.getColor())
                    background?.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            rateOption.bgColor.getColor(),
                            BlendModeCompat.SRC_ATOP
                        )
                    setOnClickListener {
                        linearLayout.root.forEachIndexed { index, child ->
                            val childBinding = InputFromUserSliFontViewBinding.bind(child)
                            if (view.root != child) {
                                service.rateOptions[index].selected = false
                                childBinding.textView.setTextColor(service.rateIconColor.getColor())
                                childBinding.sliText?.setTextColor(service.rateIconColor.getColor())
                            } else {
                                service.rateOptions[index].selected = true
                                this.setTextColor(service.rateSelectedColor.getColor())
                                childBinding.sliText?.setTextColor(service.rateSelectedColor.getColor())
                            }
                        }
                        sliCondition[service.name!![language]] = rateOption.name
                        if (rateOption.markpageIdx != null) {
                            popUpMarkPage(view.textView.text.toString(), rateOption)
                        } else if (responseModel?.pages!![viewModel.pageStateLiveData.value?.first!!]!!.makePages()
                                .filter { it != null }.size == 1
                            && sliData.attrs.service.size == 1
                        ) {

                            viewModel.pageStateLiveData.postValue(
                                Pair(
                                    viewModel.pageStateLiveData.value?.first!!.plus(
                                        1
                                    ), false
                                )
                            )

                        }
                    }
                }

                view.sliText.text = rateOption.label!![language]

                if (sliData.showRateLabels == true)
                    view.sliText.visibility = View.VISIBLE
                else
                    view.sliText.visibility = View.GONE
                linearLayout.root.addView(view.root)
                if (index > 0) {
                    val params = sliComponentLayout?.root.layoutParams as? LinearLayout.LayoutParams
                    params?.setMargins(
                        0,
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            22f,
                            resources.displayMetrics
                        ).toInt(),
                        0,
                        0
                    )
                    sliComponentLayout.root.layoutParams = params
                }
            }


            container.root.tag = SLI_TAG
            if (sliData.attrs.service.size > 1)
                sliComponentLayout.root.addView(serviceTitle.root)
            sliComponentLayout.root.addView(linearLayout.root)
            container.root.addView(sliComponentLayout.root)
        }

        return container.root
    }

    private fun popUpExitDialog() {
        val dialog = AlertDialog.Builder(this)
        val dialogView = DialogExitViewBinding.bind(
            LayoutInflater.from(this)
                .inflate(
                    R.layout.dialog_exit_view,
                    binding.container,
                    false
                )
        )

        dialog.setView(dialogView.root)
        val alertDialog = dialog.create()
        dialogView.submitExit.setOnClickListener {
            dialogView.passwordEt.text?.toString()?.let {
                if (it == sharedPreferences.getString("password", "")) {
                    viewModel.logout()
                    alertDialog.dismiss()
                }
            }
        }
        dialogView.cancel.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun popUpMarkPage(
        sliText: String,
        rateOption: GetWidgetsResponseModel.RateOptions?
    ) {
        val filteredList = responseModel?.markPageData?.filter { it.idx == rateOption?.markpageIdx }
        val markPageData = filteredList?.firstOrNull()
        val dialog = AlertDialog.Builder(this)
        val dialogView = MarkpageChooseViewBinding.bind(
            LayoutInflater.from(this)
                .inflate(
                    R.layout.markpage_choose_view,
                    binding.container,
                    false
                )
        )
        dialogView.submit.text = responseModel?.generalSettings?.submit_button_text!![language]
        dialogView.submit.setTextColor(markPageData?.submitButtonTextColor.getColor())
        dialogView.submit.background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            markPageData?.submitButtonBgColor.getColor(),
            BlendModeCompat.SRC_ATOP
        )
        dialog.setView(dialogView.root)

        dialogView.sliTextView.text = sliText
        dialogView.sliTextView.setTextColor(markPageData?.rateIconColor?.getColor()!!)
        dialogView.title.text = markPageData.title!![language]
        dialogView.title.setTextColor(markPageData.titleTextColor.getColor())


        val alertDialog = dialog.create()
        markPageData.marks.forEach { mark ->
            val markText = MarkChooseTextViewBinding.bind(
                LayoutInflater.from(this)
                    .inflate(
                        R.layout.mark_choose_text_view,
                        dialogView.markContainer,
                        false
                    )
            )
            markText.apply {

                this.checkMarkText.text = mark.name!![language]
                this.checkMarkText.setTextColor(mark.textColor.getColor())
                val backgroundDrawable = ContextCompat.getDrawable(
                    this@MainActivity,
                    R.drawable.rounded_input_language_background_8
                )
                val newBackground = (backgroundDrawable as? GradientDrawable)
                newBackground?.setColor(mark.bgColor.getColor())
                newBackground?.setStroke(
                    5,
                    mark.markBorderColor.getColor()
                )

                root.background = newBackground
                if (!mark.selected!!) {
                    this.root.background.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            mark.bgColor.getColor(),
                            BlendModeCompat.SRC_ATOP
                        )
                } else {

                    this.root.background.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            mark.markSelectedColor.getColor(),
                            BlendModeCompat.SRC_ATOP
                        )
                }
                if (markPageData.isSingle == true) {
                    dialogView.skip.visibility = View.GONE
                    dialogView.submit.visibility = View.GONE
                    root.setOnClickListener {
                        dialogView.markContainer.forEachIndexed { index, view ->
                            if (root != view) {
                                markPageData.marks[index].selected = false

                                this.root.background.colorFilter =
                                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                        mark.bgColor.getColor(),
                                        BlendModeCompat.SRC_ATOP
                                    )
                            } else {
                                this.root.background.colorFilter =
                                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                        mark.markSelectedColor.getColor(),
                                        BlendModeCompat.SRC_ATOP
                                    )
                            }
                        }
                        alertDialog.dismiss()
                        if (responseModel?.pages!![viewModel.pageStateLiveData.value?.first!!]!!.makePages()
                                .filter { it != null }.size == 1
                            && (responseModel?.pages!![viewModel.pageStateLiveData.value?.first!!]!!.makePages()
                                .filter { it != null }
                                .firstOrNull() as? GetWidgetsResponseModel.SliData)?.attrs?.service?.size == 1
                        ) {
                            viewModel.pageStateLiveData.postValue(
                                Pair(
                                    viewModel.pageStateLiveData.value?.first!!.plus(
                                        1
                                    ), false
                                )
                            )
                        }
                    }
                } else {
                    root.setOnClickListener {
                        if (mark.selected == true) {
                            mark.selected = false
                            this.root.background.colorFilter =
                                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                    mark.bgColor.getColor(),
                                    BlendModeCompat.SRC_ATOP
                                )
                        } else {
                            mark.selected = true
                            this.root.background.colorFilter =
                                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                    mark.markSelectedColor.getColor(),
                                    BlendModeCompat.SRC_ATOP
                                )
                        }
                    }
                }
            }
            dialogView.markContainer.addView(markText.root)
        }
        dialogView.skip.setOnClickListener {
            markPageData.marks.forEach {
                it.selected = false
            }
            alertDialog.dismiss()
        }
        dialogView.submit.setOnClickListener {
            alertDialog.dismiss()
            if (responseModel?.pages!![viewModel.pageStateLiveData.value?.first!!]!!.makePages()
                    .filter { it != null }.size == 1
                && (responseModel?.pages!![viewModel.pageStateLiveData.value?.first!!]!!.makePages()
                    .filter { it != null }
                    .firstOrNull() as? GetWidgetsResponseModel.SliData)?.attrs?.service?.size == 1
            ) {
                viewModel.pageStateLiveData.postValue(
                    Pair(
                        viewModel.pageStateLiveData.value?.first!!.plus(
                            1
                        ), false
                    )
                )
            }
        }
        alertDialog.show()
    }
}