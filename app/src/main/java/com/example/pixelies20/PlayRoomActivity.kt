package com.example.pixelies20

import com.example.pixelies20.Quest
import com.example.pixelies20.Reward
import com.example.pixelies20.QuestRepository
import com.google.gson.Gson
import android.animation.ObjectAnimator

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.SensorEventListener
import android.hardware.SensorEvent
import android.text.InputType
import android.util.Log
import java.util.Locale
import java.text.SimpleDateFormat
import android.os.Looper
import android.os.Handler
import android.widget.Toast
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min
import android.view.View
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.media.MediaPlayer






class PlayRoomActivity : AppCompatActivity(), SensorEventListener, QuestManagerListener {

    companion object {
        private const val REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 1
    }
    private lateinit var viewQuestButton: Button

    private var hungerLevel = 6
    private var thirstLevel = 5
    var moodLevel = 5
    private var hungerZeroTimestamp: Long = 0
    private var thirstZeroTimestamp: Long = 0
    private var moodZeroTimestamp: Long = 0
    private var lastMoodBoostTime: Long = 0
    private var tapCount = 0
    private var calorieGoal = 0
    private var waterGoal = 0
    private var caloriesConsumedToday = 0
    var caloriesBurnedToday = 0
    private var caloriesBurnedFromSteps = 0

    private var waterConsumedToday = 0
    var currentQuest: Quest? = null

    private var isPetHandled = false
    private var skippedDaysCountMood = 0
    private var skippedDaysCountThirst = 0
    private var skippedDaysCountHunger= 0

    private lateinit var totalCaloriesConsumedTextView: TextView
    private lateinit var caloriesBurnedTextView: TextView

