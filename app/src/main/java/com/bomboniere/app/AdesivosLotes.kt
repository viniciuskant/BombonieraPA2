package com.bomboniere.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bomboniere.app.Lotes.Item
import com.bomboniere.app.Lotes.ItemAdapter
import com.bomboniere.app.Lotes.Lote
import com.bomboniere.app.etiquetas.StickerPdfGenerator
import com.bomboniere.app.etiquetas.StickerPdfGenerator.Sticker
import com.bomboniere.app.etiquetas.PdfOpener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdesivosLotes : AppCompatActivity() {


    private lateinit var listaDeItens: MutableList<Item>
    private lateinit var adapter: ItemAdapter
    private lateinit var loteManager: Lote
    private var valorNotificacao: Int = -1

    private lateinit var edtData: EditText
    private lateinit var txtNomeProduto: EditText
    private lateinit var txtQuantidade: EditText
    private lateinit var txtLote: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adesivos_lotes)

        bindViews()
        setupRecycler()
        setupNotificacao()
        setupBotoes()
        inicializarData()
    }

    private fun bindViews() {
        edtData = findViewById(R.id.edtData)
        txtNomeProduto = findViewById(R.id.edtNomeProduto)
        txtQuantidade = findViewById(R.id.edtQuantidade)
        txtLote = findViewById(R.id.viewLote)
    }

    private fun setupRecycler() {
        listaDeItens = mutableListOf()
        loteManager = Lote(this)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewItens)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ItemAdapter(listaDeItens)
        recyclerView.adapter = adapter
    }

    private fun setupNotificacao() {
        val switch = findViewById<Switch>(R.id.switchNotificacao)
        val layout = findViewById<TextInputLayout>(R.id.layoutNotificacao)
        val edit = findViewById<TextInputEditText>(R.id.editNotificacao)

        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                layout.visibility = View.VISIBLE
                edit.requestFocus()
            } else {
                layout.visibility = View.GONE
                edit.setText("")
                valorNotificacao = -1
            }
        }

        edit.addTextChangedListener {
            valorNotificacao = it.toString().toIntOrNull() ?: -1
        }
    }

    private fun setupBotoes() {
        findViewById<Button>(R.id.btnAdicionarProduto).setOnClickListener {
            adicionarProduto()
        }

        findViewById<Button>(R.id.btnGerarPDF).setOnClickListener {
            gerarPdf()
        }
    }

    private fun inicializarData() {
        val calendar = Calendar.getInstance()

        atualizarDataELote(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        )

        edtData.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day -> atualizarDataELote(year, month, day) },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
            ).show()
        }
    }

    private fun atualizarDataELote(year: Int, month: Int, day: Int) {
        val c = Calendar.getInstance().apply { set(year, month, day) }
        edtData.setText(String.format("%02d/%02d/%04d", day, month + 1, year))

        val lote = loteManager.calcularHashEUltimos6Digitos(c.time)
        txtLote.text = "Lote: $lote"
    }

    private fun adicionarProduto() {
        val dataString = edtData.text.toString()
        val nomeProduto = txtNomeProduto.text.toString()
        val quantidadeProduto = txtQuantidade.text.toString().toIntOrNull()
        val lote = txtLote.text.toString().replace("Lote: ", "").toIntOrNull() ?: 0

        if (nomeProduto.isBlank()) {
            toast("Informe o nome do produto")
            return
        }

        if (quantidadeProduto == null || quantidadeProduto <= 0) {
            toast("Informe uma quantidade válida")
            return
        }

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val dataInt = try {
            LocalDate.parse(dataString, formatter)
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                .toInt()
        } catch (e: Exception) {
            toast("Data inválida. Use DD/MM/AAAA")
            return
        }

        val novoItem = Item(
            nome = nomeProduto,
            quantidade = quantidadeProduto,
            data = dataInt,
            lote = lote,
            notificar = valorNotificacao,
        )

        listaDeItens.add(novoItem)
        adapter.notifyItemInserted(listaDeItens.size - 1)
        toast("Produto adicionado com sucesso")

        txtNomeProduto.text.clear()
        txtQuantidade.text.clear()
        txtNomeProduto.requestFocus()
    }

    private fun gerarPdf() {
        if (listaDeItens.isEmpty()) {
            toast("Nenhum item para salvar")
            return
        }

        try {
            loteManager.armazenarTodosLotes(listaDeItens)
        } catch (e: Exception) {
            toast("Erro ao salvar lotes: ${e.message}")
            return
        }

        val stickers = listaDeItens.flatMap { item ->
            val qtd = item.quantidade ?: return@flatMap emptyList()
            List(qtd) {
                Sticker(
                    productName = item.nome,
                    lote = item.lote.toString(),
                )
            }
        }

        val outputDir = getExternalFilesDir(null) ?: filesDir
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val outputFile = File(outputDir, "adesivos_$timestamp.pdf")

        try {
            StickerPdfGenerator.gerarFolha(this, stickers, outputFile)
        } catch (e: Exception) {
            toast("Erro ao gerar PDF: ${e.message}")
            return
        }

        toast("${stickers.size} adesivos gerados")
        android.util.Log.d("PDF", "Gerado: ${outputFile.absolutePath}")

        listaDeItens.clear()
        adapter.notifyDataSetChanged()

        PdfOpener.abrir(this, outputFile)
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}