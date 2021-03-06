package org.stepic.droid.notifications

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.Api
import javax.inject.Inject

class StepicInstanceIdService : FirebaseInstanceIdService() {
    val hacker = HackerFcmInstanceId()

    override fun onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        updateAnywhere(hacker.api, hacker.sharedPreferences, hacker.analytic)
    }

    companion object {
        fun updateAnywhere(api: Api, sharedPreferences: SharedPreferenceHelper, analytic: Analytic) {
            val tokenNullable : String? = FirebaseInstanceId.getInstance().token
            try {
                sharedPreferences.authResponseFromStore!! //for logged user only work
                val token = tokenNullable!!
                val response = api.registerDevice(token).execute()
                if (!response.isSuccessful && response.code() != 400) { //400 -- device already registered
                    throw Exception("response was failed. it is ok. code: " + response.code())
                }
                sharedPreferences.setIsGcmTokenOk(true)

                analytic.reportEvent(Analytic.Notification.TOKEN_UPDATED)
            } catch (e: Exception) {
                analytic.reportEvent(Analytic.Notification.TOKEN_UPDATE_FAILED)
                sharedPreferences.setIsGcmTokenOk(false)
            }
        }
    }

}

class HackerFcmInstanceId() {
    @Inject
    lateinit var sharedPreferences: SharedPreferenceHelper

    @Inject
    lateinit var api: Api

    @Inject
    lateinit var analytic : Analytic


    init {
        App.component().inject(this)
    }
}