package com.example.taller2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.taller2.databinding.ActivityMapsBinding
import org.osmdroid.api.IMapController
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MapsActivity : AppCompatActivity(), MapEventsReceiver {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private val LIGHT_THRESHOLD = 5000f

    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private var lastRecordedLocation: GeoPoint? = null

    private var searchMarker: Marker? = null
    private var routeOverlay: Polyline? = null
    private lateinit var roadManager: OSRMRoadManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Permitir llamadas de red en hilo principal (solo pruebas)
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())

        // Configurar rutas de osmdroid
        var osmdroidBasePath = File(cacheDir, "osmdroid").apply { mkdirs() }
        var osmdroidTileCache = File(osmdroidBasePath, "Tiles").apply { mkdirs() }
        Configuration.getInstance().apply {
            osmdroidBasePath = osmdroidBasePath
            osmdroidTileCache = osmdroidTileCache
            userAgentValue = "com.example.taller2" // Usa tu package name
            load(this@MapsActivity, getPreferences(Context.MODE_PRIVATE))
        }
        Configuration.getInstance().isDebugMapView = true
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.osmMap.setTileSource(TileSourceFactory.MAPNIK)
        binding.osmMap.setMultiTouchControls(true)
        val controller: IMapController = binding.osmMap.controller
        controller.setZoom(15.0)

        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), binding.osmMap)
        // Verificar permisos antes de centrar en la ubicación del usuario
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myLocationOverlay.enableMyLocation()
            myLocationOverlay.runOnFirstFix {
                runOnUiThread {
                    controller.setCenter(myLocationOverlay.myLocation)
                }
            }
        } else {
            // Posición por defecto si no hay permisos
            controller.setCenter(GeoPoint(4.5983024, -74.0759898))
        }

        roadManager = OSRMRoadManager(this, "ANDROID")
        binding.osmMap.overlays.clear() // Limpiar overlays al inicio
        binding.osmMap.overlays.add(myLocationOverlay) // Agregar solo lo necesario
        binding.osmMap.overlays.add(MapEventsOverlay(this)) // <-- Asegurar esta línea

        binding.addressEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                performAddressSearch()
                true
            } else false
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 100)
        } else {
            enableLocationFeatures()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableLocationFeatures()
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var locationManager: LocationManager
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val geoPoint = GeoPoint(location.latitude, location.longitude)
            updateUserLocation(geoPoint)
        }
    }

    private fun enableLocationFeatures() {
        myLocationOverlay.enableMyLocation()
        binding.osmMap.overlays.add(myLocationOverlay)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,  // Intervalo de actualización: 5 segundos
                30f,   // Distancia mínima: 30 metros
                locationListener
            )
        }
    }

    override fun onResume() {
        super.onResume()
        binding.osmMap.onResume()
        lightSensor?.also { sensorManager.registerListener(lightSensorListener, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onPause() {
        super.onPause()
        binding.osmMap.onPause()
        sensorManager.unregisterListener(lightSensorListener)
    }

    private val lightSensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: android.hardware.SensorEvent) {
            applyMapStyleBasedOnLight(event.values[0])
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private fun applyMapStyleBasedOnLight(lux: Float) {
        // Invertir colores en modo oscuro
        val tilesOverlay = binding.osmMap.overlayManager.tilesOverlay
        if (lux < LIGHT_THRESHOLD) {
            tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        } else {
            tilesOverlay.setColorFilter(null)
        }
        binding.osmMap.invalidate()
    }

    private fun updateUserLocation(location: GeoPoint) {
        if (lastRecordedLocation == null || lastRecordedLocation!!.distanceToAsDouble(location) > 30) {
            lastRecordedLocation = location
            saveLocationToJson(location)
            binding.osmMap.controller.animateTo(location)
        }
    }

    private fun saveLocationToJson(location: GeoPoint) {
        val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val rec = "{\"latitude\": ${location.latitude}, \"longitude\": ${location.longitude}, \"date\": \"$ts\"}\n"
        try {
            openFileOutput("locations.json", MODE_APPEND).use { it.write(rec.toByteArray()) }
        } catch (e: IOException) { e.printStackTrace() }
    }

    private fun performAddressSearch() {
        val text = binding.addressEditText.text.toString().trim()
        if (text.isEmpty()) {
            Toast.makeText(this, "La dirección está vacía", Toast.LENGTH_SHORT).show()
            return
        }
        val geocoder = Geocoder(this, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocationName(text, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    handleAddressSearchResult(addresses)
                }
                override fun onError(errorMessage: String?) {
                    Toast.makeText(this@MapsActivity, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            @Suppress("DEPRECATION")
            try {
                val addresses = geocoder.getFromLocationName(text, 1)
                handleAddressSearchResult(addresses)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun handleAddressSearchResult(addresses: MutableList<Address>?) {
        runOnUiThread {
            if (!addresses.isNullOrEmpty()) {
                val pt = GeoPoint(addresses[0].latitude, addresses[0].longitude)
                searchMarker?.let { binding.osmMap.overlays.remove(it) }
                searchMarker = Marker(binding.osmMap).apply { position = pt; title = addresses[0].getAddressLine(0) }
                binding.osmMap.overlays.add(searchMarker)
                binding.osmMap.controller.setZoom(15.0); binding.osmMap.controller.animateTo(pt)
                showDistanceAndRoute(pt)
            }
        }
    }

    override fun longPressHelper(p: GeoPoint): Boolean {
        val geocoder = Geocoder(this, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(p.latitude, p.longitude, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    handleGeocodeResult(addresses, p)
                }
                override fun onError(errorMessage: String?) {
                    Toast.makeText(this@MapsActivity, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            @Suppress("DEPRECATION")
            try {
                val addresses = geocoder.getFromLocation(p.latitude, p.longitude, 1)
                handleGeocodeResult(addresses, p)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return true
    }

    // En handleGeocodeResult():
    private fun handleGeocodeResult(addresses: MutableList<Address>?, p: GeoPoint) {
        runOnUiThread {
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0].getAddressLine(0) ?: "Ubicación desconocida"
                searchMarker?.let { binding.osmMap.overlays.remove(it) }
                searchMarker = Marker(binding.osmMap).apply {
                    position = p
                    title = addr
                }
                binding.osmMap.overlays.add(searchMarker)
                binding.osmMap.controller.animateTo(p) // Ahora en el hilo UI
                showDistanceAndRoute(p)
            }
        }
    }

    override fun singleTapConfirmedHelper(p: GeoPoint) = false

    private fun showDistanceAndRoute(target: GeoPoint) {
        (myLocationOverlay.myLocation ?: lastRecordedLocation)?.let { userLoc ->
            val dist = userLoc.distanceToAsDouble(target)
            runOnUiThread {
                Toast.makeText(this, "Distancia: ${"%.2f".format(dist)} m", Toast.LENGTH_LONG).show()
                drawRoute(userLoc, target)
            }
        }
    }

    private fun drawRoute(start: GeoPoint, end: GeoPoint) {
        routeOverlay?.let { binding.osmMap.overlays.remove(it) }
        val routePoints = ArrayList<GeoPoint>(listOf(start, end))
        try {
            val road = roadManager.getRoad(routePoints)
            if (road.mStatus != Road.STATUS_OK) {
                Log.e("ROUTE", "Error en la ruta: ${road.mStatus}")
                Toast.makeText(this, "Error al calcular la ruta", Toast.LENGTH_SHORT).show()
                return
            }
            routeOverlay = RoadManager.buildRoadOverlay(road).apply {
                outlinePaint.color = Color.RED
                outlinePaint.strokeWidth = 8f
            }
            binding.osmMap.overlays.add(routeOverlay)
            binding.osmMap.invalidate()
        } catch (e: Exception) {
            Log.e("ROUTE", "Excepción: ${e.message}")
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
