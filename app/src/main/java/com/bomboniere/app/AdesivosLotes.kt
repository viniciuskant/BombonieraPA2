package com.bomboniere.app

import com.bomboniere.app.Lotes.*

import android.content.Intent
import android.os.Bundle
import android.app.DatePickerDialog
import android.view.View
import android.widget.*
import java.time.format.DateTimeFormatter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import java.util.*
import java.time.LocalDate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class AdesivosLotes : AppCompatActivity() {

    private lateinit var listaDeItens: MutableList<Item>
    private lateinit var adapter: ItemAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var loteManager: Lote


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_adesivos_lotes)

        val back_home = findViewById<Button>(R.id.back_home)
        val btnAdicionarProduto = findViewById<Button>(R.id.btnAdicionarProduto)
        val btnGerarPDF = findViewById<Button>(R.id.btnGerarPDF)

        val edtData = findViewById<EditText>(R.id.edtData)
        val txtNomeProduto = findViewById<EditText>(R.id.edtNomeProduto)
        val txtQuantidade = findViewById<EditText>(R.id.edtQuantidade)
        val txtLote = findViewById<TextView>(R.id.viewLote)
        val calendar = Calendar.getInstance()

        val switchNotificacao = findViewById<Switch>(R.id.switchNotificacao)
        val layoutNotificacao = findViewById<TextInputLayout>(R.id.layoutNotificacao)
        val editNotificacao = findViewById<TextInputEditText>(R.id.editNotificacao)

        listaDeItens = mutableListOf()
        loteManager = Lote(this)
        recyclerView = findViewById(R.id.recyclerViewItens)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ItemAdapter(listaDeItens)
        recyclerView.adapter = adapter
        var valorNotificacao: Int = -1


        back_home.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        switchNotificacao.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                layoutNotificacao.visibility = View.VISIBLE
                editNotificacao.requestFocus()
            } else {
                layoutNotificacao.visibility = View.GONE
                editNotificacao.setText("")
                valorNotificacao = -1
            }
        }

        editNotificacao.addTextChangedListener {
            valorNotificacao = it.toString().toIntOrNull() ?: -1
        }


        btnAdicionarProduto.setOnClickListener {
            val dataString = edtData.text.toString()
            val nomeProduto = txtNomeProduto.text.toString()
            val quantidadeProduto = txtQuantidade.text.toString().toIntOrNull()
            val lote = txtLote.text.toString().replace("Lote: ", "").toIntOrNull() ?: 0

            if (nomeProduto.isBlank()) {
                Toast.makeText(this, "Informe o nome do produto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (quantidadeProduto == null || quantidadeProduto <= 0) {
                Toast.makeText(this, "Informe uma quantidade válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val dataInt = try {
                val localDate = LocalDate.parse(dataString, formatter)
                localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInt()
            } catch (e: Exception) {
                Toast.makeText(this, "Data inválida. Use DD/MM/AAAA", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val novoItem = Item(nome = nomeProduto, quantidade = quantidadeProduto, data = dataInt, lote = lote, notificar = valorNotificacao)

            listaDeItens.add(novoItem)
            adapter.notifyItemInserted(listaDeItens.size - 1)

            Toast.makeText(this, "Produto adicionado com sucesso", Toast.LENGTH_SHORT).show()

            txtNomeProduto.text.clear()
            txtQuantidade.text.clear()
            txtNomeProduto.requestFocus()
        }


        btnGerarPDF.setOnClickListener {
            if (listaDeItens.isEmpty()) {
                Toast.makeText(this, "Nenhum item para salvar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                loteManager.armazenarTodosLotes(listaDeItens)

                Toast.makeText(this, "${listaDeItens.size} itens salvos com sucesso!", Toast.LENGTH_LONG).show()


            } catch (e: Exception) {
                Toast.makeText(this, "Erro ao salvar itens: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            listaDeItens.clear()
            adapter.notifyDataSetChanged()
        }

        fun atualizarDataELote(year: Int, month: Int, day: Int) {
            val calendarSelecionado = Calendar.getInstance()
            calendarSelecionado.set(year, month, day)

            val dataFormatada = String.format("%02d/%02d/%04d", day, month + 1, year)
            edtData.setText(dataFormatada)

            val lote = loteManager.calcularHashEUltimos6Digitos(calendarSelecionado.time)
            txtLote.text = "Lote: $lote"
        }

        atualizarDataELote(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        edtData.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    atualizarDataELote(year, month, day)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
}