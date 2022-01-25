package com.example.qmeter.utils

import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.widget.AppCompatImageView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.qmeter.R
import com.example.qmeter.service.model.remote.response.AuthenticationResponseModel
import com.example.qmeter.service.model.remote.response.PageComponent
import java.util.*
import kotlin.collections.ArrayList

fun AuthenticationResponseModel.Page.makePages(): ArrayList<PageComponent?> {

    val components = arrayListOf<PageComponent?>().apply {
        add(this@makePages.commentData)
        add(this@makePages.customerData)
        add(this@makePages.sliData)
        add(this@makePages.customFieldFeedbackComponent)
    }

    components.sortedBy { it?.position }

    return components
}

fun ArrayList<Int>.getColor(): Int {
    return Color.rgb(this[0], this[1], this[2])
}

fun String.resolveIconFromAwesome(): String {
    return when (this) {
        "excellent" -> "A"
        "good" -> "B"
        "neutral" -> "C"
        "bad" -> "D"
        "unacceptable" -> "E"
        else -> ""
    }
}

fun AppCompatImageView.loadSvgOrOther(
    myUrl: String?,
    cache: Boolean = true,
    errorImg: Int = R.drawable.ic_launcher_background
) {

    myUrl?.let {
        if (it.toLowerCase(Locale.ROOT).endsWith("svg")) {
            val imageLoader = ImageLoader.Builder(this.context)
                .componentRegistry {
                    add(SvgDecoder(this@loadSvgOrOther.context))
                }.build()

            val request = ImageRequest.Builder(this.context).apply {
                error(errorImg)
                placeholder(errorImg)
                data(it).decoder(SvgDecoder(this@loadSvgOrOther.context))
            }.target(this).build()

            imageLoader.enqueue(request)
        } else {
            val imageLoader = ImageLoader(this.context)

            val request = ImageRequest.Builder(this.context).apply {
                if (cache) {
                    memoryCachePolicy(CachePolicy.ENABLED)
                } else {
                    memoryCachePolicy(CachePolicy.DISABLED)
                }
                error(errorImg)
                placeholder(errorImg)
                data(it)
            }.target(this).build()

            imageLoader.enqueue(request)
        }
    }
}

fun SharedPreferences.encodeCredentials(): String =
    Base64.getEncoder().encodeToString((this.getString("username", "")+":"+this.getString("password", "")).encodeToByteArray())
