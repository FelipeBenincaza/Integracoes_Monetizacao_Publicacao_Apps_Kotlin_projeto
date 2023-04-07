package com.benincaza.projetointegracoeskotlin.services

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.benincaza.projetointegracoeskotlin.R

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            val titulo = intent?.getStringExtra("title") ?: ""
            val mensagem = intent?.getStringExtra("message") ?: ""

            val notification: NotificationCompat.Builder = NotificationCompat.Builder(context, "INFNET")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(titulo)
                .setContentText(mensagem)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(0, notification.build())
        }
    }
}