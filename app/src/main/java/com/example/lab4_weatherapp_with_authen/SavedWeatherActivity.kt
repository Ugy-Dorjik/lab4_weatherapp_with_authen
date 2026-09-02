package com.example.lab4_weatherapp_with_authen

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab4_weatherapp_with_authen.adapter.WeatherAdapter
import com.example.lab4_weatherapp_with_authen.database.WeatherDbHelper

class SavedWeatherActivity : AppCompatActivity() {

    private lateinit var dbHelper: WeatherDbHelper
    private lateinit var adapter: WeatherAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoRecords: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_saved_weather)

        dbHelper = WeatherDbHelper(this)

        recyclerView = findViewById(R.id.recyclerWeather)
        tvNoRecords = findViewById(R.id.tvNoRecords)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = WeatherAdapter(
            emptyList()
        ) { weatherRecord ->

            val intent = Intent(
                this@SavedWeatherActivity,
                EditWeatherActivity::class.java
            )

            intent.putExtra(
                "record_id",
                weatherRecord.id
            )

            startActivity(intent)
        }

        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()

        loadRecords()
    }

    private fun loadRecords() {

        val records = dbHelper.getAllWeather()

        adapter.updateData(records)

        if (records.isEmpty()) {

            tvNoRecords.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE

        } else {

            tvNoRecords.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}