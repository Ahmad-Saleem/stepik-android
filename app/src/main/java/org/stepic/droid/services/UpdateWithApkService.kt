package org.stepic.droid.services

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.net.Uri
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.util.FileUtil
import org.stepic.droid.util.SuppressFBWarnings
import java.io.File
import javax.inject.Inject

class UpdateWithApkService : IntentService("update_with_apk") {
    companion object {
        val linkKey = "LINK_KEY"
    }

    @Inject
    lateinit var userPrefs: UserPreferences

    @Inject
    lateinit var analytic: Analytic

    override fun onHandleIntent(intent: Intent?) {
        try {
            val linkFromServer = intent?.getStringExtra(linkKey)
            updateFromRemoteApk(path = linkFromServer!!)
        } catch (e: Exception) {
            analytic.reportError(Analytic.Error.UPDATE_FROM_APK_FAILED, e)
        }

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        App.component().inject(this)
        return Service.START_REDELIVER_INTENT
    }

    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "false positive")
    private fun updateFromRemoteApk(path: String) {
        val filename = "updating.apk"
        FileUtil.saveFileToDisk(filename, path, userPrefs.userDownloadFolder)

        val intent = Intent(Intent.ACTION_VIEW)
        val result = "${userPrefs.userDownloadFolder.path}/$filename";
        intent.setDataAndType(Uri.fromFile(File(result)), "application/vnd.android.package-archive")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK // without this flag android returned a intent error!
        App.getAppContext().startActivity(intent)
    }
}
