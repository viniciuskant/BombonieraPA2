package com.bomboniere.app.conversao

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bomboniere.app.R
import com.bomboniere.app.data.model.Dimensao
import com.bomboniere.app.data.model.Unidade

class UnidadesAdapter(
    private val onEdit: (Unidade) -> Unit,
    private val onDelete: (Unidade) -> Unit
) : ListAdapter<Unidade, UnidadesAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<Unidade>() {
        override fun areItemsTheSame(a: Unidade, b: Unidade) = a.id == b.id
        override fun areContentsTheSame(a: Unidade, b: Unidade) = a == b
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val nome: TextView = v.findViewById(R.id.itemNome)
        val detalhe: TextView = v.findViewById(R.id.itemDetalhe)
        val editar: ImageButton = v.findViewById(R.id.buttonEditar)
        val apagar: ImageButton = v.findViewById(R.id.buttonApagar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_unidade, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val u = getItem(pos)
        h.nome.text = u.nome
        h.detalhe.text = "${u.dimensao.name} • 1 ${u.nome} = ${fmt(u.fatorParaBase)} ${unidadeBase(u.dimensao)}"

        h.editar.isEnabled = !u.predefinida
        h.apagar.isEnabled = !u.predefinida
        h.editar.alpha = if (u.predefinida) 0.3f else 1f
        h.apagar.alpha = if (u.predefinida) 0.3f else 1f

        h.editar.setOnClickListener { onEdit(u) }
        h.apagar.setOnClickListener { onDelete(u) }
    }

    private fun fmt(v: Double): String =
        if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()

    private fun unidadeBase(d: Dimensao): String = when (d) {
        Dimensao.MASSA -> "g"
        Dimensao.VOLUME -> "ml"
        Dimensao.CONTAGEM -> "unidade"
    }
}