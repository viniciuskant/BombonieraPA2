package com.bomboniere.app.Lotes

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*

class LoteNotificationManager {

    companion object {
        const val CHANNEL_ID = "lote_verification_channel"
        const val CHANNEL_NAME = "Verificação de Lotes"
        const val NOTIFICATION_ID = 1001
        const val ACTION_DELAY = "com.bomboniere.app.Lotes.DELAY"
        const val ACTION_STOP = "com.bomboniere.app.Lotes.STOP"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificações sobre lotes próximos ao vencimento"
                    enableVibration(true)
                    setSound(null, null)
                }

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun scheduleDailyCheck(context: Context) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, LoteAlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                alarmManager.cancel(pendingIntent)

                val calendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)

                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }

                // Verifica se pode agendar alarmes exatos (Android 12+)
                val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmManager.canScheduleExactAlarms()
                } else {
                    true
                }

                if (canScheduleExact) {
                    // Caminho normal: alarme exato
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                    android.util.Log.d("LoteNotification", "Alarme exato agendado para: ${calendar.time}")
                } else {
                    // Fallback: sem permissão de alarme exato
                    // Usa setAndAllowWhileIdle (não-exato) — o sistema pode atrasar alguns minutos
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                    android.util.Log.w(
                        "LoteNotification",
                        "Sem permissão de alarme exato. Agendado alarme inexato para: ${calendar.time}"
                    )
                }
            } catch (e: SecurityException) {
                // Segurança extra: se mesmo assim falhar, loga e não quebra o app
                android.util.Log.e("LoteNotification", "SecurityException ao agendar: ${e.message}", e)
            } catch (e: Exception) {
                android.util.Log.e("LoteNotification", "Erro ao agendar alarme: ${e.message}", e)
            }
        }

        fun showNotification(context: Context, items: List<Item>) {
            createNotificationChannel(context)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

            val delayIntent = Intent(context, LoteActionReceiver::class.java).apply {
                action = ACTION_DELAY
                putExtra("items_count", items.size)
                putExtra("items", ArrayList(items))
            }

            val stopIntent = Intent(context, LoteActionReceiver::class.java).apply {
                action = ACTION_STOP
                putExtra("items_count", items.size)
                putExtra("items", ArrayList(items))
            }

            val delayPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                delayIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val stopPendingIntent = PendingIntent.getBroadcast(
                context,
                1,  // RequestCode diferente
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val itemNames = items.joinToString("\n") { it.nome }

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Produtos Próximos ao Vencimento")
                .setContentText("${items.size} item(ns) precisa(m) de atenção")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("Itens:\n    $itemNames\n") )
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .addAction(
                    android.R.drawable.ic_menu_rotate,
                    "Adiar 3 dias",
                    delayPendingIntent
                )
                .addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Parar Notificações",
                    stopPendingIntent
                )
                .setAutoCancel(true)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)

            android.util.Log.d("LoteNotification", "Notificação exibida para ${items.size} itens")

        }
    }
}