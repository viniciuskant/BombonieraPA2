package com.bomboniere.app.receitas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bomboniere.app.BomboniereApp
import com.bomboniere.app.R
import com.bomboniere.app.data.model.Receita
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ReceitasActivity : AppCompatActivity() {

    private val repo by lazy { (application as BomboniereApp).repo }
    private lateinit var adapter: ReceitasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receitas)

        val recycler = findViewById<RecyclerView>(R.id.recyclerReceitas)
        val fab = findViewById<FloatingActionButton>(R.id.fabAdicionar)
        val textVazio = findViewById<TextView>(R.id.textVazio)

        adapter = ReceitasAdapter(
            onItemClick = { receita ->
                val i = Intent(this, ReceitaDetalheActivity::class.java)
                i.putExtra("receitaId", receita.id)
                startActivity(i)
            },
            onEdit = { receita -> abrirDialog(receita) },
            onDelete = { receita -> confirmarApagar(receita) }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        fab.setOnClickListener { abrirDialog(null) }

        // Observa e recalcula custos em tempo real
        lifecycleScope.launch {
            repo.receitasFlow.collect { lista ->
                val ui = lista.map { r ->
                    ReceitaUi(
                        receita = r,
                        custoTotal = repo.custoTotalReceita(r.id),
                        custoPorUnidade = repo.custoPorUnidadeReceita(r.id)
                    )
                }
                adapter.submitList(ui)

                val vazio = ui.isEmpty()
                textVazio.visibility = if (vazio) TextView.VISIBLE else TextView.GONE
                recycler.visibility = if (vazio) RecyclerView.GONE else RecyclerView.VISIBLE
            }
        }

        // Força recálculo quando insumos/unidades/itens mudam
        lifecycleScope.launch {
            repo.insumosFlow.collect { _ ->
                val ui = adapter.currentList.map { r ->
                    ReceitaUi(
                        receita = r.receita,
                        custoTotal = repo.custoTotalReceita(r.receita.id),
                        custoPorUnidade = repo.custoPorUnidadeReceita(r.receita.id)
                    )
                }
                adapter.submitList(ui)
            }
        }
        lifecycleScope.launch {
            repo.itensFlow.collect { _ ->
                val ui = adapter.currentList.map { r ->
                    ReceitaUi(
                        receita = r.receita,
                        custoTotal = repo.custoTotalReceita(r.receita.id),
                        custoPorUnidade = repo.custoPorUnidadeReceita(r.receita.id)
                    )
                }
                adapter.submitList(ui)
            }
        }
    }

    /**
     * Abre o dialog de adicionar (receita = null) ou editar (receita preenchida).
     */
    private fun abrirDialog(receita: Receita?) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_receita, null)
        val editNome = view.findViewById<EditText>(R.id.editNome)
        val editQtd = view.findViewById<EditText>(R.id.editQtdProduzida)

        if (receita != null) {
            editNome.setText(receita.nome)
            editQtd.setText(receita.quantidadeProduzida.toString())
        }

        val titulo = if (receita == null) "Adicionar receita" else "Editar receita"

        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setView(view)
            .setPositiveButton("Salvar", null)
            .setNegativeButton("Cancelar", null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val nome = editNome.text.toString()
                        val qtd = editQtd.text.toString().toDoubleOrNull() ?: 1.0

                        lifecycleScope.launch {
                            val result = if (receita == null) {
                                repo.criarReceita(nome, qtd)
                            } else {
                                repo.atualizarReceita(
                                    receita.copy(
                                        nome = nome,
                                        quantidadeProduzida = qtd
                                    )
                                )
                            }

                            result
                                .onSuccess {
                                    Toast.makeText(
                                        this@ReceitasActivity,
                                        if (receita == null) "Receita criada" else "Receita atualizada",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    dialog.dismiss()
                                }
                                .onFailure {
                                    Toast.makeText(
                                        this@ReceitasActivity,
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

    private fun confirmarApagar(receita: Receita) {
        AlertDialog.Builder(this)
            .setTitle("Apagar receita")
            .setMessage("Apagar \"${receita.nome}\"? Os itens dentro dela também serão removidos.")
            .setPositiveButton("Apagar") { _, _ ->
                lifecycleScope.launch {
                    repo.removerReceita(receita)
                    Toast.makeText(this@ReceitasActivity, "Receita apagada", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}