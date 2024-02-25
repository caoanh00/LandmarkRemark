package com.example.landmarkremark.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.landmarkremark.R
import com.example.landmarkremark.databinding.FragmentMapBinding
import com.example.landmarkremark.models.LocationData
import com.example.landmarkremark.ui.add.AddLocationNoteActivity
import com.example.landmarkremark.ui.main.MainActivity
import com.example.landmarkremark.ui.main.MainActivity.Companion.ADD_LOCATION_NOTE_REQUEST_CODE
import com.example.landmarkremark.ui.main.MainViewModel
import com.example.landmarkremark.utilities.SharedPreferenceUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class MapFragment : Fragment(), LocationListener {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val MIN_TIME_FOR_UPDATE: Long = 5000
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATE: Float = 0f
    }

    private lateinit var map: GoogleMap
    private lateinit var geocoder: Geocoder
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var marker: Marker? = null

    private lateinit var mainViewModel: MainViewModel

    private var isGpsEnabled: Boolean? = false
    private var isNetworkEnabled: Boolean? = false
    private lateinit var locationManager: LocationManager

    private var haveAskedPermission = false
    private var haveSetUI = false

    private var myLocation: LatLng? = null
    private lateinit var binding: FragmentMapBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activity = context as MainActivity
        mainViewModel = ViewModelProvider(activity).get(MainViewModel::class.java)
    }

    override fun onStop() {
        super.onStop()
        binding.locationMap.onStop()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var isTouchInfoFold = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        geocoder = Geocoder(requireContext(), Locale.getDefault())
        binding.locationMap.onCreate(savedInstanceState)

        mainViewModel.getLocations().observe(viewLifecycleOwner, Observer {
            if (::map.isInitialized) {
                updateMapWithNotes(it)
            } else {
                mainViewModel.getLocations()
                mainViewModel.getMyLocations()
            }
        })

        setLocationInfoUI()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showMapUI()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.locationMap.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        binding.locationMap.onResume()

        val context = this.context ?: return
        if (!haveSetUI) {
            haveSetUI = true
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || !haveAskedPermission) {
                    haveAskedPermission = true
                    showPermissionDialog(context)
                }
            } else {
                showMapUI()
            }
        }
    }

    private fun updateMapWithNotes(locations: List<LocationData>) {
        val accessToken = SharedPreferenceUtils.getUserId()
        if (::map.isInitialized) {
            for (data: LocationData in locations) {
                if (data.lat != null && data.lng != null) {
                    val markOptions = MarkerOptions()
                    if (data.creatorId == accessToken) {
                        markOptions.icon(
                            BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_VIOLET
                            )
                        )
                    } else {
                        markOptions.icon(
                            BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_YELLOW
                            )
                        )
                    }
                    markOptions.title(data.title!!).position(LatLng(data.lat, data.lng))
                    map.addMarker(markOptions)
                }
            }
        }
    }

    private fun showMapUI() {
        val context = this.context ?: return

        TransitionManager.beginDelayedTransition(binding.searchLocationContainer)
        binding.locationMap.visibility = VISIBLE

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        MapsInitializer.initialize(context)

        binding.locationMap.getMapAsync {
            map = it
            map.setOnMapClickListener { latLng ->
                myLocation = latLng
                showLocationInfo(null, true)
            }
            map.setOnMapLongClickListener { latLng ->
                updateLocation(latLng)
            }
            map.setOnMarkerClickListener { marker ->
                mainViewModel.getLocations().value?.firstOrNull() { locationData ->
                    locationData.title == marker.title
                }.let { data ->
                    showLocationInfo(data, showAdd = data == null)
                }
                false
            }
            map.setOnInfoWindowClickListener { marker ->
                mainViewModel.getLocations().value?.firstOrNull() { locationData ->
                    locationData.title == marker.title
                }.let { data ->
                    showLocationInfo(data, showAdd = data == null)
                }
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                // Location may be null
                try {
                    if (location == null) {
                        var lastLocation = location
                        when {
                            // Use GPS first if available
                            isGpsEnabled == true -> {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    if (::locationManager.isInitialized) {
                                        //requestLocationUpdates
                                        locationManager.requestLocationUpdates(
                                            LocationManager.GPS_PROVIDER,
                                            MIN_TIME_FOR_UPDATE,
                                            MIN_DISTANCE_CHANGE_FOR_UPDATE,
                                            this
                                        )
                                    }
                                    // use locationManager's last known location
                                    lastLocation = locationManager.getLastKnownLocation(
                                        LocationManager.GPS_PROVIDER
                                    )
                                    Timber.d("lastLocation from GPS: ${lastLocation?.latitude}, ${lastLocation?.longitude}")
                                }
                            }

                            isNetworkEnabled == true -> {
                                if (::locationManager.isInitialized) {
                                    //requestLocationUpdates
                                    locationManager.requestLocationUpdates(
                                        LocationManager.NETWORK_PROVIDER,
                                        MIN_TIME_FOR_UPDATE, MIN_DISTANCE_CHANGE_FOR_UPDATE, this
                                    )
                                }
                                // use locationManager's last known location
                                lastLocation =
                                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                                Timber.d("lastLocation from NETWORK: ${lastLocation?.latitude}, ${lastLocation?.longitude}")
                            }
                        }
                        if (lastLocation != null) {
                            updateLocation(LatLng(lastLocation.latitude, lastLocation.longitude))
                        } else {
                            updateLocation(LatLng(-37.81, 144.96))
                        }

                    } else {
                        Timber.d("lastLocation from fusedLocationClient: ${location?.latitude}, ${location?.longitude}")
                        updateLocation(LatLng(location.latitude, location.longitude))
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }


    private fun updateLocation(latLng: LatLng) {
        if (::map.isInitialized) {
            marker?.remove()
            marker =
                map.addMarker(
                    MarkerOptions().position(latLng).title(getString(R.string.title_new_location))
                )
            marker?.isVisible = true
            val zoom = if (map.cameraPosition.zoom > 10F) map.cameraPosition.zoom else 10F
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder().target(
                        latLng
                    ).zoom(zoom).tilt(30F).build()
                )
            )
        }
        myLocation = latLng
    }

    override fun onLocationChanged(location: Location) {
        updateLocation(LatLng(location.latitude, location.longitude))
    }

    private fun showPermissionDialog(context: Context) {
        val dialog = AlertDialog.Builder(context, R.style.AlertDialogTheme)
        dialog.setTitle(R.string.location_permission_alert_title)
        dialog.setMessage(R.string.location_permission_alert_message)
        dialog.setPositiveButton(R.string.common_confirm) { _, _ ->
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        dialog.setNegativeButton(R.string.common_cancel) { _, _ ->
            Toast.makeText(context, R.string.location_permission_alert_message, Toast.LENGTH_LONG)
                .show()
            showPermissionDialog(context)
        }
        dialog.show()
    }

    fun showLocationInfo(locationData: LocationData? = null, showAdd: Boolean? = false) {
        if (locationData == null) {
            binding.locationInfoTitle.text = getString(R.string.title_new_location)
            binding.locationInfoDescription.text = getString(R.string.msg_un_noted_location)
            binding.locationInfoCreatorName.visibility = GONE
            binding.locationInfoCreatedTime.visibility = GONE
            myLocation?.let {
                updateLocation(LatLng(it.latitude, it.longitude))
            }
        } else {
            updateLocation(LatLng(locationData.lat!!, locationData.lng!!))
            binding.locationInfoTitle.text = locationData.title
            binding.locationInfoDescription.text = locationData.description

            val creatorTxt = getString(R.string.creator_name_txt, locationData.email ?: "")
            binding.locationInfoCreatorName.text = creatorTxt
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            locationData.createdTime?.let {
                val nowTime = dateFormat.format(Date(locationData.createdTime.toLong()))
                binding.locationInfoCreatedTime.text = nowTime
            }
            binding.locationInfoCreatorName.visibility = VISIBLE
            binding.locationInfoCreatedTime.visibility = VISIBLE
        }

        binding.locationInfoAdd.visibility = if (showAdd == true) VISIBLE else GONE
        binding.locationInfoContainer.animate().alpha(1f).translationY(0f).duration = 250
        binding.locationInfoContainer.visibility = VISIBLE
    }

    private fun hideLocationInfo() {
        binding.locationInfoTitle.text = null
        binding.locationInfoDescription.text = null
        binding.locationInfoContainer.visibility = GONE
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setLocationInfoUI() {
        binding.locationInfoFold.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isTouchInfoFold = true
                }

                MotionEvent.ACTION_UP -> {
                    if (isTouchInfoFold) {
                        binding.locationInfoContainer.animate().alpha(0f).translationY(0f).duration = 250
                        isTouchInfoFold = false
                        return@setOnTouchListener false
                    }
                }
            }
            true
        }

        binding.locationInfoAdd.setOnClickListener {
            hideLocationInfo()
            val intent = Intent(it.context, AddLocationNoteActivity::class.java)
            intent.putExtra("latLng", myLocation)

            (it.context as MainActivity).startActivityForResult(
                intent,
                ADD_LOCATION_NOTE_REQUEST_CODE
            )
        }

        binding.locationInfoFold.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isTouchInfoFold = true
                }

                MotionEvent.ACTION_UP -> {
                    if (isTouchInfoFold) {
                        binding.locationInfoContainer.animate().alpha(0f).translationY(0f).duration = 250
                        isTouchInfoFold = false
                        return@setOnTouchListener false
                    }
                }
            }
            true
        }
    }
}