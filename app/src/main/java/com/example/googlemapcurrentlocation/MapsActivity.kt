package com.example.googlemapcurrentlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.googlemapcurrentlocation.databinding.ActivityMapsBinding
import com.example.googlemapcurrentlocation.model.DrugStoreList
import com.example.googlemapcurrentlocation.network.DrugStoreService
import com.example.googlemapcurrentlocation.utils.Common
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var styleList = arrayListOf<String>()

    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()
    private var mMarker: Marker? = null

    companion object {
        private const val LOCATION_REQUEST_CODE : Int = 1
    }

    private lateinit var mService: DrugStoreService
    internal lateinit var drugStoreList: DrugStoreList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        addListStyle()
        styleClick()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission()
        }
        buildLocationRequest()
        buildLocationCallBack()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())

        mService = Common.googleApiService

        binding.bottomView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_school -> getAllDrugStore("school")
                R.id.action_medical -> getAllDrugStore("hospital")
                R.id.action_restaurant -> getAllDrugStore("restaurant")
            }
            true
        }

    }

    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                lastLocation = p0.locations.get(p0.locations.size - 1)
                if (mMarker != null) {
                    mMarker!!.remove()
                }

                latitude = lastLocation.latitude
                longitude = lastLocation.longitude

                val currentLatLng = LatLng(latitude, longitude)
                val markerOptions = MarkerOptions().position(currentLatLng).title("Your position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                mMarker = mMap.addMarker(markerOptions)

                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                mMap.animateCamera(CameraUpdateFactory.zoomBy(11f))
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }

    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            }
            return false
        }
        else
            return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (checkLocationPermission()) {
                            mMap.isMyLocationEnabled = true
                        }
                    }
                }
                else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getAllDrugStore(typePlace: String) {
        mMap.clear()
        val url = getUrl(latitude, longitude, typePlace)

        mService.getNearByPlace(url).enqueue(object : Callback<DrugStoreList> {
            override fun onResponse(call: Call<DrugStoreList>, response: Response<DrugStoreList>) {
                drugStoreList = response.body()!!

                if (response.isSuccessful) {
                    for (i in 0 until response.body()!!.drugStores.size) {
                        val markerOptions = MarkerOptions()
                        val googlePlace = response.body()!!.drugStores[i]
                        val lat = googlePlace.geometry.location.lat
                        val lng = googlePlace.geometry.location.lng
                        val placeName = googlePlace.name
                        val latLng = LatLng(lat, lng)

                        markerOptions.position(latLng)
                        markerOptions.title(placeName)
                        if (typePlace == "hospital") {
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_hospital))
                        } else if (typePlace == "school") {
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_school))
                        } else if (typePlace == "restaurant") {
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_restaurant))
                        }
                        markerOptions.snippet(i.toString())
                        mMap.addMarker(markerOptions)
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
//                        mMap.animateCamera(CameraUpdateFactory.zoomBy(0.3f))
                    }
                }
            }

            override fun onFailure(call: Call<DrugStoreList>, t: Throwable) {
                Toast.makeText(baseContext, "" + t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getUrl(latitude: Double, longitude: Double, typePlace: String): String {
        val googlePlaceUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googlePlaceUrl.append("?location=$latitude,$longitude")
        googlePlaceUrl.append("&radius=3000")
        googlePlaceUrl.append("&type=$typePlace")
        googlePlaceUrl.append("&key=AIzaSyDdUtQJNhArwSsRvXsiMyl5NIJ52JQ1h7Q")

        Log.d("URL_DEBUG", googlePlaceUrl.toString())
        return googlePlaceUrl.toString()
    }

    private fun styleClick() {
        val spinner = findViewById<Spinner>(R.id.action_bar_spinner)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL)
                    1 -> mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN)
                    2 -> mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE)
                    3 -> mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

        }

    }

    private fun addListStyle() {
        styleList.add("Style 1")
        styleList.add("Style 2")
        styleList.add("Style 3")
        styleList.add("Style 4")

        val spinner = findViewById<Spinner>(R.id.action_bar_spinner)
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, styleList)
        spinner.adapter = arrayAdapter
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        //get current location
        mMap.uiSettings.isMyLocationButtonEnabled = true
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            return
        }
        mMap.isMyLocationEnabled = true

        mMap.setOnMarkerClickListener { marker ->
            if (marker.snippet != null) {
                Common.currentResult = drugStoreList.drugStores[(marker.snippet)!!.toInt()]
                startActivity(Intent(this@MapsActivity, ViewPlaceActivity::class.java))
            }
            true
        }
    }

    override fun onMarkerClick(p0: Marker) = false

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

}