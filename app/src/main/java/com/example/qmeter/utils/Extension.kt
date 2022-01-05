package com.example.qmeter.utils

import android.graphics.Color
import com.example.qmeter.service.model.remote.response.AuthenticationResponseModel
import com.example.qmeter.service.model.remote.response.PageComponent

fun AuthenticationResponseModel.Page.makePages(): ArrayList<PageComponent?> {

    val components = arrayListOf<PageComponent?>().apply {
        add(this@makePages.commentData)
        add(this@makePages.customerData)
        add(this@makePages.sliData)
    }

    components.sortedBy { it?.position }

    return components
}

fun ArrayList<Int>.getColor(): Int {
    return Color.argb(this[0], this[1], this[2], this[3])
}