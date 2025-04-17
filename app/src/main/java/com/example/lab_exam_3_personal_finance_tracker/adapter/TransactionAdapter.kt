package com.example.lab_exam_3_personal_finance_tracker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.lab_exam_3_personal_finance_tracker.R
import com.example.lab_exam_3_personal_finance_tracker.data.SharedPrefManager
import com.example.lab_exam_3_personal_finance_tracker.model.Transaction
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class TransactionAdapter(
    private val transactionList: MutableList<Transaction>,
    private val onEditClick: (Transaction) -> Unit,
    private val onDeleteClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val ivTransactionType: ImageView = view.findViewById(R.id.ivTransactionType)
        val categoryColorIndicator: View = view.findViewById(R.id.categoryColorIndicator)
        val categoryChip: Chip = view.findViewById(R.id.categoryChip)
        val btnEdit: MaterialButton = view.findViewById(R.id.btnEdit)
        val btnDelete: MaterialButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]
        val context = holder.itemView.context
        val currency = SharedPrefManager(context).loadCurrency()

        // Title and date
        holder.tvTitle.text = transaction.title
        holder.tvDate.text = transaction.date

        // Amount with currency and type-based coloring
        holder.tvAmount.text = "$currency %.2f".format(transaction.amount)

        if (transaction.type.equals("Income", ignoreCase = true)) {
            holder.ivTransactionType.setImageResource(R.drawable.ic_arrow_upward)
            holder.ivTransactionType.setColorFilter(ContextCompat.getColor(context, R.color.income_green))
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.income_green))
        } else {
            holder.ivTransactionType.setImageResource(R.drawable.ic_arrow_downward)
            holder.ivTransactionType.setColorFilter(ContextCompat.getColor(context, R.color.expense_red))
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.expense_red))
        }

        // Category chip
        holder.categoryChip.text = transaction.category
        holder.categoryChip.chipIcon = ContextCompat.getDrawable(context, getCategoryIconRes(transaction.category))
        holder.categoryChip.chipIconTint = ContextCompat.getColorStateList(context, R.color.teal_700)
        holder.categoryChip.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.white)
        holder.categoryChip.chipStrokeColor = ContextCompat.getColorStateList(context, R.color.light_gray)
        holder.categoryChip.chipStrokeWidth = 1f

        // Category color stripe
        holder.categoryColorIndicator.setBackgroundColor(getCategoryColorRes(transaction.category, context))

        // Click actions
        holder.btnEdit.setOnClickListener { onEditClick(transaction) }
        holder.btnDelete.setOnClickListener { onDeleteClick(transaction) }
    }

    override fun getItemCount(): Int = transactionList.size

    fun updateList(newList: List<Transaction>) {
        transactionList.clear()
        transactionList.addAll(newList)
        notifyDataSetChanged()
    }

    private fun getCategoryIconRes(category: String): Int {
        return when (category.trim().lowercase()) {
            "food" -> R.drawable.ic_category_food
            "transport" -> R.drawable.ic_category_transport
            "salary" -> R.drawable.ic_category_salary
            "freelance" -> R.drawable.ic_category_freelance
            "entertainment" -> R.drawable.ic_category_entertainment
            "bills" -> R.drawable.ic_category_bills
            "shopping" -> R.drawable.ic_category_shopping
            "health" -> R.drawable.ic_category_health
            "other" -> R.drawable.ic_category_other
            else -> R.drawable.ic_category_default
        }
    }

    private fun getCategoryColorRes(category: String, context: Context): Int {
        return when (category.trim().lowercase()) {
            "food" -> ContextCompat.getColor(context, R.color.food_color)
            "transport" -> ContextCompat.getColor(context, R.color.transport_color)
            "salary" -> ContextCompat.getColor(context, R.color.salary_color)
            "freelance" -> ContextCompat.getColor(context, R.color.freelance_color)
            "entertainment" -> ContextCompat.getColor(context, R.color.entertainment_color)
            "bills" -> ContextCompat.getColor(context, R.color.bills_color)
            "shopping" -> ContextCompat.getColor(context, R.color.shopping_color)
            "health" -> ContextCompat.getColor(context, R.color.health_color)
            "other" -> ContextCompat.getColor(context, R.color.other_color)
            else -> ContextCompat.getColor(context, R.color.colorPrimary)
        }
    }
}
