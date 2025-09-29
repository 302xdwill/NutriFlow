package com.example.nutriflow.data.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.app.NotificationManager
import androidx.core.app.NotificationCompat

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val mealName = inputData.getString("MEAL_NAME") ?: "Comida"
        // Lógica real para mostrar la notificación
        showNotification(mealName)
        return Result.success()
    }

    private fun showNotification(mealName: String) {
        // Usar NotificationManager para mostrar el recordatorio
        // ... (código para construir el canal y la notificación)
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(applicationContext, "MEAL_CHANNEL_ID")
            .setContentTitle("Recordatorio de Comida")
            .setContentText("¡Es hora de comer tu $mealName!")
            .setSmallIcon(com.example.nutriflow.R.drawable.ic_notification) // Asegúrate de tener este icono
            .setAutoCancel(true)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}