package com.bomboniere.app.data.model
import androidx.room.*

@Entity(tableName = "receitas")
data class Receita(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val quantidadeProduzida: Double = 1.0
)