package com.bomboniere.app

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoriaAdapter(
    private val categorias: List<Categoria>,
    private val onClick: (Categoria) -> Unit
) : RecyclerView.Adapter<CategoriaAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val imagem: ImageView = view.findViewById(R.id.imagemCategoria)
        val titulo: TextView = view.findViewById(R.id.tituloCategoria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categoria, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val cat = categorias[position]
        holder.titulo.text = formatarNome(cat.nome)

        try {
            holder.itemView.context.assets.open(cat.previewPath).use { input ->
                holder.imagem.setImageBitmap(BitmapFactory.decodeStream(input))
            }
        } catch (e: Exception) {
            holder.imagem.setImageResource(R.mipmap.ic_launcher)
        }

        holder.itemView.setOnClickListener { onClick(cat) }
    }

    override fun getItemCount() = categorias.size

    private fun formatarNome(nome: String): String =
        nome.replace("_", " ")
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}