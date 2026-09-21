package com.bomboniere.app.insumos

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
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
import com.bomboniere.app.data.model.Unidade
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class InsumosActivity : AppCompatActivity() {

    private val repo by lazy { (application as BomboniereApp).repo }
    private lateinit var adapter: InsumosAdapter
    private var unidades: List<Unidade> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insumos)

        val recycler = findViewById<RecyclerView>(R.id.recyclerInsumos)
        val fab = findViewById<FloatingActionButton>(R.id.fabAdicionar)
        val textVazio = findViewById<TextView>(R.id.textVazio)

        adapter = InsumosAdapter(
            onEdit = { insumo -> abrirDialog(insumo) },
            onDelete = { insumo -> confirmarApagar(insumo) }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        fab.setOnClickListener { abrirDialog(null) }

        // Observa unidades e insumos
        lifecycleScope.launch {
            repo.unidadesFlow.collect { unidades = it }
        }

        lifecycleScope.launch {
            repo.insumosFlow.collect { lista ->
                val mapa = unidades.associateBy { it.id }
                val ui = lista.map { InsumoUi(it, mapa[it.unidadeId]) }
                adapter.submitList(ui)

                val vazio = ui.isEmpty()
                textVazio.visibility = if (vazio) TextView.VISIBLE else TextView.GONE
                recycler.visibility = if (vazio) RecyclerView.GONE else RecyclerView.VISIBLE
            }
        }
    }

    /**
     * Abre o dialog de adicionar (insumo = null) ou editar (insumo preenchido).
     */
    private fun abrirDialog(insumo: Insumo?) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_insumo, null)
        val editNome = view.findViewById<EditText>(R.id.editNome)
        val editQtd = view.findViewById<EditText>(R.id.editQuantidade)
        val editValor = view.findViewById<EditText>(R.id.editValor)
        val spinner = view.findViewById<Spinner>(R.id.spinnerUnidade)

        // Popula spinner
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            unidades.map { it.nome }
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Se editando, preenche os campos
        if (insumo != null) {
            editNome.setText(insumo.nome)
            editQtd.setText(insumo.quantidade.toString())
            editValor.setText(insumo.valor.toString())
            val pos = unidades.indexOfFirst { it.id == insumo.unidadeId }
            if (pos >= 0) spinner.setSelection(pos)
        }

        val titulo = if (insumo == null) "Adicionar insumo" else "Editar insumo"

        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setView(view)
            .setPositiveButton("Salvar", null) // null para controlar manualmente
            .setNegativeButton("Cancelar", null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val nome = editNome.text.toString()
                        val qtd = editQtd.text.toString().toDoubleOrNull() ?: 0.0
                        val valor = editValor.text.toString().toDoubleOrNull() ?: 0.0
                        val unidadeId = unidades.getOrNull(spinner.selectedItemPosition)?.id ?: 0L

                        lifecycleScope.launch {
                            val result = if (insumo == null) {
                                repo.inserirInsumo(nome, qtd, unidadeId, valor)
                            } else {
                                repo.atualizarInsumo(
                                    insumo.copy(
                                        nome = nome,
                                        quantidade = qtd,
                                        unidadeId = unidadeId,
                                        valor = valor
                                    )
                                )
                            }

                            result
                                .onSuccess {
                                    Toast.makeText(
                                        this@InsumosActivity,
                                        if (insumo == null) "Insumo salvo" else "Insumo atualizado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    dialog.dismiss()
                                }
                                .onFailure {
                                    Toast.makeText(
                                        this@InsumosActivity,
                                        it.message ?: "Erro",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                    }
                }
            }
            .show()
    }

    private fun confirmarApagar(insumo: Insumo) {
        AlertDialog.Builder(this)
            .setTitle("Apagar insumo")
            .setMessage("Apagar \"${insumo.nome}\"? Receitas que usam esse insumo terão o custo recalculado.")
            .setPositiveButton("Apagar") { _, _ ->
                lifecycleScope.launch {
                    repo.removerInsumo(insumo)
                    Toast.makeText(this@InsumosActivity, "Insumo apagado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}