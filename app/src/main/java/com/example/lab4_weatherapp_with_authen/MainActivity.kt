package com.example.lab4_weatherapp_with_authen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.example.lab4_weatherapp_with_authen.api.RetrofitClient
import com.example.lab4_weatherapp_with_authen.api.WeatherResponse
import com.example.lab4_weatherapp_with_authen.database.WeatherDbHelper
import com.example.lab4_weatherapp_with_authen.database.WeatherRecord
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tvLocation: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvCondition: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvWind: TextView
    private lateinit var tvError: TextView

    private lateinit var progressBar: ProgressBar

    private lateinit var switchUnit: SwitchCompat

    private lateinit var dbHelper: WeatherDbHelper

    private var currentLocationName =
        "Unknown Location"

    private var currentTemperature = 0.0

    private var currentCondition = ""

    private var currentHumidity = 0

    private var currentWindSpeed = 0.0

    private var weatherLoaded = false

    private val permissionLauncher =

        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineLocation =
                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] ?: false

            val coarseLocation =
                permissions[
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ] ?: false

            if (fineLocation || coarseLocation) {

                getCurrentLocation()

            } else {

                showError(
                    "Location permission denied. " +
                            "Weather cannot be loaded."
                )
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (
            FirebaseAuth.getInstance()
                .currentUser == null
        ) {

            openLogin()

            return
        }

        dbHelper =
            WeatherDbHelper(this)

        tvLocation =
            findViewById(R.id.tvLocation)

        tvTemperature =
            findViewById(R.id.tvTemperature)

        tvCondition =
            findViewById(R.id.tvCondition)

        tvHumidity =
            findViewById(R.id.tvHumidity)

        tvWind =
            findViewById(R.id.tvWind)

        tvError =
            findViewById(R.id.tvError)

        progressBar =
            findViewById(
                R.id.weatherProgressBar
            )

        switchUnit =
            findViewById(R.id.switchUnit)

        val btnRefresh =
            findViewById<Button>(
                R.id.btnRefresh
            )

        val btnSave =
            findViewById<Button>(
                R.id.btnSave
            )

        val btnSaved =
            findViewById<Button>(
                R.id.btnSavedRecords
            )

        val btnShare =
            findViewById<Button>(
                R.id.btnShare
            )

        val btnLogout =
            findViewById<Button>(
                R.id.btnLogout
            )

        // SharedPreferences
        val preferences =
            getSharedPreferences(
                "weather_preferences",
                MODE_PRIVATE
            )

        val useFahrenheit =
            preferences.getBoolean(
                "use_fahrenheit",
                false
            )

        switchUnit.isChecked =
            useFahrenheit

        switchUnit.setOnCheckedChangeListener {
                _, isChecked ->

            preferences.edit()
                .putBoolean(
                    "use_fahrenheit",
                    isChecked
                )
                .apply()

            if (weatherLoaded) {

                loadWeather()
            }
        }

        btnRefresh.setOnClickListener {

            loadWeather()
        }

        btnSave.setOnClickListener {

            saveWeather()
        }

        btnSaved.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    SavedWeatherActivity::class.java
                )
            )
        }

        btnShare.setOnClickListener {

            shareWeather()
        }

        btnLogout.setOnClickListener {

            FirebaseAuth.getInstance()
                .signOut()

            openLogin()
        }

        loadWeather()
    }

    private fun loadWeather() {

        hideError()

        if (!isInternetAvailable()) {

            showError(
                "Internet connection is unavailable."
            )

            return
        }

        checkLocationPermission()
    }

    private fun checkLocationPermission() {

        val fineLocation =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        val coarseLocation =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        if (
            fineLocation ==
            PackageManager.PERMISSION_GRANTED ||
            coarseLocation ==
            PackageManager.PERMISSION_GRANTED
        ) {

            getCurrentLocation()

        } else {

            permissionLauncher.launch(

                arrayOf(

                    Manifest.permission.ACCESS_FINE_LOCATION,

                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun getCurrentLocation() {

        showLoading(true)

        val fusedLocationClient =
            LocationServices
                .getFusedLocationProviderClient(
                    this
                )

        try {

            val cancellationToken =
                CancellationTokenSource()

            fusedLocationClient
                .getCurrentLocation(
                    Priority
                        .PRIORITY_HIGH_ACCURACY,
                    cancellationToken.token
                )
                .addOnSuccessListener {
                        location ->

                    if (location != null) {

                        val latitude =
                            location.latitude

                        val longitude =
                            location.longitude

                        getLocationName(
                            latitude,
                            longitude
                        )

                        fetchWeather(
                            latitude,
                            longitude
                        )

                    } else {

                        showLoading(false)

                        showError(
                            "Unable to retrieve current location. " +
                                    "Make sure location services are enabled."
                        )
                    }
                }
                .addOnFailureListener {

                    showLoading(false)

                    showError(
                        "Failed to retrieve location: " +
                                (
                                        it.message
                                            ?: "Unknown error"
                                        )
                    )
                }

        } catch (
            exception:
            SecurityException
        ) {

            showLoading(false)

            showError(
                "Location permission is required."
            )
        }
    }

    private fun getLocationName(
        latitude: Double,
        longitude: Double
    ) {

        val geocoder =
            Geocoder(
                this,
                Locale.getDefault()
            )

        if (Build.VERSION.SDK_INT >= 33) {

            geocoder.getFromLocation(
                latitude,
                longitude,
                1
            ) { addresses ->

                if (
                    addresses.isNotEmpty()
                ) {

                    val address =
                        addresses[0]

                    currentLocationName =
                        address.locality
                            ?: address.subAdminArea
                                    ?: address.adminArea
                                    ?: address.countryName
                                    ?: "Unknown Location"

                    tvLocation.text =
                        "Location: $currentLocationName"
                }
            }

        } else {

            try {

                @Suppress("DEPRECATION")

                val addresses =
                    geocoder.getFromLocation(
                        latitude,
                        longitude,
                        1
                    )

                if (
                    !addresses.isNullOrEmpty()
                ) {

                    val address =
                        addresses[0]

                    currentLocationName =
                        address.locality
                            ?: address.subAdminArea
                                    ?: address.adminArea
                                    ?: address.countryName
                                    ?: "Unknown Location"

                    tvLocation.text =
                        "Location: $currentLocationName"
                }

            } catch (
                exception: Exception
            ) {

                currentLocationName =
                    "Lat %.3f, Lon %.3f".format(
                        latitude,
                        longitude
                    )

                tvLocation.text =
                    "Location: $currentLocationName"
            }
        }
    }

    private fun fetchWeather(
        latitude: Double,
        longitude: Double
    ) {

        val temperatureUnit =

            if (switchUnit.isChecked) {

                "fahrenheit"

            } else {

                "celsius"
            }

        RetrofitClient.api
            .getCurrentWeather(

                latitude = latitude,

                longitude = longitude,

                temperatureUnit =
                    temperatureUnit
            )
            .enqueue(

                object :
                    Callback<WeatherResponse> {

                    override fun onResponse(
                        call:
                        Call<WeatherResponse>,

                        response:
                        Response<WeatherResponse>
                    ) {

                        showLoading(false)

                        if (
                            response.isSuccessful &&
                            response.body() != null
                        ) {

                            val weather =
                                response.body()!!.current

                            currentTemperature =
                                weather.temperature

                            currentHumidity =
                                weather.humidity

                            currentWindSpeed =
                                weather.windSpeed

                            currentCondition =
                                getWeatherCondition(
                                    weather.weatherCode
                                )

                            weatherLoaded = true

                            displayWeather()

                        } else {

                            showError(
                                "Failed to retrieve weather information."
                            )
                        }
                    }

                    override fun onFailure(
                        call:
                        Call<WeatherResponse>,

                        throwable:
                        Throwable
                    ) {

                        showLoading(false)

                        showError(
                            "Weather API request failed: " +
                                    (
                                            throwable.message
                                                ?: "Unknown error"
                                            )
                        )
                    }
                }
            )
    }

    private fun displayWeather() {

        val unit =

            if (
                switchUnit.isChecked
            ) {

                "°F"

            } else {

                "°C"
            }

        tvTemperature.text =
            "Temperature: " +
                    String.format(
                        Locale.getDefault(),
                        "%.1f %s",
                        currentTemperature,
                        unit
                    )

        tvCondition.text =
            "Condition: $currentCondition"

        tvHumidity.text =
            "Humidity: $currentHumidity%"

        tvWind.text =
            "Wind Speed: " +
                    String.format(
                        Locale.getDefault(),
                        "%.1f km/h",
                        currentWindSpeed
                    )
    }

    private fun getWeatherCondition(
        code: Int
    ): String {

        return when (code) {

            0 ->
                "Clear sky"

            1 ->
                "Mainly clear"

            2 ->
                "Partly cloudy"

            3 ->
                "Overcast"

            45,
            48 ->
                "Fog"

            51,
            53,
            55 ->
                "Drizzle"

            56,
            57 ->
                "Freezing drizzle"

            61,
            63,
            65 ->
                "Rain"

            66,
            67 ->
                "Freezing rain"

            71,
            73,
            75 ->
                "Snow"

            77 ->
                "Snow grains"

            80,
            81,
            82 ->
                "Rain showers"

            85,
            86 ->
                "Snow showers"

            95 ->
                "Thunderstorm"

            96,
            99 ->
                "Thunderstorm with hail"

            else ->
                "Unknown"
        }
    }

    private fun saveWeather() {

        if (!weatherLoaded) {

            Toast.makeText(
                this,
                "Load weather before saving.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val unit =

            if (
                switchUnit.isChecked
            ) {

                "°F"

            } else {

                "°C"
            }

        val dateFormat =
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm",
                Locale.getDefault()
            )

        val record =
            WeatherRecord(

                location =
                    currentLocationName,

                temperature =
                    currentTemperature,

                condition =
                    currentCondition,

                humidity =
                    currentHumidity,

                windSpeed =
                    currentWindSpeed,

                unit =
                    unit,

                savedAt =
                    dateFormat.format(
                        Date()
                    )
            )

        val result =
            dbHelper.insertWeather(
                record
            )

        if (result != -1L) {

            Toast.makeText(
                this,
                "Weather saved.",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            Toast.makeText(
                this,
                "Failed to save weather.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun shareWeather() {

        if (!weatherLoaded) {

            Toast.makeText(
                this,
                "Load weather before sharing.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val unit =

            if (
                switchUnit.isChecked
            ) {

                "°F"

            } else {

                "°C"
            }

        val text = """

            Weather in $currentLocationName

            Temperature: $currentTemperature $unit

            Condition: $currentCondition

            Humidity: $currentHumidity%

            Wind Speed: $currentWindSpeed km/h

        """.trimIndent()

        val shareIntent =
            Intent(
                Intent.ACTION_SEND
            ).apply {

                type = "text/plain"

                putExtra(
                    Intent.EXTRA_TEXT,
                    text
                )
            }

        startActivity(
            Intent.createChooser(
                shareIntent,
                "Share weather using"
            )
        )
    }

    private fun isInternetAvailable():
            Boolean {

        val connectivityManager =
            getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager

        val network =
            connectivityManager.activeNetwork
                ?: return false

        val capabilities =
            connectivityManager
                .getNetworkCapabilities(
                    network
                )
                ?: return false

        return capabilities.hasCapability(
            NetworkCapabilities
                .NET_CAPABILITY_INTERNET
        )
    }

    private fun showLoading(
        loading: Boolean
    ) {

        progressBar.visibility =

            if (loading)
                View.VISIBLE
            else
                View.GONE
    }

    private fun showError(
        message: String
    ) {

        tvError.visibility =
            View.VISIBLE

        tvError.text =
            message

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun hideError() {

        tvError.visibility =
            View.GONE
    }

    private fun openLogin() {

        val intent =
            Intent(
                this,
                LoginActivity::class.java
            )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        finish()
    }
}