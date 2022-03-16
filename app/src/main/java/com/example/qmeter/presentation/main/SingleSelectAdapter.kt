package com.example.qmeter.presentation.main

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatTextView
import kotlinx.android.synthetic.main.single_select_view.view.*
import com.example.qmeter.service.model.remote.response.GetWidgetsResponseModel
import com.example.qmeter.utils.setDynamicSize


class SingleSelectAdapter(
    private val language: String?,
    private val context: Activity,
    private val resourceId: Int,
    private val dropDownView: Int,
    private val size: String?,
    list: MutableList<GetWidgetsResponseModel.SelectOption?>
) : ArrayAdapter<GetWidgetsResponseModel.SelectOption?>(
    context, resourceId, list
) {
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var rowView = convertView
        if (rowView == null){
            rowView = context.layoutInflater.inflate(dropDownView, null)
        }
        (rowView as? AppCompatTextView)?.text = getItem(position)?.option!![language]
        (rowView as? AppCompatTextView)?.setDynamicSize(size)
        (rowView as? AppCompatTextView)?.setPadding(18, 18, 18,18 )
        return rowView!!

    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var rowView = convertView
        if (rowView == null){
            rowView = context.layoutInflater.inflate(resourceId, null)
        }
        rowView?.appCompatTextView?.text = getItem(position)?.option!![language]
        rowView?.appCompatTextView?.setDynamicSize(size)
        return rowView!!
    }
}