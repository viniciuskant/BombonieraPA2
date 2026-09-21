package com.bomboniere.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AdesivosPersonalizados : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adesivos_pesonalizados)

        val recycler = findViewById<RecyclerView>(R.id.recyclerCategorias)
        recycler.layoutManager = GridLayoutManager(this, 2)

        val categorias = carregarCategorias()
        recycler.adapter = CategoriaAdapter(categorias) { cat ->
            val it = Intent(this, AdesivosCategoriaActivity::class.java)
            it.putExtra("categoria", cat.pasta)
            startActivity(it)
        }
    }

    private fun carregarCategorias(): List<Categoria> {
        val lista = mutableListOf<Categoria>()
        val pastas = assets.list("pdfs")?.sorted() ?: return lista

        // Capa preferida por pasta (pode trocar os nomes aqui)
        val capasPreferidas = mapOf(
            "bolos_pote"     to "brigadeiro.webp",
            "personalizados" to "generico.webp"
        )

        for (pasta in pastas) {
            val previews = assets.list("preview/$pasta")?.sorted().orEmpty()
            if (previews.isEmpty()) continue

            val capa = capasPreferidas[pasta]?.takeIf { it in previews }
                ?: previews.first()

            lista.add(
                Categoria(
                    nome = pasta,
                    pasta = pasta,
                    previewPath = "preview/$pasta/$capa"
                )
            )
        }
        return lista
    }
}