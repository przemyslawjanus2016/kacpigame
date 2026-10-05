package pl.janusdigital.kacperikapi.platformer

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private val crashPrefsName = "platformer_crash"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(crashPrefsName, MODE_PRIVATE)
        val previousCrash = prefs.getString("last_crash", null)
        if (!previousCrash.isNullOrBlank()) {
            showCrashScreen(previousCrash)
            return
        }

        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                prefs.edit()
                    .putString("last_crash", throwable.stackTraceToString())
                    .apply()
            }
            oldHandler?.uncaughtException(thread, throwable)
        }

        try {
            setContentView(PlatformerView(this))
        } catch (t: Throwable) {
            prefs.edit().putString("last_crash", t.stackTraceToString()).apply()
            showCrashScreen(t.stackTraceToString())
        }
    }

    private fun showCrashScreen(stack: String) {
        val density = resources.displayMetrics.density
        val pad = (18 * density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            setBackgroundColor(Color.rgb(245, 248, 252))
        }

        root.addView(TextView(this).apply {
            text = "Kacper & Kapi – diagnostyka"
            textSize = 24f
            setTextColor(Color.rgb(20, 35, 55))
        })

        root.addView(TextView(this).apply {
            text = "Gra wykryła błąd z poprzedniego uruchomienia. Zrób zdjęcie tego ekranu albo wyślij mi widoczny komunikat.\n\n$stack"
            textSize = 12f
            setTextColor(Color.rgb(40, 50, 65))
            setTextIsSelectable(true)
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ))

        root.addView(Button(this).apply {
            text = "Wyczyść błąd i uruchom ponownie"
            setOnClickListener {
                getSharedPreferences(crashPrefsName, MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()
                recreate()
            }
        })

        setContentView(root)
    }
}
