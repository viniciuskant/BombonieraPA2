package com.bomboniere.app

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import java.time.ZoneId

class Lote(context: Context) {

    private val dbHelper = LoteDatabaseHelper(context)

    fun calcularHashEUltimos6Digitos(data: Date): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val dataString = sdf.format(data)

        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(dataString.toByteArray())

        val decimalString = hash.joinToString("") { "%d".format(it.toInt() and 0xFF) }

        return decimalString.takeLast(6)
    }

    fun calcularDataDoLote(lote: String): Date? {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val currentDate = Calendar.getInstance()

        for (i in 0..365 * 10) { // tentativa por 10 anos
            currentDate.add(Calendar.DATE, -1)
            val data = currentDate.time
            val hashGerado = calcularHashEUltimos6Digitos(data)
            if (hashGerado == lote) {
                return data
            }
        }
        return null
    }

    fun armazenarLote(item: Item) {
        val db = dbHelper.writableDatabase
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        // Converter LocalDate para Date
        val date = Date.from(item.data.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val dataString = sdf.format(date)

        val values = ContentValues().apply {
            put("nome_produto", item.nome)
            put("quantidade", item.quantidade)
            put("lote", item.lote)
            put("data", dataString)
            put("notificar", if (item.notificar) 1 else 0)
        }

        db.insert("Lotes", null, values)
    }

    fun armazenarTodosLotes(itens: List<Item>) {
        val db = dbHelper.writableDatabase
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

        db.beginTransaction()
        try {
            for (item in itens) {
                val date = Date.from(item.data.atStartOfDay(ZoneId.systemDefault()).toInstant())
                val dataString = sdf.format(date)

                val values = ContentValues().apply {
                    put("nome_produto", item.nome)
                    put("quantidade", item.quantidade)
                    put("lote", item.lote)
                    put("data", dataString)
                    put("notificar", if (item.notificar) 1 else 0)
                }

                db.insert("Lotes", null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun buscarLotes(): List<String> {
        val db = dbHelper.readableDatabase
        val cursor = db.query("Lotes", null, null, null, null, null, null)
        val lotes = mutableListOf<String>()
        while (cursor.moveToNext()) {
            val lote = cursor.getString(cursor.getColumnIndex("lote"))
            lotes.add(lote)
        }
        cursor.close()
        return lotes
    }

    fun buscarTodosItens(): List<Item> {
        val db = dbHelper.readableDatabase
        val cursor = db.query("Lotes", null, null, null, null, null, null)
        val itens = mutableListOf<Item>()

        while (cursor.moveToNext()) {
            val nome = cursor.getString(cursor.getColumnIndex("nome_produto"))
            val quantidade = cursor.getInt(cursor.getColumnIndex("quantidade"))
            val lote = cursor.getString(cursor.getColumnIndex("lote"))
            val dataString = cursor.getString(cursor.getColumnIndex("data"))
            val notificar = cursor.getInt(cursor.getColumnIndex("notificar")) == 1

            val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val date = sdf.parse(dataString)
            val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

            itens.add(Item(nome, quantidade, localDate, lote, notificar))
        }
        cursor.close()
        return itens
    }

    fun limparTabelaLotes() {
        val db = dbHelper.writableDatabase
        db.delete("Lotes", null, null)
    }

    public class LoteDatabaseHelper(context: Context) :
        SQLiteOpenHelper(context, "LoteDB", null, 1) {

        override fun onCreate(db: SQLiteDatabase?) {
            db?.execSQL(
                """
                CREATE TABLE Lotes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome_produto TEXT,
                    quantidade INTEGER,
                    lote TEXT,
                    data TEXT,
                    notificar INTEGER
                );
                """.trimIndent()
            )
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db?.execSQL("DROP TABLE IF EXISTS Lotes")
            onCreate(db)
        }
    }
}