    private lateinit var caloriesConsumedTextView: TextView
    private lateinit var waterConsumedTextView: TextView
    private lateinit var petImageView: ImageView
    private lateinit var feedButton: Button
    private lateinit var drinkButton: Button
    private lateinit var playButton: Button
    private lateinit var homeButton: Button
    private lateinit var thirstBar: ImageView
    private lateinit var hungerBar: ImageView
    private lateinit var moodBar: ImageView
    private lateinit var demoModeLabel: TextView
    private lateinit var demoNextDayButton: Button
    private lateinit var demoClockTextView: TextView
    private var shouldShowMoodWarning = false
    private var demoTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 5)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }
    private val DEMO_DAY_DURATION = 5 * 60 * 1000L // 5 minutes in milliseconds
    private var previousMoodLevel = 5
    private var questAcceptedDialog: AlertDialog? = null
    private var blinkRunnable: Runnable? = null
    private var handler: Handler? = null

    private val LAST_RESET_DATE_KEY = "lastResetDate"
    //private var isFirstLaunch = true

    private var selectedPet: String? = null

    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("pixeliesPrefs", MODE_PRIVATE)
    }
    private var isDemoMode = false
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var totalSteps = 0f
    private var initialSteps = 0f
    //private var caloriesBurnedFromSteps = 0

    private lateinit var questManager: QuestManager
    private lateinit var feedSound: MediaPlayer
    private lateinit var drinkSound: MediaPlayer
    private lateinit var playSound: MediaPlayer
    private lateinit var headPatSound: MediaPlayer
    private lateinit var questAcceptedSound: MediaPlayer
    private lateinit var questCompletedSound: MediaPlayer
    private lateinit var questFailedSound: MediaPlayer
    private lateinit var questQuitSound: MediaPlayer
    private lateinit var stepSound: MediaPlayer

    private var backgroundMusic: MediaPlayer? = null
    private var lastPlayedTrack: Int? = null
    private val musicTracks = arrayOf(R.raw.track1, R.raw.track4, R.raw.track5, R.raw.track6, R.raw.track7)
    private var musicdialog = false
    private var currentRunFrame = 0
    private var runningWithSword = false
    private var runningAnimationFrames = arrayOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playroom)
        handler = Handler(Looper.getMainLooper())

        questManager = QuestManager(this, QuestRepository.quests)
        logAllPreferences()
        //  Log.d("PlayRoomActivity", "PlayRoomActivity started")
        // Log.d("PlayRoomActivity", "Selected pet: $selectedPet")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permission = Manifest.permission.ACTIVITY_RECOGNITION
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_ACTIVITY_RECOGNITION_PERMISSION)
            } else {
                initializeSensor()
            }
        } else {
            initializeSensor()
        }
        //initializeUI()
        initializeSounds()

        petImageView = findViewById(R.id.petImageView)
        feedButton = findViewById(R.id.feedButton)
        homeButton = findViewById(R.id.homeButton)
        drinkButton = findViewById(R.id.drinkButton)
        playButton = findViewById(R.id.playButton)
        thirstBar = findViewById(R.id.thirstBar)
        hungerBar = findViewById(R.id.hungerBar)
        moodBar = findViewById(R.id.moodBar)
        caloriesConsumedTextView = findViewById(R.id.caloriesConsumedTextView)
        waterConsumedTextView = findViewById(R.id.waterConsumedTextView)
        totalCaloriesConsumedTextView = findViewById(R.id.totalCaloriesConsumedTextView)
        caloriesBurnedTextView = findViewById(R.id.caloriesBurnedTextView)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // sharedPreferences = getSharedPreferences("pixeliesPrefs", MODE_PRIVATE)
        selectedPet = intent.getStringExtra("selectedPet") ?: sharedPreferences.getString("selectedPet", "character1")

        isDemoMode = intent.getBooleanExtra("isDemoMode", false)
        demoModeLabel = findViewById(R.id.demoModeLabel)
        demoNextDayButton = findViewById(R.id.demoNextDayButton)
        demoClockTextView = findViewById(R.id.demoClockTextView)
        decreaseMoodOverTime()


        if (isDemoMode) {
            demoModeLabel.visibility = View.VISIBLE
            demoNextDayButton.visibility = View.VISIBLE
            demoClockTextView.visibility = View.VISIBLE
            startFakeClock()

        }
        demoNextDayButton.setOnClickListener {
            // Log.d("PlayRoomActivity", "Demo mode: $isDemoMode")
            simulateNextDay()
        }

        feedButton.setOnClickListener {
            showInputDialog("Feed", "Enter calories consumed")
            skippedDaysCountHunger = 0

        }

        drinkButton.setOnClickListener {
            showInputDialog("Drink", "Enter cups of water")
            skippedDaysCountThirst = 0
        }

        playButton.setOnClickListener {
            if (isSoundEnabled()) {
                playSound.start()
            }
            AlertDialog.Builder(this)
                .setTitle("Choose an option")
                .setMessage("Would you like to take on a quest or would you like to input burned calories?")
                .setPositiveButton("Input Activity") { dialog, _ ->
                    skippedDaysCountMood = 0
                    showInputDialog("Play", "Enter calories burned")
                    dialog.dismiss()
                }
                .setNegativeButton("Let's go on a quest!") { dialog, _ ->
                    if (isSoundEnabled()) {
                        backgroundMusic?.start()
                    }
                    skippedDaysCountMood = 0
                    currentQuest?.let { quest ->
                        AlertDialog.Builder(this)
                            .setTitle("Quest Active")
                            .setMessage("You currently have an active quest. Would you like to continue or start a new quest?")
                            .setPositiveButton("Continue Quest") { dialog, _ ->
                                showQuestAcceptedDialog(quest)
                                dialog.dismiss()
                            }
                            .setNegativeButton("Start New Quest") { dialog, _ ->
                                showQuestDialog()

                                dialog.dismiss()
                            }
                            .show()
                    } ?: showQuestDialog()
                    dialog.dismiss()
                }
                .show()
        }
        viewQuestButton = findViewById(R.id.viewQuestButton)
        viewQuestButton.setOnClickListener {
            currentQuest?.let { quest ->
                showQuestAcceptedDialog(quest)
            } ?: Toast.makeText(this, "No active quest!", Toast.LENGTH_SHORT).show()
            if (currentQuest == null) {
                AlertDialog.Builder(this)
                    .setTitle("No Quest")
                    .setMessage("No active quest is selected. Would you like to start one?")
                    .setPositiveButton("Yes") { dialog, which ->
                        showQuestDialog()
                    }
                    .setNegativeButton("No") { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                showQuestAcceptedDialog(currentQuest!!)
            }
        }
        homeButton.setOnClickListener {
            saveStats()
            val currentSelectedPet = sharedPreferences.getString("selectedPet", "character1")
            sharedPreferences.edit().putString("selectedPet", currentSelectedPet ?: "character1").apply()
            val intent = Intent(this, MainActivity::class.java)
            backgroundMusic?.stop()

            startActivity(intent)
        }

        loadPetImage()
        loadStats()

        updateHealthBars()
        setupDailyResetAlarm()
        // checkDeathConditions()
        if (sharedPreferences.getBoolean("firstLaunch", true)) {
            showBeginnerDialog()
            sharedPreferences.edit().putBoolean("firstLaunch", false).apply()
        }
        //  val moodDecreaseInterval = 60 * 60 * 1000 // 1 hr
        //    moodBar.postDelayed({ decreaseMoodOverTime() }, moodDecreaseInterval.toLong())
        petImageView.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            Log.d("PlayRoomActivity", "Current time: $currentTime, Last mood boost time: $lastMoodBoostTime")
            val timeSinceLastBoost = if (isDemoMode) 15 * 1000 else 60 * 60 * 1000 // 1 minute in demo mode, 1 hour otherwise

            if (currentTime - lastMoodBoostTime > timeSinceLastBoost) { // More than 1 hour (or 1 minute in demo mode) has passed since the last mood boost
                tapCount++
                Log.d("PlayRoomActivity", "Tap count: $tapCount")

                if (tapCount >= 5) {
                    moodLevel = min(moodLevel + 1, 5)
                    Log.d("PlayRoomActivity", "Mood level increased to: $moodLevel")

                    updateHealthBars()
                    if (isSoundEnabled()) {
                        headPatSound.start()
                    }
                    lastMoodBoostTime = currentTime
                    tapCount = 0
                    val petName = sharedPreferences.getString("petName", "your pet")
                    AlertDialog.Builder(this)
                        .setMessage("$petName thanks you for the head rubs and it increased their mood!")
                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                        .show()

                }
            }
        }
    }
    private fun updateMoodBasedOnSteps() {
        val stepsPerMilestone = 2000
        val moodIncreasePerMilestone = 1
        val maxMoodIncreasePerDay = 3
        val milestonesReached = (totalSteps / stepsPerMilestone).toInt()

        if (milestonesReached > 0) {
            val moodBoost = min(milestonesReached * moodIncreasePerMilestone, maxMoodIncreasePerDay)
            moodLevel = min(moodLevel + moodBoost, 5)
            updateHealthBars()
            sharedPreferences.edit().putInt("moodLevel", moodLevel).apply()
            Log.d("PlayRoomActivity", "Mood increased by steps to: $moodLevel")
        }
    }

    private fun loadPetImage() {
        // val prefs = getSharedPreferences("pixeliesPrefs", Context.MODE_PRIVATE)

        val selectedPet = sharedPreferences.getString("selectedPet", null)
        Log.d("MakeaPetActivity", "Selected pet: $selectedPet")

        val petDrawableResId = when (selectedPet) {
            "character1" -> R.drawable.character1
            "character2" -> R.drawable.character2
            "character3" -> R.drawable.character3
            else -> null
        }
        petDrawableResId?.let {
            petImageView.setImageResource(it)
        }
    }
    /*fun startQuest(quest: Quest, difficulty: String) {
        updateQuestUI(quest, difficulty)
        QuestManager.startQuest(quest, difficulty)
    }*/
    private fun updateQuestUI(quest: Quest?, difficulty: String) {
        val imageResId = if (quest != null) {
            // Quest is active, show  pet with sword/knife/whatever it technically is
            when (selectedPet) {
                "character1" -> R.drawable.character1_sword
                "character2" -> R.drawable.character2_sword
                "character3" -> R.drawable.character3_sword
                else -> R.drawable.character1_sword
            }
        } else {
            when (selectedPet) {
                "character1" -> R.drawable.character1
                "character2" -> R.drawable.character2
                "character3" -> R.drawable.character3
                else -> R.drawable.character1
            }
        }
        petImageView.setImageResource(imageResId)
    }


    fun updateHealthBars() {
        //  Log.d("PlayRoomActivity", "updateHealthBars: isOnResume = " + isOnResume);
        //   Log.d("PlayRoomActivity", "updateHealthBars: Hunger level = " + hungerLevel + ", Was sick = " + sharedPreferences.getBoolean("wasSick", false));

        val thirstDrawableResId = when (thirstLevel) {
            1 -> R.drawable.thirst_1
            2 -> R.drawable.thirst_2
            3 -> R.drawable.thirst_3
            4 -> R.drawable.thirst_4
            else -> R.drawable.thirst_5
        }
        thirstBar.setImageResource(thirstDrawableResId)

        val hungerDrawableResId = when {
            caloriesConsumedToday - caloriesBurnedToday > calorieGoal -> R.drawable.hunger_6
            hungerLevel == 1 -> R.drawable.hunger_1
            hungerLevel == 2 -> R.drawable.hunger_2
            hungerLevel == 3 -> R.drawable.hunger_3
            hungerLevel == 4 -> R.drawable.hunger_4
            hungerLevel == 6 -> R. drawable.hunger_6
            else -> R.drawable.hunger_5
        }
        hungerBar.setImageResource(hungerDrawableResId)

        val moodDrawableResId = when (moodLevel) {
            1 -> R.drawable.mood_1
            2 -> R.drawable.mood_2
            3 -> R.drawable.mood_3
            4 -> R.drawable.mood_4
            else -> R.drawable.mood_5
        }
        moodBar.setImageResource(moodDrawableResId)
      //  Log.d("PlayRoomActivity", "Hunger level: $hungerLevel, Was sick: ${sharedPreferences.getBoolean("wasSick", false)}")


    }
    fun updatePetAnimationToNormal(selectedPetIndex: String) {
        val prefix = "${selectedPetIndex}run_"
        runningAnimationFrames = Array(7) { i ->
            val frameNumber = String.format("%03d", i + 1)
            resources.getIdentifier("$prefix$frameNumber", "drawable", packageName)
        }
        petImageView.setImageResource(runningAnimationFrames[0])
    }
    fun updateRunningAnimationFrames(selectedPetIndex: String) {
        val prefix = if (runningWithSword) "c${selectedPetIndex}attack_" else "c${selectedPetIndex}run_"
        runningAnimationFrames = Array(7) { i ->
            val frameNumber = String.format("%03d", i + 1)
            resources.getIdentifier("$prefix$frameNumber", "drawable", packageName)
        }
    }
    fun updateDieAnimationFrames(selectedPetIndex: String) {
        val prefix = "c${selectedPetIndex}die_"
        val dieAnimationFrames = Array(12) { i ->
            val frameNumber = String.format("%03d", i)
            resources.getIdentifier("$prefix$frameNumber", "drawable", packageName)
        }
        animatePet(dieAnimationFrames)
    }
    fun animatePet(frames: Array<Int>, interval: Long = 100L) {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            var index = 0
            override fun run() {
                if (index < frames.size) {
                    petImageView.setImageResource(frames[index])
                    index++
                    handler.postDelayed(this, interval)
                } else {
                    //something I think is needed here... but what
                }
            }
        }
        handler.post(runnable)
    }


    fun showQuestDialog() {
        musicdialog = true

        if (isSoundEnabled()) {
            prepareBackgroundMusic()
           // backgroundMusic?.start()
        }
        val quest = QuestRepository.getRandomQuest()
        val currentDifficulty = sharedPreferences.getString("activityLevel", "Easy") ?: "Easy"
        //  Log.d("QuestDialog", "Current Difficulty: $currentDifficulty")
        val description = quest.description[currentDifficulty] ?: "No description available."

        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle(quest.title)
            .setMessage(description)
            .setCancelable(false) // Prevent dialog from being canceled by clicking outside- Need to do this on all of the choice ones I think tbh- but yes, this stop the music from continuing if no quest
            //  .setMessage(quest.description[sharedPreferences.getString("activityLevel", "Easy") ?: "Easy"] ?: "No description available.")
            .setPositiveButton("Accept") { dialog, _ ->
                questManager.startQuest(quest, currentDifficulty)
                currentQuest = quest
                musicdialog = false
                dialog.dismiss()
            }
            .setNegativeButton("Find another quest") { dialog, _ ->
                musicdialog = true
                //  prepareBackgroundMusic()
                dialog.dismiss()
                showQuestDialog()

            }
            .setNeutralButton("Cancel") { dialog, _ ->
                if (currentQuest == null) {
                    backgroundMusic?.stop()
                }
                musicdialog= false
                dialog.dismiss()
            }
        val dialog = dialogBuilder.create()
        dialog.setOnDismissListener {
            // Check if the dialog is dismissed by any way other than the buttons
            if (!musicdialog && currentQuest == null) {
                backgroundMusic?.stop()
                backgroundMusic = null
            }
        }

        dialog.show()
        if (isSoundEnabled()) {
            backgroundMusic?.start() // Ensure music starts after dialog is shown
        }    }

    //  fun onViewQuestDetailsClicked(view: View) {
    //    currentQuest?.let {
    //      showQuestAcceptedDialog(it)
    // } ?: Toast.makeText(this, "No active quest!", Toast.LENGTH_SHORT).show()
    // }
    private fun playSoundMultipleTimes(mediaPlayer: MediaPlayer, times: Int) {
        var count = times
        mediaPlayer.setOnCompletionListener {
            count--
            if (count > 0) {
                mediaPlayer.start()
            } else {
                mediaPlayer.setOnCompletionListener(null)
            }
        }
        mediaPlayer.start()
    }

    private fun initializeSounds() {
        feedSound = MediaPlayer.create(this, R.raw.feed)
        drinkSound = MediaPlayer.create(this, R.raw.drink)
        playSound = MediaPlayer.create(this, R.raw.play)
        headPatSound = MediaPlayer.create(this, R.raw.headpat)
        questAcceptedSound = MediaPlayer.create(this, R.raw.track2_accepted)
        questCompletedSound = MediaPlayer.create(this, R.raw.track3_completed)
        questFailedSound = MediaPlayer.create(this, R.raw.rush)
        questQuitSound = MediaPlayer.create(this, R.raw.quit)
        stepSound = MediaPlayer.create(this, R.raw.step)
        prepareBackgroundMusic()
    }
    private fun prepareBackgroundMusic() {
        if ( isSoundEnabled() && currentQuest != null || musicdialog) {
            backgroundMusic?.release()
            backgroundMusic = null

            // Filter to avoid the last played track
            val availableTracks = musicTracks.filterIndexed { index, _ -> index != lastPlayedTrack }
            val trackIndex = availableTracks.indices.random()
            val selectedTrack = availableTracks[trackIndex]

            lastPlayedTrack = musicTracks.indexOf(selectedTrack)

            // Create a new MediaPlayer for new track
            backgroundMusic = MediaPlayer.create(this, selectedTrack).also { mediaPlayer ->
                mediaPlayer.isLooping = false
                mediaPlayer.setOnCompletionListener {
                    it.release()
                    prepareBackgroundMusic()
                }
                mediaPlayer.setOnErrorListener { mp, what, extra ->
                    Log.e("MediaPlayer", "Playback Error: What $what, Extra $extra")
                    mp.release()
                    prepareBackgroundMusic()
                    true
                }
                mediaPlayer.start()
            }
        }
    }


    private fun showInputDialog(title: String, message: String) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setView(input)
            .setPositiveButton("Submit") { dialog, _ ->
                val value = input.text.toString().toIntOrNull() ?: 0
                handleInput(title, value)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()
    }
    fun showQuestAcceptedDialog(quest: Quest) {

        //val buttonOptions = arrayOf("Complete Quest", "Quit Quest", "Need More Time")
        val currentDifficulty = sharedPreferences.getString("activityLevel", "Easy") ?: "Easy"
        questAcceptedDialog?.dismiss()
        questAcceptedDialog = null


        val newDialog = AlertDialog.Builder(this)
            .setTitle("Quest Active")
            .setMessage("Your quest is underway. What would you like to do?")
            .setPositiveButton("Complete Quest") { dialog, _ ->
                // onQuestCompleted(quest)
                questManager.completeQuest(quest, currentDifficulty)
                Log.d("PlayRoomActivity", "Completing quest: ${quest.title}")
                dialog.dismiss()
            }
            .setNegativeButton("Quit Quest") { dialog, _ ->
                //QuestManager.quitQuest(currentQuest, currentDifficulty)
                questManager.quitQuest(quest, currentDifficulty)

               // Log.d("PlayRoomActivity", "Quitting quest: ${quest.title}")
                currentQuest = null
                updateQuestUI(null, currentDifficulty)
                updateIdleState()

                if (isSoundEnabled()) {
                    backgroundMusic?.stop()
                    questQuitSound.start()
                }
                dialog.dismiss()
            }
            .setNeutralButton("Need More Time") { dialog, _ ->
                //  Log.d("PlayRoomActivity", "Continuing quest: ${quest.title}")
                //dialog.dismiss()
                showQuestDetailsDialog(quest, currentDifficulty)
                dialog.dismiss()

            }
            // .setOnDismissListener {
            //     questAcceptedDialog = null
            //}
            .create()
        newDialog.show()
        questAcceptedDialog = newDialog

    }
    private fun showQuestDetailsDialog(quest: Quest, difficulty: String) {
        val description = quest.description[difficulty] ?: "No description available."
        val action = quest.action[difficulty] ?: "No action specified."

        val detailsDialog = AlertDialog.Builder(this)
            .setTitle(quest.title)
            .setMessage("Description: $description\nAction: $action")
            .setPositiveButton("OK") { dialog, _ ->
//                questAcceptedDialog?.dismiss()
                dialog.dismiss()
            }
            .create()
        detailsDialog.show()

    }
    private fun handleInput(inputType: String, value: Int) {
        when (inputType) {
            "Feed" -> {
                caloriesConsumedToday += value
                updateHungerLevel()
                checkHungerWarning()
                if (hungerLevel > 1) {
                    hungerZeroTimestamp = if (isDemoMode) demoTime.timeInMillis else System.currentTimeMillis()
                    //    Log.d("PlayRoomActivity", "Hunger zero timestamp set to: $hungerZeroTimestamp")

                }
                playSoundMultipleTimes(feedSound, 3)
            }

            "Drink" -> {
                waterConsumedToday += value
                updateThirstLevel()
                if (thirstLevel > 1) {
                    thirstZeroTimestamp = if (isDemoMode) demoTime.timeInMillis else System.currentTimeMillis()
                }
                if (isSoundEnabled()) {
                    drinkSound.start()
                }

            }

            "Play" -> {
                val activityLevel = sharedPreferences.getString("activityLevel", "Easy")
                caloriesBurnedToday += value
                // caloriesBurnedToday = sharedPreferences.getInt("caloriesBurnedToday", 0) + value
                //i think above is redundant- need to check- dont delete yet in case
                val moodBoost = when {
                    value < 100 -> 1
                    value in 100..300 -> 2
                    value in 301..500 -> 3
                    else -> 4
                }
                moodLevel = min(moodLevel + moodBoost, 5)
                if ((activityLevel == "Medium" && caloriesBurnedToday > 300) ||
                    (activityLevel == "Hard" && caloriesBurnedToday > 500)) {
                    AlertDialog.Builder(this)
                        .setMessage("Congratulations! That's a lot of activity today! Timed mood decreases will be paused for the rest of today.")
                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                        .show()
                }
                // updateHungerLevel()
                //caloriesConsumedToday = max(0, caloriesConsumedToday - value)
                //moodLevel = min(moodLevel + 1, 5)
                ///shouldShowMoodWarning = true
                if (moodLevel > 1) {
                    moodZeroTimestamp = if (isDemoMode) demoTime.timeInMillis else System.currentTimeMillis()
                }
                if ((caloriesConsumedToday - caloriesBurnedToday) <= calorieGoal && sharedPreferences.getBoolean("wasSick", true)) {
                    Log.d("PlayRoomActivity", "Showing recovery notification")
                    hungerLevel = 5
                    // updateHungerLevel()
                    //  updateHealthBars()
                    // Pet has recovered
                    showRecoveryNotification()
                    sharedPreferences.edit().putBoolean("wasSick", false).apply()
                }
            }
        }
        saveStats()
        updateHealthBars()
        updateStatsTextViews()
    }

    fun logAllPreferences() {
        val allPrefs = sharedPreferences.all
        allPrefs.forEach { (key, value) ->
            Log.d("SharedPreferences", "$key: $value")
        }
    }

    /*private fun updateHungerLevel() {
        val netCaloriesConsumedToday = caloriesConsumedToday - caloriesBurnedToday
        hungerLevel = when {
            caloriesConsumedToday >= calorieGoal -> 5
            caloriesConsumedToday >= calorieGoal * 0.75 -> 4
            caloriesConsumedToday >= calorieGoal * 0.5 -> 3
            caloriesConsumedToday >= calorieGoal * 0.25 -> 2
            caloriesConsumedToday > 0 -> 1
            else -> 1
        }
        //if (caloriesConsumedToday > calorieGoal) {
          //  moodLevel = max(moodLevel - 1, 1) // Decrease mood if overeating
            //hungerLevel = 6

        //}
        if (hungerLevel == 1 && hungerZeroTimestamp == 0L) {
            hungerZeroTimestamp = if (isDemoMode) demoTime.timeInMillis else System.currentTimeMillis()
        } else if (hungerLevel > 1) {
            hungerZeroTimestamp = 0
        }

        // Check if the pet is sick due to overeating
            // if (hungerLevel == 6) {
           // sharedPreferences.edit().putBoolean("wasSick", true).apply()
            //showSickNotification()
       // }

        if (netCaloriesConsumedToday > calorieGoal && !sharedPreferences.getBoolean("wasSick", false)) {
            sharedPreferences.edit().putBoolean("wasSick", true).apply()
            hungerLevel = 6
            showSickNotification()
        } else if (netCaloriesConsumedToday <= calorieGoal && sharedPreferences.getBoolean("wasSick", true)) {
            sharedPreferences.edit().putBoolean("wasSick", false).apply()
            showRecoveryNotification()
        }
        updateHealthBars()
        checkHungerWarning()

    }*/

    private fun updateHungerLevel() {
        val netCaloriesConsumedToday = caloriesConsumedToday - caloriesBurnedToday
        hungerLevel = when {
            caloriesConsumedToday >= calorieGoal -> 5
            caloriesConsumedToday >= calorieGoal * 0.75 -> 4
            caloriesConsumedToday >= calorieGoal * 0.5 -> 3
            caloriesConsumedToday >= calorieGoal * 0.25 -> 2
            caloriesConsumedToday > 0 -> 1
            else -> 1
        }

        if (hungerLevel == 1 && hungerZeroTimestamp == 0L) {
            hungerZeroTimestamp = if (isDemoMode) demoTime.timeInMillis else System.currentTimeMillis()
        } else if (hungerLevel > 1) {
            hungerZeroTimestamp = 0
        }

        if (netCaloriesConsumedToday > calorieGoal) {
            if (!sharedPreferences.getBoolean("wasSick", false)) {
                sharedPreferences.edit().putBoolean("wasSick", true).apply()
                hungerLevel = 6
                showSickNotification()
            } else {
                showSickNotification() //think I need to fix this, it appears sometimes when not needed
            }
        } else if (netCaloriesConsumedToday <= calorieGoal && sharedPreferences.getBoolean("wasSick", true)) {
            sharedPreferences.edit().putBoolean("wasSick", false).apply()
            showRecoveryNotification()
        }

        updateHealthBars()
    }

    private fun updateThirstLevel() {
        thirstLevel = when {
            waterConsumedToday >= waterGoal -> 5
            waterConsumedToday >= waterGoal * 0.75 -> 4
            waterConsumedToday >= waterGoal * 0.5 -> 3
            waterConsumedToday >= waterGoal * 0.25 -> 2
            waterConsumedToday > 0 -> 1
            else -> 0
        }
        if (thirstLevel == 1 && thirstZeroTimestamp == 0L) {
            thirstZeroTimestamp = if (isDemoMode) demoTime.timeInMillis else System.currentTimeMillis()
            //  Log.d("PlayRoomActivity", "Thirst zero timestamp set to: $thirstZeroTimestamp")

        } else if (thirstLevel > 1) {
            thirstZeroTimestamp = 0
        }
        checkThirstWarning()

    }
    private fun simulateNextDay() {

        // Fake the next day for testing/ demo purposes
        checkDeathConditions()
        //  if (caloriesConsumedToday == 0 && waterConsumedToday == 0 && moodLevel > 1) {
        //      moodLevel = max(moodLevel - 2, 1)
        //   }
        //else if (moodLevel == 1) {
        // moodZeroTimestamp = 0;
        //   }
        resetDailyStats()
        updateHealthBars()
        updateStatsTextViews()


        if (hungerLevel == 1) {
            skippedDaysCountHunger++
            hungerZeroTimestamp = demoTime.timeInMillis - DEMO_DAY_DURATION // Set to 24 hours ago in demo mode
        }
        if (thirstLevel == 1) {
            skippedDaysCountThirst++
            thirstZeroTimestamp = demoTime.timeInMillis - DEMO_DAY_DURATION
        }
        if (moodLevel == 1) {
            skippedDaysCountMood++
            moodZeroTimestamp = demoTime.timeInMillis - DEMO_DAY_DURATION
        }
        //   val currentTime = System.currentTimeMillis()
        //  if (hungerLevel == 1) {
        //    hungerZeroTimestamp = currentTime - (24 * 60 * 60 * 1000) // Set to 24 hours ago
        //}
        //if (thirstLevel == 1) {
        //  thirstZeroTimestamp = currentTime - (24 * 60 * 60 * 1000)
        // }


        // Check if mood  decrease due to neglect
        //if (caloriesConsumedToday == 0 && waterConsumedToday == 0 && moodLevel > 1) {
        //  moodLevel = max(moodLevel - 2, 1)
        //} else if (moodLevel == 1) {
        //   moodZeroTimestamp = currentTime - (24 * 60 * 60 * 1000)
        // }
        // Reset the demo clock to 5 am
        if (isDemoMode) {
            demoTime.set(Calendar.HOUR_OF_DAY, 5)
            demoTime.set(Calendar.MINUTE, 0)
            demoTime.set(Calendar.SECOND, 0)
            updateDemoClock()
        }
        // Check if the pet should die due to too long hunger, thirst, or low mood

        Toast.makeText(this, "Advanced to the next day in demo mode", Toast.LENGTH_SHORT).show()
    }

    private fun saveStats() {
        val editor = sharedPreferences.edit()
        editor.putInt("caloriesConsumedToday", caloriesConsumedToday)
        editor.putInt("caloriesBurnedToday", caloriesBurnedToday)
        editor.putInt("waterConsumedToday", waterConsumedToday)
        editor.putLong("hungerZeroTimestamp", hungerZeroTimestamp)
        editor.putLong("thirstZeroTimestamp", thirstZeroTimestamp)
        editor.putLong("moodZeroTimestamp", moodZeroTimestamp)
        editor.putInt("hungerLevel", hungerLevel)
        editor.putInt("thirstLevel", thirstLevel)
        editor.putInt("moodLevel", moodLevel)
        editor.putInt("previousMoodLevel", previousMoodLevel)
        editor.putFloat("totalSteps", totalSteps)
        editor.putFloat("initialSteps", initialSteps)
        // Log.d("PlayRoomActivity", "Saving consumed: caloriesConsumedToday=$caloriesConsumedToday, waterConsumedToday=$waterConsumedToday")

        editor.apply()
    }
    private fun showRecoveryNotification() {
      //  Log.d("PlayRoomActivity", "showRecoveryNotification: Displaying recovery notification")
//thsi shows up just a little more than supposed to. Can't figure out what is triggering it because it's so random
        AlertDialog.Builder(this)
            .setTitle("Good news!")
            .setMessage("Your pet isn't feeling sick anymore!")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    // fun initializeQuestManager() {
    //    questManager = QuestManager(this, QuestRepository.quests)
    //  questManager.onQuestCompleted = {quest, difficulty ->
    //    updatePetStatus(quest, difficulty)
    //  showQuestCompletionDialog(quest, difficulty)

    //}
    // }





    /*fun completeQuest(quest: Quest, difficulty: String) {
        val reward = quest.reward[difficulty] ?: Reward(0, 0)
        moodLevel = min(moodLevel + reward.moodBoost, 5)
        caloriesBurnedToday += reward.caloriesBurned
        updateStatsTextViews()

        val normalImageResId = when (sharedPreferences.getString("selectedPet", "defaultPet")) {
            "character1" -> R.drawable.character1
            "character2" -> R.drawable.character2
            "character3" -> R.drawable.character3
            else -> R.drawable.character1
        }
        petImageView.setImageResource(normalImageResId)

        showQuestCompletionDialog(quest, difficulty)
    }
*/
    //fun updatePetStatus(quest: Quest, difficulty: String) {
    //    val reward = quest.reward[difficulty] ?: Reward(0, 0)
    //  moodLevel = min(moodLevel + reward.moodBoost, 5)
    //caloriesBurnedToday += reward.caloriesBurned

    //updateHealthBars()
    //updateStatsTextViews()
    //}
    fun showQuestCompletionDialog(quest: Quest, reward: Reward) {
        val title = "Quest Completed!"
        val difficulty = sharedPreferences.getString("activityLevel", "Easy") ?: "Easy"
        val message = "Congratulations! You've completed the quest: ${quest.title} on $difficulty difficulty."
        val reward = quest.reward[difficulty]
        val additionalMessage = if (reward != null) {
            "\nYou earned ${reward.moodBoost} mood points and burned ${reward.caloriesBurned} calories!"
        } else {
            "\nNo rewards this time."
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message + additionalMessage)
            .setPositiveButton("Great!") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
        if (isSoundEnabled()) {
            backgroundMusic?.stop()
            questCompletedSound.start()
        }

        currentQuest = null  // Reset current quest
        updateQuestUI(null, difficulty)
        updateIdleState()

        updateHungerLevel()

        updateHealthBars()
        saveStats()
    }


    private fun loadStats() {
        calorieGoal = sharedPreferences.getInt("calorieGoal", 0)
        waterGoal = sharedPreferences.getInt("waterGoal", 0)
        moodLevel = sharedPreferences.getInt("moodLevel", -1)
        previousMoodLevel = sharedPreferences.getInt("previousMoodLevel", 5)
        caloriesBurnedToday = sharedPreferences.getInt("caloriesBurnedToday", 0)
        totalSteps = sharedPreferences.getFloat("totalSteps", 0f)
        initialSteps = sharedPreferences.getFloat("initialSteps", 0f)
        //Log.d("PlayRoomActivity", "Loaded goals: calorieGoal=$calorieGoal, waterGoal=$waterGoal")

        val isFirstLaunch = sharedPreferences.getBoolean("firstLaunch", true)

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        val todayDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(calendar.time)
        val lastResetDate = sharedPreferences.getString(LAST_RESET_DATE_KEY, "")

        val isNewDay = currentHour >= 5 && todayDate != lastResetDate

        if (isNewDay || isFirstLaunch) {
            caloriesConsumedToday = 0
            waterConsumedToday = 0
            caloriesBurnedToday = 0
            caloriesBurnedFromSteps = 0
            sharedPreferences.edit().putString(LAST_RESET_DATE_KEY, todayDate).apply()

        } else {
            caloriesConsumedToday = sharedPreferences.getInt("caloriesConsumedToday", 0)
            waterConsumedToday = sharedPreferences.getInt("waterConsumedToday", 0)
        }
        // Log.d("PlayRoomActivity", "Loaded consumed: caloriesConsumedToday=$caloriesConsumedToday, waterConsumedToday=$waterConsumedToday")

        hungerLevel = if (isNewDay || isFirstLaunch) 1 else sharedPreferences.getInt("hungerLevel", 1)
        thirstLevel = if (isNewDay || isFirstLaunch) 1 else sharedPreferences.getInt("thirstLevel", 1)

        moodLevel = sharedPreferences.getInt("moodLevel", -1)
        if (moodLevel == -1) {
            moodLevel = 5
            val editor = sharedPreferences.edit()
            editor.putInt("moodLevel", moodLevel)
            editor.apply()
        }

        // Load timestamps
        hungerZeroTimestamp = sharedPreferences.getLong("hungerZeroTimestamp", 0)
        thirstZeroTimestamp = sharedPreferences.getLong("thirstZeroTimestamp", 0)
        moodZeroTimestamp = sharedPreferences.getLong("moodZeroTimestamp", 0)
        // Log.d("PlayRoomActivity", "Loaded stats: hungerLevel=$hungerLevel, thirstLevel=$thirstLevel, moodLevel=$moodLevel")

    }



    private fun setupDailyResetAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, DailyResetReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 5)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        val interval = if (isDemoMode) 60 * 1000L else AlarmManager.INTERVAL_DAY // 1 minute interval for demo mode

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            interval,
            pendingIntent
        )
    }


    private fun checkMoodLevel() {
        if (moodLevel > 1) {
            moodZeroTimestamp = 0
        }
        if (moodLevel == 1 && moodZeroTimestamp == 0L) {
            moodZeroTimestamp = System.currentTimeMillis()
            showMoodWarning("miserable")
            //   Log.d("PlayRoomActivity", "Mood zero timestamp set to: $moodZeroTimestamp")

        } else if (moodLevel > 1) {
            moodZeroTimestamp = 0
        }
        if (shouldShowMoodWarning && moodLevel < previousMoodLevel) {
            when (moodLevel) {
                2 -> showMoodWarning("unhappy")
                3 -> showMoodWarning("bored")
                4 -> showMoodWarning("content")
                5 -> showMoodWarning("happy")
            }
            shouldShowMoodWarning = false
            previousMoodLevel = moodLevel

        }

    }

    private fun decreaseMoodOverTime() {
        val activityLevel = sharedPreferences.getString("activityLevel", "Easy")
        Log.d("PlayRoomActivity", "Activity level: $activityLevel")
        val pauseMoodDecrease = when (activityLevel) {
            //easy doesnt need it because it only docks one per day anyway, in non demo mode
            "Medium" -> caloriesBurnedToday > 300
            "Hard" -> caloriesBurnedToday > 500
            else -> false
        }

        // val moodDecreaseInterval = when (activityLevel) {
        //   "easy" -> if (isDemoMode)  60 * 1000L else 24 * 60 * 60 * 1000L // 1 minute in demo mode, 24 hours otherwise
        // "medium" -> if (isDemoMode) 45 * 1000L else 12 * 60 * 60 * 1000L // 45 seconds in demo mode, 12 hours otherwise
        // "hard" -> if (isDemoMode) 30 * 1000L else 6 * 60 * 60 * 1000L // 30 seconds in demo mode, 6 hours otherwise
        // else -> if (isDemoMode)  60 * 1000L else 24 * 60 * 60 * 1000L // Default to 1 minute in demo mode, 24 hours otherwise
        // }

        val moodDecreaseInterval = when {
            activityLevel == "Hard" && isDemoMode -> 30 * 1000L // 30 seconds in demo mode
            activityLevel == "Medium" && isDemoMode -> 45 * 1000L // 45 seconds in demo mode
            activityLevel == "Easy" && isDemoMode -> 60 * 1000L // 1 minute in demo mode
            activityLevel == "Hard" -> 6 * 60 * 60 * 1000L // 6 hours
            activityLevel == "Medium" -> 12 * 60 * 60 * 1000L // 12 hours
            else -> 24 * 60 * 60 * 1000L // 24 hours for "easy" and default
        }

        Log.d("PlayRoomActivity", "Mood decrease interval: $moodDecreaseInterval")

        if (!pauseMoodDecrease) {
            moodLevel = max(moodLevel - 1, 1)
            updateHealthBars()
            checkMoodLevel()
        }
        updateHealthBars()
        checkMoodLevel()
        // checkDeathConditions()

        // Schedule the next decrease
        moodBar.postDelayed({
            decreaseMoodOverTime()
            shouldShowMoodWarning = true
            //    checkDeathConditions()
        }, moodDecreaseInterval)    }

    fun resetDailyStats() {
        if (caloriesConsumedToday == 0 || waterConsumedToday == 0) {
            moodLevel = max(moodLevel - 2, 1)
            AlertDialog.Builder(this)
                .setTitle("Mood Decrease Alert")
                .setMessage("Hey! ${sharedPreferences.getString("petName", "your pet")}'s mood decreased due to your neglect. Make sure they have food and water... or else.")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
        caloriesConsumedToday = 0
        waterConsumedToday = 0
        hungerLevel = 1
        thirstLevel = 1
        caloriesBurnedToday = 0
        caloriesBurnedFromSteps = 0
        totalSteps = 0f
        initialSteps = 0f

        saveStats()
        updateHealthBars()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("currentQuest", currentQuest)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentQuest = savedInstanceState.getSerializable("currentQuest") as? Quest
    }

    private fun checkDeathConditions() {
        val currentTime = System.currentTimeMillis()
        val twoDaysInMillis =  2 * 24 * 60 * 60 * 1000L
        val fourDaysInMillis = 4 * 24 * 60 * 60 * 1000L
        // Log.d("PlayRoomActivity", "Checking death conditions in check death conditions function")

        if (!isPetHandled) {
            if(isDemoMode){
                if(moodLevel == 1 && skippedDaysCountMood >= 4) {
                    handleMoodDeparture()
                    isPetHandled = true
                }
                else if(thirstLevel == 1 && skippedDaysCountThirst >= 2) {
                    handleDeath()
                    isPetHandled = true
                }
                else if(hungerLevel == 1 && skippedDaysCountHunger >= 2){
                    handleDeath()
                    isPetHandled = true
                }
            }
            else if (hungerLevel == 1 && hungerZeroTimestamp != 0L && currentTime - hungerZeroTimestamp > twoDaysInMillis) {
                handleDeath()
                isPetHandled = true
            }
            if (thirstLevel == 1 && thirstZeroTimestamp != 0L && currentTime - thirstZeroTimestamp > twoDaysInMillis) {
                handleDeath()
                isPetHandled = true
            }
            if (moodLevel == 1 && moodZeroTimestamp != 0L && currentTime - moodZeroTimestamp > fourDaysInMillis) {
                handleMoodDeparture()
                isPetHandled = true
            }
        }
    }
    private fun updateDemoClock() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        demoClockTextView.text = "Demo Time: ${timeFormat.format(demoTime.time)}"
    }

    private fun handleMoodDeparture() {
        //   Log.d("PlayRoomActivity", "Checking mood departure conditions")

        val petName = sharedPreferences.getString("petName", "your pet")
        val animator = ObjectAnimator.ofFloat(petImageView, "alpha", 1f, 0f)
        animator.duration = 1000 // Duration in milliseconds
        animator.start()
        Handler().postDelayed({
            questQuitSound.start()
            AlertDialog.Builder(this)
                .setTitle("Pet Departure")
                .setMessage("Oh, so now you decide to come back. Well, it's too late. $petName found a new family. One that will keep its promises! Better luck with the next one.")
                .setPositiveButton("Start over") { dialog, _ ->
                    resetApp()
                    dialog.dismiss()
                }
                .show()
        }, 3000) // 3 sec

    }
    private fun checkHungerWarning() {
        //   val currentTime = System.currentTimeMillis()
        // if (hungerLevel == 1 && (currentTime - hungerZeroTimestamp >= 2 * 60 * 60 * 1000)) {
        // Show warning if the pet has been hungry for more than 2 hours- need to come back to this in future bc it ain't happening now
        //    AlertDialog.Builder(this)
        //      .setTitle("Hunger Warning")
        //    .setMessage("Hey! ${sharedPreferences.getString("petName", "your pet")} isn't looking too good. They haven't eaten in a while. Better throw them some bones before they turn into some.")
        //  .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        // .show()
        //}
        when (hungerLevel) {
            1 -> showHungerWarning("starving")
            2 -> showHungerWarning("very hungry")
            3 -> showHungerWarning("slightly hungry")
            4 -> showHungerWarning("snackish")
            5 -> showHungerWarning("full")
        }
    }
    private fun showMoodWarning(level: String) {
        val message = when (level) {
            "miserable" -> "${sharedPreferences.getString("petName", "your pet")} is miserable and wanting some TLC. At least give them a few head pats by touching the screen. This will buy you some time until you decide you want to be a good pet parent, again. But caution- pets can only be this sad for so long before they run away."
            "unhappy" -> "${sharedPreferences.getString("petName", "your pet")} isn't very happy with you. Pets need attention, you know. Better play with them soon."
            "bored" -> "Hey there- uh, ${sharedPreferences.getString("petName", "your pet")} is feeling a little bored. You might want to take them for a walk or play."
            "content" -> "${sharedPreferences.getString("petName", "your pet")} is feeling content. Don't forget to make sure they stay active."
            "happy" -> "${sharedPreferences.getString("petName", "your pet")}'s mood is very high! They are feeling very happy with you. Let's keep it that way!"
            else -> ""
        }

        if (message.isNotEmpty()) {
            if (!isFinishing && !isDestroyed) {
                AlertDialog.Builder(this)
                    .setTitle("Mood Warning")
                    .setMessage(message)
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }
    }
    private fun showHungerWarning(level: String) {
        val message = when (level) {
            "slightly hungry" -> "${sharedPreferences.getString("petName", "your pet")} is still slightly hungry. Time for a treat?"
            "hungry" -> "${sharedPreferences.getString("petName", "your pet")} is still hungry. Don't forget to feed them later!"
            "very hungry" -> "${sharedPreferences.getString("petName", "your pet")} is very hungry. Please feed them more!"
            "starving" -> "Hey! ${sharedPreferences.getString("petName", "your pet")} isn't looking too good. They are starving! Better throw them some bones before they turn into some."
            "snackish" -> "${sharedPreferences.getString("petName", "your pet")} is still feeling a bit snackish."
            "full" ->"${sharedPreferences.getString("petName", "your pet")} has a nice full tummy!"
            else -> ""
        }

        if (message.isNotEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Hunger Warning")
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun showThirstWarning(level: String) {
        val message = when (level) {
            "thirsty" -> "${sharedPreferences.getString("petName", "your pet")} is still feeling slightly thirsty. Keep drinking!"
            "parched" -> "${sharedPreferences.getString("petName", "your pet")} is still pretty parched. Don't forget to give them more water later!"
            "very thirsty" -> "${sharedPreferences.getString("petName", "your pet")} is still very thirsty. Please give them more water soon!"
            "dehydrated" -> "Hey! Excuse me? ${sharedPreferences.getString("petName", "your pet")} is dehydrated! Give them water immediately!"
            "hydrated" -> "${sharedPreferences.getString("petName", "your pet")} is feeling refreshed and hydrated! Great job!"
            else -> ""
        }

        if (message.isNotEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Thirst Warning")
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }
    private fun showSickNotification() {
        AlertDialog.Builder(this)
            .setTitle("Your pet is sick!")
            .setMessage("Your ${sharedPreferences.getString("petName", "pet")} is sick due to overeating! What would you like to do?")
            .setPositiveButton("Play") { dialog, _ ->
                showPlayOptions()
                dialog.dismiss()
            }
            .setNegativeButton("Ignore") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
        //sharedPreferences.edit().putBoolean("wasSick", true).apply()
    }
    //private fun startPlayroomActivity() {
    //    val intent = Intent(this, PlayRoomActivity::class.java)
    //    intent.putExtra("isDemoMode", isDemoMode)
    //    startActivity(intent)
    //      finish()
    // }
    private fun showPlayOptions() {
        AlertDialog.Builder(this)
            .setTitle("Choose an option")
            .setMessage("Would you like to take on a quest or would you like to input burned calories?")
            .setPositiveButton("Input Activity") { dialog, _ ->
                showInputDialog("Play", "Enter calories burned")
                dialog.dismiss()
            }
            .setNegativeButton("Let's go on a quest!") { dialog, _ ->
                //  val intent = Intent(this, QuestActivity::class.java)
                showQuestDialog()
                dialog.dismiss()
            }
            .show()
    }
    private fun showBeginnerDialog() {
        val petName = sharedPreferences.getString("petName", "your pet")
        AlertDialog.Builder(this)
            .setTitle("Welcome!")
            .setMessage("Meet $petName! $petName will be your goal accountability buddy! $petName is hungry and thirsty and needs you to take care of them! Please input your real-life food and water intake throughout the day. Be careful not to wait too long! You wouldn't want anything BAD to happen to $petName... would you?")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    fun updateStatsTextViews() {
        val netCalories = caloriesConsumedToday - (caloriesBurnedToday + caloriesBurnedFromSteps.toInt())
        caloriesConsumedTextView.text = "Net Calories: $netCalories / $calorieGoal"
        totalCaloriesConsumedTextView.text = "Gross Cal: $caloriesConsumedToday"
        caloriesBurnedTextView.text = "Calories Burned: ${caloriesBurnedToday + caloriesBurnedFromSteps.toInt()}"
    }



    private fun handleDeath() {
        // Log.d("PlayRoomActivity", "Checking death conditions in handle death")

        val deathMessages = listOf(
            "RIP [Pet Name]. They lived a short but memorable life. Next time, let's aim for a longer friendship!",
            "Game over! [Pet Name] has ascended to pet heaven, probably to get away from your care. Better luck with your next furry friend!",
            "Oops! [Pet Name] has left the building...permanently. Remember, pets need love (and food, and water) to survive.",
            "[Pet Name] has officially ghosted you. In the afterlife, they're probably enjoying the care they missed out on.",
            "It's a sad day in pet town. [Pet Name] has moved on to greener pastures, literally. Time to reflect and try again!",
            "Tragic news! [Pet Name] couldn't survive the neglect. Let's pour one out and start anew with a lesson learned.",
            "Alas, [Pet Name] is no more. They've gone to a place where pets get regular meals and attention. Hint, hint."
        )

        val petName = sharedPreferences.getString("petName", "your pet")
        val randomDeathMessage = deathMessages.random().replace("[Pet Name]", petName ?: "your pet")
        Handler().postDelayed({
            if (isSoundEnabled()) {
                questFailedSound.start()
            }
            val selectedPetIndex = derivePetIndex()
            updateDieAnimationFrames(selectedPetIndex)
            Handler().postDelayed({
                if (isSoundEnabled()) {
                    questQuitSound.start()
                }
                petImageView.visibility = View.GONE

                AlertDialog.Builder(this)
                    .setTitle("Sad news...")
                    .setMessage(randomDeathMessage)
                    .setPositiveButton("Start over") { dialog, _ ->
                        resetApp()
                        dialog.dismiss()
                    }
                    .show()
            }, 2000) // 2sec
        }, 4000) // 4sec (doing this because the pet will die when its a new day in skip day/demo mode so the dialog boxes block the death scene


    }
    private fun startFakeClock() {
        val clockUpdateInterval = 1000L // 1 second
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                demoTime.add(Calendar.MINUTE, 1)
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                demoClockTextView.text = "Demo Time: ${timeFormat.format(demoTime.time)}"
                handler.postDelayed(this, clockUpdateInterval)
            }
        }
        handler.post(runnable)
    }
    override fun onResume() {
        super.onResume()
        // backgroundMusic?.stop()
        loadPetImage()

        sharedPreferences.getString("currentQuest", null)?.let {
            currentQuest = Gson().fromJson(it, Quest::class.java) // Deserialize JSON (Note to self- Google again if needed)
            updateQuestUI(currentQuest, "currentDifficulty")
        }
        if (isSoundEnabled()) {
            if (currentQuest != null || musicdialog) {
                if (backgroundMusic?.isPlaying != true) {
                    prepareBackgroundMusic()
                    backgroundMusic?.start()
                }
            }
        }
        loadStats()
        updateStatsTextViews()
        checkThirstWarning()
        checkHungerWarning()
        //val activityLevel = sharedPreferences.getString("activityLevel", "Easy")
        selectedPet = sharedPreferences.getString("selectedPet", "character1")
        val currentDifficulty = sharedPreferences.getString("activityLevel", "Easy") ?: "Easy"


        if (moodLevel == 1){
            showMoodWarning("miserable")
        }
        if (currentQuest == null && !runningWithSword) {
            updateIdleState()
        } else{
            updateQuestUI(currentQuest, currentDifficulty)
            updateRunningAnimationFrames(derivePetIndex())

        }


        val sensorEnabled = sharedPreferences.getBoolean("sensorEnabled", true)
        if (sensorEnabled) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            if (stepSensor != null) {
                sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
                initialSteps = totalSteps
                Toast.makeText(this, "Step sensor enabled.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No step counter sensor found!", Toast.LENGTH_SHORT).show()
            }
        } else {
            if (stepSensor != null) {
                sensorManager.unregisterListener(this, stepSensor)
            }
            Toast.makeText(this, "Step sensor disabled to preserve battery.", Toast.LENGTH_SHORT).show()
        }
    }
    fun blinkPet(blinkResourceId: Int, normalResourceId: Int, doubleBlink: Boolean) {
        petImageView.setImageResource(blinkResourceId)
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            petImageView.setImageResource(normalResourceId)  // Reset to normal after  blink
            if (doubleBlink) {
                handler.postDelayed({
                    // double blink
                    petImageView.setImageResource(blinkResourceId)
                    handler.postDelayed({
                        petImageView.setImageResource(normalResourceId)
                    }, 200)  //  delay for  second blink
                }, 200)
            }
        }, 200)
    }

    fun startBlinking(normalResourceId: Int, blinkResourceId: Int) {
        // Remove any existing callbacks
        stopBlinking()

        val newRunnable = object : Runnable {
            var nextDoubleBlink = false
            override fun run() {
                blinkPet(blinkResourceId, normalResourceId, nextDoubleBlink)
                nextDoubleBlink = !nextDoubleBlink
                Log.d("startBlinking", "Runnable running, next double blink: $nextDoubleBlink")
                handler?.postDelayed(this, 10000)
            }
        }
        blinkRunnable = newRunnable

        if (handler != null) {
            handler?.post(blinkRunnable!!)
            Log.d("startBlinking", "Blinking started with resource ID: $blinkResourceId")
        } else {
            Log.e("startBlinking", "Handler not initialized")
        }
    }


    fun stopBlinking() {
        val localRunnable = blinkRunnable
        if (localRunnable != null) {
            handler?.removeCallbacks(localRunnable)
            blinkRunnable = null
            Log.d("stopBlinking", "Blinking stopped.")

        }
    }
    fun updateIdleState() {
         Log.d("updateIdleState", "Current quest: $currentQuest, Running with sword: $runningWithSword")

        if (currentQuest == null && !runningWithSword) {
            val selectedPetIndex = derivePetIndex()
            val normalResourceId = resources.getIdentifier("character$selectedPetIndex", "drawable", packageName)
            val blinkResourceId = resources.getIdentifier("c${selectedPetIndex}idle_007", "drawable", packageName)

            startBlinking(normalResourceId, blinkResourceId)
            Log.d("updateIdleState", "Idle animation started for pet index: $selectedPetIndex")

        } else {
            stopBlinking()
            Log.d("updateIdleState", "Idle animation stopped due to active quest or running state")

            val selectedPetIndex = derivePetIndex()
            updateRunningAnimationFrames(selectedPetIndex)
        }
    }


    override fun onPause() {
        super.onPause()

        // Save quest state and other prefs
        val editor = sharedPreferences.edit()
        currentQuest?.let {
            val questJson = Gson().toJson(it) // Serialize again
            editor.putString("currentQuest", questJson)
        } ?: editor.remove("currentQuest") // trash it if not active quest
        editor.apply()

        saveStats()

        backgroundMusic?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            backgroundMusic = null
        }

        if (sharedPreferences.getBoolean("sensorEnabled", true) && sensorManager != null) {
            try {
                sensorManager?.unregisterListener(this)
                sensorManager.unregisterListener(this)
            } catch (e: Exception) {
                Log.e("PlayRoomActivity", "Error unregistering sensor: ${e.message}")
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.d("SensorDebug", "Sensor event received")

        try {
            event?.let {
                if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                    if (initialSteps == 0f) {
                        initialSteps = event.values[0]
                    }
                    totalSteps = event.values[0] - initialSteps
                    findViewById<TextView>(R.id.stepCountTextView).text = "Steps: ${totalSteps.toInt()}"

                    val selectedPetIndex = derivePetIndex()
                    if (runningWithSword) {
                        updateRunningAnimationFrames(selectedPetIndex)  // Use sword running sprites
                    } else {
                        updatePetAnimationToNormal(selectedPetIndex)  // Use normal running sprites
                    }
                    petImageView.setImageResource(runningAnimationFrames[currentRunFrame])

                    if (isSoundEnabled()) {
                        if (stepSound.isPlaying) {
                            stepSound.pause()
                            stepSound.seekTo(0)
                        }
                        stepSound.start()
                    }

                    // Calculate calories burned by steps
                    val stepsPerMile = 2000
                    val caloriesPerMile = 100
                    val milesWalked = totalSteps / stepsPerMile
                    caloriesBurnedFromSteps = (milesWalked * caloriesPerMile).toInt()

                    findViewById<TextView>(R.id.caloriesBurnedTextView).text = "Calories Burned: ${caloriesBurnedToday + caloriesBurnedFromSteps}"

                    //need delays because it will prob kill battery otherwise
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        updateStatsTextViews()
                        updateMoodBasedOnSteps()
                    }, 5000)
                }
            }
        } catch (e: Exception) {
            Log.e("SensorDebug", "Error handling sensor event", e)
        }
    }



    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // i think for now, not needed?
        // oh nvm def dont delete bc then it breaks. yay
    }

    private fun resetApp() {
        sharedPreferences.edit().clear().apply()

        hungerLevel = 1
        thirstLevel = 1
        moodLevel = 5
        caloriesConsumedToday = 0
        waterConsumedToday = 0

        updateHealthBars()
        isPetHandled = false

        val intent = Intent(this, MainActivity::class.java).apply{
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        finish()
    }
    private fun isSoundEnabled(): Boolean {
        return getSharedPreferences("AppSettingsPrefs", Context.MODE_PRIVATE).getBoolean("SoundEnabled", true)
    }

    private fun initializeSensor() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        if (sensorManager != null) {
            val sensorEnabled = getSharedPreferences("pixeliesPrefs", Context.MODE_PRIVATE).getBoolean("sensorEnabled", true)
            if (sensorEnabled) {
                stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
                if (stepSensor != null) {
                    sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
                    Toast.makeText(this, "Step sensor enabled.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No step counter sensor available.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Step sensor is disabled in settings.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "SensorManager not available.", Toast.LENGTH_SHORT).show()
        }
    }




    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ACTIVITY_RECOGNITION_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    initializeSensor()
                } else {
                    Toast.makeText(this, "Permission denied. Can't track steps.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkThirstWarning() {
        //  val currentTime = System.currentTimeMillis()
        // if (thirstLevel == 1 && (currentTime - thirstZeroTimestamp >= 2 * 60 * 60 * 1000)) {
        // Show warning if the pet has been thirsty for more than 2 hours- come back to this
        //  AlertDialog.Builder(this)
        //    .setTitle("Thirst Warning")
        //  .setMessage("Uh oh, ${sharedPreferences.getString("petName", "your pet")} is feeling rather parched. Please give them some water before they turn into a raisin.")
        //.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        //.show()
        // }
        when (thirstLevel) {
            1 -> showThirstWarning("dehydrated")
            2 -> showThirstWarning("very thirsty")
            3 -> showThirstWarning("parched")
            4 -> showThirstWarning("thirsty")
            5 -> showThirstWarning("hydrated")
        }



    }
    override fun onQuestStarted(quest: Quest) {
        val selectedPetIndex = when (selectedPet) {
            "character1" -> "1"
            "character2" -> "2"
            "character3" -> "3"
            else -> "1"
        }


        runningWithSword = true
        currentQuest = quest
        stopBlinking()
        updateIdleState()

        updateRunningAnimationFrames(derivePetIndex())
        updateQuestUI(quest, "currentDifficulty")
    }
    override fun onDestroy() {
        super.onDestroy()
        feedSound.release()
        drinkSound.release()
        playSound.release()
        backgroundMusic?.release()
        headPatSound.release()
        questAcceptedSound.release()
        questCompletedSound.release()
        questFailedSound.release()
        questQuitSound.release()
        stepSound.release()
        backgroundMusic?.release()
        backgroundMusic = null

    }

    override fun onQuestCompleted(quest: Quest, reward: Reward) {
        // if(reward.moodBoost == -1){
        //moodLevel = min(moodLevel - 1, 5)}
        val selectedPetIndex = derivePetIndex()
        runningWithSword = false
        updatePetAnimationToNormal(selectedPetIndex)

        moodLevel = min(moodLevel + reward.moodBoost, 5)
        caloriesBurnedToday += reward.caloriesBurned
        updateStatsTextViews()

        updateHealthBars()
        currentQuest = null
        runningWithSword = false
        updateIdleState()
        showQuestCompletionDialog(quest, reward)


    }

    fun derivePetIndex(): String {
        return when (selectedPet) {
            "character1" -> "1"
            "character2" -> "2"
            "character3" -> "3"
            else -> "1"
        }
    }

    override fun onQuestFailed(quest: Quest, message: String) {
        //   Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        val currentDifficulty = sharedPreferences.getString("activityLevel", "Easy") ?: "Easy"
        if (moodLevel > 1) {
            moodLevel -= 1
        }
        saveStats()
        updateHealthBars()

        if (message == "quit"){
            val selectedPetIndex = derivePetIndex()
            runningWithSword = false
            updatePetAnimationToNormal(selectedPetIndex)

            currentQuest = null
            loadStats()
            updateHealthBars()

            updateQuestUI(null, currentDifficulty)

            updateIdleState()
            if (isSoundEnabled()) {
                backgroundMusic?.stop()
                questQuitSound.start()
            }
        } else if (message == "fail"){
            if (isSoundEnabled()) {
                backgroundMusic?.pause()
                questFailedSound.start()
                backgroundMusic?.start()
            }
        }
    }



    class DailyResetReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val sharedPreferences = context?.getSharedPreferences("pixeliesPrefs", MODE_PRIVATE)
            sharedPreferences?.edit()?.apply {
                putInt("caloriesConsumedToday", 0)
                putInt("waterConsumedToday", 0)
                apply()
            }
        }
    }

}