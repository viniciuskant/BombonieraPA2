package com.bomboniere.app.data.dao

import com.bomboniere.app.data.model.Receita
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceitaDao {
    @Insert suspend fun inserir(r: Receita): Long
    @Update suspend fun atualizar(r: Receita)
    @Delete suspend fun deletar(r: Receita)

    @Query("SELECT * FROM receitas ORDER BY nome") fun observar(): Flow<List<Receita>>
    @Query("SELECT * FROM receitas WHERE id = :id") suspend fun buscarPorId(id: Long): Receita?
}