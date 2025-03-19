package com.sherazsadiq.dermascan.manageprofile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.setStatusBarColor

class HospitalLocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private var selectedLocation: LatLng? = null
    private var selectedLocationName: String = "Unknown Location"
    private var selectedLocationUrl: String = ""



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_hospital_location)
        setStatusBarColor(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize MapView
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this) // No error here

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyCrmREiQ30QCg4mQVmdFdeMFTLeA1k8LA8")
        }


        val searchView = findViewById<SearchView>(R.id.locationSearch)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    openPlaceSearch(query) // Pass the query
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })



        // Handle Save Button Click
        val saveButton = findViewById<TextView>(R.id.saveButton)
        saveButton.setOnClickListener {
            // Log selected location details
            selectedLocation?.let {
                println("Selected Location: $selectedLocationName")
                println("Selected Location URL: $selectedLocationUrl")
                println("Selected Location Latitude: ${it.latitude}")
                println("Selected Location Longitude: ${it.longitude}")
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Default location (Pakistan)
        val defaultLocation = LatLng(30.3753, 69.3451)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 6f))

        // Add marker on map click
        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            val markerOptions = MarkerOptions().position(latLng).title("Selected Location")
            val marker = googleMap.addMarker(markerOptions)
            marker?.showInfoWindow()

            selectedLocation = latLng
            selectedLocationName = "Hospital at (${latLng.latitude}, ${latLng.longitude})"
            selectedLocationUrl = "https://www.google.com/maps/search/?api=1&query=${latLng.latitude},${latLng.longitude}"
        }
    }


    // Required MapView Lifecycle Methods
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
    override fun onDestroy() { super.onDestroy(); mapView.onDestroy() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }

    private fun openPlaceSearch(query: String) {
        val fields = listOf(Place.Field.LAT_LNG, Place.Field.NAME)

        // Pass the query into Autocomplete
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .setInitialQuery(query) // This ensures the query is used
            .build(this)

        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            selectedLocation = place.latLng
            selectedLocationName = place.name ?: "Unknown Location"

            // Move camera to selected location
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation!!, 15f))

            // Add marker
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(selectedLocation!!).title(selectedLocationName))
        }
    }

}