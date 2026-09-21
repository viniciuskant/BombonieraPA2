// Dimensao.kt
package com.bomboniere.app.data.model

enum class Dimensao { MASSA, VOLUME, CONTAGEM }

enum class TipoItem { INSUMO, RECEITA }

object UnidadeBase {
    const val MASSA = "g"
    const val VOLUME = "ml"
    const val CONTAGEM = "unidade"
}