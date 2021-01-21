package com.phalder.locationreminder.locationreminders

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.phalder.locationreminder.R
import com.phalder.locationreminder.base.BaseFragment
import com.phalder.locationreminder.base.NavigationCommand
import com.phalder.locationreminder.databinding.FragmentSelectLocationBinding
import com.phalder.locationreminder.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private val REQUEST_LOCATION_PERMISSION = 1
    private val TAG = SelectLocationFragment::class.java.simpleName

    // Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private var marker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.save.setOnClickListener {
            if (marker == null) {
                _viewModel.showSnackBar.postValue("Select a Location for Reminder")
            } else {
                _viewModel.navigationCommand.postValue(NavigationCommand.Back)
            }
        }

        return binding.root
    }

    private fun onLocationSelected(poiMarker: Marker) {
        // When the user selects a particular location,
        // this method will be called to update ViewModel data
        // Final navigation to the previous fragment will happen when user clicks save button
        _viewModel.latitude.postValue(poiMarker.position.latitude)
        _viewModel.longitude.postValue(poiMarker.position.longitude)
        _viewModel.reminderSelectedLocationStr.postValue(poiMarker.title)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    // Used to change the map type and its style rendered by the Google Map
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_map -> {
                map.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }

            R.id.hybrid_map -> {
                map.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }

            R.id.satellite_map -> {
                map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }

            R.id.terrain_map -> {
                map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Default location when MAP is launched
        val latitude = 37.820004
        val longitude = -122.478185

        val homeLatLong = LatLng(latitude, longitude)
        val zoomLevel = 15f

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLong, zoomLevel))
        map.addMarker(MarkerOptions().position(homeLatLong))

        setMapStyle(map)
        setMapLongClick(map)
        setPoiClick(map)
        enableMyLocation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    // Set a marker in the google map
    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            marker?.remove()
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )            // Used to add the marker to the map on long click.
            marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .snippet(snippet)
                    .title("Selected Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            marker?.let {
                onLocationSelected(it)
            }
            marker?.showInfoWindow()
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            marker?.remove()
            marker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
            )
            _viewModel.selectedPOI.postValue(poi)
            marker?.let {
                onLocationSelected(it)
            }
            marker?.showInfoWindow()
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Map style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "can't find style error", e)
        }
    }

    // Gets the user's permission to use location services
    private fun isLocationPermissionsGranted(): Boolean {
        return ((ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED))
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isLocationPermissionsGranted()) {
            map.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    // Move camera to the users position
    private fun moveCameraToUsersLocation() {
    }
}