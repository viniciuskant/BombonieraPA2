package com.bomboniere.app.Lotes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bomboniere.app.R
import java.time.format.DateTimeFormatter

class ItemAdapter(private val itens: List<Item>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNome: TextView = itemView.findViewById(R.id.tvNome)
        val tvQuantidade: TextView = itemView.findViewById(R.id.tvQuantidade)
        val tvData: TextView = itemView.findViewById(R.id.tvData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produto, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itens[position]

        holder.tvNome.text = item.nome
        holder.tvQuantidade.text = item.quantidade?.toString() ?: "0"
        holder.tvData.text = item.data.toString()
    }

    override fun getItemCount() = itens.size
}