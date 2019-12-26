package com.example.mytaxi

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.activity_passenger_maps.*

class PassengerMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private var locationTextView: TextView? = null
    private var locationUpdateTimeTextView: TextView? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var settingsClient: SettingsClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationSettingsRequest: LocationSettingsRequest? = null
    private var locationCallback: LocationCallback? = null
    private var currentLocation: Location? = null
    private var isLocationUpdatesActive = false
    private var radius: Double = 1.0
    private var isDriverFound = false
    private lateinit var nerestDriverId: String
    private lateinit var drivers: DatabaseReference
    private lateinit var nearestDriverLocation: DatabaseReference
    private var driverMarker: Marker? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passenger_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices
            .getFusedLocationProviderClient(this)
        settingsClient = LocationServices.getSettingsClient(this)
        buildLocationRequest()
        buildLocationCallBack()
        buildLocationSettingsRequest()
        startLocationUpdates()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drivers = FirebaseDatabase.getInstance().reference.child("driversLoc")
        btnBookTaxi.setOnClickListener {
            btnBookTaxi.text = getString(R.string.find_taxi)
            getNerestTaxi()
        }

    }

    private fun getNerestTaxi() {
        val geoFire = GeoFire(drivers)
        if (currentLocation != null) {
            val geoQuery = geoFire.queryAtLocation(
                GeoLocation(
                    currentLocation!!.latitude,
                    currentLocation!!.longitude
                ), radius
            )
            geoQuery.removeAllListeners()
            geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
                override fun onGeoQueryReady() {
                    if (!isDriverFound) {
                        radius++
                        getNerestTaxi()
                    }
                }

                override fun onKeyEntered(key: String, location: GeoLocation?) {
                    if (!isDriverFound) {
                        isDriverFound = true
                        nerestDriverId = key
                        getNerestDriverLocation()
                    }
                }

                override fun onKeyMoved(key: String?, location: GeoLocation?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onKeyExited(key: String?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onGeoQueryError(error: DatabaseError?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                fun getNerestDriverLocation() {
                    nearestDriverLocation =
                        FirebaseDatabase.getInstance().reference.child("driversLoc").child(nerestDriverId).child("l")
                    nearestDriverLocation.addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            /*val td : Map<String,Object> = p0.getValue()
                            val list = td.values()
                            val latitudeStr = list?.get(0)?.toString()
                            val lontidudeSrt = list?.get(1)?.toString()*/
                            val latitudeStr = p0.child("0").value as Double
                            val lontidudeSrt = p0.child("1").value as Double

                            val latitude = latitudeStr?.toDouble()
                            val lontidude = lontidudeSrt?.toDouble()
                            var driverLatLng: LatLng? = null
                            if (latitude != null && lontidude != null) {
                                driverLatLng = LatLng(latitude, lontidude)
                                    driverMarker?.remove()
                                val driverLocation = Location("").apply {
                                    setLatitude(latitude)
                                    setLongitude(lontidude)
                                }
                                val distanceToDriver = driverLocation.distanceTo(currentLocation)
                                btnBookTaxi.text = "До водителя $distanceToDriver"
                                driverMarker =
                                    mMap.addMarker(
                                        MarkerOptions().position(driverLatLng).title(
                                            "Водитель здесь"
                                        )
                                    )


                            }

                        }
                    })
                }
            })
        }


    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.signOut -> {
                val passengerUserId = FirebaseAuth.getInstance().currentUser?.uid
                val passenger = FirebaseDatabase.getInstance().reference.child("passengersLoc")
                val geoFire = GeoFire(passenger)
                geoFire.removeLocation(passengerUserId)

                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@PassengerMapsActivity, ChooseModeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
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

        // Add a marker in Sydney and move the camera
        val passengerLocation = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(passengerLocation).title("Passenger Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(passengerLocation))
    }

    private fun stopLocationUpdates() {
        if (!isLocationUpdatesActive) {
            return
        }
        fusedLocationClient!!.removeLocationUpdates(locationCallback)
            .addOnCompleteListener(this) {
                isLocationUpdatesActive = false

            }
    }

    private fun startLocationUpdates() {
        isLocationUpdatesActive = true

        settingsClient!!.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener(this@PassengerMapsActivity,
                OnSuccessListener {
                    if (ActivityCompat.checkSelfPermission(
                            this@PassengerMapsActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) !=
                        PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat
                            .checkSelfPermission(
                                this@PassengerMapsActivity,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) !=
                        PackageManager.PERMISSION_GRANTED
                    ) { // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return@OnSuccessListener
                    }
                    fusedLocationClient!!.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.myLooper()
                    )
                    updateLocationUi()
                })
            .addOnFailureListener(this) { e ->
                when ((e as ApiException).statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvableApiException =
                            e as ResolvableApiException
                        resolvableApiException.startResolutionForResult(
                            this@PassengerMapsActivity,
                            CHECK_SETTINGS_CODE
                        )
                    } catch (sie: IntentSender.SendIntentException) {
                        sie.printStackTrace()
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        val message = "Adjust location settings on your device"
                        Toast.makeText(
                            this@PassengerMapsActivity, message,
                            Toast.LENGTH_LONG
                        ).show()
                        isLocationUpdatesActive = false

                    }
                }
                updateLocationUi()
            }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CHECK_SETTINGS_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    Log.d(
                        "MainActivity", "User has agreed to change location" +
                                "settings"
                    )
                    startLocationUpdates()
                }
                Activity.RESULT_CANCELED -> {
                    Log.d(
                        "MainActivity", "User has not agreed to change location" +
                                "settings"
                    )
                    isLocationUpdatesActive = false
                    updateLocationUi()
                }
            }
        }
    }

    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest!!)
        locationSettingsRequest = builder.build()
    }

    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                currentLocation = locationResult.lastLocation
                updateLocationUi()
            }
        }
    }

    private fun updateLocationUi() {
        if (currentLocation != null) {
            val currentLatLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12f))
            mMap.addMarker(MarkerOptions().position(currentLatLng).title("Passenger Location"))
            val passengerUserId = FirebaseAuth.getInstance().currentUser?.uid
            val passengers = FirebaseDatabase.getInstance().reference.child("passengersLoc")
            val geoFire = GeoFire(passengers)
            geoFire.setLocation(
                passengerUserId,
                GeoLocation(currentLocation!!.latitude, currentLocation!!.longitude)
            )

        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest!!.interval = 10000
        locationRequest!!.fastestInterval = 3000
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }


    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    override fun onStart() {
        super.onStart()
        if (isLocationUpdatesActive && checkLocationPermission()) {
            startLocationUpdates()
        } else if (!checkLocationPermission()) {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        val shouldProvideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(
                this@PassengerMapsActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        if (shouldProvideRationale) {
            showSnackBar(
                "Location permission is needed for " +
                        "app functionality",
                "OK",
                View.OnClickListener {
                    ActivityCompat.requestPermissions(
                        this@PassengerMapsActivity, arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ),
                        REQUEST_LOCATION_PERMISSION
                    )
                }
            )
        } else {
            ActivityCompat.requestPermissions(
                this@PassengerMapsActivity, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun showSnackBar(
        mainText: String,
        action: String,
        listener: View.OnClickListener
    ) {
        Snackbar.make(
            findViewById(android.R.id.content),
            mainText,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(
                action,
                listener
            )
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isEmpty()) {
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isLocationUpdatesActive) {
                    startLocationUpdates()
                }
            } else {
                showSnackBar(
                    "Turn on location on settings",
                    "Settings",
                    View.OnClickListener {
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID,
                            null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                )
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            this@PassengerMapsActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        Log.d("MainActivity", "checkLocationPermission permissionState $permissionState")

        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val CHECK_SETTINGS_CODE = 111
        private const val REQUEST_LOCATION_PERMISSION = 222
    }
}
