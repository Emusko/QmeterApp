package com.example.paydaychallenge.utils

import com.example.paydaychallenge.service.model.remote.response.GetTransactionResponseModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

private class EventDetailSortByDate : Comparator<GetTransactionResponseModel> {
    override fun compare(
        transaction1: GetTransactionResponseModel,
        transaction2: GetTransactionResponseModel
    ): Int {
        val dateObject1: Date = stringToDate(transaction1.date)
        val dateObject2: Date = stringToDate(transaction2.date)
        val cal1: Calendar = Calendar.getInstance()
        cal1.time = dateObject1
        val cal2: Calendar = Calendar.getInstance()
        cal2.time = dateObject2
        val month1: Int = cal1.get(Calendar.MONTH)
        val month2: Int = cal2.get(Calendar.MONTH)
        return if (month1 < month2) -1 else if (month1 == month2) cal1.get(Calendar.DAY_OF_MONTH) - cal2.get(
            Calendar.DAY_OF_MONTH
        ) else 1
    }
    private fun stringToDate( theDateString: String ): Date {
        var returnDate = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-ddTHH:mm:ssZ")
        try {
            returnDate = dateFormat.parse(theDateString)
        } catch (e: ParseException) {
            val altdateFormat = SimpleDateFormat("dd-MM-yyyy")
            try {
                returnDate = altdateFormat.parse(theDateString)
            } catch (ex: ParseException) {
                ex.printStackTrace()
            }
        }
        return returnDate
    }
}