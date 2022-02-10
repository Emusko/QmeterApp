package com.example.qmeter.presentation.main

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.RadioGroup
import androidx.activity.viewModels
import androidx.appcompat.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import com.bumptech.glide.Glide
import com.example.qmeter.R
import com.example.qmeter.databinding.ActivityMainBinding
import com.example.qmeter.di.base.BaseActivity
import com.example.qmeter.di.factory.ViewModelProviderFactory
import com.example.qmeter.service.model.remote.response.AuthenticationResponseModel
import com.example.qmeter.utils.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.choose_language_view.view.*
import kotlinx.android.synthetic.main.input_from_user_sli_font_view.view.*
import kotlinx.android.synthetic.main.input_from_user_sli_view.*
import kotlinx.android.synthetic.main.input_from_user_sli_view.view.*
import kotlinx.android.synthetic.main.input_from_user_sli_view.view.textView
import kotlinx.android.synthetic.main.input_from_user_view.view.*
import kotlinx.android.synthetic.main.markpage_choose_view.view.*
import kotlinx.android.synthetic.main.select_dropdown_view.*
import kotlinx.android.synthetic.main.select_dropdown_view.view.*
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject


const val SLI_TAG = "sli_data"
const val COMMENT_TAG = "comment_data"
const val CUSTOMER_TAG = "customer_data"

