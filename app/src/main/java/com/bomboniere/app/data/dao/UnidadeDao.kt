package com.bomboniere.app.data.dao

import com.bomboniere.app.data.model.Unidade
import com.bomboniere.app.data.model.Dimensao
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface UnidadeDao {
    @Insert suspend fun inserir(u: Unidade): Long
    @Update suspend fun atualizar(u: Unidade)
    @Delete suspend fun deletar(u: Unidade)

    @Query("SELECT * FROM unidades ORDER BY nome") suspend fun listar(): List<Unidade>
    @Query("SELECT * FROM unidades ORDER BY nome") fun observar(): Flow<List<Unidade>>
    @Query("SELECT * FROM unidades WHERE id = :id") suspend fun buscarPorId(id: Long): Unidade?
    @Query("SELECT * FROM unidades WHERE nome = :nome LIMIT 1") suspend fun buscarPorNome(nome: String): Unidade?
    @Query("SELECT * FROM unidades WHERE dimensao = :d ORDER BY nome") suspend fun listarPorDimensao(d: Dimensao): List<Unidade>
}