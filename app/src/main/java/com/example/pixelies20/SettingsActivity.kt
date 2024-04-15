package com.example.pixelies20

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Context
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import android.util.Log
import android.hardware.Sensor
import android.hardware.SensorManager

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val selectedPet = intent.getStringExtra("selectedPet") ?: "character1"
        Log.d("SettingsActivity", "Selected pet: $selectedPet")

        val homeButton: Button = findViewById(R.id.homeButton)
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply{
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            intent.putExtra("selectedPet", selectedPet)
            startActivity(intent)
        }

        val adjustGoalsButton: Button = findViewById(R.id.adjustGoalsButton)
        adjustGoalsButton.setOnClickListener {
            val selectedPet = intent.getStringExtra("selectedPet") ?: "character1"
            val intent = Intent(this, MakeaPetActivity::class.java).apply{
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            intent.putExtra("adjustGoalsOnly", true)
            intent.putExtra("selectedPet", selectedPet)
            startActivity(intent)
        }

        val soundToggle: SwitchCompat = findViewById(R.id.soundToggle)
        val prefs = getSharedPreferences("AppSettingsPrefs", Context.MODE_PRIVATE)
        soundToggle.isChecked = prefs.getBoolean("SoundEnabled", true)

        soundToggle.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("SoundEnabled", isChecked).apply()

            if (!isChecked) {
                Toast.makeText(this, "Sound disabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Sound enabled", Toast.LENGTH_SHORT).show()
            }
        }

        // step sensor toggle setup
        val sensorToggle: SwitchCompat = findViewById(R.id.sensorToggle)
        sensorToggle.isChecked = getSharedPreferences("pixeliesPrefs", Context.MODE_PRIVATE).getBoolean("sensorEnabled", true)
        sensorToggle.setOnCheckedChangeListener { _, isChecked ->
            val prefs = getSharedPreferences("pixeliesPrefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("sensorEnabled", isChecked).apply()

            if (!isChecked) {
                Toast.makeText(this, "Step sensor disabled to preserve battery.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Step sensor enabled.", Toast.LENGTH_SHORT).show()
            }
        }


        val resetAllButton: Button = findViewById(R.id.resetAllButton)
        resetAllButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reset All")
                .setMessage("Are you sure you want to wipe everything and start over?")
                .setPositiveButton("Yes") { dialog, _ ->
                    // Clear all preferences
                    val prefs = getSharedPreferences("pixeliesPrefs", Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()

                    // Go back to MainActivity as a first launch
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("firstLaunch", true)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }


        val userGuideButton: Button = findViewById(R.id.userGuideButton)
        userGuideButton.setOnClickListener {
            // Placeholder for user guide
            Toast.makeText(this, "User guide coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
}
