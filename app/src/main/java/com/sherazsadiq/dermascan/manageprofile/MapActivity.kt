package com.sherazsadiq.dermascan.manageprofile

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.firebase.DocLocation
import com.sherazsadiq.dermascan.firebase.FirebaseReadService
import com.sherazsadiq.dermascan.firebase.FirebaseWriteService
import com.sherazsadiq.dermascan.setStatusBarColor
import java.io.IOException

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var recyclerView: RecyclerView
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private lateinit var locName: String
    private lateinit var locAddress: String
    private lateinit var locPhone: String
    private lateinit var locWebsite: String
    private lateinit var locationURL: String

    val firbaseReadService = FirebaseReadService()
    val firebaseWriteService = FirebaseWriteService()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map)
        setStatusBarColor(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backButton = findViewById<FrameLayout>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val saveButton = findViewById<TextView>(R.id.SaveButton)
        saveButton.setOnClickListener {
            uplaodLocationData(locName, locAddress, locPhone, locWebsite, locationURL)
            finish()
        }



        // Initialize the Places API
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchLocation(it)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null && newText.isNotEmpty()) {
                    performAutocomplete(newText)
                } else {
                    recyclerView.visibility = RecyclerView.GONE
                }
                return false
            }
        })

        val btnStreetView = findViewById<ImageButton>(R.id.btnStreetView)
        val btnSatelliteView = findViewById<ImageButton>(R.id.btnSatelliteView)

        btnStreetView.setOnClickListener {
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            btnStreetView.visibility = View.GONE
            btnSatelliteView.visibility = View.VISIBLE
        }

        btnSatelliteView.setOnClickListener {
            googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            btnStreetView.visibility = View.VISIBLE
            btnSatelliteView.visibility = View.GONE
        }

        getDeviceLocation()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        googleMap.uiSettings.isScrollGesturesEnabled = true
        googleMap.uiSettings.isZoomGesturesEnabled = true

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            getDeviceLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }

        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            val marker = googleMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))

            // Get the nearest address using Geocoder
            val geocoder = Geocoder(this)
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                marker?.tag = address

                // Store the information
                locName = address.featureName ?: "N/A"
                locAddress = address.getAddressLine(0) ?: "N/A"
                locPhone = "N/A" // Geocoder does not provide phone number
                locWebsite = "N/A" // Geocoder does not provide website
                locationURL = "https://www.google.com/maps/search/?api=1&query=${latLng.latitude},${latLng.longitude}"

            } else {
                marker?.tag = "No address found"
            }

            val currentZoom = googleMap.cameraPosition.zoom
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, currentZoom))
        }

        googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                val view = layoutInflater.inflate(R.layout.custom_info_window, null)
                val nameTextView = view.findViewById<TextView>(R.id.name)
                val addressTextView = view.findViewById<TextView>(R.id.address)
                val phoneTextView = view.findViewById<TextView>(R.id.phone)
                val websiteTextView = view.findViewById<TextView>(R.id.website)

                val tag = marker.tag
                if (tag is Place) {
                    val place = tag
                    nameTextView.text = place.name ?: "N/A"
                    addressTextView.text = place.address ?: "N/A"
                    phoneTextView.text = place.phoneNumber ?: "N/A"
                    websiteTextView.text = place.websiteUri?.toString() ?: "N/A"
                } else if (tag is Address) {
                    nameTextView.text = tag.featureName ?: "N/A"
                    addressTextView.text = tag.getAddressLine(0) ?: "N/A"
                    phoneTextView.text = ""
                    websiteTextView.text = ""
                } else {
                    nameTextView.text = "No address found"
                    addressTextView.text = ""
                    phoneTextView.text = ""
                    websiteTextView.text = ""
                }

                return view
            }
        })
    }

    private fun getDeviceLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val locationResult: Task<Location> = fusedLocationClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            val currentLatLng = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                        }
                    } else {
                        // Handle location not found
                    }
                }
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun searchLocation(location: String) {
        val geocoder = Geocoder(this)
        try {
            val addressList = geocoder.getFromLocationName(location, 1)
            if (!addressList.isNullOrEmpty()) {
                val address = addressList[0]
                val latLng = LatLng(address.latitude, address.longitude)
                googleMap.clear()
                googleMap.addMarker(MarkerOptions().position(latLng).title(location))
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun performAutocomplete(query: String) {
        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setSessionToken(token)
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            val predictions = response.autocompletePredictions
            val adapter = AutocompleteAdapter(predictions) { prediction ->
                val placeId = prediction.placeId
                val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI)
                val placeRequest = FetchPlaceRequest.newInstance(placeId, placeFields)

                placesClient.fetchPlace(placeRequest).addOnSuccessListener { placeResponse ->
                    val place = placeResponse.place
                    val latLng = place.latLng
                    if (latLng != null) {
                        googleMap.clear()
                        val marker = googleMap.addMarker(MarkerOptions().position(latLng).title(place.name))
                        marker?.tag = place

                        // Store the information
                        locName = place.name ?: "N/A"
                        locAddress = place.address ?: "N/A"
                        locPhone = place.phoneNumber ?: "N/A"
                        locWebsite = place.websiteUri?.toString() ?: "N/A"

                        // Construct the URL using latitude and longitude
                        locationURL = "https://www.google.com/maps/search/?api=1&query=${latLng.latitude},${latLng.longitude}"

                        // Log the information
                        Log.d("MapActivity", "Latitude: ${latLng.latitude}, Longitude: ${latLng.longitude}")
                        Log.d("MapActivity", "Location URL: $locationURL")

                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        recyclerView.visibility = RecyclerView.GONE
                    }
                }
            }
            recyclerView.adapter = adapter
            recyclerView.visibility = RecyclerView.VISIBLE
        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                val apiException = exception as ApiException
                val statusCode = apiException.statusCode
                // Handle error with given status code.
            }
        }
    }

    private fun uplaodLocationData(name: String, address: String, phone: String, website: String, url: String) {
        val docLoc = DocLocation(
            LocName = name,
            LocAddress = address,
            LocPhone = phone,
            LocWebsite = website,
            LocURL = url,
            LocComplete = true
        )

        // Log the information
        Log.d("MapActivity", "Location Name: $name")
        Log.d("MapActivity", "Location Address: $address")
        Log.d("MapActivity", "Location Phone: $phone")
        Log.d("MapActivity", "Location Website: $website")
        Log.d("MapActivity", "Location URL: $url")

        Log.d("MapActivity", "DocLocation URL: ${docLoc.LocURL}")


        val uid = firbaseReadService.getCurrentUserUid()
        if (uid == null) {
            Toast.makeText(this, "Failed to get user id", Toast.LENGTH_SHORT).show()
            return
        } else {
            firebaseWriteService.updateDoctorLocation(uid, docLoc) { success ->
                if (success) {
                    Toast.makeText(this, "Location updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}


