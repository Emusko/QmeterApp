package com.example.qmeter.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.example.qmeter.R
import com.example.qmeter.databinding.ActivityMainBinding
import com.example.qmeter.di.base.BaseActivity
import com.example.qmeter.di.factory.ViewModelProviderFactory
import com.example.qmeter.service.model.remote.response.AuthenticationResponseModel
import com.example.qmeter.utils.getColor
import com.example.qmeter.utils.makePages
import com.google.android.material.textfield.TextInputLayout
import javax.inject.Inject

class MainActivity : BaseActivity() {
    @Inject
    lateinit var factory: ViewModelProviderFactory

    private val viewModel: MainViewModel by viewModels{ factory }

    private val responseModel by lazy { (intent.getSerializableExtra("pagesResponse") as? AuthenticationResponseModel) }
    private val pages by lazy { responseModel?.pages?: arrayListOf() }

    private lateinit var binding: ActivityMainBinding

    private val language = "en"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListener()
    }

    private fun setListener() {

        getLanguageFromUser()

        viewModel.pageStateLiveData.observe(this, { t ->
            when {
                t<0 -> {
                    viewModel.pageStateLiveData.value = 0
                }
                t>pages.size -> {
                    viewModel.pageStateLiveData.value = pages.size-1
                }
                t<pages.size -> {
                    if (pages[t].properties?.isBackButtonEnabled!!){
                        binding.back.visibility =
                            View.VISIBLE
                    } else {
                        binding.back.visibility =
                        View.GONE
                    }
                    if (pages[t].properties?.isNextButtonEnabled!!){
                        binding.next.visibility =
                            View.VISIBLE
                    } else {
                        binding.next.visibility =
                        View.GONE
                    }
                    if (pages[t].properties?.isSubmitEnabled!!) {
                        binding.submit.visibility = View.VISIBLE
                    } else {
                        binding.submit.visibility = View.GONE
                    }
                    pages[t].makePages().forEach { pageComponent ->
                        when (pageComponent) {
                            is AuthenticationResponseModel.CommentData -> {
                                populateCommentView(pageComponent)
                            }
                            is AuthenticationResponseModel.SliData -> {
                                populateSliView(pageComponent)
                            }
                        }

                    }
                }
            }
        })

        binding.next.setOnClickListener {
            binding.container.removeAllViews()
            viewModel.pageStateLiveData.value = (viewModel.pageStateLiveData.value?.plus(1))
        }
        binding.back.setOnClickListener {
            binding.container.removeAllViews()

            viewModel.pageStateLiveData.value = (viewModel.pageStateLiveData.value?.minus(1))
        }

        viewModel.pageStateLiveData.value = 0
    }

    private fun getLanguageFromUser() {

    }

    private fun populateCommentView(commentData: AuthenticationResponseModel.CommentData){
        val view = LayoutInflater.from(this).inflate(R.layout.input_from_user_view, binding.container, false)

        (view as? TextInputLayout)?.hint = commentData.attrs?.placeholder!![language]?: ""

        binding.container.addView(view)
    }
    private fun populateSliView(sliData: AuthenticationResponseModel.SliData){

        val title = LayoutInflater.from(this).inflate(R.layout.input_from_user_sli_view, binding.container, false).apply {
            (this as AppCompatTextView).text = sliData.componentTitle!![language]?: ""
            this.setTextColor(sliData.componentTitleTextColor.getColor())
            this.setBackgroundColor(sliData.componentTitleBgColor.getColor())
        }


        binding.container.addView(title)
        sliData.attrs?.service?.forEach {

            val linearLayout = LinearLayoutCompat(this)
            linearLayout.orientation = LinearLayoutCompat.HORIZONTAL

            it.rateOptions.forEach { rateOption ->
                val view = LayoutInflater.from(this).inflate(R.layout.input_from_user_sli_view, linearLayout, false)

                (view as AppCompatTextView).apply {
                    text = rateOption.label!![language]?: ""
                    setTextColor(rateOption.textColor.getColor())
                }

                linearLayout.addView(view)
            }



            binding.container.addView(linearLayout)
        }



    }
}