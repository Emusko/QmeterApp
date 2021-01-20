package com.example.paydaychallenge.presentation.accounts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.paydaychallenge.R
import com.example.paydaychallenge.service.model.remote.response.GetAccountResponseModel
import kotlinx.android.synthetic.main.item_list_view.view.*

class AccountsAdapter(
    private val accountList: ArrayList<GetAccountResponseModel>,
    private val onClick: () -> Unit
) : RecyclerView.Adapter<AccountsAdapter.FormAListViewHolder>() {
    class FormAListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(
            account: GetAccountResponseModel,
            onClick: () -> Unit
        ) {
            itemView.category.text = account.iban
            val accountActivation = if (account.active){
                "Aktiv"
            } else {
                "Deaktiv"
            }
                itemView.amount.text = accountActivation
            itemView.description.text = account.type
            itemView.date.text = account.date_created
            itemView.setOnClickListener {
                onClick.invoke()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        FormAListViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_view, parent, false)
        )

    override fun getItemCount() = accountList.size

    override fun onBindViewHolder(holder: FormAListViewHolder, position: Int) {
        holder.bind(accountList[position], onClick)
    }
}