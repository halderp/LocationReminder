package com.phalder.locationreminder.locationreminders

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.phalder.locationreminder.R
import com.phalder.locationreminder.base.BaseFragment
import com.phalder.locationreminder.base.NavigationCommand
import com.phalder.locationreminder.data.ReminderDTO
import com.phalder.locationreminder.databinding.FragmentSaveReminderBinding
import com.phalder.locationreminder.locationreminders.geofence.GeofenceBroadcastReceiver
import com.phalder.locationreminder.locationreminders.geofence.GeofenceErrorMessages
import com.phalder.locationreminder.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

private const val GEOFENCE_RADIUS_IN_METERS = 1000
class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    lateinit var geofencingClient: GeofencingClient

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //Navigate to Map fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            // Add the reminder to Database and Add Geofence for it as well
            val geofencingClient = LocationServices.getGeofencingClient(requireActivity())
            val reminder = ReminderDTO(title, description, location, latitude, longitude)
            //_viewModel.validateAndSaveReminder(ReminderDataItem(title, description, location, latitude, longitude))
            add(requireContext(),  reminder, geofencingClient)
        }
    }

    private fun add(context: Context, reminder: ReminderDTO, geofencingClient: GeofencingClient?) {
        val geofence = buildGeofence(reminder)
        if (geofence != null && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            geofencingClient?.addGeofences(buildGeofencingRequest(geofence), geofencePendingIntent)
                ?.addOnSuccessListener {
                    _viewModel.validateAndSaveReminder(ReminderDataItem(
                        reminder.title,
                        reminder.description,
                        reminder.location,
                        reminder.latitude,
                        reminder.longitude,
                        reminder.id))
                }?.addOnFailureListener {
                GeofenceErrorMessages.getErrorString(context, it)
            }
        }
    }
    private fun buildGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(0)
            .addGeofences(listOf(geofence))
            .build()
    }

    private fun buildGeofence(reminder: ReminderDTO): Geofence? {
        val latitude = reminder.latitude
        val longitude = reminder.longitude
        val radius =  GEOFENCE_RADIUS_IN_METERS
        if ((latitude != null) && (longitude != null) && (radius != null)) {
            return Geofence.Builder()
                .setRequestId(reminder.id)
                .setCircularRegion(latitude, longitude, radius.toFloat())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build()
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
