package com.vibhu.moneyplanner.listAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vibhu.moneyplanner.R
import com.vibhu.moneyplanner.models.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class SearchListAdapter (
    private var transactions: List<Transaction>,
    private val context: Context,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<SearchListAdapter.TransactionListViewHolder>() {

    class TransactionListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.textViewTransactionName)
        val amountTextView: TextView = itemView.findViewById(R.id.textViewTransactionAmount)
        val dateTextView: TextView = itemView.findViewById(R.id.textViewTransactionDate)
//        val isIncomeIndicatorTop: View = itemView.findViewById(R.id.representationOfExpenseIncomeTop)
//        val isIncomeIndicatorBottom: View = itemView.findViewById(R.id.representationOfExpenseIncomeBottom)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TransactionListViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.nameTextView.text = transaction.transactionName

        if(transaction.isIncome){
            holder.amountTextView.text = "+ $" + transaction.amount
            holder.amountTextView.setTextColor(ContextCompat.getColor(context, R.color.green_text))
            holder.amountTextView.background = ContextCompat.getDrawable(context, R.drawable.green_container)
        }
        else{
            holder.amountTextView.text = "- $" + transaction.amount
            holder.amountTextView.setTextColor(ContextCompat.getColor(context, R.color.red_text))
            holder.amountTextView.background = ContextCompat.getDrawable(context, R.drawable.red_container)
//            holder.isIncomeIndicatorBottom.setBackgroundColor(ContextCompat.getColor(context, R.color.red))
//            holder.isIncomeIndicatorTop.setBackgroundColor(ContextCompat.getColor(context, R.color.red))
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(transaction.date)
        holder.dateTextView.text = formattedDate

        holder.itemView.setOnClickListener{
            onItemClick(transaction)
        }
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    fun updateItems(newFilteredTransactions: List<Transaction>) {
        this.transactions = newFilteredTransactions
        notifyDataSetChanged()
    }
}