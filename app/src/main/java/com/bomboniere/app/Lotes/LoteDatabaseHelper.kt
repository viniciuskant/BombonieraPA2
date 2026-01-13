package com.bomboniere.app.Lotes

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LoteDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "lotes.db"
        const val DATABASE_VERSION = 2

        const val TABLE_LOTES = "Lotes"
        const val COLUMN_ID = "_id"
        const val COLUMN_NOME = "nome_produto"
        const val COLUMN_QUANTIDADE = "quantidade"
        const val COLUMN_LOTE = "lote"
        const val COLUMN_DATA = "data"
        const val COLUMN_NOTIFICAR = "notificar"

        private const val SQL_CREATE_TABLE = """
            CREATE TABLE $TABLE_LOTES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOME TEXT NOT NULL,
                $COLUMN_QUANTIDADE INTEGER,
                $COLUMN_LOTE INTEGER,
                $COLUMN_DATA INTEGER,
                $COLUMN_NOTIFICAR INTEGER DEFAULT 7
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_LOTES ADD COLUMN $COLUMN_NOTIFICAR INTEGER DEFAULT 7")
        }
    }
}