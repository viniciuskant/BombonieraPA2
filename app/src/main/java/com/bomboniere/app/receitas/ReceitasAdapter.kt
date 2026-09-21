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
import com.bomboniere.app.data.model.Receita
import java.text.NumberFormat
import java.util.Locale

data class ReceitaUi(
    val receita: Receita,
    val custoTotal: Double,
    val custoPorUnidade: Double
)

class ReceitasAdapter(
    private val onItemClick: (Receita) -> Unit,
    private val onEdit: (Receita) -> Unit,
    private val onDelete: (Receita) -> Unit
) : ListAdapter<ReceitaUi, ReceitasAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<ReceitaUi>() {
        override fun areItemsTheSame(a: ReceitaUi, b: ReceitaUi) = a.receita.id == b.receita.id
        override fun areContentsTheSame(a: ReceitaUi, b: ReceitaUi) = a == b
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
            .inflate(R.layout.item_receita, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val ui = getItem(pos)
        val f = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        h.nome.text = ui.receita.nome
        h.detalhe.text = "Produz ${ui.receita.quantidadeProduzida} unidade(s)"
        h.custo.text = "${f.format(ui.custoTotal)}  •  ${f.format(ui.custoPorUnidade)}/un"

        h.itemView.setOnClickListener { onItemClick(ui.receita) }
        h.editar.setOnClickListener { onEdit(ui.receita) }
        h.apagar.setOnClickListener { onDelete(ui.receita) }
    }
}