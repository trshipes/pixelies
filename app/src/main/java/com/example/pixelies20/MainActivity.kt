package com.example.pixelies20

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer

import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class MainActivity : AppCompatActivity() {
    private lateinit var demoModeToggleReceiver: BroadcastReceiver

    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var startButton: Button
    private lateinit var settingsButton: Button
   // private lateinit var prefs: SharedPreferences
    private lateinit var demoModeButton: Button
    private lateinit var demoStatusLabel: TextView

    private lateinit var prefs: SharedPreferences
   // private var selectedPet: String? = null
    private var isDemoMode: Boolean = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = getSharedPreferences("pixeliesPrefs", MODE_PRIVATE)
        //Log.d("MainActivity", "Selected pet: ${prefs.getString("selectedPet", "default")}")
       // prefs.edit().putBoolean("isDemoMode", false).apply()
        demoModeButton = findViewById(R.id.demoModeButton)
        demoStatusLabel = findViewById(R.id.demoStatusLabel)
        isDemoMode = prefs.getBoolean("isDemoMode", false) //default should be false
       // val calorieGoal = prefs.getInt("calorieGoal", -1)
       // val waterGoal = prefs.getInt("waterGoal", -1)
        //val caloriesConsumedToday = prefs.getInt("caloriesConsumedToday", -1)
       // val waterConsumedToday = prefs.getInt("waterConsumedToday", -1)
       // val hungerLevel = prefs.getInt("hungerLevel", -1)
       // val thirstLevel = prefs.getInt("thirstLevel", -1)
       // val moodLevel = prefs.getInt("moodLevel", -1)
        //Log.d("MainActivity", "Stats on launch: calorieGoal=" + calorieGoal + ", waterGoal=" + waterGoal
                //+ ", caloriesConsumedToday=" + caloriesConsumedToday + ", waterConsumedToday=" + waterConsumedToday
            //    + ", hungerLevel=" + hungerLevel + ", thirstLevel=" + thirstLevel + ", moodLevel=" + moodLevel);

        titleTextView = findViewById(R.id.titleTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        startButton = findViewById(R.id.startButton)
        settingsButton = findViewById(R.id.settingsButton)
        prefs = getSharedPreferences("pixeliesPrefs", Context.MODE_PRIVATE)
        descriptionTextView.text = getString(R.string.welcome_message)
        demoStatusLabel.text = "Demo on"
        demoStatusLabel.visibility = View.VISIBLE
        updateDemoModeUI()  // Update UI based on the demo mode on/off

       // val isFirstLaunch = prefs.getBoolean("firstLaunch", true)
        //val selectedPet = prefs.getString("selectedPet", null)

      //  if (!isFirstLaunch && selectedPet != null) {
            // Not first launch + a pet is selected, go to PlayRoomActivity
      //      val intent = Intent(this, PlayRoomActivity::class.java)
         //   startActivity(intent)
       //     finish()
      //  } else {
            // First launch or no pet selected, show welcome message
      //  }
        demoModeButton.setOnClickListener {
            toggleDemoMode()
            playButtonToggleSound()
        }

       // val bodyClicks = prefs.getInt("bodyClicks", 0)
   //     if (bodyClicks >= 5) {
    //        demoModeButton.visibility = View.VISIBLE
    //    } else {
    //        demoModeButton.visibility = View.GONE
    //    }

        demoModeToggleReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    "ACTION_TOGGLE_DEMO_MODE_BUTTON" -> {
                        demoModeButton.visibility = if (demoModeButton.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                    }
                    "ACTION_DISABLE_DEMO_MODE" -> {
                        // check demo mode is disabled
                        if (isDemoMode) {
                            toggleDemoMode()
                        }
                    }
                }
            }
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(demoModeToggleReceiver, IntentFilter().apply {
            addAction("ACTION_TOGGLE_DEMO_MODE_BUTTON")
            addAction("ACTION_DISABLE_DEMO_MODE")
        })
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            val currentSelectedPet = prefs.getString("selectedPet", "character1")
            intent.putExtra("selectedPet", currentSelectedPet)
            startActivity(intent)
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
            playButtonClickSound()
        }
        startButton.setOnClickListener {
            val intent = if (isFirstLaunch() || prefs.getString("selectedPet", null) == null) {
                prefs.edit().clear().apply()
                Intent(this, MakeaPetActivity::class.java)
            } else {
                Intent(this, PlayRoomActivity::class.java).apply {
                    putExtra("selectedPet", prefs.getString("selectedPet", "character1"))
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            }
            intent.putExtra("isDemoMode", prefs.getBoolean("isDemoMode", false))
            startActivity(intent)
            playButtonBarkSound()

        }

    }
    private fun updateDemoModeUI() {
        demoStatusLabel.text = if (isDemoMode) "Demo Mode ON" else "Demo Mode OFF"
        demoStatusLabel.visibility = if (isDemoMode) View.VISIBLE else View.GONE
    }
    private fun playButtonClickSound() {
        if (isSoundEnabled()) {
            val mediaPlayer = MediaPlayer.create(this, R.raw.click)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { mp ->
                mp.release()
            }
        }
    }
    private fun playButtonToggleSound() {
        if (isSoundEnabled()) {
            val mediaPlayer = MediaPlayer.create(this, R.raw.toggle)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { mp ->
                mp.release()
            }
        }
    }
    private fun playButtonBarkSound() {
        if (isSoundEnabled()) {
            val mediaPlayer = MediaPlayer.create(this, R.raw.bark)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { mp ->
                mp.release()
            }
        }
    }
    private fun isSoundEnabled(): Boolean {
        return getSharedPreferences("AppSettingsPrefs", Context.MODE_PRIVATE).getBoolean("SoundEnabled", true)
    }
    private fun toggleDemoMode() {
        isDemoMode = !isDemoMode
        prefs.edit().putBoolean("isDemoMode", isDemoMode).apply()

        updateDemoModeUI()

        if (isDemoMode) {
            Toast.makeText(this, "Demo mode enabled", Toast.LENGTH_SHORT).show()
            AlertDialog.Builder(this)
                .setTitle("Demo Mode")
                .setMessage("Do you want to reset for a first launch?")
                .setPositiveButton("Yes") { dialog, _ ->
                    prefs.edit()
                        .putBoolean("firstLaunch", true)
                        .apply()
                    Toast.makeText(this, "Demo mode enabled with first launch", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            Toast.makeText(this, "Demo mode disabled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isFirstLaunch(): Boolean {
        return prefs.getBoolean("firstLaunch", true)
    }
    override fun onResume() {
        super.onResume()
      //  isDemoMode = prefs.getBoolean("isDemoMode", false)
        isDemoMode = prefs.getBoolean("isDemoMode", false)
        updateDemoModeUI()
    }
    override fun onDestroy() {
        super.onDestroy()
        // need to unregister receiver to prevent leaks
        LocalBroadcastManager.getInstance(this).unregisterReceiver(demoModeToggleReceiver)
    }

}
class RetroDogView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val dogBodyPaint = Paint().apply {
        color = Color.parseColor("#8B4513")
        style = Paint.Style.FILL
    }

    private val dogDetailsPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private val eyeWhitePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val eyePupilPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private var tailRotationAngle = 0f
//    private var tailRotationDirection = 1 // 1 for clockwise, -1 for counterclockwise
    private var isBlinking = false

    init {
        startBlinkAnimation()
        startTailWagAnimation()
    }

    private val scaleFactor = 2f

    init {
        setOnClickListener {
            val prefs = context.getSharedPreferences("pixeliesPrefs", Context.MODE_PRIVATE)
            val clicks = prefs.getInt("bodyClicks", 0) + 1
            prefs.edit().putInt("bodyClicks", clicks).apply()

            if (clicks % 5 == 0) {
                LocalBroadcastManager.getInstance(context).sendBroadcast(Intent("ACTION_TOGGLE_DEMO_MODE_BUTTON"))

                if (clicks % 10 == 0) {
                    LocalBroadcastManager.getInstance(context).sendBroadcast(Intent("ACTION_DISABLE_DEMO_MODE"))
                }
            }
        }
    }



    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)

        // Calculate canvas dimensions but I hate this and this will be trashed because its UGLY.
        val canvasWidth = width.toFloat()
        val canvasHeight = height.toFloat()

        // Draw body
        val dogWidth = 150f * scaleFactor
        val dogHeight = 150f * scaleFactor
        val dogBodyTop =
            canvasHeight - dogHeight


        drawPixelatedRect(
            canvas,
            (canvasWidth - dogWidth) / 2,
            dogBodyTop,
            (canvasWidth + dogWidth) / 2,
            canvasHeight,
            dogBodyPaint
        )

        drawPixelatedCircle(
            canvas,
            (canvasWidth / 2),
            (dogBodyTop - 50f * scaleFactor),
            50f * scaleFactor,
            dogBodyPaint
        )

        drawEar(
            canvas,
            (canvasWidth / 2 - 50f * scaleFactor),
            (dogBodyTop - 100f * scaleFactor),
            100f * scaleFactor,
            20f * scaleFactor
        )
        drawEar(
            canvas,
            (canvasWidth / 2 + 50f * scaleFactor),
            (dogBodyTop - 100f * scaleFactor),
            200f * scaleFactor,
            20f * scaleFactor
        )

        drawEyes(
            canvas,
            (canvasWidth / 2 - 20f * scaleFactor),
            (dogBodyTop - 70f * scaleFactor),
            4f * scaleFactor
        )
        drawEyes(
            canvas,
            (canvasWidth / 2 + 20f * scaleFactor),
            (dogBodyTop - 70f * scaleFactor),
            4f * scaleFactor
        )


        canvas.drawCircle(
            canvasWidth / 2,
            (dogBodyTop - 30f * scaleFactor),
            7f * scaleFactor,
            dogDetailsPaint
        )
        drawTail(canvas)

        //  drawLegs(canvas, dogBodyTop)
    }


    private fun drawPixelatedRect(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        paint: Paint
    ) {
        val rect = RectF(left, top, right, bottom)
        canvas.drawRect(rect, paint)
    }
    // private fun drawLegs(canvas: Canvas, dogBodyTop: Float) {
    //val canvasWidth = canvas.width.toFloat()

    //val legStartY = dogBodyTop - 50f * scaleFactor
    //val legEndY = dogBodyTop + 50f * scaleFactor

    // Front legs
    // val frontLeg1StartX = (canvasWidth / 2) - 75f * scaleFactor
    //val frontLeg1EndX = (canvasWidth / 2) - 75f * scaleFactor
    //canvas.drawLine(frontLeg1StartX, legStartY, frontLeg1EndX, legEndY, dogDetailsPaint)

    //val frontLeg2StartX = (canvasWidth / 2) + 75f * scaleFactor
    //val frontLeg2EndX = (canvasWidth / 2) + 75f * scaleFactor
    //canvas.drawLine(frontLeg2StartX, legStartY, frontLeg2EndX, legEndY, dogDetailsPaint)

    // Back legs
    //val backLeg1StartX = (canvasWidth / 2) - 50f * scaleFactor
    //val backLeg1EndX = (canvasWidth / 2) - 50f * scaleFactor
    //canvas.drawLine(backLeg1StartX, legStartY, backLeg1EndX, legEndY - 50f * scaleFactor, dogDetailsPaint)

    //val backLeg2StartX = (canvasWidth / 2) + 50f * scaleFactor
    //val backLeg2EndX = (canvasWidth / 2) + 50f * scaleFactor
    //  canvas.drawLine(backLeg2StartX, legStartY, backLeg2EndX, legEndY - 50f * scaleFactor, dogDetailsPaint)
    //}


    private fun drawPixelatedCircle(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        radius: Float,
        paint: Paint
    ) {
        canvas.drawCircle(centerX, centerY, radius, paint)
    }

    private fun drawEar(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        width: Float,
        height: Float
    ) {
        val path = Path().apply {
            moveTo(centerX, centerY - height / 2)
            lineTo(centerX - width / 2, centerY + height / 2)
            lineTo(centerX + width / 2, centerY + height / 2)
            close()
        }
        canvas.drawPath(path, dogBodyPaint)
    }

    private fun drawEyes(canvas: Canvas, centerX: Float, centerY: Float, eyeRadius: Float) {
        val pupilRadius = eyeRadius / 2

        canvas.drawCircle(centerX, centerY, eyeRadius * 2, eyeWhitePaint) // 4x size
        canvas.drawCircle(centerX, centerY, pupilRadius * 2, eyePupilPaint) // 4x size
    }


    private fun drawTail(canvas: Canvas) {
        val canvasWidth = canvas.width.toFloat()
        val canvasHeight = canvas.height.toFloat()

        val tailWidth = 100f * scaleFactor
        val tailHeight = 30f * scaleFactor

        val tailStartX = -30 + (canvasWidth - tailWidth) / 3
        val tailEndX = -30 + (canvasWidth + tailWidth) / 3

        // Note to self- I'll never remember this -Google quadratic bezier curve for crescent shape
        val controlPoint1X = (tailStartX + tailEndX) / 2 + 50f * scaleFactor
        val controlPoint1Y = canvasHeight - tailHeight
        //val controlPoint2X = (tailStartX + tailEndX) / 2 - 50f * scaleFactor
        //    val controlPoint2Y = tailY - tailHeight

        val path = Path()
        path.moveTo(tailStartX, canvasHeight)
        path.quadTo(controlPoint1X, controlPoint1Y, tailEndX, canvasHeight)

        // val tailRotationPivotX = (tailStartX + tailEndX) / 2
        //   val tailRotationPivotY = tailY
        canvas.rotate(tailRotationAngle, tailEndX, canvasHeight)
        canvas.drawPath(path, dogDetailsPaint)
    }


    private fun startBlinkAnimation() {
        val blinkDuration = 300L
        val blinkAnimator = ObjectAnimator.ofInt(this, "blinkRadius", 0, 1).apply {
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            duration = blinkDuration
            doOnEnd {
                isBlinking = false
            }
        }
        blinkAnimator.start()
    }

    private fun startTailWagAnimation() {
        val animator = ValueAnimator.ofFloat(0f, 45f, 0f, -45f, 0f)
        animator.addUpdateListener { animation ->
            tailRotationAngle = animation.animatedValue as Float
            invalidate()
        }
        animator.interpolator = LinearInterpolator()
        animator.duration = 1000 // 1 second for each cycle
        animator.repeatCount = ValueAnimator.INFINITE
        animator.start()
    }



    @Suppress("unused")
    fun setBlinkRadius(blinkRadius: Int) {
        if (!isBlinking) {
            eyeWhitePaint.alpha = 255 - (blinkRadius * 255)
            invalidate()
        }
    }


}