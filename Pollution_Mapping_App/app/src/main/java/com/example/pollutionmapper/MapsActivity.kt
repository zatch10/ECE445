package com.example.pollutionmapper

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Build

import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.widget.Toast
import android.location.LocationManager
import android.location.Location
import android.content.Context
import android.location.LocationListener
import android.util.Log
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import android.widget.TextView
import androidx.annotation.RequiresApi


import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.CircleOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback{

    private lateinit var mMap: GoogleMap
    private var permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
    private var currentLocation: Location? = null
    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationByGps : Location? = null
    private var locationByNetwork : Location? = null
    var currentLatitude : Double? = null
    var currentLongitude : Double? = null
    var googleMapObject: GoogleMap? = null
    var mapReady = false
    lateinit var notificationChannel: NotificationChannel
    lateinit var notificationManager: NotificationManager
    lateinit var builder: Notification.Builder
    private val channelId = "12345"
    private val description = "Test Notification"


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (isLocationPermissionGranted()){
                enableView()
            } else{
                requestPermissions(permissions, 2)
            }
        } else{
            enableView()
        }
        getLocation()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        BluetoothServerController(this).start()
        UpdateMap(this).start()
        val textView = findViewById<TextView>(R.id.text_view_id)
        //final TextView helloTextView = (TextView) findViewById(R.id.text_view_id);
        textView.text = "CO2: xxxx, CO: xxxx, Propane: xxxx"
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager
        notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMapObject = googleMap
        // Get current location and move the camera to that location to allow users to see
        // contamination zones around them. Mark the current location with a Blue marker.
        val currentLocation = LatLng(currentLatitude!!.toDouble(), currentLongitude!!.toDouble())
        mMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))

        // Adding a dummy contamination zone coordinate close to the user's location.
        // Marking the contamination zone with a marker and showing the gas values in the Info Window
        // Drawing a circle with the radius of 100 meters around the contamination zone
        val testContaminationLocation  = LatLng(37.421998, -122.085)
        mMap.addMarker(MarkerOptions().position(testContaminationLocation).title("Contamination Zone")
            .snippet("CO2: xxx, CO: xxx, Propane: xxx"))
        mMap.addCircle(CircleOptions().center(testContaminationLocation).radius(100.0).clickable(true))
        mapReady = true
//        while(true){
//            Thread.sleep(10000)
//            Log.d("map", "clearing")
//            mMap.clear()
//        }
//        Log.d("Main", "end of onMapReady")
    }

    private fun enableView(){
        Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    public fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val gpsLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationByGps= location
                Log.d("AndroidLocation", "GPS Latitude : " + locationByGps!!.latitude)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        val networkLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationByNetwork= location
                Log.d("AndroidLocation", "Network Latitude : " + locationByNetwork!!.latitude)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (hasGps || hasNetwork){
            if (hasGps){
                Log.d("AndroidLocation", "hasGps")
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, gpsLocationListener)
            }
            if (hasNetwork){
                Log.d("AndroidLocation", "hasNetwork")
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, networkLocationListener)
            }
            val lastKnownLocationByGps =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            lastKnownLocationByGps?.let {
                locationByGps = lastKnownLocationByGps
            }
//            Log.d("AndroidLocation", "locationByGps: " + locationByGps.toString())
            val lastKnownLocationByNetwork =
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            lastKnownLocationByNetwork?.let {
                locationByNetwork = lastKnownLocationByNetwork
            }
//            Log.d("AndroidLocation", "locationByNetwork: " + locationByNetwork.toString())

            if (locationByGps != null || locationByNetwork != null) {
//                Log.d("AndroidLocation", "entered function")
                if (locationByGps != null) {
                    currentLocation = locationByGps
//                    Log.d("AndroidLocation", "GPS Latitude : " + currentLocation!!.latitude)
//                    Log.d("AndroidLocation", "GPS Longitude : " + currentLocation!!.longitude)
                    this.currentLatitude = currentLocation!!.latitude
                    this.currentLongitude = currentLocation!!.longitude
                } else {
                    currentLocation = locationByNetwork
//                    Log.d("AndroidLocation", "Network Latitude : " + currentLocation!!.latitude)
//                    Log.d("AndroidLocation", "Network Longitude : " + currentLocation!!.longitude)
                    this.currentLatitude = currentLocation!!.latitude
                    this.currentLongitude = currentLocation!!.longitude
                }
            }
        }
    }
    private fun isLocationPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                2
            )
            false
        } else {
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun notify_user(text: String){
        var builder = Notification.Builder(this, channelId)
            .setContentTitle("Contamination detected")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_background)
        notificationManager.notify(12345, builder.build())
    }

}

