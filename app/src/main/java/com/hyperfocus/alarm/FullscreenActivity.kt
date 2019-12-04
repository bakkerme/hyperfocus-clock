package com.hyperfocus.alarm

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextClock
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_fullscreen.*
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

class FullscreenActivity : AppCompatActivity(), SensorEventListener {
    private val TAG = "Hyperfocus"

    private lateinit var sensorManager: SensorManager
    private var light: Sensor? = null

    private val MIN_LIGHT = 10;
    private val MAX_LIGHT = 640;

    private val MIN_BRIGHTNESS = 0
    private val MAX_BRIGHTNESS = 1


    private var lastBrightnessDecision = 1f
    private var lastBrightnessTimestamp = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen)
        fullscreen_content.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        fullscreen_content.setKeepScreenOn(true)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if( light != null ) {
            Log.i(TAG,"Got Light Sensor")
        } else {
            Log.i(TAG,"No Light Sensor???")
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)
        fullscreen_content.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.i(TAG,"Accuracy changed")
        // Do something here if sensor accuracy changes.
    }

    override fun onSensorChanged(event: SensorEvent) {
        val lux = event.values[0]
        setBrightness(lux)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun getScreeBrightnessValueForLux(lux: Float): Float {
        // Map light input to screen brightness
        // Y = (X-A)/(B-A) * (D-C) + C
        val brightness = (lux - MIN_LIGHT)/(MAX_LIGHT-MIN_LIGHT) * (MAX_BRIGHTNESS-MIN_BRIGHTNESS) + MIN_BRIGHTNESS;

        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING

        return df.format(brightness).toFloat()
    }

    private fun setBrightness(lux: Float) {
        Log.i(TAG, lux.toString())
        lightLevel.text = lux.toString()

        val screenBrightness = getScreeBrightnessValueForLux(lux)
        lastBrightnessDecision = screenBrightness

        Log.i(TAG, screenBrightness.toString())

        Handler().postDelayed({
            if(screenBrightness - lastBrightnessDecision <= 0.1) {
                changeScreenBrightness(lastBrightnessDecision)
            }

            if(screenBrightness < 0.01) {
                if(lastBrightnessDecision < 0.01) {
                    changeToLowBrightnessMode()
                }

            } else {
                if(lastBrightnessDecision >= 0.01) {
                    changeToStandardBrightnessMode()
                }
            }
        }, 5000)
    }

    private fun changeScreenBrightness(brightness: Float) {
        val lp = this.window.attributes
        lp.screenBrightness = brightness
        this.window.attributes = lp
    }

    private fun changeToLowBrightnessMode() {
        val imageView = findViewById<FrameLayout>(R.id.fullscreen_content) as FrameLayout
        imageView.setBackgroundColor(Color.BLACK)

        val textColor = ContextCompat.getColor(getApplicationContext(),R.color.textDarkMode)
        val clockText = findViewById<TextClock>(R.id.textClock) as TextClock
        clockText.setTextColor(textColor)
    }

    private fun changeToStandardBrightnessMode() {
        val bgColor = ContextCompat.getColor(getApplicationContext(),R.color.colorBG)

        val imageView = findViewById<FrameLayout>(R.id.fullscreen_content) as FrameLayout
        imageView.setBackgroundColor(bgColor)


        val clockText = findViewById<TextClock>(R.id.textClock) as TextClock
        clockText.setTextColor(Color.WHITE)
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }
}
