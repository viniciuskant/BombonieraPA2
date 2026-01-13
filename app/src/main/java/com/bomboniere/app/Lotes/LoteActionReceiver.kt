package com.bomboniere.app.Lotes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import java.util.Calendar

class LoteActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val items = intent.getSerializableExtra("items") as? List<Item> ?: return
        val hoje = Calendar.getInstance()
        val hojeInt = hoje.get(Calendar.YEAR) * 10000 + (hoje.get(Calendar.MONTH) + 1) * 100 + hoje.get(Calendar.DAY_OF_MONTH)

        when (intent.action) {
            LoteNotificationManager.ACTION_DELAY -> {
                items.forEach { item ->
                    val newNotificationDays = hojeInt - item.data  + 3
                    updateItemNotificationDays(context, item, newNotificationDays)
                }
                Toast.makeText(context, "Notificações adiadas por 3 dias", Toast.LENGTH_SHORT).show()
            }

            LoteNotificationManager.ACTION_STOP -> {
                items.forEach { item ->
                    val newNotificationDays = hojeInt - item.data  - 1
                    updateItemNotificationDays(context, item, newNotificationDays)
                }
                Toast.makeText(context, "Notificações paradas para esses itens", Toast.LENGTH_SHORT).show()
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as android.app.NotificationManager
        notificationManager.cancel(LoteNotificationManager.NOTIFICATION_ID)
    }

    private fun updateItemNotificationDays(context: Context, item: Item, newDays: Int) {
        val dbHelper = LoteDatabaseHelper(context)
        val db = dbHelper.writableDatabase

        val values = android.content.ContentValues().apply {
            put("notificar", newDays)
        }
        val whereClause = "nome_produto = ? AND lote = ?"
        val whereArgs = arrayOf(item.nome, item.lote.toString())

        db.update("Lotes", values, whereClause, whereArgs)
    }
}