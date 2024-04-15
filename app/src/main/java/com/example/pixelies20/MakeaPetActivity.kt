package com.example.pixelies20

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.GridLayout
import android.widget.Toast
import android.util.Log



class MakeaPetActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var petNameEditText: EditText
    private lateinit var calorieGoalEditText: EditText
    private lateinit var waterGoalEditText: EditText
    private var activityLevel: String? = null
    private var selectedActivityButton: Button? = null
    private var selectedPetImageView: ImageView? = null
    private var isDemoMode = false
    private lateinit var characterSelectionGrid: GridLayout
    private var selectedPet: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.makeapet)

        sharedPreferences = getSharedPreferences("pixeliesPrefs", MODE_PRIVATE)
        selectedPet = intent.getStringExtra("selectedPet") ?: sharedPreferences.getString("selectedPet", "character1")

        calorieGoalEditText = findViewById(R.id.calorieGoalEditText)
        waterGoalEditText = findViewById(R.id.waterGoalEditText)
        characterSelectionGrid = findViewById(R.id.characterSelectionGrid)
        petNameEditText = findViewById(R.id.petNameEditText)
        isDemoMode = intent.getBooleanExtra("isDemoMode", false)

        val saveButton: Button = findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            if (validateInputs()) {
                savePreferences()
                startPlayroomActivity()
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
        val adjustGoalsOnly = intent.getBooleanExtra("adjustGoalsOnly", false)
        if (adjustGoalsOnly) {
            // Disable pet name and character selection

            petNameEditText.isEnabled = false
            characterSelectionGrid.visibility = View.GONE
           // val selectedPet = intent.getStringExtra("selectedPet") ?: "character1"

            saveButton.setOnClickListener {
                if (calorieGoalEditText.text.isNotBlank()) {
                    sharedPreferences.edit()
                        .putInt("calorieGoal", calorieGoalEditText.text.toString().toInt()).apply()
                }
                if (waterGoalEditText.text.isNotBlank()) {
                    sharedPreferences.edit()
                        .putInt("waterGoal", waterGoalEditText.text.toString().toInt()).apply()
                }
                if (activityLevel != null) {
                    sharedPreferences.edit()
                        .putString("activityLevel", activityLevel).apply()
                }
                val currentSelectedPet = sharedPreferences.getString("selectedPet", "character1")
                sharedPreferences.edit().putString("selectedPet", currentSelectedPet).apply()
                Log.d("MakeaPetActivity", "Saving adjusted goals, selected pet: $currentSelectedPet")

                val intent = Intent(this, SettingsActivity::class.java).apply{
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("selectedPet", selectedPet ?: "character1") // Pass selected pet back to SettingsActivity, pls don't accidentally delete. This was a 14 hr nightmare.
                }

                startActivity(intent)
            }
        }else {
            saveButton.setOnClickListener {
                savePreferences()
                startPlayroomActivity()
            }
        }


        initCharacterSelection()
    }

    private fun savePreferences() {
        val editor = sharedPreferences.edit()
        editor.putInt("calorieGoal", calorieGoalEditText.text.toString().toInt())
        editor.putInt("waterGoal", waterGoalEditText.text.toString().toInt())
        if (activityLevel != null) {
            editor.putString("activityLevel", activityLevel)
           // Log.d("MakeaPetActivity", "Saving activity level: $activityLevel")

        }
        val petName = petNameEditText.text.toString()
        if (petName.isNotBlank()) {
            editor.putString("petName", petName)
        }
        editor.putBoolean("firstLaunch", false)
        editor.apply()
    }

    private fun startPlayroomActivity() {
        val intent = Intent(this, PlayRoomActivity::class.java).apply{
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        intent.putExtra("isDemoMode", isDemoMode) // Pass the isDemoMode flag to PlayRoomActivity- I think this broke somehow?
        startActivity(intent)
        finish()
    }

    private fun initCharacterSelection() {
        val character1ImageView: ImageView = findViewById(R.id.character1ImageView)
        val character2ImageView: ImageView = findViewById(R.id.character2ImageView)
        val character3ImageView: ImageView = findViewById(R.id.character3ImageView)

        character1ImageView.setOnClickListener {
            selectCharacter("character1", character1ImageView)
        }

        character2ImageView.setOnClickListener {
            selectCharacter("character2", character2ImageView)
        }

        character3ImageView.setOnClickListener {
            selectCharacter("character3", character3ImageView)
        }

        when (selectedPet) {
            "character1" -> selectCharacter("character1", character1ImageView)
            "character2" -> selectCharacter("character2", character2ImageView)
            "character3" -> selectCharacter("character3", character3ImageView)
        }
    }
    private fun validateInputs(): Boolean {
        return petNameEditText.text.isNotBlank() &&
                calorieGoalEditText.text.isNotBlank() &&
                waterGoalEditText.text.isNotBlank() &&
                activityLevel != null &&
                selectedPetImageView != null
    }
    private fun selectCharacter(characterTag: String, imageView: ImageView) {
        selectedPetImageView?.background = null

        imageView.setBackgroundResource(R.drawable.selected_border)
        selectedPetImageView = imageView

        val editor = sharedPreferences.edit()
        editor.putString("selectedPet", characterTag)
        editor.apply()
        Log.d("MakeaPetActivity", "Selected pet: $characterTag")

    }

    fun onActivityLevelSelected(view: View) {
        selectedActivityButton?.setBackgroundResource(android.R.color.transparent)

        view.setBackgroundResource(R.drawable.selected_border)
        selectedActivityButton = view as Button

        activityLevel = when (view.id) {
            R.id.easyButton -> "Easy"
            R.id.mediumButton -> "Medium"
            R.id.hardButton -> "Hard"
            else -> null
        }
    }
}
