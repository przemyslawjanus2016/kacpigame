package pl.kacperikapi.mathadventure.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability

object PlayUpdateChecker {
    sealed interface Status {
        data object Checking : Status
        data class Available(val versionCode: Int) : Status
        data object UpToDate : Status
        data object Unavailable : Status
    }

    fun check(context: Context, onResult: (Status) -> Unit) {
        onResult(Status.Checking)
        AppUpdateManagerFactory.create(context.applicationContext)
            .appUpdateInfo
            .addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    onResult(Status.Available(info.availableVersionCode()))
                } else {
                    onResult(Status.UpToDate)
                }
            }
            .addOnFailureListener {
                onResult(Status.Unavailable)
            }
    }

    fun openPlayStore(context: Context) {
        val packageName = context.packageName
        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageName")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        runCatching { context.startActivity(marketIntent) }
            .recoverCatching { context.startActivity(webIntent) }
    }
}
