package com.maher.irrigator

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.tasks.Task
import com.johnhiott.darkskyandroidlib.RequestBuilder
import com.johnhiott.darkskyandroidlib.models.DataBlock
import com.johnhiott.darkskyandroidlib.models.Request
import com.johnhiott.darkskyandroidlib.models.WeatherResponse
import com.maher.irrigator.dependency.component.DaggerLocationComponent
import com.maher.irrigator.dependency.module.ContextModule
import com.maher.irrigator.model.Latitude
import com.maher.irrigator.model.Longitude
import com.maher.irrigator.model.Moisture
import com.maher.irrigator.model.kc.KcPlant
import com.maher.irrigator.model.l.LPlant
import com.maher.irrigator.sharedTool.SavedData
import com.maher.irrigator.widget.Constant
import com.maher.irrigator.widget.ViewDialog
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.progress_view.*
import kotlinx.android.synthetic.main.toolbar_title.*
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit.RetrofitError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.*

const val DEV = "dev"
const val LATE = "late"
var CHANNEL_ID = ""
var READ_API_KEY = ""
var WRITE_API_KEY = ""

class DetailsActivity : AppCompatActivity(), LocationListener {

    @Inject
    lateinit var locationTask: Task<LocationSettingsResponse>

    @Inject
    lateinit var locationManager: LocationManager

    private val weather = RequestBuilder()
    private val request = Request()

    private lateinit var lPlant: LPlant
    private lateinit var kcPlant: KcPlant
    private var area: Int = 0
    private lateinit var calendar: Calendar
    private lateinit var thingSpeak: thingSpeakService


    private var latitude: String? = null
    private var longitude: String? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        DaggerLocationComponent.builder().contextModule(ContextModule(context = viewContext())).build().inject(this)

        lPlant = SavedData.getL(this, intent.getStringExtra(("lKey")))
        kcPlant = SavedData.getKc(this, intent.getStringExtra(("kcKey")))
        area = intent.getIntExtra("area", -1)


        if (lPlant == null || kcPlant == null || area == -1) {
            onBackPressed()
        }

        when (area) {
            0 -> {
                CHANNEL_ID = BuildConfig.CHANNEL_ID_ONE
                READ_API_KEY = BuildConfig.READ_KEY_ONE
                WRITE_API_KEY = BuildConfig.WRITE_KEY_ONE
            }

            1 -> {
                CHANNEL_ID = BuildConfig.CHANNEL_ID_TWO
                READ_API_KEY = BuildConfig.READ_KEY_TWO
                WRITE_API_KEY = BuildConfig.WRITE_KEY_TWO
            }

            2 -> {
                CHANNEL_ID = BuildConfig.CHANNEL_ID_THIRD
                READ_API_KEY = BuildConfig.READ_KEY_THIRD
                WRITE_API_KEY = BuildConfig.WRITE_KEY_THIRD
            }
        }
        toolbar_title.text = lPlant.name
        calendar = Calendar.getInstance()

        retrofit()

        var policy = StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        val callLatitude = thingSpeak.getlatitude(CHANNEL_ID, READ_API_KEY)
        val callLongitude = thingSpeak.getLongitude(CHANNEL_ID, READ_API_KEY)

