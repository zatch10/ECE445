package com.example.pollutionmapper
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.fasterxml.jackson.databind.JsonNode
//import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import com.fasterxml.jackson.module.kotlin.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class UpdateMap(activity: MapsActivity) : Thread(){
    private val activity = activity
    private val client = OkHttpClient()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun run(){
        while(true){
            Thread.sleep(30000)
            Log.d("server", java.time.LocalTime.now().toString())
            this.post()
            this.activity.runOnUiThread{
                Toast.makeText(activity,"Updating...",Toast.LENGTH_SHORT).show()
                activity.googleMapObject!!.clear()
                val currentLocation = LatLng(this.activity.currentLatitude!!.toDouble(), this.activity.currentLongitude!!.toDouble())
                activity.googleMapObject!!.addMarker(MarkerOptions().position(currentLocation).title("Current Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                activity.googleMapObject!!.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
            }
            this.get()
        }


    }

    private fun get(){
        val request = Request.Builder()
            .url("https://ssdoyd2p7c.execute-api.us-east-1.amazonaws.com/alpha/pollution-data")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("server", "error in get request", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val mapper = jacksonObjectMapper()

                    val data = mapper.readValue<JsonNode>(response.body?.string() ?: "")
                    Log.d("server" , data["body"].toString())
                    for (obj in data["body"]){
                        val gps = obj["gps"].toString().replace("\"", "").split(",")
//                        Log.d("sever", gps[0])
                        val lat = gps[0].toDouble()
                        val long = gps[1].toDouble()
                        val co2 = obj["CO2"].toString().replace("\"", "")
                        val co = obj["CO"].toString().replace("\"", "")
                        val propane = obj["Propane"].toString().replace("\"", "")
                        val contaminationLocation = LatLng(lat, long)
                        val string = "CO2: $co2, CO: $co, Propane: $propane"
                        activity.runOnUiThread{
                            Toast.makeText(activity,"Updating...",Toast.LENGTH_SHORT).show()
                            activity.googleMapObject!!.addMarker(
                                MarkerOptions().position(contaminationLocation).title("Contamination Zone")
                                .snippet(string))
                            activity.googleMapObject!!.addCircle(CircleOptions().center(contaminationLocation).radius(100.0).clickable(true))
                        }

                    }
                }
            }
        })
    }

    public fun post(){
        this.activity.runOnUiThread{
            activity.getLocation()
        }
        val gps = String.format("%.4f", activity.currentLatitude) + "," + String.format("%.4f", activity.currentLongitude)
        val co2 = Bluetooth_data.sensor_data[0].toString()
        val co = Bluetooth_data.sensor_data[1].toString()
        val propane = Bluetooth_data.sensor_data[2].toString()
        val postBody = "{\"gps\":\"$gps\", \"CO2\":\"$co2\", \"CO\":\"$co\", \"Propane\":\"$propane\"}"
        Log.d("server", postBody)
        val request = Request.Builder()
            .url("https://ssdoyd2p7c.execute-api.us-east-1.amazonaws.com/alpha/pollution-data")
            .post(postBody.toRequestBody(MEDIA_TYPE_MARKDOWN))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            Log.d("server" , response.body!!.string())
        }
    }

    companion object {
        val MEDIA_TYPE_MARKDOWN = "text/x-markdown; charset=utf-8".toMediaType()
    }

}