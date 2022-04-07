package com.example.qmeter.presentation.main

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.*
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.activity.viewModels
import androidx.appcompat.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import androidx.core.view.marginTop
import androidx.core.view.size
import com.bumptech.glide.Glide
import com.example.qmeter.R
import com.example.qmeter.databinding.ActivityMainBinding
import com.example.qmeter.di.base.BaseActivity
import com.example.qmeter.di.factory.ViewModelProviderFactory
import com.example.qmeter.presentation.auth.AuthenticateActivity
import com.example.qmeter.service.model.remote.response.GetWidgetsResponseModel
import com.example.qmeter.utils.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.choose_language_view.view.*
import kotlinx.android.synthetic.main.dialog_exit_view.view.*
import kotlinx.android.synthetic.main.input_from_user_sli_font_view_final_page.view.*
import kotlinx.android.synthetic.main.input_from_user_sli_view.view.textView
import kotlinx.android.synthetic.main.input_from_user_view.view.passwordEt
import kotlinx.android.synthetic.main.mark_choose_text_view.view.*
import kotlinx.android.synthetic.main.page_language_container_layout.view.*
import kotlinx.android.synthetic.main.markpage_choose_view.view.*
import kotlinx.android.synthetic.main.markpage_choose_view.view.submit
import kotlinx.android.synthetic.main.multi_select_container_view.view.*
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

    private var languageContainer: ConstraintLayout? = null

    private var sliCondition = hashMapOf<String?, String?>()

    private var condition: GetWidgetsResponseModel.ConditionOverallData? = null

    private var finalPageCondition: GetWidgetsResponseModel.Reaction? = null

    private var language = "en"

    private var mCountDownTimer: CountDownTimer? = null

    private var exitCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setPageView()

        setListener()
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

        viewModel.pageStateLiveData.observe(this) {
            val pageIndex = it.first
            val back = it.second
            languageContainer?.visibility = View.GONE
            pageViews[pageIndex]?.visibility = View.VISIBLE
            pageViews[pageIndex + 1]?.visibility = View.GONE
            pageViews[pageIndex - 1]?.visibility = View.GONE
            val handler = Handler(Looper.getMainLooper())

            mCountDownTimer?.cancel()
            pages[pageIndex]?.properties?.timeout?.let {
                if (it.time!! > 0 && it.enable == true) {
                    mCountDownTimer = object :CountDownTimer(it.time.toLong(), 10L){
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
            binding.next.background?.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    pages[pageIndex]?.properties?.nextButtonBgColor.getColor() ?: 0,
                    BlendModeCompat.SRC_ATOP
                )

            binding.back.setTextColor(pages[pageIndex]?.properties?.backButtonTxtColor.getColor())
            binding.back.background?.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    pages[pageIndex]?.properties?.backButtonBgColor.getColor(),
                    BlendModeCompat.SRC_ATOP
                )


            binding.submit.setTextColor(pages[pageIndex]?.properties?.submitButtonTxtColor.getColor())
            binding.back.background?.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    pages[pageIndex]?.properties?.submitButtonBgColor.getColor(),
                    BlendModeCompat.SRC_ATOP
                )

            binding.motherLayout.setBackgroundColor(responseModel?.pages!![pageIndex]?.properties?.pageBg?.getColor()!!)

            if (sliCondition.isNotEmpty()) {
                var sliFeedBacks = ""
                sliCondition.forEach {
                    sliFeedBacks += it.value
                }
                if (sliFeedBacks.contains("unacceptable") || sliFeedBacks.contains("bad")) {
                    finalPageCondition = responseModel?.finalPageData?.negative
                    condition = pages[pageIndex]?.condition?.overall?.negative
                } else if (sliFeedBacks.contains("excellent") || sliFeedBacks.contains("good")) {
                    finalPageCondition = responseModel?.finalPageData?.positive
                    condition = pages[pageIndex]?.condition?.overall?.positive
                } else if (sliFeedBacks.contains("neutral")) {
                    finalPageCondition = responseModel?.finalPageData?.neutral
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
                        next.visibility = View.GONE
                    } else {
                        next.visibility = View.VISIBLE
                        binding.submit.visibility = View.GONE
                    }
                }
            }
            if (pageIndex == 0 && !responseModel?.languagePage?.languages.isNullOrEmpty() && responseModel?.languagePage?.languages?.size == 1)
                binding.back.visibility = View.GONE
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
                pageViews.forEach { (_, linearLayoutCompat) ->
                    linearLayoutCompat?.visibility = View.GONE
                }
                binding.next.visibility = View.GONE
                binding.back.visibility = View.GONE
                binding.submit.visibility = View.GONE
                getLanguageFromUser()
            }
        }
        binding.submit.setOnClickListener {
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
            val finalPageData = responseModel?.finalPageData
            finalPageData?.pageBg?.let {
                binding.motherLayout.setBackgroundColor(it.getColor())
            }
            val title = LayoutInflater.from(this)
                .inflate(
                    R.layout.input_from_user_sli_font_view_final_page,
                    binding.container,
                    false
                ).apply {
                    finalPageCondition?.let {
                        this.smileView.text =
                            it.text!![language]?.resolveIconFromAwesome()
                        this.smileView.setBackgroundColor(
                            it.textBgColor.getColor() ?: 0
                        )
                        this.smileView.setTextColor(it.textColor.getColor())
                        this.smileView.setDynamicSize(it.textSize)
                    }
                }
            val finalText = LayoutInflater.from(this)
                .inflate(
                    R.layout.input_from_user_sli_view,
                    binding.container,
                    false
                ).apply {
                    finalPageCondition?.let {

                        this.textView.text =
                            it.text!![language]
                        this.textView.background?.colorFilter =
                            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                it.textBgColor.getColor(),
                                BlendModeCompat.SRC_ATOP
                            )
                        this.textView.setTextColor(it.textColor.getColor())
                        this.textView.setDynamicSize(it.textSize)
                    }
                }
            binding.container.addView(title)
            binding.container.addView(finalText)
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
            val pageLayout = LayoutInflater.from(this)
                .inflate(
                    R.layout.page_linear_layout,
                    binding.container,
                    false
                ) as? LinearLayoutCompat
            val title = LayoutInflater.from(this@MainActivity)
                .inflate(R.layout.input_from_user_sli_view, container, false)
                .apply {
                    this.textView.setDynamicSize(page?.properties?.pageTitleSize)
                    this.textView.text = page?.properties?.pageTitle!![language]
                    this.textView.setTextColor(page.properties.pageTitleTextColor.getColor() ?: 0)
                    background?.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            page.properties.pageTitleBgColor.getColor() ?: 0,
                            BlendModeCompat.SRC_ATOP
                        )
                }
            if (page?.properties?.isPageTitle == true)
                pageLayout?.addView(title)

            page?.makePages()?.sortedBy { it?.position }?.forEach { pageComponent ->
                when (pageComponent) {
                    is GetWidgetsResponseModel.CommentData -> {
                        val view = populateCommentView(pageComponent)
                        view.addTopMargin(24f, this)
                        pageLayout?.addView(view)
                    }
                    is GetWidgetsResponseModel.SliData -> {
                        val view = populateSliView(pageComponent)
                        view.addTopMargin(24f, this)
                        pageLayout?.addView(view)
                    }
                    is GetWidgetsResponseModel.CustomerData -> {
                        val view = populateCustomerView(pageComponent)
                        view.addTopMargin(24f, this)
                        pageLayout?.addView(view)
                    }
                    is GetWidgetsResponseModel.CustomFieldFeedbackComponent -> {
                        val view = populateCustomFeedbackView(pageComponent)
                        view.addTopMargin(24f, this)
                        pageLayout?.addView(view)
                    }
                }

            }
            pageLayout?.visibility = View.GONE
            pageViews[index] = pageLayout
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

        val container = LayoutInflater.from(this)
            .inflate(R.layout.page_linear_layout, binding.container, false) as? LinearLayoutCompat

        val attrs = pageComponent.attrs

        val title = LayoutInflater.from(this@MainActivity)
            .inflate(R.layout.title_view, container, false) as AppCompatTextView

        title.text = pageComponent.component_title!![language]

        title.setTextColor(pageComponent.component_title_text_color.getColor())
        title.background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            pageComponent.component_title_bg_color.getColor(),
            BlendModeCompat.SRC_ATOP
        )
        title.setDynamicSize(pageComponent.component_title_size)
        if (pageComponent.is_component_title == true)
            container?.addView(title)

        attrs.sortBy { it.position }
        attrs.forEach {
            when (it.type) {
                "text" -> {
                    val view = LayoutInflater.from(this)
                        .inflate(R.layout.input_from_user_view, binding.container, false)
                        .apply {
                            this.passwordEt?.setDynamicSize(it.label_text_size)
                            this.passwordEt.tag = it.name
                            this.passwordEt?.hint = it.placeholder!![language] ?: ""
                            if (it.name == "email") {
                                (this as? TextInputLayout)?.setEndIconDrawable(R.drawable.ic_email)
                            }
                        }
                    container?.addView(view)
                }
                "date" -> {
                    val view = LayoutInflater.from(this)
                        .inflate(R.layout.date_input_from_user_view, binding.container, false)
                        .apply {
                            this.passwordEt.tag = it.name
                            this.passwordEt?.hint = it.placeholder!![language] ?: ""
                            this.passwordEt?.setDynamicSize(it.label_text_size)
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
                    container?.addView(view)
                }
                "number" -> {
                    val view = LayoutInflater.from(this)
                        .inflate(R.layout.number_input_from_user_view, binding.container, false)
                        .apply {
                            val editText = this.passwordEt
                            editText.tag = it.name
                            editText?.hint = it.placeholder!![language] ?: ""
                            this.passwordEt?.setDynamicSize(it.label_text_size)
                            editText.setText(it.prefix)
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
                    container?.addView(view)
                }
                "select" -> {
                    if (it.select_design == null || it.select_design.value == "dropdown") {
                        val view = (LayoutInflater.from(this)
                            .inflate(
                                R.layout.select_dropdown_view,
                                binding.container,
                                false
                            ) as AppCompatSpinner)
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

                                this.adapter = spinnerArrayAdapter
                                this.tag = it.name
                            }
                        container?.addView(view)
                    } else if (it.select_design.value == "radio_button") {
                        val containerView = RadioGroup(this)
                        val title = LayoutInflater.from(this@MainActivity)
                            .inflate(R.layout.input_from_user_sli_view, containerView, false)
                            .apply {
                                this.textView.text = it.placeholder!![language] ?: ""
                            }
                            .apply {
                                it.select?.forEach { selectOption ->
                                    val checkBox = AppCompatRadioButton(this@MainActivity)
                                        .apply {
                                            this.text = selectOption.option!![language]
                                        }
                                    containerView.addView(checkBox)
                                }
                            }
                        val params = RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        params.setMargins(100, 0,0,0)
                        containerView.layoutParams = params
                        containerView.tag = it.name
                        container?.addView(title)
                        container?.addView(containerView)
                    }
                }
                "multi-select" -> {
                    val containerView = LayoutInflater.from(this@MainActivity)
                        .inflate(
                            R.layout.multi_select_container_view,
                            container,
                            false
                        ) as LinearLayoutCompat

                    containerView.title_text_view.text = it.placeholder!![language]
                    containerView.title_text_view.setDynamicSize(it.label_text_size)
                    containerView.title_text_view.setTextColor(it.label_text_color.getColor())
                    containerView.title_text_view?.background?.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            it.label_bg_color.getColor(),
                            BlendModeCompat.SRC_ATOP
                        )
//                    containerView.title_text_view.setBackgroundColor(it.label_bg_color.getColor())
                    it.select?.forEach { selectOption ->
                        val checkBox = LayoutInflater.from(this@MainActivity).inflate(
                            R.layout.multi_select_checkbox,
                            container,
                            false
                        ) as AppCompatCheckBox
                        checkBox.setDynamicSize(it.label_text_size)
                        checkBox.text = selectOption.option!![language] ?: ""
                        checkBox.tag = selectOption.id

                        containerView.addView(checkBox)
                    }

                    containerView.tag = it.name
                    container?.addView(containerView)
                }
            }
        }
        container?.tag = CUSTOMER_TAG
        return container
    }

    private fun populateCustomFeedbackView(pageComponent: GetWidgetsResponseModel.CustomFieldFeedbackComponent): LinearLayoutCompat? {

        val container = LayoutInflater.from(this)
            .inflate(R.layout.page_linear_layout, binding.container, false) as? LinearLayoutCompat

        val attrs = pageComponent.attrs

        attrs?.sortBy { it.position }
        attrs?.forEach {
            when (it.type) {
                "text" -> {
                    val view = LayoutInflater.from(this)
                        .inflate(R.layout.input_from_user_view, binding.container, false)
                        .apply {
                            this.passwordEt.tag = it.name
                            this.passwordEt?.hint = it.placeholder!![language] ?: ""
                        }
                    container?.addView(view)

                }
                "number" -> {
                    val view = LayoutInflater.from(this)
                        .inflate(R.layout.number_input_from_user_view, binding.container, false)
                        .apply {
                            val editText = this.passwordEt
                            editText.tag = it.name
                            editText?.hint = it.placeholder!![language] ?: ""
                            editText.setText(it.prefix)
                            Selection.setSelection(editText.text, editText.text?.length ?: 0)


                            this.passwordEt.addTextChangedListener(object : TextWatcher {
                                override fun onTextChanged(
                                    s: CharSequence,
                                    start: Int,
                                    before: Int,
                                    count: Int
                                ) {
                                }

                                override fun beforeTextChanged(
                                    s: CharSequence, start: Int, count: Int,
                                    after: Int
                                ) {
                                }

                                override fun afterTextChanged(s: Editable) {
                                    if (!s.toString().startsWith(it.prefix ?: "")) {
                                        editText.setText(it.prefix ?: "")
                                        Selection.setSelection(
                                            editText.text,
                                            editText.text?.length ?: 0
                                        )
                                    }
                                }
                            })

                        }
                    container?.addView(view)
                }
                "date" -> {
                    val view = LayoutInflater.from(this)
                        .inflate(R.layout.date_input_from_user_view, binding.container, false)
                        .apply {
                            this.passwordEt.tag = it.name
                            this.passwordEt?.hint = it.placeholder!![language] ?: ""
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
                    container?.addView(view)
                }
                "select" -> {
                    if (it.select_design == null || it.select_design.value == "dropdown") {
                        val view = (LayoutInflater.from(this)
                            .inflate(
                                R.layout.select_dropdown_view,
                                binding.container,
                                false
                            ) as AppCompatSpinner)
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

                                this.adapter = spinnerArrayAdapter
                                this.tag = it.name
                            }
                        container?.addView(view)
                    } else if (it.select_design.value == "radio_button") {
                        val containerView = LinearLayoutCompat(this)
                        val title = LayoutInflater.from(this@MainActivity)
                            .inflate(R.layout.input_from_user_sli_view, containerView, false)
                            .apply {
                                this.textView.text = it.placeholder!![language] ?: ""
                                this.textView.setTextColor(it.label_text_color?.getColor() ?: 0)
                            }
                            .apply {
                                it.select?.forEach { selectOption ->
                                    val checkBox = AppCompatRadioButton(this@MainActivity)
                                        .apply {
                                            this.text = selectOption.option!![language]
                                        }
                                    containerView.addView(checkBox)
                                }
                            }
                        val params = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
                        params.setMargins(100, 100,0,0)
                        containerView.layoutParams = params
                        containerView.tag = it.name
                        container?.addView(title)
                        container?.addView(containerView)
                    }
                }
                "multi-select" -> {
                    val containerView = LayoutInflater.from(this@MainActivity)
                        .inflate(
                            R.layout.multi_select_container_view,
                            container,
                            false
                        ) as LinearLayoutCompat

                    containerView.title_text_view.text = it.placeholder!![language]
                    containerView.title_text_view.setDynamicSize(it.label_text_size)
                    containerView.title_text_view.setTextColor(it.label_text_color.getColor())
                    containerView.title_text_view?.background?.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            it.label_bg_color.getColor(),
                            BlendModeCompat.SRC_ATOP
                        )
//                    containerView.title_text_view.setBackgroundColor(it.label_bg_color.getColor())
                    it.select?.forEach { selectOption ->
                        val checkBox = LayoutInflater.from(this@MainActivity).inflate(
                            R.layout.multi_select_checkbox,
                            container,
                            false
                        ) as AppCompatCheckBox
                        checkBox.setDynamicSize(it.label_text_size)
                        checkBox.text = selectOption.option!![language] ?: ""
                        checkBox.tag = selectOption.id

                        containerView.addView(checkBox)
                    }

                    containerView.tag = it.name
                    container?.addView(containerView)
                }
            }
        }
        container?.tag = CUSTOM_FEEDBACK_TAG
        return container
    }

    private fun getLanguageFromUser() {
        val timer = Timer()
        binding.languageImageBackground.visibility = View.VISIBLE
        binding.languageImageBackgroundDimmer.visibility = View.VISIBLE
        binding.next.visibility = View.GONE
        binding.back.visibility = View.GONE
        binding.submit.visibility = View.GONE
        binding.container.removeView(languageContainer)
        languageContainer = LayoutInflater.from(this)
            .inflate(R.layout.page_language_container_layout, binding.container, false) as? ConstraintLayout

        Glide.with(this).load(responseModel?.languagePage?.properties?.backgroundImageUrl).into(binding.languageImageBackground)
        binding.languageImageBackgroundDimmer.alpha = responseModel?.languagePage?.properties?.dimmerOpacity?.toFloat()?: 0.0f
        binding.languageImageBackgroundDimmer.setBackgroundColor(responseModel?.languagePage?.properties?.dimmerColor?.getColor()?: 0)
        if (responseModel?.languagePage != null) {
            languageIsActive = true
            binding.logo.visibility = View.GONE
            val languages = responseModel?.languagePage?.languages
            val title = (LayoutInflater.from(this)
                .inflate(
                    R.layout.input_from_user_sli_view,
                    binding.container,
                    false
                ) as? LinearLayoutCompat).apply {
                this?.textView?.text = languages?.firstOrNull()?.title ?: ""
                this?.textView?.setTextColor(responseModel?.languagePage?.properties?.titleColor.getColor())
                this?.textView?.setDynamicSize(responseModel?.languagePage?.properties?.animatedTitleSize)
                this?.background = null
            }
            timer.schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread {

                        val index =
                            languages?.indexOfFirst { it.title == title?.textView?.text.toString() }
                        val newIndex = if (index == languages?.size?.minus(1)) {
                            0
                        } else {
                            index?.plus(1)
                        }
                        title?.textView?.text = languages!![newIndex!!].title
                    }
                }

            }, 0L, 2000L)
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


                val smileView = LayoutInflater.from(this)
                    .inflate(
                        R.layout.input_from_user_sli_font_view_final_page,
                        languageContainer?.languageContainer,
                        false
                    )
                smileView.smileView.apply {
                    when(responseModel?.languagePage?.properties?.smileySize){
                        "S" -> {this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)}
                        "M" -> {this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 52f)}
                        "L" -> {this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 82f)}
                    }
                    text = "A"
                    setTextColor(responseModel?.languagePage?.properties?.smileyColor?.getColor()!!)
                }
                responseModel?.languagePage?.properties?.showSmile?.let {
                    if (it)
                        languageContainer?.languageContainer?.addView(smileView)
                }
                languageContainer?.languageContainer?.addView(title)
                languages.forEach { languageModel ->

                    val linearLayout = LayoutInflater.from(this)
                        .inflate(R.layout.choose_language_view, binding.container, false).apply {
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
                            background = newBackground

                            responseModel?.languagePage?.properties?.languageLabelStyle?.forEach {
                                when (it) {
                                    "b" -> textView.setTypeface(textView.typeface, Typeface.BOLD)
                                    "u" -> textView.paintFlags =
                                        textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                                    "i" -> textView.setTypeface(textView.typeface, Typeface.ITALIC)
                                }
                            }
                            if (responseModel?.languagePage?.properties?.showLabels == false)
                                textView.visibility = View.GONE
                            textView.text = languageModel.label
                            textView.setTextColor(languageModel.labelColor.getColor())
                            textView.setDynamicSize(responseModel?.languagePage?.properties?.languageLabelSize)
                            setOnClickListener {
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


                    languageContainer?.languageContainer?.addView(linearLayout)
                }


            }
            binding.container.addView(languageContainer)
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
        val container = LayoutInflater.from(this)
            .inflate(R.layout.page_linear_layout, binding.container, false) as? LinearLayoutCompat

        val view = LayoutInflater.from(this)
            .inflate(R.layout.input_from_user_view, container, false)
        val title = LayoutInflater.from(this@MainActivity)
            .inflate(R.layout.input_from_user_sli_view, container, false)
            .apply {
                this.textView.setDynamicSize(commentData.componentTitleSize)
                this.textView.text = commentData.componentTitle!![language] ?: ""
                this.textView.setTextColor(commentData.componentTitleTextColor?.getColor() ?: 0)
                background?.colorFilter =
                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        commentData.componentTitleBgColor?.getColor() ?: 0,
                        BlendModeCompat.SRC_ATOP
                    )
            }
        view.minimumHeight =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100.0f, resources.displayMetrics)
                .toInt()
        view.passwordEt?.tag = commentData.attrs?.name
        view.passwordEt?.setDynamicSize(commentData.attrs?.label_text_size)
        view.passwordEt?.setTextColor(commentData.attrs?.label_text_color.getColor())
        view.passwordEt?.hint = commentData.attrs?.placeholder!![language] ?: ""
        view.loginInputLayout.background.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                commentData.attrs.textarea_bg_color?.getColor() ?: 0,
                BlendModeCompat.SRC_ATOP
            )

        container?.tag = COMMENT_TAG
        if (commentData.isComponentTitle == true)
            container?.addView(title)
        container?.addView(view)

        return container
    }

    private fun populateSliView(sliData: GetWidgetsResponseModel.SliData): LinearLayoutCompat? {

        val container = LayoutInflater.from(this)
            .inflate(R.layout.page_linear_layout, binding.container, false) as? LinearLayoutCompat

        sliData.componentTitle!![language]?.let {
            val title = LayoutInflater.from(this)
                .inflate(R.layout.input_from_user_sli_view, container, false).apply {
                    this.textView.text = it
                    this.textView.setTextColor(sliData.componentTitleTextColor.getColor())
                    background?.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            sliData.componentTitleBgColor.getColor(),
                            BlendModeCompat.SRC_ATOP
                        )
                    textView.setDynamicSize(sliData.componentTitleSize)
                }
            container?.addView(title)
        }


        sliData.attrs?.service?.sortedBy { it.position }?.forEach { service ->

            val sliComponentLayout = LayoutInflater.from(this)
                .inflate(R.layout.sli_page_linear_layout, container, false) as? LinearLayoutCompat
            sliComponentLayout?.background?.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    service.bgColor.getColor(),
                    BlendModeCompat.SRC_ATOP
                )
            val linearLayout = LinearLayoutCompat(this)

            linearLayout.orientation = LinearLayoutCompat.HORIZONTAL

            val serviceTitle = LayoutInflater.from(this)
                .inflate(R.layout.sli_title_view, linearLayout, false)
                .apply {
                    this.textView.text = service.name!![language] ?: ""
                    this.textView.setDynamicSize(sliData.attrs.serviceNameSize)
                    this.textView.setTextColor(service.textColor.getColor())
                }

            service.rateOptions.forEachIndexed { index, rateOption ->
                val view = LayoutInflater.from(this)
                    .inflate(R.layout.input_from_user_sli_font_view, linearLayout, false)

                view.textView.apply {
                    text = rateOption.name?.resolveIconFromAwesome()
                    setTextColor(rateOption.rateIconColor.getColor())
                    background?.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            rateOption.bgColor.getColor(),
                            BlendModeCompat.SRC_ATOP
                        )
                    setOnClickListener {
                        linearLayout.forEachIndexed { index, child ->
                            if (view != child) {
                                service.rateOptions[index].selected = false
                                child.textView.setTextColor(rateOption.rateIconColor.getColor())
                            } else {
                                service.rateOptions[index].selected = true
                                this.setTextColor(rateOption.rateSelectedColor.getColor())
                            }
                        }
                        sliCondition[service.name!![language]] = rateOption.name
                        if (rateOption.markpageIdx != null) {
                            popUpMarkPage(view.textView.text.toString(), rateOption)
                        }
                    }
                }
                val sliText = LayoutInflater.from(this)
                    .inflate(R.layout.sli_title_view, linearLayout, false)
                    .apply {
                        this.textView.text = rateOption.label!![language] ?: ""
                        this.textView.setTextColor(rateOption.rateIconColor.getColor())
                    }
                if (sliData.showRateLabels == true)
                    (view as? LinearLayoutCompat)?.addView(sliText)
                linearLayout.addView(view)
                if (index > 0) {
                    val params = sliComponentLayout?.layoutParams as LinearLayout.LayoutParams
                    params.setMargins(
                        0,
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            22f,
                            resources.displayMetrics
                        ).toInt(),
                        0,
                        0
                    )
                    sliComponentLayout.layoutParams = params
                }
            }


            container?.tag = SLI_TAG
            if (sliData.attrs.service.size > 1)
                sliComponentLayout?.addView(serviceTitle)
            sliComponentLayout?.addView(linearLayout)
            container?.addView(sliComponentLayout)
        }

        return container
    }

    private fun popUpExitDialog() {
        val dialog = AlertDialog.Builder(this)
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_exit_view, LinearLayoutCompat(this), false)
        dialog.setView(dialogView)
        val alertDialog = dialog.create()
        dialogView.submit.setOnClickListener {
            dialogView.passwordEt.text?.toString()?.let {
                if (it == sharedPreferences.getString("password", "")) {
                    sharedPreferences.edit().clear().apply()
                    startActivity(Intent(this, AuthenticateActivity::class.java))
                    finish()
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
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.markpage_choose_view, LinearLayoutCompat(this), false)
        dialog.setView(dialogView)

        dialogView.sliTextView.text = sliText
        dialogView.title.text = markPageData?.title!![language]


        val alertDialog = dialog.create()
        markPageData.marks.forEach { mark ->
            val markText = LayoutInflater.from(this)
                .inflate(
                    R.layout.mark_choose_text_view,
                    dialogView.markContainer,
                    false
                ) as LinearLayoutCompat
            markText.apply {
                this.check_mark_text.text = mark.name!![language]
                if (!mark.selected!!) {
                    this.setBackgroundColor(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.white
                        )
                    )
                    this.checked_image.visibility = View.GONE
                } else {
                    this.setBackgroundColor(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.colorMarkPageSelected
                        )
                    )

                    this.checked_image.visibility = View.VISIBLE
                }
                if (markPageData.isSingle == true) {
                    dialogView.skip.visibility = View.GONE
                    dialogView.submit.visibility = View.GONE
                    setOnClickListener {
                        dialogView.markContainer.forEachIndexed { index, view ->
                            if (this != view) {
                                markPageData.marks[index].selected = false

                                this.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this@MainActivity,
                                        R.color.white
                                    )
                                )
                                this.checked_image.visibility = View.GONE
                            } else {
                                markPageData.marks[index].selected = mark.selected == false
                                this.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this@MainActivity,
                                        R.color.colorMarkPageSelected
                                    )
                                )

                                this.checked_image.visibility = View.VISIBLE
                            }
                        }
                        alertDialog.dismiss()
                    }
                } else {
                    setOnClickListener {
                        if (mark.selected == true) {
                            mark.selected = false
                            this.background = ContextCompat.getDrawable(
                                this@MainActivity,
                                R.drawable.rounded_input_background_8
                            )
                            this.setBackgroundColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.white
                                )
                            )
                            this.checked_image.visibility = View.GONE
                        } else {
                            mark.selected = true
                            this.setBackgroundColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.colorMarkPageSelected
                                )
                            )
                            this.checked_image.visibility = View.VISIBLE
                        }
                    }
                }
            }
            dialogView.markContainer.addView(markText)
        }
        dialogView.skip.setOnClickListener {
            markPageData.marks.forEach {
                it.selected = false
            }
            alertDialog.dismiss()
        }
        dialogView.submit.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }
}