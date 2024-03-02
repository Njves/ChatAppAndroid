package com.example.androiduni

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.androiduni.message.geo.GeoRequest
import com.example.androiduni.message.geo.model.GeoObject
import com.example.androiduni.message.geo.model.UserGeo
import com.example.androiduni.socket.Socket
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Arrays


class MapActivity : AppCompatActivity() {
    private lateinit var locationOverlay: MyLocationNewOverlay
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var map : MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val geoService = Client.getClient().create(GeoRequest::class.java)
    private var roomId: Int = -1
    private lateinit var root: LinearLayout
    private var roomName: String = ""
    private var snackbar: Snackbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        root = findViewById(R.id.root)
        intent.extras.let {
            if (it != null) {
                roomId = it.getInt("roomId", 0)
                roomName = it.getString("roomName", "")
            }
        }
        if(roomId == -1 || roomName.isEmpty()) {
            finish()
        }
        askLocation()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Карта комнаты $roomName"
        requestPermissionsIfNecessary(listOf(Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))

        getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        map = findViewById(R.id.map)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Нет прав", Toast.LENGTH_SHORT).show()
            return
        }
        setStartMapSettings()
        setGeoListener()
    }

    private fun setStartMapSettings() {
        map.setTileSource(TileSourceFactory.MAPNIK)
        val startPoint = GeoPoint(56.0230903, 92.8376283);
        map.controller.setZoom(9.5)
        map.controller.setCenter(startPoint)

        val rotationGestureOverlay = RotationGestureOverlay(map)
        rotationGestureOverlay.isEnabled
        map.setMultiTouchControls(true)
        map.overlays.add(rotationGestureOverlay)
    }

    private fun setGeoListener() {
        val gson = Gson()
        Socket.get().on("geo") {
            Log.d("MainActity", Arrays.toString(it))
            val userGeo: UserGeo = gson.fromJson(it.get(0).toString(), UserGeo::class.java)
            runOnUiThread{
                createMarker(userGeo)
            }
        }
    }

    private fun askLocation() {
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }

        if (!gps_enabled && !network_enabled) {
            AlertDialog.Builder(this)
                .setMessage("Включите геоположение и перезайдите")
                .setPositiveButton("Ok")
                { paramDialogInterface, paramInt ->

                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun createMarker(userGeo: UserGeo) {
        if(isDestroyed){
            return
        }
        val marker = Marker(map)
        Log.d(this@MapActivity.toString(), userGeo.toString())
        marker.position = GeoPoint(userGeo.lat, userGeo.lon)
        marker.icon = ContextCompat.getDrawable(this, R.drawable.ic_marker)
        marker.title = userGeo.user.username
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        map.overlays.add(marker)
    }

    override fun onResume() {
        super.onResume()
        geoService.getGeo(UserProvider.token!!, roomId).enqueue(object: Callback<GeoObject> {
            override fun onResponse(call: Call<GeoObject>, response: Response<GeoObject>) {
                Log.d(this@MapActivity.toString(), response.toString())
                if(response.isSuccessful) {
                    response.body()?.let {
                        it.users.forEach {userGeo ->
                            createMarker(userGeo)
                        }
                    }
                }
            }
            override fun onFailure(call: Call<GeoObject>, t: Throwable) {
                Log.d(this@MapActivity.toString(), t.toString())
            }
        })

        val gpsProvider = GpsMyLocationProvider(this)
        locationOverlay = MyLocationNewOverlay(gpsProvider, this.map);

        fusedLocationClient.lastLocation.addOnSuccessListener {
            if(it == null) {
                snackbar = Snackbar.make(root, "Неудалось получить местоположение", Snackbar.LENGTH_INDEFINITE)
                snackbar?.show()
            } else {
                snackbar?.dismiss()
            }
            it?.let {
                Log.d(this@MapActivity.toString(), "$it")
                Socket.get().emit("push_geo", Gson().toJson(mapOf("lon" to it.longitude, "lat" to it.latitude)))
            }
        }

        val success = gpsProvider.startLocationProvider(locationOverlay)
        locationOverlay.enableMyLocation();
        if(success) {
            val loc = gpsProvider.lastKnownLocation
            if(loc == null) {
                snackbar = Snackbar.make(root, "Неудалось получить местоположение", Snackbar.LENGTH_INDEFINITE)
//                snackbar?.show()
            } else {
                snackbar?.dismiss()
                Socket.get().emit("push_geo", Gson().toJson(mapOf("lon" to loc.longitude, "lat" to loc.latitude)))
            }
        }

        map.overlays.add(locationOverlay);
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionsToRequest = ArrayList<String>()
        var i = 0
        while (i < grantResults.size) {
            permissionsToRequest.add(permissions[i])
            i++
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }


    private fun requestPermissionsIfNecessary(permissions: List<String>) {
        val permissionsToRequest = mutableListOf<String>()
        permissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Socket.get().emit("recall_geo")
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}