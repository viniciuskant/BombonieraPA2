package com.bomboniere.app.data

import android.content.Context
import androidx.room.Database
import com.bomboniere.app.data.dao.*
import com.bomboniere.app.data.model.*
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
@Database(
    entities = [Unidade::class, Insumo::class, Receita::class, ReceitaItem::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun unidadeDao(): UnidadeDao
    abstract fun insumoDao(): InsumoDao
    abstract fun receitaDao(): ReceitaDao
    abstract fun receitaItemDao(): ReceitaItemDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bomboniere.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Unidades pré-definidas (fator para a base da dimensão)
                            db.execSQL("INSERT INTO unidades (nome, dimensao, fatorParaBase, predefinida) VALUES ('g','MASSA',1.0,1)")
                            db.execSQL("INSERT INTO unidades (nome, dimensao, fatorParaBase, predefinida) VALUES ('mg','MASSA',0.001,1)")
                            db.execSQL("INSERT INTO unidades (nome, dimensao, fatorParaBase, predefinida) VALUES ('kg','MASSA',1000.0,1)")
                            db.execSQL("INSERT INTO unidades (nome, dimensao, fatorParaBase, predefinida) VALUES ('ml','VOLUME',1.0,1)")
                            db.execSQL("INSERT INTO unidades (nome, dimensao, fatorParaBase, predefinida) VALUES ('l','VOLUME',1000.0,1)")
                            db.execSQL("INSERT INTO unidades (nome, dimensao, fatorParaBase, predefinida) VALUES ('unidade','CONTAGEM',1.0,1)")
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
    }
}