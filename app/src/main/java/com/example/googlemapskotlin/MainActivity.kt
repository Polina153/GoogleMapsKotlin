package com.example.googlemapskotlin

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity(), GoogleMap.OnMapClickListener {

    private lateinit var googleMap: GoogleMap
    private val markers = mutableListOf<Marker>()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    lateinit var geocoder: Geocoder

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        } else { // Если разрешение уже есть, включаем слой
            googleMap.isMyLocationEnabled = true
        }

        geocoder = Geocoder(this, Locale.getDefault())

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.getMapAsync { map ->
            googleMap = map
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(55.751244, 37.618423),
                    10f
                )
            ) // Москва

            googleMap.setOnMapClickListener(this)
        }

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onMapClick(latLng: LatLng) {
// Удаление маркера
        val markerToRemove = markers.find {
            val position = it.position
            val threshold = 0.0005 // погрешность
            kotlin.math.abs(position.latitude - latLng.latitude) < threshold &&
                    kotlin.math.abs(position.longitude - latLng.longitude) < threshold
        }
        if (markerToRemove != null) {
            markerToRemove.remove()
            markers.remove(markerToRemove)
        } else {
            // Иначе добавляем маркер
            getCityNameFromLatLng(latLng, geocoder) ?: "Unknown place"

        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getCityNameFromLatLng(latLng: LatLng, geocoder: Geocoder) {
        geocoder.getFromLocation(
            latLng.latitude, latLng.longitude, 3, @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            object : Geocoder.GeocodeListener {

                override fun onError(errorMessage: String?) {
                    Log.e("Geocoding", "Ошибка геокодинга: $errorMessage")
                }

                override fun onGeocode(addresses: List<Address?>) {
                    var address = "Адрес не найден."
                    if (!addresses.isEmpty()) {
                        val firstAddress = addresses.first()
                        address = "Город: ${firstAddress?.locality}"
                    }
                    Log.d("Geocoding", address)
                    lifecycleScope.launch {
                        val marker = googleMap.addMarker(
                            MarkerOptions().position(latLng)
                                .title(address)
                        )
                        marker?.let { markers.add(it) }
                    }
                }
            })

        /*lifecycleScope.launch {
            try {
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 3)
                var address = "Адрес не найден."
                if (!addresses.isNullOrEmpty()) {
                    val firstAddress =
                        addresses[0]
                    address = "Город: ${firstAddress.locality ?: "не определен"}"
                }
                Log.d("Geocoding", address)
                Handler(Looper.getMainLooper()).post(object : Runnable {
                    override fun run() {
                        val marker = googleMap.addMarker(
                            MarkerOptions().position(latLng)
                                .title(address)
                        )

                        marker?.let {
                            markers.add(it)
                        }
                    }
                })

               *//* val marker = googleMap.addMarker(
                    MarkerOptions().position(latLng).title(address)
                )
                marker?.let { markers.add(it) }*//*
            } catch (e: Exception) {
                Log.e("Geocoding", "Ошибка: ${e.message}")
            }
        }*/


    }

}
