package com.technolink.qmeter.utils

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet


class FontAwesome : androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    private fun init() {

        //Font name should not contain "/".
        val tf = Typeface.createFromAsset(
            context.assets,
            "q_meter.ttf"
        )
        typeface = tf
    }
}