package com.bomboniere.app.data.model
import androidx.room.*

// Insumo.kt
@Entity(tableName = "insumos")
data class Insumo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val quantidade: Double,      // ex.: 5
    val unidadeId: Long,          // ex.: kg
    val valor: Double             // valor TOTAL pago por essa quantidade (R$)
)