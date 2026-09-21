package com.bomboniere.app.receitas

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bomboniere.app.BomboniereApp
import com.bomboniere.app.R
import com.bomboniere.app.data.model.Insumo
import com.bomboniere.app.data.model.Receita
import com.bomboniere.app.data.model.ReceitaItem
import com.bomboniere.app.data.model.TipoItem
import com.bomboniere.app.data.model.Unidade
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ReceitaDetalheActivity : AppCompatActivity() {

    private val repo by lazy { (application as BomboniereApp).repo }
    private val receitaId: Long by lazy { intent.getLongExtra("receitaId", 0L) }

    private var unidades: List<Unidade> = emptyList()
    private var insumos: List<Insumo> = emptyList()
    private var todasReceitas: List<Receita> = emptyList()

    private lateinit var adapter: ReceitaItensAdapter
    private lateinit var spinnerRef: Spinner
    private lateinit var spinnerUnidade: Spinner
    private lateinit var radioInsumo: RadioButton
    private lateinit var radioReceita: RadioButton
    private lateinit var textCustoTotal: TextView
    private lateinit var textCustoUnitario: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receita_detalhe)

        val textNome = findViewById<TextView>(R.id.textNomeReceita)
        textCustoTotal = findViewById(R.id.textCustoTotal)
        textCustoUnitario = findViewById(R.id.textCustoUnitario)
        val recycler = findViewById<RecyclerView>(R.id.recyclerItens)
        val editQtd = findViewById<EditText>(R.id.editQtdItem)
        val buttonAdd = findViewById<android.widget.Button>(R.id.buttonAdicionarItem)

        radioInsumo = findViewById(R.id.radioInsumo)
        radioReceita = findViewById(R.id.radioReceita)
        spinnerRef = findViewById(R.id.spinnerReferencia)
        spinnerUnidade = findViewById(R.id.spinnerUnidadeItem)

        // Adapter simples: passa 3 lambdas (nome, unidade, custo) + 2 de ação
        adapter = ReceitaItensAdapter(
            nomeProvider = { item -> buscarNomeRef(item) },
            unidadeProvider = { item ->
                unidades.firstOrNull { it.id == item.unidadeId }?.nome ?: "?"
            },
            custoProvider = { item -> repo.custoItemReceita(item) },
            onEdit = { item -> abrirDialogEditarItem(item) },
            onDelete = { item ->
                lifecycleScope.launch {
                    repo.removerItem(item)
                    Toast.makeText(
                        this@ReceitaDetalheActivity,
                        "Item removido",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // --- Observa receitas ---
        lifecycleScope.launch {
            repo.receitasFlow.collect { lista ->
                todasReceitas = lista
                lista.firstOrNull { it.id == receitaId }?.let { textNome.text = it.nome }
                atualizarSpinnerReferencia()
                atualizarCustos()
            }
        }

        // --- Observa insumos ---
        lifecycleScope.launch {
            repo.insumosFlow.collect { lista ->
                insumos = lista
                atualizarSpinnerReferencia()
                atualizarCustos()
            }
        }

        // --- Observa unidades ---
        lifecycleScope.launch {
            repo.unidadesFlow.collect { lista ->
                unidades = lista
                spinnerUnidade.adapter = ArrayAdapter(
                    this@ReceitaDetalheActivity,
                    android.R.layout.simple_spinner_item,
                    lista.map { it.nome }
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
            }
        }

        // --- Observa itens ---
        lifecycleScope.launch {
            repo.itensFlow.collect { todos ->
                adapter.submitList(todos.filter { it.receitaId == receitaId })
                atualizarCustos()
            }
        }

        radioInsumo.setOnClickListener { atualizarSpinnerReferencia() }
        radioReceita.setOnClickListener { atualizarSpinnerReferencia() }

        buttonAdd.setOnClickListener {
            val qtd = editQtd.text.toString().toDoubleOrNull() ?: 0.0
            val unidadeId = unidades.getOrNull(spinnerUnidade.selectedItemPosition)?.id ?: 0L
            val usarReceitas = radioReceita.isChecked

            lifecycleScope.launch {
                val result = if (usarReceitas) {
                    val refId = todasReceitas
                        .filter { it.id != receitaId }
                        .getOrNull(spinnerRef.selectedItemPosition)?.id ?: 0L
                    repo.adicionarItem(receitaId, TipoItem.RECEITA, refId, qtd, unidadeId)
                } else {
                    val refId = insumos.getOrNull(spinnerRef.selectedItemPosition)?.id ?: 0L
                    repo.adicionarItem(receitaId, TipoItem.INSUMO, refId, qtd, unidadeId)
                }

                result
                    .onSuccess {
                        Toast.makeText(
                            this@ReceitaDetalheActivity,
                            "Item adicionado",
                            Toast.LENGTH_SHORT
                        ).show()
                        editQtd.text?.clear()
                    }
                    .onFailure {
                        Toast.makeText(
                            this@ReceitaDetalheActivity,
                            it.message ?: "Erro",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }
    }

    /** Devolve o nome do insumo ou da sub-receita referenciada. */
    private fun buscarNomeRef(item: ReceitaItem): String = when (item.tipo) {
        TipoItem.INSUMO -> insumos.firstOrNull { it.id == item.referenciaId }?.nome ?: "?"
        TipoItem.RECEITA -> todasReceitas.firstOrNull { it.id == item.referenciaId }?.nome ?: "?"
    }

    private fun atualizarSpinnerReferencia() {
        val usarReceitas = radioReceita.isChecked
        val nomes = if (usarReceitas) {
            todasReceitas.filter { it.id != receitaId }.map { it.nome }
        } else {
            insumos.map { it.nome }
        }
        spinnerRef.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            nomes
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun atualizarCustos() {
        lifecycleScope.launch {
            val f = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            val total = repo.custoTotalReceita(receitaId)
            val unit = repo.custoPorUnidadeReceita(receitaId)
            textCustoTotal.text = "Custo total: ${f.format(total)}"
            textCustoUnitario.text = "Custo por unidade: ${f.format(unit)}"
        }
    }

    private fun abrirDialogEditarItem(item: ReceitaItem) {
        val input = EditText(this).apply {
            setText(item.quantidade.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setSelection(text.length)
        }

        AlertDialog.Builder(this)
            .setTitle("Editar quantidade")
            .setView(input)
            .setPositiveButton("Salvar") { _, _ ->
                val nova = input.text.toString().toDoubleOrNull()
                    ?: return@setPositiveButton
                lifecycleScope.launch {
                    repo.removerItem(item)
                    repo.adicionarItem(
                        item.receitaId,
                        item.tipo,
                        item.referenciaId,
                        nova,
                        item.unidadeId
                    ).onFailure {
                        Toast.makeText(
                            this@ReceitaDetalheActivity,
                            it.message ?: "Erro",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}