const val CUSTOM_FEEDBACK_TAG = "custom_field_feedback_component"
private const val emailExpn =
    ("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
            + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
            + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
            + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$")

class MainActivity : BaseActivity() {
    @Inject
    lateinit var factory: ViewModelProviderFactory

    private val viewModel: MainViewModel by viewModels { factory }

    private val responseModel by lazy { (intent.getSerializableExtra("pagesResponse") as? AuthenticationResponseModel) }
    private val pages by lazy { responseModel?.pages ?: arrayListOf() }

    private lateinit var binding: ActivityMainBinding

    private val pageViews = hashMapOf<Int, LinearLayoutCompat?>()
    private var languageIsActive = true

    private var languageContainer: LinearLayoutCompat? = null

    private var sliCondition = hashMapOf<String?, String?>()

    private var condition: AuthenticationResponseModel.ConditionOverallData? = null

    private var finalPageCondition: AuthenticationResponseModel.Reaction? = null

    private var language = "en"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setPageView()

        setListener()
    }

    private fun setPageView() {
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

        viewModel.pageStateLiveData.observe(this, {
            val pageIndex = it.first
            val back = it.second
            languageContainer?.visibility = View.GONE
            pageViews[pageIndex]?.visibility = View.VISIBLE
            pageViews[pageIndex + 1]?.visibility = View.GONE
            pageViews[pageIndex - 1]?.visibility = View.GONE

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
                sliCondition.clear()
            }

            condition?.let { reaction ->
                if (reaction.commentData == true)
                    binding.container.findViewWithTag<LinearLayoutCompat>(COMMENT_TAG).visibility =
                        View.VISIBLE
                else
                    binding.container.findViewWithTag<LinearLayoutCompat>(COMMENT_TAG).visibility =
                        View.GONE

                if (reaction.customerData == true)
                    binding.container.findViewWithTag<LinearLayoutCompat>(CUSTOMER_TAG)?.visibility =
                        View.VISIBLE
                else
                    binding.container.findViewWithTag<LinearLayoutCompat>(CUSTOMER_TAG)?.visibility =
                        View.GONE

                if (reaction.customFields == true)
                    binding.container.findViewWithTag<LinearLayoutCompat>(CUSTOM_FEEDBACK_TAG)?.visibility =
                        View.VISIBLE
                else
                    binding.container.findViewWithTag<LinearLayoutCompat>(CUSTOM_FEEDBACK_TAG)?.visibility =
                        View.GONE
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
                        || (pageIndex == 0  && responseModel?.languagePage?.languages?.size!! >= 1)) {
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
        })

        binding.next.setOnClickListener {
            if (pages.size - 1 != viewModel.pageStateLiveData.value?.first) {
                val page = pages[viewModel.pageStateLiveData.value!!.first]
                page?.makePages()?.forEach { pageComponent ->
                    when (pageComponent) {
                        is AuthenticationResponseModel.SliData -> {
                            val returning = arrayOfNulls<Boolean>(pageComponent.attrs?.service?.size?: 0)
                            var conditionToReturn = false
                            run loop@{
                                pageComponent.attrs?.service?.forEachIndexed { index, service ->
                                    service.rateOptions.forEach { rateOption ->
                                        if (rateOption.selected!!) {
                                            returning[index] = rateOption.selected!!
                                        }
                                    }
                                }
                            }
                            run loop@{
                                returning.forEach {
                                    if (!(it != null && it != false))
                                        conditionToReturn = true
                                    return@loop
                                }
                            }
                            if (conditionToReturn){
                                return@setOnClickListener
                            }
                        }
                        is AuthenticationResponseModel.CustomerData -> {
                            pageComponent.attrs.forEach { attr ->
                                if (attr.required == true) {
                                    when (val dataView =
                                        binding.container.findViewWithTag<View>(attr.name)) {
                                        is TextInputEditText -> {
                                            dataView.text?.toString()?.let {
                                                if (it.isEmpty()) {
                                                    dataView.error =
                                                        getString(R.string.field_error_message)
                                                    return@setOnClickListener
                                                } else {
                                                    if (attr.name == "email" && !it.isEmailValid()){
                                                        dataView.error =
                                                            getString(R.string.email_field_error_message)
                                                        return@setOnClickListener
                                                    }
                                                }
                                            }
                                        }
                                        is AppCompatSpinner -> {
                                            dataView.selectedItem?.toString()?.let {
                                                if (it.isEmpty()) {
                                                    (dataView.parent as? LinearLayoutCompat)?.findViewById<AppCompatTextView>(
                                                        R.id.text_input_error
                                                    )?.text =
                                                        getString(R.string.field_error_message)
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
                                                (dataView.parent as? LinearLayoutCompat)?.findViewById<AppCompatTextView>(
                                                    R.id.text_input_error
                                                )?.text =
                                                    getString(R.string.field_error_message)
                                                return@setOnClickListener
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        is AuthenticationResponseModel.CustomFieldFeedbackComponent -> {
                            pageComponent.attrs?.forEach { attr ->
                                if (attr.required == true) {
                                    when (val dataView =
                                        binding.container.findViewWithTag<View>(attr.name)) {
                                        is TextInputEditText -> {
                                            dataView.text?.toString()?.let {
                                                if (it.isEmpty()) {
                                                    dataView.error =
                                                        getString(R.string.field_error_message)
                                                    return@setOnClickListener
                                                }
                                            }
                                        }
                                        is AppCompatSpinner -> {
                                            dataView.selectedItem?.toString()?.let {
                                                if (it.isEmpty()) {
                                                    (dataView.parent as? LinearLayoutCompat)?.findViewById<AppCompatTextView>(
                                                        R.id.text_input_error
                                                    )?.text =
                                                        getString(R.string.field_error_message)
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
                                                (dataView.parent as? LinearLayoutCompat)?.findViewById<AppCompatTextView>(
                                                    R.id.text_input_error
                                                )?.text =
                                                    getString(R.string.field_error_message)
                                                return@setOnClickListener
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        is AuthenticationResponseModel.CommentData -> {
                            pageComponent.attrs?.let { attr ->
                                if (attr.required == true) {
                                    when (val dataView =
                                        binding.container.findViewWithTag<View>(attr.name)) {
                                        is TextInputEditText -> {
                                            dataView.text?.toString()?.let {
                                                if (it.isEmpty()) {
                                                    dataView.error =
                                                        getString(R.string.field_error_message)
                                                    return@setOnClickListener
                                                }
                                            }
                                        }
                                        is AppCompatSpinner -> {
                                            dataView.selectedItem?.toString()?.let {
                                                if (it.isEmpty()) {
                                                    (dataView.parent as? LinearLayoutCompat)?.findViewById<AppCompatTextView>(
                                                        R.id.text_input_error
                                                    )?.text =
                                                        getString(R.string.field_error_message)
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
                                                (dataView.parent as? LinearLayoutCompat)?.findViewById<AppCompatTextView>(
                                                    R.id.text_input_error
                                                )?.text =
                                                    getString(R.string.field_error_message)
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
            pageViews.forEach { page ->
                page.value?.forEach { pageView ->
                    when (pageView.tag as? String) {
                        COMMENT_TAG -> {
                            viewModel.bindCommentDataToRequest(
                                pageView as? LinearLayoutCompat,
                                responseModel?.pages!![page.key]?.commentData
                            )
                        }
                        CUSTOMER_TAG -> {
                            viewModel.bindCustomerDataToRequest(
                                pageView as? LinearLayoutCompat,
                                responseModel?.pages!![page.key]?.customerData
                            )
                        }
                        CUSTOM_FEEDBACK_TAG -> {
                            viewModel.bindCustomFieldFeedbackDataToRequest(
                                pageView as? LinearLayoutCompat,
                                responseModel?.pages!![page.key]?.customFieldFeedbackComponent
                            )
                        }
                        SLI_TAG -> {
                            viewModel.bindSliDataToRequest(
                                language,
                                page.key,
                                responseModel?.pages!![page.key]?.sliData,
                                responseModel?.markPageData
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

        viewModel.dataPost.observe(this, {
            val finalPageData = responseModel?.finalPageData
            finalPageData?.pageBg?.let {
                binding.motherLayout.setBackgroundColor(it.getColor())
            }
            val title = LayoutInflater.from(this)
                .inflate(R.layout.input_from_user_sli_view, binding.container, false).apply {
                    this.textView.text = finalPageCondition?.text!![language]
                    this.textView.setBackgroundColor(finalPageCondition?.textBgColor?.getColor()?: 0)
                    this.textView.setTextColor(finalPageCondition?.textColor?.getColor()?: 0)
                }
            binding.container.addView(title)
            val handler = Handler(Looper.getMainLooper())
            if (it) {
                responseModel?.pages?.forEach {
                    it?.makePages()?.forEach {
                        if (it is AuthenticationResponseModel.SliData) {
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
                    if (it.time!! > 0) {
                        handler
                            .postDelayed({
                                binding.container.removeAllViews()
                                pageViews.clear()
                                getLanguageFromUser()
                                condition = null
                                finalPageCondition = null
                            }, it.time.toLong())
                    }
                }

                binding.qmeterAppLogo.setOnClickListener {
                    binding.container.removeAllViews()
                    pageViews.clear()
                    getLanguageFromUser()
                    condition = null
                    finalPageCondition = null
                }

            }
        })

    }

    private fun initializeViews() {
        pages.forEachIndexed { index, page ->
            val pageLayout = LayoutInflater.from(this)
                .inflate(
                    R.layout.page_linear_layout,
                    binding.container,
                    false
                ) as? LinearLayoutCompat
            page?.makePages()?.sortedBy { it?.position }?.forEach { pageComponent ->
                when (pageComponent) {
                    is AuthenticationResponseModel.CommentData -> {
                        pageLayout?.addView(populateCommentView(pageComponent))
                    }
                    is AuthenticationResponseModel.SliData -> {
                        pageLayout?.addView(populateSliView(pageComponent))
                    }
                    is AuthenticationResponseModel.CustomerData -> {
                        pageLayout?.addView(populateCustomerView(pageComponent))
                    }
                    is AuthenticationResponseModel.CustomFieldFeedbackComponent -> {
                        pageLayout?.addView(populateCustomFeedbackView(pageComponent))
                    }
                }
            }
            pageLayout?.visibility = View.GONE
            pageViews[index] = pageLayout
            binding.container.addView(pageViews[index])
        }
    }

    override fun onBackPressed() {
        if (viewModel.pageStateLiveData.value != null && viewModel.pageStateLiveData.value?.first != 0) {
            viewModel.pageStateLiveData.value =
                Pair(viewModel.pageStateLiveData.value?.first?.minus(1) ?: 0, true)
        } else {
            if (languageIsActive) {
                if (!(viewModel.pageStateLiveData.value?.first == 0  && responseModel?.languagePage?.languages?.size!! >= 1))
                    super.onBackPressed()
                else
                    binding.back.visibility = View.GONE
            } else {
                pageViews.forEach { _, linearLayoutCompat ->
                    linearLayoutCompat?.visibility = View.GONE
                }
                binding.next.visibility = View.GONE
                binding.back.visibility = View.GONE
                binding.submit.visibility = View.GONE
                languageContainer?.visibility = View.VISIBLE
            }
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

    private fun populateCustomerView(pageComponent: AuthenticationResponseModel.CustomerData): LinearLayoutCompat? {

        val container = LayoutInflater.from(this)
            .inflate(R.layout.page_linear_layout, binding.container, false) as? LinearLayoutCompat

        val attrs = pageComponent.attrs

        attrs.sortBy { it.position }
        attrs.forEach {
            when (it.type) {
                "text" -> {
                    val view = LayoutInflater.from(this)
                        .inflate(R.layout.input_from_user_view, binding.container, false)
                        .apply {
                            (this as? TextInputLayout)?.hint = it.placeholder!![language] ?: ""
                            this.passwordEt.tag = it.name
                        }
                    container?.addView(view)
                }
                "date" -> {
                    val view = LayoutInflater.from(this)
                        .inflate(R.layout.date_input_from_user_view, binding.container, false)
                        .apply {
                            this.passwordEt.tag = it.name
                            this.passwordEt.setOnClickListener {
                                val newCalendar = Calendar.getInstance()
                                DatePickerDialog(
                                    this@MainActivity,
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
                            (this as? TextInputLayout)?.hint = it.placeholder!![language] ?: ""
                        }
                    container?.addView(view)
                }
                "number" -> {
                    val view = LayoutInflater.from(this)
                        .inflate(R.layout.number_input_from_user_view, binding.container, false)
                        .apply {
                            val editText = this.passwordEt
                            editText.tag = it.name
                            editText.setText(it.prefix)
                            Selection.setSelection(editText.text, editText.text?.length?: 0)


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
                                    if (!s.toString().startsWith(it.prefix?: "")) {
                                        editText.setText(it.prefix?: "")
                                        Selection.setSelection(editText.text, editText.text?.length?: 0)
                                    }
                                }
                            })
                            (this as? TextInputLayout)?.hint = it.placeholder!![language] ?: ""
                        }
                    container?.addView(view)
                }
                "select" -> {
                    if (it.select_design == null || it.select_design.value == "dropdown") {
                        val view = LayoutInflater.from(this)
                            .inflate(R.layout.select_dropdown_view, binding.container, false)
                            .apply {
                                val spinnerArrayAdapter = SingleSelectAdapter(
                                    language,
                                    this@MainActivity,
                                    android.R.layout.simple_spinner_item,
                                    mutableListOf<AuthenticationResponseModel.SelectOption?>().apply {
                                        it.select?.forEach { selectOption ->
                                            this.add(selectOption)
                                        }
                                    }
                                )

                                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                this.spinner?.adapter = spinnerArrayAdapter
                                this.spinner?.tag = it.name
                                this.selectTitle.text = it.placeholder!![language] ?: ""
                            }
                        container?.addView(view)
                    } else if (it.select_design.value == "radio_button") {
                        val containerView = RadioGroup(this)
                        val title = LayoutInflater.from(this@MainActivity)
                            .inflate(R.layout.input_from_user_sli_view, containerView, false)
                            .apply {
                                this.textView.text = it.label!![language] ?: ""
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
                        containerView.tag = it.name
                        container?.addView(title)
                        container?.addView(containerView)
                    }
                }
                "multi-select" -> {
                    val containerView = LinearLayoutCompat(this).apply {
                        orientation = LinearLayoutCompat.VERTICAL
                    }
                    val title = LayoutInflater.from(this@MainActivity)
                        .inflate(R.layout.input_from_user_sli_view, containerView, false)
                        .apply {
                            this.textView.text = it.label!![language] ?: ""
                            this.textView.setTextColor(it.label_text_color?.getColor() ?: 0)
                        }
                        .apply {
                            it.select?.forEach { selectOption ->
                                val checkBox = AppCompatCheckBox(this@MainActivity)
                                    .apply {
                                        this.text = selectOption.option!![language] ?: ""
                                        this.tag = selectOption.id
                                    }
                                containerView.addView(checkBox)
                            }
                        }
                    containerView.tag = it.name
                    container?.addView(title)
                    container?.addView(containerView)
                }
            }
        }
        container?.tag = CUSTOMER_TAG
        return container
    }

    private fun populateCustomFeedbackView(pageComponent: AuthenticationResponseModel.CustomFieldFeedbackComponent): LinearLayoutCompat? {

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
                            (this as? TextInputLayout)?.hint = it.placeholder!![language] ?: ""
                            this.passwordEt.tag = it.name
                        }
                    container?.addView(view)

                }
                "number" -> {
                    val view = LayoutInflater.from(this)
                        .inflate(R.layout.number_input_from_user_view, binding.container, false)
                        .apply {
                            val editText = this.passwordEt
                            editText.tag = it.name
                            editText.setText(it.prefix)
                            Selection.setSelection(editText.text, editText.text?.length?: 0)


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
                                    if (!s.toString().startsWith(it.prefix?: "")) {
                                        editText.setText(it.prefix?: "")
                                        Selection.setSelection(editText.text, editText.text?.length?: 0)
                                    }
                                }
                            })
                            (this as? TextInputLayout)?.hint = it.placeholder!![language] ?: ""

                        }
                    container?.addView(view)
                }
                "date" -> {
                    val view = LayoutInflater.from(this)
                        .inflate(R.layout.date_input_from_user_view, binding.container, false)
                        .apply {
                            this.passwordEt.tag = it.name
                            this.passwordEt.setOnClickListener { _ ->
                                val newCalendar = Calendar.getInstance()
                                DatePickerDialog(
                                    this@MainActivity,
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
                            (this as? TextInputLayout)?.hint = it.placeholder!![language] ?: ""
                        }
                    container?.addView(view)
                }
                "select" -> {
                    if (it.select_design == null || it.select_design.value == "dropdown") {
                        val view = LayoutInflater.from(this)
                            .inflate(R.layout.select_dropdown_view, binding.container, false)
                            .apply {
                                val spinnerArrayAdapter = SingleSelectAdapter(
                                    language,
                                    this@MainActivity,
                                    android.R.layout.simple_spinner_item,
                                    mutableListOf<AuthenticationResponseModel.SelectOption?>().apply {
                                        it.select?.forEach { selectOption ->
                                            this.add(selectOption)
                                        }
                                    }
                                )

                                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                this.spinner?.adapter = spinnerArrayAdapter
                                this.spinner?.tag = it.name
                                this.selectTitle.text = it.placeholder!![language] ?: ""
                            }
                        container?.addView(view)
                    } else if (it.select_design.value == "radio_button") {
                        val containerView = LinearLayoutCompat(this)
                        val title = LayoutInflater.from(this@MainActivity)
                            .inflate(R.layout.input_from_user_sli_view, containerView, false)
                            .apply {
                                this.textView.text = it.label!![language] ?: ""
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
                        containerView.tag = it.name
                        container?.addView(title)
                        container?.addView(containerView)
                    }
                }
                "multi-select" -> {
                    val containerView = LinearLayoutCompat(this)
                    val title = LayoutInflater.from(this@MainActivity)
                        .inflate(R.layout.input_from_user_sli_view, containerView, false)
                        .apply {
                            this.textView.text = it.label!![language] ?: ""
                            this.textView.setTextColor(it.label_text_color?.getColor() ?: 0)
                        }
                        .apply {
                            it.select?.forEach { selectOption ->
                                val checkBox = AppCompatCheckBox(this@MainActivity)
                                    .apply {
                                        this.text = selectOption.option!![language]
                                        this.tag = selectOption.id
                                    }
                                containerView.addView(checkBox)
                            }
                        }
                    containerView.tag = it.name
                    container?.addView(title)
                    container?.addView(containerView)
                }
            }
        }
        container?.tag = CUSTOM_FEEDBACK_TAG
        return container
    }

    private fun getLanguageFromUser() {

        binding.next.visibility = View.GONE
        binding.back.visibility = View.GONE

        languageContainer = LayoutInflater.from(this)
            .inflate(R.layout.page_linear_layout, binding.container, false) as? LinearLayoutCompat

        if (responseModel?.languagePage != null) {
            languageIsActive = true
            binding.next.visibility = View.GONE
            binding.back.visibility = View.GONE
            binding.submit.visibility = View.GONE
            val languages = responseModel?.languagePage?.languages
            val title = LayoutInflater.from(this)
                .inflate(R.layout.input_from_user_sli_view, binding.container, false).apply {
                    this.textView.text = "Choose language"
                }


            languageContainer?.addView(title)
            if (languages?.size?.compareTo(1) == 0){
                languageIsActive = false
                language = languages[0].langCode ?: "en"
                viewModel.requestModel["language"] = language
                initializeViews()
                viewModel.pageStateLiveData.value = Pair(0, false)
            } else {
            languages?.forEach { languageModel ->

                val linearLayout = LayoutInflater.from(this)
                    .inflate(R.layout.choose_language_view, binding.container, false).apply {
                        this.textView.text = languageModel.label
                        this.textView.setTextColor(languageModel.labelColor.getColor())
                        this.setOnClickListener {
                            languageIsActive = false
                            language = languageModel.langCode ?: "en"
                            viewModel.requestModel["language"] = language
                            initializeViews()
                            viewModel.pageStateLiveData.value = Pair(0, false)
                        }
                    }
                linearLayout.flagImage.loadSvgOrOther(languageModel.flagUrl)

                languageContainer?.addView(linearLayout)
            }
            }
            binding.container.addView(languageContainer)
        } else {
            languageIsActive = false
            language = "en"
            viewModel.pageStateLiveData.value = Pair(0, false)
        }
    }

    private fun populateCommentView(commentData: AuthenticationResponseModel.CommentData): LinearLayoutCompat? {
        val container = LayoutInflater.from(this)
            .inflate(R.layout.page_linear_layout, binding.container, false) as? LinearLayoutCompat

        val view = LayoutInflater.from(this)
            .inflate(R.layout.input_from_user_view, container, false)

        (view as? TextInputLayout)?.hint = commentData.attrs?.placeholder!![language] ?: ""

        view.passwordEt?.tag = commentData.attrs.name

        container?.tag = COMMENT_TAG
        container?.addView(view)

        return container
    }

    private fun populateSliView(sliData: AuthenticationResponseModel.SliData): LinearLayoutCompat? {

        val container = LayoutInflater.from(this)
            .inflate(R.layout.page_linear_layout, binding.container, false) as? LinearLayoutCompat

        sliData.componentTitle!![language]?.let {
            val title = LayoutInflater.from(this)
                .inflate(R.layout.input_from_user_sli_view, container, false).apply {
                    this.textView.text = it
                    this.textView.setTextColor(sliData.componentTitleTextColor.getColor())
                    this.textView.setBackgroundColor(sliData.componentTitleBgColor.getColor())
                }
            container?.addView(title)
        }


        sliData.attrs?.service?.forEach { service ->

            val linearLayout = LinearLayoutCompat(this)
            linearLayout.orientation = LinearLayoutCompat.HORIZONTAL

            val serviceTitle = LayoutInflater.from(this)
                .inflate(R.layout.input_from_user_sli_view, linearLayout, false)
                .apply {
                    this.textView.text = service.name!![language] ?: ""
                    this.textView.setTextColor(service.textColor.getColor())
                }

            service.rateOptions.forEach { rateOption ->
                val view = LayoutInflater.from(this)
                    .inflate(R.layout.input_from_user_sli_font_view, linearLayout, false)

                view.textView.apply {
                    text = rateOption.name?.resolveIconFromAwesome()
                    setTextColor(rateOption.rateIconColor.getColor())
                    setBackgroundColor(rateOption.rateBgColor.getColor())
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

                linearLayout.addView(view)
            }


            container?.tag = SLI_TAG
            if (sliData.attrs.service.size > 1)
            container?.addView(serviceTitle)
            container?.addView(linearLayout)
        }

        return container
    }

    private fun popUpMarkPage(
        sliText: String,
        rateOption: AuthenticationResponseModel.RateOptions?
    ) {
        val markPageData =
            responseModel?.markPageData?.filter { it.idx == rateOption?.markpageIdx }?.firstOrNull()
        val dialog = AlertDialog.Builder(this)
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.markpage_choose_view, LinearLayoutCompat(this), false)
        dialog.setView(dialogView)

        dialogView.sliTextView.text = sliText
        dialogView.title.text = markPageData?.title!![language]


        val alertDialog = dialog.create()
        markPageData.marks.forEach { mark ->
            val markText = LayoutInflater.from(this)
                .inflate(R.layout.mark_choose_text_view, dialogView.markContainer, false)
            markText.apply {
                (this as? AppCompatTextView)?.text = mark.name!![language]
                if (markPageData.isSingle == true) {
                    setOnClickListener {
                        dialogView.markContainer.forEach { child ->
                            if (markText != child) {
                                mark.selected = false
                                child.background = ContextCompat.getDrawable(
                                    this@MainActivity,
                                    R.drawable.rounded_input_background_8
                                )
                                (child as? AppCompatTextView)?.setTextColor(
                                    ContextCompat.getColor(
                                        this@MainActivity,
                                        R.color.black
                                    )
                                )
                            } else {
                                mark.selected = true
                                child.background = ContextCompat.getDrawable(
                                    this@MainActivity,
                                    R.drawable.rounded_accent_input_background_8
                                )
                                (child as? AppCompatTextView)?.setTextColor(
                                    ContextCompat.getColor(
                                        this@MainActivity,
                                        R.color.white
                                    )
                                )
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
                            (this as? AppCompatTextView)?.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.black
                                )
                            )
                        } else {
                            mark.selected = true
                            this.background = ContextCompat.getDrawable(
                                this@MainActivity,
                                R.drawable.rounded_accent_input_background_8
                            )
                            (this as? AppCompatTextView)?.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.white
                                )
                            )
                        }
                    }
                }
            }
            dialogView.markContainer.addView(markText)
        }
        dialogView.skip.setOnClickListener { alertDialog.dismiss() }
        dialogView.submit.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }
}