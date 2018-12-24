package com.maher.irrigator.dependency.module

import android.content.Context
import android.location.LocationManager
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import dagger.Module
import dagger.Provides

@Module(includes = [ContextModule::class])
class LocationModule {

    @Provides
    fun locationCallback(context: Context, locationSettingRequest: LocationSettingsRequest): Task<LocationSettingsResponse> {
        return LocationServices.getSettingsClient(context).checkLocationSettings(locationSettingRequest)
    }

    @Provides
    fun locationSetting(locationRequest: LocationRequest): LocationSettingsRequest {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        return builder.build()
    }

    @Provides
    fun locationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(30 * 1000)
        locationRequest.setFastestInterval(5 * 1000)
        return locationRequest
    }

    @Provides
    fun manager(context: Context): LocationManager {
        return context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
}