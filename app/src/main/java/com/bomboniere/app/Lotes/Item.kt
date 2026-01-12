package com.bomboniere.app.Lotes

import java.time.LocalDate

data class Item(
    val nome: String,
    val quantidade: Int?,
    val data: LocalDate,
    val lote: String,
    val notificar: Boolean = true
)