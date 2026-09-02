package com.example.lab4_weatherapp_with_authen.database

data class WeatherRecord(

    val id: Int = 0,

    val location: String,

    val temperature: Double,

    val condition: String,

    val humidity: Int,

    val windSpeed: Double,

    val unit: String,

    val savedAt: String
)