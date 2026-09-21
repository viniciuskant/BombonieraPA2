package com.bomboniere.app.data

// Unidade.kt
@Entity(
    tableName = "unidades",
    indices = [Index(value = ["nome"], unique = true)]
)
data class Unidade(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val dimensao: Dimensao,
    val fatorParaBase: Double,
    val predefinida: Boolean = false
)