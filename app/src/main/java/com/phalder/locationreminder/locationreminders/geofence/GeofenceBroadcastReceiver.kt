package com.phalder.locationreminder.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        //TODO: implement the onReceive method to receive the geofencing events at the background
        GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
    }
}