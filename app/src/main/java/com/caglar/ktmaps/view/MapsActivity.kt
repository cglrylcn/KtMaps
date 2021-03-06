package com.caglar.ktmaps.view

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import androidx.room.RoomDatabase
import com.caglar.ktmaps.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.caglar.ktmaps.databinding.ActivityMapsBinding
import com.caglar.ktmaps.model.Place
import com.caglar.ktmaps.roomdb.PlaceDao
import com.caglar.ktmaps.roomdb.PlaceDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.Exception

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    private var trackBoolean: Boolean ?= null
    private var selectedLatitude: Double ?= null
    private var selectedLongitude: Double ?= null
    private lateinit var db: PlaceDatabase
    private lateinit var placeDao: PlaceDao
    private val compositeDisposable = CompositeDisposable()
    var placeFromMain : Place ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerLauncher()
        selectedLatitude = 0.0
        selectedLongitude = 0.0
        binding.saveButton.isEnabled = false
        sharedPreferences = getSharedPreferences("com.caglar.ktmaps", MODE_PRIVATE)
        trackBoolean = false

        db = Room.databaseBuilder(
            applicationContext,
            PlaceDatabase::class.java, "Places")
//            .allowMainThreadQueries()
            .build()
        placeDao = db.placeDao()
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
        mMap.setOnMapLongClickListener(this)

        val intent = intent
        val info = intent.getStringExtra("info")
        println(info)
        if (info == "new") {
            binding.saveButton.visibility = View.VISIBLE
            binding.deleteButton.visibility = View.GONE

            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager //Casting
            locationListener = object : LocationListener {
                override fun onLocationChanged(p0: Location) {
                    trackBoolean = sharedPreferences.getBoolean("trackBoolean",false)
                    if (!trackBoolean!!) {
                        val userLocation = LatLng(p0.latitude,p0.longitude)
//                    mMap.addMarker(MarkerOptions().position(userLocation).title("Location")) //Add marker
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15f))
                        sharedPreferences.edit().putBoolean("trackBoolean", true).apply()
                    }
                }
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Snackbar.make(binding.root, "Permission Needed",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission") {
                        //Request Permission
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }.show()
                }
                else {
                    //Request Permission
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
            else {
                //Permission granted
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null) {
                    val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))
                }
                mMap.isMyLocationEnabled = true
            }
        }
        else {
            mMap.clear()
            placeFromMain = intent.getSerializableExtra("selectedPlace") as? Place
            placeFromMain?.let {place ->
                val latLng = LatLng(placeFromMain!!.latitude, placeFromMain!!.longitude)
                mMap.addMarker(MarkerOptions().position(latLng).title(place.name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17f))
                binding.placeText.setText(place.name)
                binding.saveButton.visibility = View.GONE
                binding.deleteButton.visibility = View.VISIBLE
            }
        }



        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(48.853915, 2.291315)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15F))
    }

    private fun registerLauncher() {
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //Permission Granted
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                    val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastLocation != null) {
                        val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))
                    }
                    mMap.isMyLocationEnabled = true
                }
            }
            else {
                //Permission Denied
                Toast.makeText(this@MapsActivity,"Permission needed", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onMapLongClick(p0: LatLng) {
        mMap.clear() //Clear markers
        mMap.addMarker(MarkerOptions().position(p0))
        selectedLatitude = p0.latitude
        selectedLongitude = p0.longitude
        binding.saveButton.isEnabled = true
    }

    fun save(view : View) {
        if (selectedLatitude != null && selectedLongitude != null) {
            val place = Place(binding.placeText.text.toString(), selectedLatitude!!, selectedLongitude!!)
            compositeDisposable.add(
                placeDao.insert(place)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )
        }
    }

    private fun handleResponse() {
        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    fun delete(view : View) {
        placeFromMain?.let { place ->
            compositeDisposable.add(
                placeDao.delete(place)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe()
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}