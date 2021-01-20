package com.example.paydaychallenge.presentation.transactions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.paydaychallenge.R
import com.example.paydaychallenge.service.model.remote.response.GetTransactionResponseModel
import kotlinx.android.synthetic.main.item_list_view.view.*

class TransactionsAdapter(
    private val transactionList: ArrayList<GetTransactionResponseModel>,
    private val context: Context
) : RecyclerView.Adapter<TransactionsAdapter.FormAListViewHolder>(), Filterable {
    class FormAListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(
            transaction: GetTransactionResponseModel,
            context: Context
        ) {
            itemView.category.text = transaction.category
            itemView.amount.text = transaction.amount.toString()
            itemView.description.text = transaction.vendor
            itemView.date.text = context.resources.getString(
                R.string.date_format,
                transaction.date.split("T")[0],
                transaction.date.split("T")[1].removeSuffix("Z")
            )
        }
    }

    override fun getFilter(): Filter? {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val latestResults = arrayListOf<GetTransactionResponseModel>()
                if (!transactionList.isNullOrEmpty()) {
                    for (transaction in transactionList) {
                        if (transaction.category.toLowerCase().contains(constraint.toString())) {
                            latestResults.add(transaction)
                        }
                    }
                    results.values = latestResults
                }

                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results?.values != null) {
                    transactionList.clear()
                    transactionList.addAll(results.values as ArrayList<GetTransactionResponseModel>)
                    notifyDataSetChanged()
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        FormAListViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_view, parent, false)
        )

    override fun getItemCount() = transactionList.size

    fun getItems() = transactionList

    override fun onBindViewHolder(holder: FormAListViewHolder, position: Int) {
        holder.bind(transactionList[position], context)
    }
}