package com.maher.irrigator

import com.maher.irrigator.model.Latitude
import com.maher.irrigator.model.Longitude
import com.maher.irrigator.model.Moisture
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface thingSpeakService {

    @GET("channels/{channel_id}/fields/1/last.json")
    fun getlatitude(@Path("channel_id") id: String, @Query("api_key") key: String): Call<Latitude>

    @GET("channels/{channel_id}/fields/2/last.json")
    fun getLongitude(@Path("channel_id") id: String, @Query("api_key") key: String): Call<Longitude>

    @GET("update")
    fun setProbability(@Query("api_key") key: String, @Query("field5") field: Int): Call<ResponseBody>

    @GET("channels/{channel_id}/fields/3/last.json")
    fun getMoisture(@Path("channel_id") id: String, @Query("api_key") key: String): Call<Moisture>

    @GET("update")
    fun setTime(@Query("api_key") key: String, @Query("field4") field: Int): Call<ResponseBody>

    @GET("update")
    fun setOperation(@Query("api_key") key: String, @Query("field6") field: Int): Call<Int>

}