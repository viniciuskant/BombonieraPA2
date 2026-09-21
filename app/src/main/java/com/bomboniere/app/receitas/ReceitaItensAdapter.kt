package com.bomboniere.app.receitas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bomboniere.app.R
import com.bomboniere.app.data.model.ReceitaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ReceitaItensAdapter(
    private val nomeProvider: suspend (ReceitaItem) -> String,
    private val unidadeProvider: suspend (ReceitaItem) -> String,
    private val custoProvider: suspend (ReceitaItem) -> Double,
    private val onEdit: (ReceitaItem) -> Unit,
    private val onDelete: (ReceitaItem) -> Unit
) : ListAdapter<ReceitaItem, ReceitaItensAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<ReceitaItem>() {
        override fun areItemsTheSame(a: ReceitaItem, b: ReceitaItem) = a.id == b.id
        override fun areContentsTheSame(a: ReceitaItem, b: ReceitaItem) = a == b
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val nome: TextView = v.findViewById(R.id.itemNome)
        val detalhe: TextView = v.findViewById(R.id.itemDetalhe)
        val custo: TextView = v.findViewById(R.id.itemCusto)
        val editar: ImageButton = v.findViewById(R.id.buttonEditar)
        val apagar: ImageButton = v.findViewById(R.id.buttonApagar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_receita_detalhe, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = getItem(pos)

        h.nome.text = "…"
        h.detalhe.text = "${item.quantidade} …  (${item.tipo.name})"
        h.custo.text = "Custo: …"

        CoroutineScope(Dispatchers.Main).launch {
            val nome = nomeProvider(item)
            val unidade = unidadeProvider(item)
            val custo = custoProvider(item)
            val f = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

            h.nome.text = nome
            h.detalhe.text = "${item.quantidade} $unidade  (${item.tipo.name})"
            h.custo.text = "Custo: ${f.format(custo)}"
        }

        h.editar.setOnClickListener { onEdit(item) }
        h.apagar.setOnClickListener { onDelete(item) }
    }
}