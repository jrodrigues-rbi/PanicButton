package com.uebrasil.panicbuttonapp.services.location

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import services.MainService

class LocationDppService(private val context: Context) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    fun startLocationUpdates(locationRequest: LocationRequest) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult?.let {
                    for (location in it.locations) {
                        (context as MainService).onLocationUpdate(location)
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
