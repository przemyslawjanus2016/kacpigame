package pl.kacperikapi.mathadventure

import android.app.Application
import android.os.Process
import android.util.Log
import kotlin.system.exitProcess

class KacperKapiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                getSharedPreferences(CRASH_PREFS, MODE_PRIVATE)
                    .edit()
                    .putString(LAST_CRASH, Log.getStackTraceString(throwable).take(24000))
                    .putInt(LAST_CRASH_VERSION, BuildConfig.VERSION_CODE)
                    .commit()
            }
            if (previous != null) {
                previous.uncaughtException(thread, throwable)
            } else {
                Process.killProcess(Process.myPid())
                exitProcess(10)
            }
        }
    }

    companion object {
        const val CRASH_PREFS = "kacper_kapi_crash"
        const val LAST_CRASH = "last_crash"
        const val LAST_CRASH_VERSION = "last_crash_version"
    }
}
