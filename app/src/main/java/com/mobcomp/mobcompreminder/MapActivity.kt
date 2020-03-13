package com.mobcomp.mobcompreminder

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.lang.Exception
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var gMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var selectedLocation: LatLng
    private lateinit var geofencingClient: GeofencingClient

        private val geofenceId = "REMINDER_GEOFENCE_ID"
        private val geofenceRadius = 500
        private val geofenceExpiration = 120*24*60*60*1000
        private val geofenceDwellDelay = 2*60*1000

    @ExperimentalUnsignedTypes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        (map_fragment as SupportMapFragment).getMapAsync(this)

        geofencingClient=LocationServices.getGeofencingClient(this)

        map_create.setOnClickListener{
            val reminderText = reminder_message.text.toString()
            if (reminderText.isEmpty())
            {
                toast( "Leave no empty message")
                return@setOnClickListener
            }
            if (selectedLocation==null)
            {
                toast( "Empty location")
                return@setOnClickListener
            }
            val reminder = Reminder(
                uid = null,
                time = null,
                location = String.format(
                    "%.3f,%.3f", selectedLocation.latitude,
                    selectedLocation.latitude),
                    msg = reminderText
            )
            doAsync{
                val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "reminders").build()
                val uid = db.reminderDao().insert(reminder).toInt()
                reminder.uid=uid

                db.close()
                createGeoFence(selectedLocation, reminder, geofencingClient)
            }
            finish()
            }
        }
    private fun createGeoFence(selectedLocation: LatLng, reminder: Reminder, geofencingClient: GeofencingClient){
        val geofence = Geofence.Builder().setRequestId(geofenceId).setCircularRegion(
            selectedLocation.latitude,
            selectedLocation.longitude,
            geofenceRadius.toFloat()).setExpirationDuration(geofenceExpiration.toLong()).setTransitionTypes(
            Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL).setLoiteringDelay(geofenceDwellDelay).build()
        val geofenceRequest = GeofencingRequest.Builder().setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
            .addGeofence(geofence).build()
        val intent = Intent(this, GeoFenceReceiver::class.java).putExtra("uid", reminder.uid)
            .putExtra("message", reminder.msg)
            .putExtra("location", reminder.location)

        val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        geofencingClient.addGeofences(geofenceRequest,pendingIntent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray)
    {
        if (requestCode==123) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_DENIED ||
                        grantResults[1] == PackageManager.PERMISSION_DENIED))
            {
                toast("The reminder needs all the permissions to function")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                if (grantResults.isNotEmpty() && (grantResults[2] == PackageManager.PERMISSION_DENIED))
                {
                    toast("The reminder needs all the permissions to function")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onMapReady(map: GoogleMap?) {
        gMap = map ?: return
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED))
        {
            gMap.isMyLocationEnabled = true
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->

                if (location != null)
                {
                    val latLong = LatLng(location.latitude, location.longitude)
                    with(gMap) {
                        animateCamera(CameraUpdateFactory.newLatLngZoom(latLong, 13f))
                    }
                }
            }
        }
        else
        {
            val permission = mutableListOf<String>()
            permission.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            permission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)

            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
            {
                permission.add(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            ActivityCompat.requestPermissions(
                this, permission.toTypedArray(),
                123
            )
        }

        gMap.setOnMapClickListener { location: LatLng ->
            with(gMap) {
                clear()
                animateCamera(CameraUpdateFactory.newLatLngZoom(location, 13f))

                val geocoder = Geocoder(applicationContext, Locale.getDefault())
                var title = ""
                var city = ""
                try {
                    val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    city = addressList[0].locality
                    title = addressList[0].getAddressLine(0)
                }
                catch (e:Exception)
                {
                }

                val marker = addMarker(MarkerOptions().position(location).snippet(title).title(city))
                marker.showInfoWindow()

                //addCircle(com.google.android.gms.maps.model.CircleOptions().center(location)
                //    .strokeColor(Color.argb(50, 70, 70, 70))
                 //   .fillColor(Color.argb(100, 150, 150, 150)))
                addCircle(CircleOptions().center(location).strokeColor(Color.argb(
                    50,
                    70,
                    70,
                    70)).fillColor(Color.argb(100, 150, 150, 150)))
                selectedLocation = location
            }
        }
    }
}
