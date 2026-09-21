package com.bomboniere.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream

class AdesivosCategoriaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adesivos_categoria)

        val pasta = intent.getStringExtra("categoria").orEmpty()

        val recycler = findViewById<RecyclerView>(R.id.recyclerCategorias)
        recycler.layoutManager = GridLayoutManager(this, 2)

        val itens = carregarAdesivos(pasta)
        recycler.adapter = AdesivoAdapter(itens) { item ->
            abrirPdf(item)
        }
    }

    private fun carregarAdesivos(pasta: String): List<AdesivoItem> {
        val lista = mutableListOf<AdesivoItem>()
        val pdfs = assets.list("pdfs/$pasta")?.sorted().orEmpty()
        val previews = assets.list("preview/$pasta")
            ?.sorted()
            .orEmpty()
            .map { it.removeSuffix(".webp") }
            .toSet()

        for (arquivo in pdfs) {
            if (!arquivo.endsWith(".pdf")) continue
            val nome = arquivo.removeSuffix(".pdf")

            val previewPath = if (nome in previews) {
                "preview/$pasta/$nome.webp"
            } else {
                "preview/$pasta/generico.webp"
            }

            lista.add(
                AdesivoItem(
                    nome = nome,
                    pasta = pasta,
                    pdfPath = "pdfs/$pasta/$arquivo",
                    previewPath = previewPath
                )
            )
        }
        return lista
    }

    private fun abrirPdf(item: AdesivoItem) {
        try {
            val destino = File(cacheDir, "${item.pasta}_${item.nome}.pdf")

            if (!destino.exists()) {
                assets.open(item.pdfPath).use { input ->
                    FileOutputStream(destino).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            val uri: Uri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                destino
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "Nenhum app para visualizar PDF instalado.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao abrir PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}