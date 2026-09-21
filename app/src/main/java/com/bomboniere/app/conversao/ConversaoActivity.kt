package com.bomboniere.app.conversao

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
import com.bomboniere.app.data.model.Unidade
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ConversaoActivity : AppCompatActivity() {

    private val repo by lazy { (application as BomboniereApp).repo }
    private lateinit var adapter: UnidadesAdapter
    private var unidades: List<Unidade> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversao)

        val recycler = findViewById<RecyclerView>(R.id.recyclerUnidades)
        val fab = findViewById<FloatingActionButton>(R.id.fabAdicionar)
        val textVazio = findViewById<TextView>(R.id.textVazio)

        adapter = UnidadesAdapter(
            onEdit = { unidade -> abrirDialog(unidade) },
            onDelete = { unidade -> confirmarApagar(unidade) }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        fab.setOnClickListener { abrirDialog(null) }

        lifecycleScope.launch {
            repo.unidadesFlow.collect { lista ->
                unidades = lista
                adapter.submitList(lista)

                val vazio = lista.isEmpty()
                textVazio.visibility = if (vazio) TextView.VISIBLE else TextView.GONE
                recycler.visibility = if (vazio) RecyclerView.GONE else RecyclerView.VISIBLE
            }
        }
    }

    /**
     * Abre o dialog de criação (unidade = null) ou edição (unidade preenchida).
     */
    private fun abrirDialog(unidade: Unidade?) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_conversao, null)
        val editNome = view.findViewById<EditText>(R.id.editNome)
        val editOrigem = view.findViewById<EditText>(R.id.editQtdOrigem)
        val editDestino = view.findViewById<EditText>(R.id.editQtdDestino)
        val spinnerOrigem = view.findViewById<Spinner>(R.id.spinnerOrigem)

        // Popula spinner de origem com todas as unidades existentes
        spinnerOrigem.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            unidades.map { it.nome }
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Se editando, preenche o nome. A conversão precisa ser redefinida pelo usuário
        // (o valor do fator atual não é reconstruível em "origem/destino" sem ambiguidade).
        if (unidade != null) {
            editNome.setText(unidade.nome)
            // Tenta pré-preencher com uma conversão equivalente em relação à base
            val baseNome = when (unidade.dimensao) {
                com.bomboniere.app.data.model.Dimensao.MASSA -> "g"
                com.bomboniere.app.data.model.Dimensao.VOLUME -> "ml"
                com.bomboniere.app.data.model.Dimensao.CONTAGEM -> "unidade"
            }
            val posBase = unidades.indexOfFirst { it.nome == baseNome }
            if (posBase >= 0) spinnerOrigem.setSelection(posBase)
            editOrigem.setText(unidade.fatorParaBase.toString())
            editDestino.setText("1")
        }

        val titulo = if (unidade == null) "Nova unidade" else "Editar unidade"

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
                        val qtdOrigem = editOrigem.text.toString().toDoubleOrNull() ?: 0.0
                        val qtdDestino = editDestino.text.toString().toDoubleOrNull() ?: 0.0
                        val origemId = unidades.getOrNull(spinnerOrigem.selectedItemPosition)?.id ?: 0L

                        if (qtdOrigem <= 0.0 || qtdDestino <= 0.0) {
                            Toast.makeText(
                                this@ConversaoActivity,
                                "Valores devem ser maiores que zero",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }

                        lifecycleScope.launch {
                            val result: Result<*> = if (unidade == null) {
                                repo.criarConversao(nome, qtdOrigem, origemId, qtdDestino)
                            } else {
                                // Recalcula o fator com base na origem escolhida
                                val origem = unidades.firstOrNull { it.id == origemId }
                                if (origem == null) {
                                    Toast.makeText(
                                        this@ConversaoActivity,
                                        "Unidade de origem inválida",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@launch
                                }
                                val novoFator = (qtdOrigem / qtdDestino) * origem.fatorParaBase
                                repo.atualizarUnidade(
                                    unidade.copy(
                                        nome = nome,
                                        fatorParaBase = novoFator
                                    )
                                )
                            }

                            result
                                .onSuccess {
                                    Toast.makeText(
                                        this@ConversaoActivity,
                                        if (unidade == null) "Unidade criada" else "Unidade atualizada",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    dialog.dismiss()
                                }
                                .onFailure {
                                    Toast.makeText(
                                        this@ConversaoActivity,
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

    private fun confirmarApagar(unidade: Unidade) {
        AlertDialog.Builder(this)
            .setTitle("Apagar unidade")
            .setMessage("Apagar \"${unidade.nome}\"? Essa ação não pode ser desfeita.")
            .setPositiveButton("Apagar") { _, _ ->
                lifecycleScope.launch {
                    repo.removerUnidade(unidade)
                        .onSuccess {
                            Toast.makeText(
                                this@ConversaoActivity,
                                "Unidade apagada",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .onFailure {
                            Toast.makeText(
                                this@ConversaoActivity,
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