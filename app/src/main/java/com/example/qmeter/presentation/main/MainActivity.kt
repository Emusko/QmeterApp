package com.example.qmeter.presentation.main

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import com.bumptech.glide.Glide
import com.example.qmeter.R
import com.example.qmeter.databinding.ActivityMainBinding
import com.example.qmeter.di.base.BaseActivity
import com.example.qmeter.di.factory.ViewModelProviderFactory
import com.example.qmeter.service.model.remote.response.AuthenticationResponseModel
import com.example.qmeter.utils.getColor
import com.example.qmeter.utils.loadSvgOrOther
import com.example.qmeter.utils.makePages
import com.example.qmeter.utils.resolveIconFromAwesome
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
import javax.inject.Inject

const val SLI_TAG = "sli_data"
const val COMMENT_TAG = "comment_data"
const val CUSTOMER_TAG = "customer_data"
const val CUSTOM_FEEDBACK_TAG = "custom_field_feedback_component"

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

    private var condition: AuthenticationResponseModel.ConditionOverallData? = null

    private var language = "en"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setPageView()

        setListener()
    }

    private fun setPageView() {
        Glide
            .with(this)
            .load(responseModel?.generalSettings?.logo_url)
            .into(binding.logo)
    }

    private fun setListener() {

        getLanguageFromUser()

        initializeViews()

        viewModel.pageStateLiveData.observe(this, { pageIndex ->
            languageContainer?.visibility = View.GONE
            pageViews[pageIndex]?.visibility = View.VISIBLE
            pageViews[pageIndex+1]?.visibility = View.GONE
            pageViews[pageIndex-1]?.visibility = View.GONE

//            writeDataToRequestModel(pageViews[pageIndex-1])
            when {
                pageIndex < 0 -> {
                    viewModel.pageStateLiveData.value = 0
                }
                pageIndex >= pages.size -> {
                    viewModel.pageStateLiveData.value = pages.size - 1
                }
                pageIndex < pages.size -> {
                    if (pages[pageIndex].properties?.isBackButtonEnabled!!) {
                        binding.back.visibility =
                            View.VISIBLE
                    } else {
                        binding.back.visibility =
                            View.GONE
                    }
                    if (pages[pageIndex].properties?.isNextButtonEnabled!!) {
                        binding.next.visibility =
                            View.VISIBLE
                    } else {
                        binding.next.visibility =
                            View.GONE
                    }
                    if (pages.size-1 == pageIndex) {
                        binding.submit.visibility = View.VISIBLE
                    } else {
                        binding.submit.visibility = View.GONE
                    }
                }
            }
        })

        binding.next.setOnClickListener {
            if (pages.size-1 != viewModel.pageStateLiveData.value)
            viewModel.pageStateLiveData.value = (viewModel.pageStateLiveData.value?.plus(1))
        }
        binding.back.setOnClickListener {
            if (viewModel.pageStateLiveData.value != null && viewModel.pageStateLiveData.value != 0) {
                viewModel.pageStateLiveData.value = (viewModel.pageStateLiveData.value?.minus(1))
            } else {
                pageViews.forEach { _, linearLayoutCompat ->
                    linearLayoutCompat?.visibility = View.GONE
                }
                languageContainer?.visibility = View.VISIBLE
            }
        }
        binding.submit.setOnClickListener {
            pageViews.forEach { page ->
                page.value?.forEach { pageView ->
                    when ( pageView.tag  as? String ) {
                        COMMENT_TAG -> {
                            viewModel.bindCommentDataToRequest(pageView as? LinearLayoutCompat, responseModel?.pages!![page.key].commentData)
                        }
                        CUSTOMER_TAG -> {
                            viewModel.bindCustomerDataToRequest(pageView as? LinearLayoutCompat, responseModel?.pages!![page.key].customerData)
                        }
                        CUSTOM_FEEDBACK_TAG -> {
                            viewModel.bindCustomFieldFeedbackDataToRequest(pageView as? LinearLayoutCompat, responseModel?.pages!![page.key].customFieldFeedbackComponent)
                        }
                        SLI_TAG -> {
                            viewModel.bindSliDataToRequest(language, page.key, responseModel?.pages!![page.key].sliData, responseModel?.markPageData)
                        }

                    }
                }
            }
            viewModel.postFeedback()
        }

    }

    private fun initializeViews() {
        pages.forEachIndexed { index, page ->
            val pageLayout = LayoutInflater.from(this)
                .inflate(R.layout.page_linear_layout, binding.container, false) as? LinearLayoutCompat
            page.makePages().forEach { pageComponent ->
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
        if (viewModel.pageStateLiveData.value != null && viewModel.pageStateLiveData.value != 0) {
            viewModel.pageStateLiveData.value = (viewModel.pageStateLiveData.value?.minus(1))
        } else {
            if (languageIsActive) {
                super.onBackPressed()
            } else {
                getLanguageFromUser()
            }
        }
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
                "number" -> {
                    val view = LayoutInflater.from(this)
                        .inflate(R.layout.number_input_from_user_view, binding.container, false)
                        .apply {
                            this.passwordEt.tag = it.name
                            (this as? TextInputLayout)?.hint = it.placeholder!![language] ?: ""
                        }
                    container?.addView(view)
                }
                "select" -> {
                    val view = LayoutInflater.from(this)
                        .inflate(R.layout.select_dropdown_view, binding.container, false)
                        .apply {
                            val spinnerArrayAdapter = ArrayAdapter(
                                this@MainActivity, android.R.layout.simple_spinner_item,
                                arrayListOf<String>().apply {
                                    it.select?.forEach { selectOption ->
                                        this.add(selectOption.id ?: "")
                                    }
                                }
                            )

                            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            this.spinner?.adapter = spinnerArrayAdapter
                            this.spinner?.tag = it.name
                            this.selectTitle.text = it.placeholder!![language] ?: ""
                        }
                    container?.addView(view)
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
                                        this.text = selectOption.id
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
                    val view = LayoutInflater.from(this)
                        .inflate(R.layout.select_dropdown_view, binding.container, false)
                        .apply {
                            val spinnerArrayAdapter = ArrayAdapter(
                                this@MainActivity, android.R.layout.simple_spinner_item,
                                arrayListOf<String>().apply {
                                    it.select?.forEach { selectOption ->
                                        this.add(selectOption.id ?: "")
                                    }
                                }
                            )

                            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            this.spinner?.adapter = spinnerArrayAdapter
                            this.spinner?.tag = it.name
                            this.selectTitle.text = it.placeholder!![language] ?: ""
                        }
                    container?.addView(view)
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
                                        this.text = selectOption.id
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
            languages?.forEach { languageModel ->

                val linearLayout = LayoutInflater.from(this)
                    .inflate(R.layout.choose_language_view, binding.container, false).apply {
                        this.textView.text = languageModel.label
                        this.setOnClickListener {
                            languageIsActive = false
                            language = languageModel.langCode ?: "en"
                            viewModel.requestModel.put("language", language)
                            viewModel.pageStateLiveData.value = 0
                        }
                    }
                linearLayout.flagImage.loadSvgOrOther(languageModel.flagUrl)

                languageContainer?.addView(linearLayout)
            }
            binding.container.addView(languageContainer)
        } else {
            languageIsActive = false
            language = "en"
            viewModel.pageStateLiveData.value = 0
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

    private fun populateSliView(sliData: AuthenticationResponseModel.SliData) : LinearLayoutCompat? {

        val container = LayoutInflater.from(this)
            .inflate(R.layout.page_linear_layout, binding.container, false) as? LinearLayoutCompat

        val title = LayoutInflater.from(this)
            .inflate(R.layout.input_from_user_sli_view, container, false).apply {
                this.textView.text = sliData.componentTitle!![language] ?: ""
                this.textView.setTextColor(sliData.componentTitleTextColor.getColor())
                this.textView.setBackgroundColor(sliData.componentTitleBgColor.getColor())
            }


        container?.addView(title)
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
                            if (view != child){
                                service.rateOptions[index].selected = false
                                child.textView.setTextColor(rateOption.rateIconColor.getColor())
                            } else {
                                service.rateOptions[index].selected = true
                                this.setTextColor(rateOption.rateSelectedColor.getColor())
                            }
                        }
                        if (rateOption.markpageIdx != null) {
                            popUpMarkPage(view.textView.text.toString(), rateOption)
                        }
                    }
                }

                linearLayout.addView(view)
            }


            container?.tag = SLI_TAG
            container?.addView(serviceTitle)
            container?.addView(linearLayout)
        }

        return container
    }

    private fun popUpMarkPage(sliText: String, rateOption: AuthenticationResponseModel.RateOptions?) {
        val markPageData = responseModel?.markPageData?.filter { it.idx == rateOption?.markpageIdx}?.firstOrNull()
        val dialog = AlertDialog.Builder(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.markpage_choose_view, LinearLayoutCompat(this), false)
        dialog.setView(dialogView)

        dialogView.sliTextView.text = sliText
        dialogView.title.text = markPageData?.title!![language]


        markPageData.marks.forEach { mark ->
            val markText = LayoutInflater.from(this).inflate(R.layout.mark_choose_text_view, dialogView.markContainer, false)
            markText.apply {
                (this as? AppCompatTextView)?.text = mark.name!![language]
                if (markPageData.isSingle == true){
                    setOnClickListener {
                        dialogView.markContainer.forEach { child ->
                            if (markText != child) {
                                mark.selected = false
                                child.background = ContextCompat.getDrawable(this@MainActivity, R.drawable.rounded_input_background_8)
                                (child as? AppCompatTextView)?.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.black))
                            } else {
                                mark.selected = true
                                child.background = ContextCompat.getDrawable(this@MainActivity, R.drawable.rounded_accent_input_background_8)
                                (child as? AppCompatTextView)?.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                            }
                        }
                    }
                } else {
                    setOnClickListener {
                        if (mark.selected == true){
                            mark.selected = false
                            this.background = ContextCompat.getDrawable(this@MainActivity, R.drawable.rounded_input_background_8)
                            (this as? AppCompatTextView)?.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.black))
                        } else {
                            mark.selected = true
                            this.background = ContextCompat.getDrawable(this@MainActivity, R.drawable.rounded_accent_input_background_8)
                            (this as? AppCompatTextView)?.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                        }
                    }
                }
            }
            dialogView.markContainer.addView(markText)
        }
        val alertDialog = dialog.create()
        dialogView.skip.setOnClickListener { alertDialog.dismiss() }
        dialogView.submit.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }
}