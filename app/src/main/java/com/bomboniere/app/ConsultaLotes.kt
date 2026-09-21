package com.bomboniere.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bomboniere.app.Lotes.Item
import com.bomboniere.app.Lotes.Lote
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConsultaLotes : AppCompatActivity() {

    private lateinit var loteManager: Lote
    private lateinit var adapter: LotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consulta_lotes)

        loteManager = Lote(this)

        val editLote = findViewById<TextInputEditText>(R.id.editLote)
        val buttonVerificar = findViewById<MaterialButton>(R.id.buttonVerificar)
        val textResultado = findViewById<TextView>(R.id.textResultado)
        val recycler = findViewById<RecyclerView>(R.id.recyclerLotes)
        val textVazio = findViewById<TextView>(R.id.textVazio)

        adapter = LotesAdapter()
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // Carrega os últimos 15 lotes ao abrir a tela
        carregarUltimosLotes(recycler, textVazio)

        buttonVerificar.setOnClickListener {
            val codigo = editLote.text.toString().trim()

            if (codigo.isEmpty()) {
                textResultado.visibility = View.VISIBLE
                textResultado.text = "Por favor, insira um lote."
                return@setOnClickListener
            }
            if (codigo.length != 6) {
                textResultado.visibility = View.VISIBLE
                textResultado.text = "Código inválido. O lote deve ter 6 dígitos."
                return@setOnClickListener
            }

            val data = loteManager.calcularDataDoLote(codigo)
            textResultado.visibility = View.VISIBLE

            if (data != null) {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val diffMs = Date().time - data.time
                val dias = (diffMs / (1000L * 60L * 60L * 24L)).toInt()
                val semanas = dias / 7
                val meses = dias / 30

                val tempoAtras = buildString {
                    if (meses > 0) append("$meses ${if (meses > 1) "meses" else "mês"} ")
                    if (semanas % 4 > 0) append("${semanas % 4} ${if (semanas % 4 > 1) "semanas" else "semana"} ")
                    if (dias % 7 > 0) append("${dias % 7} ${if (dias % 7 > 1) "dias" else "dia"}")
                }.trim().ifEmpty { "hoje" }

                textResultado.text =
                    "Data de Produção: ${sdf.format(data)}\nProduzido há: $tempoAtras"
            } else {
                textResultado.text = "Nenhuma data encontrada com esse lote."
            }
        }
    }

    private fun carregarUltimosLotes(recycler: RecyclerView, textVazio: TextView) {
        val todos = loteManager.buscarTodosItens()

        // Ordena pela data de produção (mais recente primeiro) e pega os 15 primeiros
        val ultimos = todos
            .sortedByDescending { it.data }
            .take(15)

        if (ultimos.isEmpty()) {
            textVazio.visibility = View.VISIBLE
            recycler.visibility = View.GONE
        } else {
            textVazio.visibility = View.GONE
            recycler.visibility = View.VISIBLE
            adapter.submitList(ultimos)
        }
    }
}

// ---------- Adapter ----------

class LotesAdapter : ListAdapter<Item, LotesAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(a: Item, b: Item) =
            a.lote == b.lote && a.nome == b.nome

        override fun areContentsTheSame(a: Item, b: Item) = a == b
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val nome: TextView = v.findViewById(R.id.itemNome)
        val data: TextView = v.findViewById(R.id.itemData)
        val lote: TextView = v.findViewById(R.id.itemLote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_consulta_lote, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = getItem(pos)
        h.nome.text = item.nome
        h.lote.text = "%06d".format(item.lote)
        h.data.text = formatarData(item.data)
    }

    /** `data` no banco é Int no formato yyyyMMdd. Ex.: 20260112 → "12/01/2026" */
    private fun formatarData(dataInt: Int): String {
        val s = dataInt.toString()
        return if (s.length == 8) {
            "${s.substring(6, 8)}/${s.substring(4, 6)}/${s.substring(0, 4)}"
        } else {
            s
        }
    }
}