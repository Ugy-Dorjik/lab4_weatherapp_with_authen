package com.example.lab4_weatherapp_with_authen

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.lab4_weatherapp_with_authen.database.WeatherDbHelper
import com.example.lab4_weatherapp_with_authen.database.WeatherRecord

class EditWeatherActivity :
    AppCompatActivity() {

    private lateinit var dbHelper:
            WeatherDbHelper

    private var recordId =
        -1

    private var currentUnit =
        ""

    private var savedDate =
        ""

    private lateinit var etLocation:
            EditText

    private lateinit var etTemperature:
            EditText

    private lateinit var etCondition:
            EditText

    private lateinit var etHumidity:
            EditText

    private lateinit var etWind:
            EditText

    private lateinit var tvUnit:
            TextView

    private lateinit var tvSavedDate:
            TextView

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_edit_weather
        )

        dbHelper =
            WeatherDbHelper(this)

        etLocation =
            findViewById(
                R.id.etEditLocation
            )

        etTemperature =
            findViewById(
                R.id.etEditTemperature
            )

        etCondition =
            findViewById(
                R.id.etEditCondition
            )

        etHumidity =
            findViewById(
                R.id.etEditHumidity
            )

        etWind =
            findViewById(
                R.id.etEditWind
            )

        tvUnit =
            findViewById(
                R.id.tvEditUnit
            )

        tvSavedDate =
            findViewById(
                R.id.tvEditSavedDate
            )

        val btnUpdate =
            findViewById<Button>(
                R.id.btnUpdate
            )

        val btnDelete =
            findViewById<Button>(
                R.id.btnDelete
            )

        recordId =
            intent.getIntExtra(
                "record_id",
                -1
            )

        if (recordId == -1) {

            Toast.makeText(
                this,
                "Invalid weather record.",
                Toast.LENGTH_SHORT
            ).show()

            finish()

            return
        }

        loadRecord()

        btnUpdate.setOnClickListener {

            updateRecord()
        }

        btnDelete.setOnClickListener {

            confirmDelete()
        }
    }

    private fun loadRecord() {

        val record =
            dbHelper.getWeatherById(
                recordId
            )

        if (record == null) {

            Toast.makeText(
                this,
                "Record not found.",
                Toast.LENGTH_SHORT
            ).show()

            finish()

            return
        }

        currentUnit =
            record.unit

        savedDate =
            record.savedAt

        etLocation.setText(
            record.location
        )

        etTemperature.setText(
            record.temperature.toString()
        )

        etCondition.setText(
            record.condition
        )

        etHumidity.setText(
            record.humidity.toString()
        )

        etWind.setText(
            record.windSpeed.toString()
        )

        tvUnit.text =
            "Unit: ${record.unit}"

        tvSavedDate.text =
            "Saved: ${record.savedAt}"
    }

    private fun updateRecord() {

        val location =
            etLocation.text
                .toString()
                .trim()

        val condition =
            etCondition.text
                .toString()
                .trim()

        val temperature =
            etTemperature.text
                .toString()
                .toDoubleOrNull()

        val humidity =
            etHumidity.text
                .toString()
                .toIntOrNull()

        val wind =
            etWind.text
                .toString()
                .toDoubleOrNull()

        if (location.isEmpty()) {

            etLocation.error =
                "Location is required"

            return
        }

        if (condition.isEmpty()) {

            etCondition.error =
                "Condition is required"

            return
        }

        if (temperature == null) {

            etTemperature.error =
                "Enter a valid temperature"

            return
        }

        if (
            humidity == null ||
            humidity !in 0..100
        ) {

            etHumidity.error =
                "Humidity must be between 0 and 100"

            return
        }

        if (wind == null) {

            etWind.error =
                "Enter valid wind speed"

            return
        }

        val updatedWeather =
            WeatherRecord(

                id =
                    recordId,

                location =
                    location,

                temperature =
                    temperature,

                condition =
                    condition,

                humidity =
                    humidity,

                windSpeed =
                    wind,

                unit =
                    currentUnit,

                savedAt =
                    savedDate
            )

        val rowsUpdated =
            dbHelper.updateWeather(
                updatedWeather
            )

        if (rowsUpdated > 0) {

            Toast.makeText(
                this,
                "Record updated.",
                Toast.LENGTH_SHORT
            ).show()

            finish()

        } else {

            Toast.makeText(
                this,
                "Failed to update record.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun confirmDelete() {

        AlertDialog.Builder(this)

            .setTitle(
                "Delete Weather"
            )

            .setMessage(
                "Are you sure you want to delete this record?"
            )

            .setPositiveButton(
                "Delete"
            ) { _, _ ->

                deleteRecord()
            }

            .setNegativeButton(
                "Cancel",
                null
            )

            .show()
    }

    private fun deleteRecord() {

        val rowsDeleted =
            dbHelper.deleteWeather(
                recordId
            )

        if (rowsDeleted > 0) {

            Toast.makeText(
                this,
                "Record deleted.",
                Toast.LENGTH_SHORT
            ).show()

            finish()

        } else {

            Toast.makeText(
                this,
                "Failed to delete record.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}