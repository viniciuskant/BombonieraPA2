package com.bomboniere.app.insumos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bomboniere.app.R
import com.bomboniere.app.data.model.Insumo
import com.bomboniere.app.data.model.Unidade
import java.text.NumberFormat
import java.util.Locale

data class InsumoUi(
    val insumo: Insumo,
    val unidade: Unidade?
) {
    val custoPorUnidade: Double
        get() = if (insumo.quantidade > 0) insumo.valor / insumo.quantidade else 0.0
}

class InsumosAdapter(
    private val onEdit: (Insumo) -> Unit,
    private val onDelete: (Insumo) -> Unit
) : ListAdapter<InsumoUi, InsumosAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<InsumoUi>() {
        override fun areItemsTheSame(a: InsumoUi, b: InsumoUi) = a.insumo.id == b.insumo.id
        override fun areContentsTheSame(a: InsumoUi, b: InsumoUi) = a == b
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
            .inflate(R.layout.item_insumo, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val ui = getItem(pos)
        val f = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val un = ui.unidade?.nome ?: "?"

        h.nome.text = ui.insumo.nome
        h.detalhe.text = "${ui.insumo.quantidade} $un"
        h.custo.text = "${f.format(ui.insumo.valor)}  •  ${f.format(ui.custoPorUnidade)}/$un"

        h.editar.setOnClickListener { onEdit(ui.insumo) }
        h.apagar.setOnClickListener { onDelete(ui.insumo) }
    }
}