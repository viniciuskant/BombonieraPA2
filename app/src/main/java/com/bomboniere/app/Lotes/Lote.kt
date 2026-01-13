package com.bomboniere.app.Lotes

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
        val currentDate = Calendar.getInstance()

        for (i in 0..365 * 10) {
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

        val values = ContentValues().apply {
            put("nome_produto", item.nome)
            put("quantidade", item.quantidade)
            put("lote", item.lote)
            put("data", item.data)
            put("notificar", item.notificar)
        }

        db.insert("Lotes", null, values)
    }

    fun armazenarTodosLotes(itens: List<Item>) {
        val db = dbHelper.writableDatabase

        db.beginTransaction()
        try {
            for (item in itens) {
                armazenarLote(item)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun buscarLotes(): List<Int> {
        val db = dbHelper.readableDatabase
        val cursor = db.query("Lotes", null, null, null, null, null, null)
        val lotes = mutableListOf<Int>()
        while (cursor.moveToNext()) {
            val lote = cursor.getColumnIndex("lote")
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
            val lote = cursor.getInt(cursor.getColumnIndex("lote"))
            val data = cursor.getInt(cursor.getColumnIndex("data"))
            val notificar = cursor.getInt(cursor.getColumnIndex("notificar"))

            itens.add(Item(nome, quantidade, data, lote, notificar))
        }
        cursor.close()
        return itens
    }

    fun updateNotificao(nome: String, novaNotificacao: Int): Boolean {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(LoteDatabaseHelper.COLUMN_NOTIFICAR, novaNotificacao)}

        val rowsAffected = db.update(LoteDatabaseHelper.TABLE_LOTES, values,
            "${LoteDatabaseHelper.COLUMN_NOME} = ?", arrayOf(nome))

        return rowsAffected > 0
    }

}