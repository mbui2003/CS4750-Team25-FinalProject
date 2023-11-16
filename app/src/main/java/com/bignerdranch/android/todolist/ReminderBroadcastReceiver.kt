package com.bignerdranch.android.todolist

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskRepository = TaskRepository.get()
        CoroutineScope(Dispatchers.IO).launch {
            val currentDate = System.currentTimeMillis()
            val tasks = taskRepository.getTasksForNotification(currentDate)
            tasks.forEach { task ->
                if (!task.isComplete) {
                    createAndShowNotification(context, task)
                }
            }
        }
    }

    private fun createAndShowNotification(context: Context, task: Task) {
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_TASK_ID", task.id.toString())
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, task.id.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, "YOUR_CHANNEL_ID")
            .setContentTitle(task.title)
            .setContentText("Due: ${formatDate(task.date)}")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(task.hashCode(), notification)
    }

    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        return formatter.format(date)
    }
}

