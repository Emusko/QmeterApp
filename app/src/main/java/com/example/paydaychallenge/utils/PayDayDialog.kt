package com.example.paydaychallenge.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.paydaychallenge.R

class PayDayDialog {
    fun getPayDayDialog(context: Context, title: String, message: String){
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(
            R.string.ok
        ) { _, _ ->

        }

        builder.setCancelable(false)
        builder.create().show()
    }
}