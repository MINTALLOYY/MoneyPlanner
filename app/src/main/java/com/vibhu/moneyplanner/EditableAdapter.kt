package com.vibhu.moneyplanner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

abstract class EditableAdapter<T>(
    protected var items: List<T>,
    protected val context: Context,
    protected val onItemUpdated: () -> Unit
) : RecyclerView.Adapter<EditableAdapter.EditableViewHolder>() {

    abstract fun getLayoutId(): Int
    abstract fun bindData(holder: EditableViewHolder, item: T)
    abstract fun showEditDialog(item: T, context: Context)
    abstract fun showDeleteConfirmationDialog(item: T, context: Context)

    inner class EditableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditableViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(getLayoutId(), parent, false)
        return EditableViewHolder(view)
    }

    override fun onBindViewHolder(holder: EditableViewHolder, position: Int) {
        val item = items[position]
        bindData(holder, item)

        holder.editButton.setOnClickListener {
            showEditDialog(item, holder.itemView.context)
        }

        holder.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(item, holder.itemView.context)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<T>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}