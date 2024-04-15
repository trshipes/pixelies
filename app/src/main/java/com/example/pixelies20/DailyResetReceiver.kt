package com.example.pixelies20

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DailyResetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // do reset stuff here
        val prefs = context.getSharedPreferences("pixeliesPrefs", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putInt("caloriesConsumedToday", 0)
            putInt("waterConsumedToday", 0)
            putInt("caloriesBurnedToday", 0)
            apply()
        }
        Log.d("DailyResetReceiver", "Daily stats have been reset.")
    }
}
