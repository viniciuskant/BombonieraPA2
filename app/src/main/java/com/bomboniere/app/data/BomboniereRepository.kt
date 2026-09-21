package com.bomboniere.app.data

import com.bomboniere.app.data.model.Dimensao
import com.bomboniere.app.data.model.Insumo
import com.bomboniere.app.data.model.Receita
import com.bomboniere.app.data.model.ReceitaItem
import com.bomboniere.app.data.model.TipoItem
import com.bomboniere.app.data.model.Unidade
import com.bomboniere.app.data.dao.*
class BomboniereRepository(private val db: AppDatabase) {

    private val unidadeDao = db.unidadeDao()
    private val insumoDao  = db.insumoDao()
    private val receitaDao = db.receitaDao()
    private val itemDao    = db.receitaItemDao()

    // Flows para observar mudanças em qualquer tabela e recalcular a UI
    val unidadesFlow   = unidadeDao.observar()
    val insumosFlow    = insumoDao.observar()
    val receitasFlow   = receitaDao.observar()
    val itensFlow      = itemDao.observarTodos()

    // ---------- Unidades / Conversões ----------

    suspend fun listarUnidades() = unidadeDao.listar()

    suspend fun atualizarInsumo(insumo: Insumo): Result<Unit> {
        if (insumo.nome.isBlank())        return Result.failure(IllegalArgumentException("Informe o nome"))
        if (insumo.quantidade <= 0.0)     return Result.failure(IllegalArgumentException("Quantidade deve ser > 0"))
        if (insumo.valor < 0.0)           return Result.failure(IllegalArgumentException("Valor inválido"))
        if (unidadeDao.buscarPorId(insumo.unidadeId) == null)
            return Result.failure(IllegalArgumentException("Unidade inválida"))
        insumoDao.atualizar(insumo)
        return Result.success(Unit)
    }

    suspend fun atualizarReceita(receita: Receita): Result<Unit> {
        if (receita.nome.isBlank())
            return Result.failure(IllegalArgumentException("Informe o nome"))
        if (receita.quantidadeProduzida <= 0.0)
            return Result.failure(IllegalArgumentException("Quantidade deve ser > 0"))
        receitaDao.atualizar(receita)
        return Result.success(Unit)
    }

    suspend fun atualizarUnidade(unidade: Unidade): Result<Unit> {
        if (unidade.nome.isBlank())
            return Result.failure(IllegalArgumentException("Informe o nome"))
        if (unidade.fatorParaBase <= 0.0)
            return Result.failure(IllegalArgumentException("Fator deve ser > 0"))

        val existente = unidadeDao.buscarPorNome(unidade.nome)
        if (existente != null && existente.id != unidade.id)
            return Result.failure(IllegalArgumentException("Já existe uma unidade chamada '${unidade.nome}'"))

        unidadeDao.atualizar(unidade)
        return Result.success(Unit)
    }

    suspend fun removerUnidade(unidade: Unidade): Result<Unit> {
        if (unidade.predefinida)
            return Result.failure(IllegalArgumentException("Unidades predefinidas não podem ser apagadas"))

        val emInsumos = insumoDao.contarUsoEmInsumos(unidade.id)
        val emItens = itemDao.contarUsoEmItens(unidade.id)
        if (emInsumos > 0 || emItens > 0) {
            return Result.failure(
                IllegalArgumentException(
                    "Essa unidade está em uso ($emInsumos insumo(s), $emItens item(ns) de receita). " +
                            "Remova ou troque esses antes de apagar."
                )
            )
        }

        unidadeDao.deletar(unidade)
        return Result.success(Unit)
    }

    suspend fun custoItemReceita(item: ReceitaItem): Double {
        val unidade = unidadeDao.buscarPorId(item.unidadeId) ?: return 0.0
        return when (item.tipo) {
            TipoItem.INSUMO -> {
                val insumo = insumoDao.buscarPorId(item.referenciaId) ?: return 0.0
                val custoBase = custoInsumoPorUnidadeBase(insumo)
                val qtdBase = item.quantidade * unidade.fatorParaBase
                qtdBase * custoBase
            }
            TipoItem.RECEITA -> {
                val sub = receitaDao.buscarPorId(item.referenciaId) ?: return 0.0
                val totalSub = custoTotalReceita(sub.id)
                val porUnidade = if (sub.quantidadeProduzida > 0) totalSub / sub.quantidadeProduzida else 0.0
                val qtdUnidades = item.quantidade * unidade.fatorParaBase
                qtdUnidades * porUnidade
            }
        }
    }

    suspend fun criarConversao(
        nomeNovaUnidade: String,
        quantidadeOrigem: Double,
        unidadeOrigemId: Long,
        quantidadeDestino: Double
    ): Result<Unidade> {
        val nome = nomeNovaUnidade.trim()
        if (nome.isBlank())            return Result.failure(IllegalArgumentException("Informe o nome da nova unidade"))
        if (quantidadeOrigem <= 0.0)   return Result.failure(IllegalArgumentException("Valor de origem deve ser > 0"))
        if (quantidadeDestino <= 0.0)  return Result.failure(IllegalArgumentException("Valor de destino deve ser > 0"))

        val origem = unidadeDao.buscarPorId(unidadeOrigemId)
            ?: return Result.failure(IllegalArgumentException("Unidade de origem não encontrada"))

        if (unidadeDao.buscarPorNome(nome) != null)
            return Result.failure(IllegalArgumentException("Já existe uma unidade chamada '$nome'"))

        // 1 nova = (qtdOrigem / qtdDestino) * origem
        val fator = (quantidadeOrigem / quantidadeDestino) * origem.fatorParaBase

        val nova = Unidade(
            nome = nome,
            dimensao = origem.dimensao,
            fatorParaBase = fator,
            predefinida = false
        )
        val id = unidadeDao.inserir(nova)
        return Result.success(nova.copy(id = id))
    }

