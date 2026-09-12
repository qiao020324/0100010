package com.autoledger.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.autoledger.app.R
import com.autoledger.app.data.Transaction
import com.autoledger.app.parse.Categories
import java.text.SimpleDateFormat
import java.util.Locale

class TxAdapter(private var items: List<Transaction>) :
    RecyclerView.Adapter<TxAdapter.VH>() {

    private val fmt = SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)

    fun update(list: List<Transaction>) {
        items = list
        notifyDataSetChanged()
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tx_name)
        val desc: TextView = view.findViewById(R.id.tx_desc)
        val amount: TextView = view.findViewById(R.id.tx_amount)
        val icon: View = view.findViewById(R.id.tx_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tx, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = items[position]
        val cat = Categories.get(t.category)
        holder.name.text = t.merchant
        holder.desc.text = "${cat.name} · ${t.payMethod} · ${fmt.format(t.time)}"
        holder.amount.text =
            (if (t.type == "income") "+" else "") + "¥" + String.format("%.2f", t.amount)
        holder.icon.setBackgroundColor(cat.color)
    }

    override fun getItemCount() = items.size
}
