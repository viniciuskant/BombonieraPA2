package com.bomboniere.app.data.dao

import kotlinx.coroutines.flow.Flow
import com.bomboniere.app.data.model.Insumo
import androidx.room.*
@Dao
interface InsumoDao {
    @Insert suspend fun inserir(i: Insumo): Long
    @Update suspend fun atualizar(i: Insumo)
    @Delete suspend fun deletar(i: Insumo)

    @Query("SELECT * FROM insumos ORDER BY nome") fun observar(): Flow<List<Insumo>>
    @Query("SELECT * FROM insumos WHERE id = :id") suspend fun buscarPorId(id: Long): Insumo?

    @Query("SELECT COUNT(*) FROM insumos WHERE unidadeId = :unidadeId")
    suspend fun contarUsoEmInsumos(unidadeId: Long): Int
}
