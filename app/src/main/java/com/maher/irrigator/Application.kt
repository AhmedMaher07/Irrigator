package com.maher.irrigator

import android.app.Application
import com.johnhiott.darkskyandroidlib.ForecastApi
import com.maher.irrigator.widget.Constant.Key.Weather


class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        ForecastApi.create(Weather)
    }
}