        callLatitude.enqueue(object : Callback<Latitude> {
            override fun onFailure(call: Call<Latitude>, t: Throwable) {
                onBackPressed()
                Toast.makeText(applicationContext, "Latitude and Longitude not Specified", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Latitude>, response: retrofit2.Response<Latitude>) {
                if (response.isSuccessful) {
                    latitude = response.body()?.field1

                    callLongitude.enqueue(object : Callback<Longitude> {
                        override fun onFailure(call: Call<Longitude>, t: Throwable) {
                            onBackPressed()
                            Toast.makeText(
                                applicationContext,
                                "Latitude and Longitude not Specified",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onResponse(call: Call<Longitude>, response: Response<Longitude>) {
                            if (response.isSuccessful) {
                                longitude = response.body()?.field2

                                if (latitude != null && longitude != null) {
                                    forecast(latitude?.toDouble()!!, longitude?.toDouble()!!)
                                }
                            } else {
                                onBackPressed()
                                Toast.makeText(
                                    applicationContext,
                                    "Latitude and Longitude not Specified",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    })

                } else {
                    onBackPressed()
                    Toast.makeText(applicationContext, "Latitude and Longitude not Specified", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


    private fun retrofit() {
        val client = OkHttpClient().newBuilder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            })
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        thingSpeak = retrofit.create(thingSpeakService::class.java)
    }


    private fun viewContext(): Context {
        return this@DetailsActivity
    }

    private fun locationRequestTask(): Task<LocationSettingsResponse> {
        return locationTask
    }

    private fun locationManager(): LocationManager {
        return locationManager
    }

    private fun forecast(lat: Double, lng: Double) {
        request.lat = lat.toString()
        request.lng = lng.toString()
        request.units = Request.Units.SI
        request.language = Request.Language.ENGLISH
        request.addExcludeBlock(Request.Block.HOURLY)
        request.addExcludeBlock(Request.Block.MINUTELY)
        request.addExcludeBlock(Request.Block.CURRENTLY)
        weather.getWeather(request, object : retrofit.Callback<WeatherResponse> {
            override fun success(weatherResponse: WeatherResponse, response: retrofit.client.Response?) {
                setData(weatherResponse.daily, lat, lng)
            }

            override fun failure(retrofitError: RetrofitError) {
            }
        })
    }

    private fun thingSpeak(precipProbability: Double, tMax: Double, tMin: Double, humidity: Double, j: Int, lat: Double, ratio: Double, pressure: Double, u: Double, lPlant: LPlant, kcPlant: KcPlant) {
        when (precipProbability > 0.3) {
            true -> isRaining(tMax, tMin, humidity, j, lat, ratio, pressure, u, lPlant, kcPlant)
            false -> isNotRaining(tMax, tMin, humidity, j, lat, ratio, pressure, u, lPlant, kcPlant)
        }
    }

    private fun isNotRaining(tMax: Double, tMin: Double, humidity: Double, j: Int, lat: Double, ratio: Double, pressure: Double, u: Double, lPlant: LPlant, kcPlant: KcPlant) {
        setProbability(WRITE_API_KEY, 0)
        setTimeOperation(WRITE_API_KEY, tMax, tMin, humidity, j, lat, ratio, pressure, u, lPlant, kcPlant)
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        val now = Date()
        var difference: Int
        var prevMoisture: Double

        thingSpeak.getMoisture(CHANNEL_ID, READ_API_KEY).enqueue(object : Callback<Moisture> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<Moisture>, response: Response<Moisture>) {
                if (response.isSuccessful) {
                    difference = now.compareTo(formatter.parse(response.body()?.createdAt))
                    if (difference > 0) {
                        progress.hide()
                        container.visibility = View.VISIBLE
                        when {
                            response.body()?.field3?.toDouble()!! > 70.0 -> {
                                stopOperation(WRITE_API_KEY)
                                water_needed.text = "No water needed, It rains"
                                water_needed_value.text = ""
                                Toast.makeText(applicationContext, "Operation Satisfied", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                prevMoisture = response.body()?.field3?.toDouble()!!
                                set10thTimeOperation(WRITE_API_KEY, tMax, tMin, humidity, j, lat, ratio, pressure, u, lPlant, kcPlant)
                                water_needed.text = "Water Needed = "
                                water_needed_value.text = (Etc(tMax, tMin, humidity, j, lat, ratio, pressure, u, lPlant, kcPlant).div(10)).toInt().toString()

                                Handler().postDelayed({
                                    call.clone().enqueue(object : Callback<Moisture> {
                                        override fun onFailure(call: Call<Moisture>, t: Throwable) {
                                            stopOperation(WRITE_API_KEY)
                                        }

                                        override fun onResponse(call: Call<Moisture>, response: Response<Moisture>) {
                                            if (response.isSuccessful) {
                                                if (response.body()?.field3?.toDouble() == prevMoisture) {
                                                    ViewDialog().showErrorDialog(applicationContext, "Something went wrong")
                                                    stopOperation(WRITE_API_KEY)
                                                } else {
                                                    stopOperation(WRITE_API_KEY)
                                                }
                                            } else {
                                                stopOperation(WRITE_API_KEY)
                                            }
                                        }

                                    })
                                }, 80000)
                            }
                        }
                    } else {
                        Handler().postDelayed({
                            call.clone().enqueue(this)
                        }, 5000)
                    }
                } else {
                    Handler().postDelayed({
                        call.clone().enqueue(this)
                    }, 5000)
                }
            }

            override fun onFailure(call: Call<Moisture>, t: Throwable) {
                Handler().postDelayed({
                    call.clone().enqueue(this)
                }, 5000)
            }
        })
    }


    private fun isRaining(tMax: Double, tMin: Double, humidity: Double, j: Int, lat: Double, ratio: Double, pressure: Double, U: Double, lPlant: LPlant, kcPlant: KcPlant) {
        setProbability(WRITE_API_KEY, 1)
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        val now = Date()
        var difference: Int
        thingSpeak.getMoisture(CHANNEL_ID, READ_API_KEY).enqueue(object : Callback<Moisture> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<Moisture>, response: Response<Moisture>) {
                if (response.isSuccessful) {
                    difference = now.compareTo(formatter.parse(response.body()?.createdAt))
                    if (difference > 0) {
                        progress.hide()
                        container.visibility = View.VISIBLE
                        when {
                            response.body()?.field3?.toDouble()!! > 70.0 -> {
                                stopOperation(WRITE_API_KEY)
                                water_needed.text = "No water needed, It rains"
                                water_needed_value.text = ""
                                Toast.makeText(applicationContext, "Operation Satisfied", Toast.LENGTH_SHORT).show()
                            }
                            response.body()?.field3?.toDouble()!! < 5.0 -> {
                                set10thTimeOperation(WRITE_API_KEY, tMax, tMin, humidity, j, lat, ratio, pressure, U, lPlant, kcPlant)
                                water_needed.text = "Water Needed = "
                                water_needed_value.text = (Etc(tMax, tMin, humidity, j, lat, ratio, pressure, U, lPlant, kcPlant).div(10)).toInt().toString()
                                stopOperation(WRITE_API_KEY)
                            }
                            else -> {
                                water_needed.text = "Wait For Raining"
                                water_needed_value.text = ""
                                stopOperation(WRITE_API_KEY)
                            }
                        }
                    } else {
                        Handler().postDelayed({
                            call.clone().enqueue(this)
                        }, 5000)
                    }
                } else {
                    Handler().postDelayed({
                        call.clone().enqueue(this)
                    }, 5000)
                }
            }

            override fun onFailure(call: Call<Moisture>, t: Throwable) {
                Handler().postDelayed({
                    call.clone().enqueue(this)
                }, 5000)
            }
        })
    }


    fun setData(weather: DataBlock, lat: Double, lng: Double) {
        val ratio: Double
        with(weather.data[calendar.get(Calendar.DAY_OF_WEEK)]) {
            when (this.icon) {
                "clear-day" -> {
                    imageView.setImageResource(R.drawable.ic_clear_day)
                    ratio = 0.9
                }
                "clear-night" -> {
                    imageView.setImageResource(R.drawable.ic_clear_night)
                    ratio = 0.9
                }
                "rain" -> {
                    imageView.setImageResource(R.drawable.ic_snow)
                    ratio = 0.1
                }
                "snow" -> {
                    imageView.setImageResource(R.drawable.ic_snow)
                    ratio = 0.0
                }
                "cloudy" -> {
                    imageView.setImageResource(R.drawable.ic_cloudy)
                    if (this.cloudClover.toDouble() >= 0.5) {
                        ratio = 0.7
                    } else {
                        ratio = 0.5
                    }
                }
                else -> {
                    imageView.setImageResource(R.drawable.ic_cloudy)
                    ratio = 0.3
                }
            }

            planting_date_value.text = lPlant.date.toString()
            t_h_l_value.text = "( ${this.temperatureMax} ) - ( ${this.temperatureMin} )"
            summary_value.text = this.summary
            hum_value.text = this.humidity
            pre_value.text = this.pressure
            wind_speed_value.text = this.windSpeed
            lat_lng_value.text = "( $lat ) - ( $lng )"

            thingSpeak(
                this.precipProbability.toDouble(),
                this.temperatureMax,
                this.temperatureMin,
                this.humidity.toDouble(),
                lPlant.date,
                lat,
                ratio,
                this.pressure.toDouble(),
                this.windSpeed.toDouble(),
                lPlant,
                kcPlant
            )
        }
    }

    fun stopOperation(key: String) {
        thingSpeak.setOperation(key, 0).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(applicationContext, "Operation Stopped", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setProbability(key: String, value: Int) {
        thingSpeak.setProbability(key, value).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {

                }
            }
        })
    }

    private fun setTimeOperation(
        key: String, tMax: Double,
        tMin: Double,
        humidity: Double,
        j: Int,
        lat: Double,
        ratio: Double,
        pressure: Double,
        U: Double,
        lPlant: LPlant,
        kcPlant: KcPlant
    ) {
        thingSpeak.setTime(key, time(tMax, tMin, humidity, j, lat, ratio, pressure, U, lPlant, kcPlant).toInt())
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {

                    }
                }
            })
    }

    fun set10thTimeOperation(
        key: String, tMax: Double,
        tMin: Double,
        humidity: Double,
        j: Int,
        lat: Double,
        ratio: Double,
        pressure: Double,
        U: Double,
        lPlant: LPlant,
        kcPlant: KcPlant
    ) {
        thingSpeak.setTime(key, time(tMax, tMin, humidity, j, lat, ratio, pressure, U, lPlant, kcPlant).div(10).toInt())
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {

                    }
                }
            })
    }

    fun Tmean(tMax: Double, tMin: Double): Double {
        return (tMax.plus(tMin)).div(2)
    }

    fun Y(pressure: Double): Double {
        return (0.665.times(10.0.pow(-3))).times(pressure)
    }

    fun delta(tMax: Double, tMin: Double): Double {
        return (4098 * (0.6108 * exp((17.27 * Tmean(tMax, tMin)) / (Tmean(tMax, tMin) + 237.3)))) / (Tmean(
            tMax,
            tMin
        ) + 237.3).pow(2)
    }

    fun es(tMax: Double, tMin: Double): Double {
        return ((0.6108.times(exp((17.27.times(tMax)).div((tMax.plus(237.3)))))).plus(
            (0.6108.times(
                exp(
                    (17.27.times(
                        tMin
                    )).div((tMin.plus(237.3)))
                )
            ))
        )).div(2)
    }

    fun ea(humidity: Double, tMax: Double, tMin: Double): Double {
        return (humidity.times(es(tMax, tMin)))
    }

    fun dr(j: Int): Double {
        return 1 + 0.033 * cos(Math.toRadians(((2 * PI) / 365) * j))
    }

    fun s(j: Int): Double {
        return 0.409 * sin(Math.toRadians(((2 * PI * j) / 365) - 1.39))
    }

    fun ws(lat: Double, j: Int): Double {
        return Math.toDegrees(acos(-tan(Math.toRadians(lat)) * tan(Math.toRadians(s(j)))))
    }

    fun Ra(j: Int, lat: Double): Double {
        return ((24 * 60) / PI) * 0.082 * dr(j) * ((ws(
            lat,
            j
        ) * sin(Math.toRadians(lat)) * sin(Math.toRadians(s(j)))) + (cos(Math.toRadians(lat)) * cos(Math.toRadians(s(j))) * sin(
            Math.toRadians(ws(lat, j))
        )))
    }

    fun Rso(j: Int, lat: Double): Double {
        return 0.75 * Ra(j, lat)
    }

    fun Rs(j: Int, lat: Double, ratio: Double): Double {
        return (0.25 + 0.5 * ratio) * Ra(j, lat)
    }

    fun Rns(j: Int, lat: Double, ratio: Double): Double {
        return 0.77 * Rs(j, lat, ratio)
    }

    fun Rnl(tMax: Double, tMin: Double, humidity: Double, j: Int, lat: Double, ratio: Double): Double {
        return (4.903 * 10.0.pow(-9)) * (((tMax + 273.15).pow(4) + (tMin + 273.15).pow(4)) / 2) * (0.34 - 0.14 * sqrt(
            ea(
                humidity,
                tMax,
                tMin
            )
        )) * (1.35 * (Rs(j, lat, ratio) / Rso(j, lat)) - 0.35)
    }

    fun Rn(tMax: Double, tMin: Double, humidity: Double, j: Int, lat: Double, ratio: Double): Double {
        return Rns(j, lat, ratio) - Rnl(tMax, tMin, humidity, j, lat, ratio)
    }

    fun G(): Int {
        return 0
    }

    fun Eto(
        tMax: Double,
        tMin: Double,
        humidity: Double,
        j: Int,
        lat: Double,
        ratio: Double,
        pressure: Double,
        U: Double
    ): Double {
        return (0.408 * delta(tMax, tMin) * (Rn(tMax, tMin, humidity, j, lat, ratio) - G()) + Y(pressure)
                * (900 / (Tmean(tMax, tMin) + 273)) * U * (es(tMax, tMin) - ea(humidity, tMax, tMin))) / (delta(
            tMax,
            tMin
        ) + Y(pressure) * (1 + 0.34 * U))
    }

    fun Kcprev(stage: String): Double {
        when (stage) {
            DEV -> return kcPlant.ini
            LATE -> return kcPlant.mid
            else -> return 0.0
        }
    }

    fun Kcnext(stage: String): Double {
        when (stage) {
            DEV -> return kcPlant.mid
            LATE -> return kcPlant.end
            else -> return 0.0
        }
    }

    fun LStage(stage: String): Int {
        when (stage) {
            DEV -> return lPlant.dev
            LATE -> return lPlant.late
            else -> return 0
        }
    }

    fun SumLStage(stage: String): Int {
        when (stage) {
            DEV -> return lPlant.init
            LATE -> return lPlant.init + lPlant.dev + lPlant.mid
            else -> return 0
        }
    }

    fun Kc(lPlant: LPlant, kcPlant: KcPlant, j: Int): Double {
        var actualDays = calendar.get(Calendar.DAY_OF_YEAR) - j
        if (actualDays <= lPlant.init) {//init
            return kcPlant.ini
        } else if (actualDays - lPlant.init <= lPlant.dev) {//dev
            return Kcprev(DEV) + ((actualDays - SumLStage(DEV)) / LStage(DEV)) * (Kcnext(DEV) - Kcprev(DEV))
        } else if (actualDays - (lPlant.init + lPlant.dev) <= lPlant.mid) {//mid
            return kcPlant.mid
        } else if (actualDays - (lPlant.init + lPlant.dev + lPlant.mid) <= lPlant.late) {//late
            return Kcprev(LATE) + ((actualDays - SumLStage(LATE)) / LStage(LATE)) * (Kcnext(LATE) - Kcprev(LATE))
        } else {
            return kcPlant.end
        }
    }

    fun Etc(
        tMax: Double,
        tMin: Double,
        humidity: Double,
        j: Int,
        lat: Double,
        ratio: Double,
        pressure: Double,
        U: Double,
        lPlant: LPlant,
        kcPlant: KcPlant
    ) =
        (Eto(tMax, tMin, humidity, j, lat, ratio, pressure, U) * Kc(lPlant, kcPlant, j)) * lPlant.count

    fun time(
        tMax: Double,
        tMin: Double,
        humidity: Double,
        j: Int,
        lat: Double,
        ratio: Double,
        pressure: Double,
        U: Double,
        lPlant: LPlant,
        kcPlant: KcPlant
    ) =
        (Etc(
            tMax,
            tMin,
            humidity,
            j,
            lat,
            ratio,
            pressure,
            U,
            lPlant,
            kcPlant
        ) * lPlant.count * 1000000) / lPlant.flow // flow rate

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String?) {}
    override fun onProviderDisabled(provider: String?) {}

    override fun onLocationChanged(location: Location) {
//        forecast(location.latitude, location.longitude)  //Mobile Location's Weather
    }

    private fun onLocationRequest() {
        if (ActivityCompat.checkSelfPermission(
                viewContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                Constant.RequestCode.PermissionLocation
            )
        } else {
            accessUserLocation()
        }
    }

    private fun accessUserLocation() {
        if (checkIfLocationIsEnabled()) {
            listenToLocations()
        } else {
            resolveLocation()
        }
    }

    private fun checkIfLocationIsEnabled() = locationManager().isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    @SuppressLint("MissingPermission")
    private fun listenToLocations() {
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null)
    }


    private fun resolveLocation() {
        locationRequestTask().addOnCompleteListener {
            try {
                it.getResult(ApiException::class.java)
                try {
                    listenToLocations()
                } catch (e: SecurityException) {
                } catch (ex: Exception) {
                }
            } catch (ex: ApiException) {
                when (ex.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            (ex as ResolvableApiException).startResolutionForResult(
                                this,
                                Constant.RequestCode.EnableLocation
                            )
                        } catch (ex: IntentSender.SendIntentException) {
                        } catch (e: ClassCastException) {
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constant.RequestCode.PermissionLocation) {
            if (grantResults.first() == (PackageManager.PERMISSION_GRANTED)) {
                onLocationRequest()
            }
        } else {
            onBackPressed()
        }
    }

    //Turn On GPS Request
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.RequestCode.EnableLocation) {
            when (resultCode) {
                Activity.RESULT_OK -> onLocationRequest()
                Activity.RESULT_CANCELED -> onBackPressed()
            }
        }
    }
}
