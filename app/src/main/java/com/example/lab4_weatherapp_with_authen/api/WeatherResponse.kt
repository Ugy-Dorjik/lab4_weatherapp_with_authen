package com.example.lab4_weatherapp_with_authen.api


import com.google.gson.annotations.SerializedName

data class WeatherResponse(

    val current: CurrentWeather
)

data class CurrentWeather(

    @SerializedName("temperature_2m")
    val temperature: Double,

    @SerializedName("relative_humidity_2m")
    val humidity: Int,

    @SerializedName("weather_code")
    val weatherCode: Int,

    @SerializedName("wind_speed_10m")
    val windSpeed: Double
)