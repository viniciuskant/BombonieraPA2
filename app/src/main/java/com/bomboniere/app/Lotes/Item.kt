package com.bomboniere.app.Lotes

import java.io.Serializable

data class Item(
    val nome: String,
    val quantidade: Int?,
    val data: Int,
    val lote: Int,
    val notificar: Int
) :  Serializable