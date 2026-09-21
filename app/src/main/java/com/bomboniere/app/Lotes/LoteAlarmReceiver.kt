package com.bomboniere.app.Lotes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*

class LoteAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("LoteAlarm", "Alarme disparado em ${Date()}")

        val serviceIntent = Intent(context, LoteVerificationService::class.java)
        context.startService(serviceIntent)

        Log.d("LoteAlarm", "Serviço iniciado")
    }
}