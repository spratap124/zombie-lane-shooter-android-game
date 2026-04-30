package com.zombielane.shooter.update

import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * [Google Play in-app updates](https://developer.android.com/guide/playcore/in-app-updates):
 * when a newer version is on Play, shows the official Play update UI.
 *
 * Uses **flexible** updates so the player can dismiss and keep playing. The Play update
 * sheet is offered again on every activity resume while Play still reports an update, until
 * the user installs the new version (only a short debounce avoids double-start if
 * [onResume] fires twice in quick succession).
 *
 * After the download completes, an [AlertDialog] offers restart via [completeUpdate]; choosing
 * "Later" clears the dialog so it can appear again on the next open.
 *
 * Works only for builds installed through **Google Play** (internal/open/production).
 * Sideloaded APKs / emulators without Play will no-op.
 */
class PlayInAppUpdateCoordinator(
    private val activity: ComponentActivity,
) {

    private val appUpdateManager = AppUpdateManagerFactory.create(activity)

    private val restartDialogLock = Any()
    private var restartDialogShown = false

    /** Last time we launched the Play update sheet for [UPDATE_AVAILABLE]; avoids duplicate calls only. */
    private var lastPlaySheetAttemptMs = 0L

    private val updateFlowLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { }

    private val installListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showUpdateReadyDialog()
        }
    }

    init {
        appUpdateManager.registerListener(installListener)
    }

    fun onResume() {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    showUpdateReadyDialog()
                    return@addOnSuccessListener
                }

                if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    if (info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        try {
                            appUpdateManager.startUpdateFlowForResult(
                                info,
                                updateFlowLauncher,
                                AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                            )
                        } catch (_: Exception) {
                        }
                    }
                    return@addOnSuccessListener
                }

                if (info.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE) {
                    return@addOnSuccessListener
                }
                if (!info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    return@addOnSuccessListener
                }

                val days = info.clientVersionStalenessDays()
                if (STALE_DAYS_BEFORE_PROMPT > 0 &&
                    days != null &&
                    days < STALE_DAYS_BEFORE_PROMPT
                ) {
                    return@addOnSuccessListener
                }

                val now = System.currentTimeMillis()
                if (now - lastPlaySheetAttemptMs < PLAY_SHEET_DEBOUNCE_MS) {
                    return@addOnSuccessListener
                }
                lastPlaySheetAttemptMs = now

                try {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        updateFlowLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                    )
                } catch (_: Exception) {
                }
            }
            .addOnFailureListener { }
    }

    private fun showUpdateReadyDialog() {
        synchronized(restartDialogLock) {
            if (restartDialogShown || activity.isFinishing || activity.isDestroyed) return
            restartDialogShown = true
        }
        AlertDialog.Builder(activity)
            .setTitle("Update ready")
            .setMessage("A new version has been downloaded. Restart now to finish installing.")
            .setPositiveButton("Restart") { _, _ ->
                try {
                    appUpdateManager.completeUpdate()
                } catch (_: Exception) {
                }
            }
            .setNegativeButton("Later") { _, _ ->
                synchronized(restartDialogLock) { restartDialogShown = false }
            }
            .setOnCancelListener {
                synchronized(restartDialogLock) { restartDialogShown = false }
            }
            .show()
    }

    fun onDestroy() {
        appUpdateManager.unregisterListener(installListener)
    }

    companion object {
        /**
         * Require this many days behind Play before showing the update sheet (0 = as soon as
         * Play reports an update).
         */
        private const val STALE_DAYS_BEFORE_PROMPT = 0
        /** Ignore duplicate [onResume] pairs within this window (same session only). */
        private const val PLAY_SHEET_DEBOUNCE_MS = 1_200L
    }
}
