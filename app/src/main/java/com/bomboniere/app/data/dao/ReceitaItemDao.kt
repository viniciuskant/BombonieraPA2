package com.bomboniere.app.data.dao

import androidx.room.*
import com.bomboniere.app.data.model.ReceitaItem
import kotlinx.coroutines.flow.Flow
@Dao
interface ReceitaItemDao {
    @Insert suspend fun inserir(i: ReceitaItem): Long
    @Delete suspend fun deletar(i: ReceitaItem)

    @Query("SELECT * FROM receita_itens WHERE receitaId = :receitaId")
    suspend fun listarPorReceita(receitaId: Long): List<ReceitaItem>

    @Query("SELECT * FROM receita_itens WHERE receitaId = :receitaId")
    fun observarPorReceita(receitaId: Long): Flow<List<ReceitaItem>>

    @Query("SELECT * FROM receita_itens")
    fun observarTodos(): Flow<List<ReceitaItem>>

    @Query("SELECT COUNT(*) FROM receita_itens WHERE unidadeId = :unidadeId")
    suspend fun contarUsoEmItens(unidadeId: Long): Int
}