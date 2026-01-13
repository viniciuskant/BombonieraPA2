package com.bomboniere.app.Lotes

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.util.*

class LoteVerificationService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LoteService", "Serviço de verificação iniciado em ${Date()}")

        Thread {
            try {
                verificarLotesVencendo()
            } catch (e: Exception) {
                Log.e("LoteService", "Erro na verificação: ${e.message}")
            } finally {
                stopSelf()
            }
        }.start()

        return START_NOT_STICKY
    }

    private fun verificarLotesVencendo() {
        val loteManager = Lote(this)
        val todosItens = loteManager.buscarTodosItens()

        if (todosItens.isEmpty()) {
            Log.d("LoteService", "Nenhum item encontrado no banco de dados")
            return
        }

        val hoje = Calendar.getInstance()
        val hojeInt = hoje.get(Calendar.YEAR) * 10000 + (hoje.get(Calendar.MONTH) + 1) * 100 + hoje.get(Calendar.DAY_OF_MONTH)

        Log.d("LoteService", "Data de hoje: $hojeInt, Itens: ${todosItens.size}")

        val itensParaNotificar = mutableListOf<Item>()

        todosItens.forEach { item ->
            Log.d("LoteService", "Item: ${item.nome}, Data: ${item.data}, Notificar: ${item.notificar}")

            if (item.notificar ?: 7 >= 0) {
                val dataNotificacao = calcularDataNotificacao(item.data, item.notificar ?: 7)
                if (dataNotificacao <= hojeInt) {
                    itensParaNotificar.add(item)
                }
            }
        }

        if (itensParaNotificar.isNotEmpty()) {
            Log.d("LoteService", "Mostrando notificação para ${itensParaNotificar.size} itens")
            LoteNotificationManager.showNotification(this, itensParaNotificar)
        }
    }

    private fun calcularDataNotificacao(dataInt: Int, diasNotificar: Int): Int {
        val ano = dataInt / 10000
        val mes = (dataInt % 10000) / 100
        val dia = dataInt % 100

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, ano)
            set(Calendar.MONTH, mes - 1)
            set(Calendar.DAY_OF_MONTH, dia)
            add(Calendar.DAY_OF_YEAR, diasNotificar)
        }

        return calendar.get(Calendar.YEAR) * 10000 +
                (calendar.get(Calendar.MONTH) + 1) * 100 +
                calendar.get(Calendar.DAY_OF_MONTH)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}