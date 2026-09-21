package com.bomboniere.app.data.model
import androidx.room.*

@Entity(
    tableName = "receita_itens",
    foreignKeys = [
        ForeignKey(
            entity = Receita::class,
            parentColumns = ["id"],
            childColumns = ["receitaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("receitaId")]
)
data class ReceitaItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receitaId: Long,
    val tipo: TipoItem,
    val referenciaId: Long,     // id do Insumo OU da Receita
    val quantidade: Double,
    val unidadeId: Long         // unidade na qual a quantidade está expressa
)