    // ---------- Insumos ----------

    suspend fun inserirInsumo(nome: String, quantidade: Double, unidadeId: Long, valor: Double): Result<Long> {
        if (nome.isBlank())    return Result.failure(IllegalArgumentException("Informe o nome"))
        if (quantidade <= 0.0) return Result.failure(IllegalArgumentException("Quantidade deve ser > 0"))
        if (valor < 0.0)       return Result.failure(IllegalArgumentException("Valor inválido"))
        if (unidadeDao.buscarPorId(unidadeId) == null)
            return Result.failure(IllegalArgumentException("Unidade inválida"))

        val id = insumoDao.inserir(
            Insumo(
                nome = nome.trim(),
                quantidade = quantidade,
                unidadeId = unidadeId,
                valor = valor
            )
        )
        return Result.success(id)
    }

    suspend fun removerInsumo(insumo: Insumo) = insumoDao.deletar(insumo)

    /** Custo por unidade-base (R$/g, R$/ml, R$/unidade). */
    suspend fun custoInsumoPorUnidadeBase(insumo: Insumo): Double {
        val u = unidadeDao.buscarPorId(insumo.unidadeId) ?: return 0.0
        val qtdBase = insumo.quantidade * u.fatorParaBase
        return if (qtdBase <= 0.0) 0.0 else insumo.valor / qtdBase
    }

    // ---------- Receitas ----------

    suspend fun criarReceita(nome: String, quantidadeProduzida: Double): Result<Long> {
        if (nome.isBlank())               return Result.failure(IllegalArgumentException("Informe o nome"))
        if (quantidadeProduzida <= 0.0)   return Result.failure(IllegalArgumentException("Quantidade produzida deve ser > 0"))
        val id = receitaDao.inserir(
            Receita(
                nome = nome.trim(),
                quantidadeProduzida = quantidadeProduzida
            )
        )
        return Result.success(id)
    }

    suspend fun removerReceita(r: Receita) = receitaDao.deletar(r)

    suspend fun listarItens(receitaId: Long) = itemDao.listarPorReceita(receitaId)

    /**
     * Adiciona um item à receita. Unidade deve ser compatível:
     * - tipo INSUMO  → qualquer unidade
     * - tipo RECEITA → apenas unidades de CONTAGEM (a saída da receita é "unidade")
     */
    suspend fun adicionarItem(
        receitaId: Long,
        tipo: TipoItem,
        referenciaId: Long,
        quantidade: Double,
        unidadeId: Long
    ): Result<Long> {
        if (quantidade <= 0.0) return Result.failure(IllegalArgumentException("Quantidade deve ser > 0"))
        val unidade = unidadeDao.buscarPorId(unidadeId)
            ?: return Result.failure(IllegalArgumentException("Unidade inválida"))

        if (tipo == TipoItem.RECEITA) {
            if (referenciaId == receitaId)
                return Result.failure(IllegalArgumentException("Uma receita não pode conter ela mesma"))
            if (unidade.dimensao != Dimensao.CONTAGEM)
                return Result.failure(IllegalArgumentException("Receitas só podem ser usadas em unidades de contagem (unidade, dúzia, ...)"))
        }

        val id = itemDao.inserir(
            ReceitaItem(
                receitaId = receitaId,
                tipo = tipo,
                referenciaId = referenciaId,
                quantidade = quantidade,
                unidadeId = unidadeId
            )
        )
        return Result.success(id)
    }

    suspend fun removerItem(item: ReceitaItem) = itemDao.deletar(item)

    suspend fun custoTotalReceita(receitaId: Long, caminho: MutableSet<Long> = mutableSetOf()): Double {
        if (!caminho.add(receitaId)) return 0.0   // ciclo detectado
        try {
            val itens = itemDao.listarPorReceita(receitaId)
            var total = 0.0

            for (item in itens) {
                val unidadeItem = unidadeDao.buscarPorId(item.unidadeId) ?: continue

                when (item.tipo) {
                    TipoItem.INSUMO -> {
                        val insumo = insumoDao.buscarPorId(item.referenciaId) ?: continue
                        val custoBase = custoInsumoPorUnidadeBase(insumo)
                        val qtdBase = item.quantidade * unidadeItem.fatorParaBase
                        total += qtdBase * custoBase
                    }
                    TipoItem.RECEITA -> {
                        val sub = receitaDao.buscarPorId(item.referenciaId) ?: continue
                        val custoSubTotal = custoTotalReceita(sub.id, caminho)
                        val custoPorUnidadeSub =
                            if (sub.quantidadeProduzida > 0) custoSubTotal / sub.quantidadeProduzida else 0.0

                        // a unidade da sub-receita é sempre "unidade" (fator 1 na dimensão CONTAGEM)
                        val qtdUnidades = item.quantidade * unidadeItem.fatorParaBase
                        total += qtdUnidades * custoPorUnidadeSub
                    }
                }
            }
            return total
        } finally {
            caminho.remove(receitaId)
        }
    }

    suspend fun custoPorUnidadeReceita(receitaId: Long): Double {
        val r = receitaDao.buscarPorId(receitaId) ?: return 0.0
        val total = custoTotalReceita(receitaId)
        return if (r.quantidadeProduzida > 0) total / r.quantidadeProduzida else 0.0
    }
}