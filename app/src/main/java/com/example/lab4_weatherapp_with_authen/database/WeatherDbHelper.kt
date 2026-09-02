package com.example.lab4_weatherapp_with_authen.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class WeatherDbHelper(
    context: Context
) : SQLiteOpenHelper(

    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {

        private const val DATABASE_NAME =
            "weather.db"

        private const val DATABASE_VERSION = 1

        private const val TABLE_WEATHER =
            "weather_records"

        private const val COLUMN_ID = "id"

        private const val COLUMN_LOCATION =
            "location"

        private const val COLUMN_TEMPERATURE =
            "temperature"

        private const val COLUMN_CONDITION =
            "condition"

        private const val COLUMN_HUMIDITY =
            "humidity"

        private const val COLUMN_WIND =
            "wind_speed"

        private const val COLUMN_UNIT =
            "unit"

        private const val COLUMN_SAVED_AT =
            "saved_at"
    }

    override fun onCreate(db: SQLiteDatabase) {

        val createTable = """

            CREATE TABLE $TABLE_WEATHER (

                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,

                $COLUMN_LOCATION TEXT NOT NULL,

                $COLUMN_TEMPERATURE REAL NOT NULL,

                $COLUMN_CONDITION TEXT NOT NULL,

                $COLUMN_HUMIDITY INTEGER NOT NULL,

                $COLUMN_WIND REAL NOT NULL,

                $COLUMN_UNIT TEXT NOT NULL,

                $COLUMN_SAVED_AT TEXT NOT NULL
            )

        """.trimIndent()

        db.execSQL(createTable)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {

        db.execSQL(
            "DROP TABLE IF EXISTS $TABLE_WEATHER"
        )

        onCreate(db)
    }

    fun insertWeather(
        weather: WeatherRecord
    ): Long {

        val db = writableDatabase

        val values = ContentValues().apply {

            put(
                COLUMN_LOCATION,
                weather.location
            )

            put(
                COLUMN_TEMPERATURE,
                weather.temperature
            )

            put(
                COLUMN_CONDITION,
                weather.condition
            )

            put(
                COLUMN_HUMIDITY,
                weather.humidity
            )

            put(
                COLUMN_WIND,
                weather.windSpeed
            )

            put(
                COLUMN_UNIT,
                weather.unit
            )

            put(
                COLUMN_SAVED_AT,
                weather.savedAt
            )
        }

        return db.insert(
            TABLE_WEATHER,
            null,
            values
        )
    }

    fun getAllWeather():
            MutableList<WeatherRecord> {

        val weatherList =
            mutableListOf<WeatherRecord>()

        val db = readableDatabase

        val cursor = db.rawQuery(

            "SELECT * FROM $TABLE_WEATHER " +
                    "ORDER BY $COLUMN_ID DESC",

            null
        )

        cursor.use {

            while (it.moveToNext()) {

                weatherList.add(

                    WeatherRecord(

                        id = it.getInt(
                            it.getColumnIndexOrThrow(
                                COLUMN_ID
                            )
                        ),

                        location =
                            it.getString(
                                it.getColumnIndexOrThrow(
                                    COLUMN_LOCATION
                                )
                            ),

                        temperature =
                            it.getDouble(
                                it.getColumnIndexOrThrow(
                                    COLUMN_TEMPERATURE
                                )
                            ),

                        condition =
                            it.getString(
                                it.getColumnIndexOrThrow(
                                    COLUMN_CONDITION
                                )
                            ),

                        humidity =
                            it.getInt(
                                it.getColumnIndexOrThrow(
                                    COLUMN_HUMIDITY
                                )
                            ),

                        windSpeed =
                            it.getDouble(
                                it.getColumnIndexOrThrow(
                                    COLUMN_WIND
                                )
                            ),

                        unit =
                            it.getString(
                                it.getColumnIndexOrThrow(
                                    COLUMN_UNIT
                                )
                            ),

                        savedAt =
                            it.getString(
                                it.getColumnIndexOrThrow(
                                    COLUMN_SAVED_AT
                                )
                            )
                    )
                )
            }
        }

        return weatherList
    }

    fun getWeatherById(
        id: Int
    ): WeatherRecord? {

        val db = readableDatabase

        val cursor = db.query(

            TABLE_WEATHER,

            null,

            "$COLUMN_ID = ?",

            arrayOf(id.toString()),

            null,

            null,

            null
        )

        cursor.use {

            if (it.moveToFirst()) {

                return WeatherRecord(

                    id = it.getInt(
                        it.getColumnIndexOrThrow(
                            COLUMN_ID
                        )
                    ),

                    location =
                        it.getString(
                            it.getColumnIndexOrThrow(
                                COLUMN_LOCATION
                            )
                        ),

                    temperature =
                        it.getDouble(
                            it.getColumnIndexOrThrow(
                                COLUMN_TEMPERATURE
                            )
                        ),

                    condition =
                        it.getString(
                            it.getColumnIndexOrThrow(
                                COLUMN_CONDITION
                            )
                        ),

                    humidity =
                        it.getInt(
                            it.getColumnIndexOrThrow(
                                COLUMN_HUMIDITY
                            )
                        ),

                    windSpeed =
                        it.getDouble(
                            it.getColumnIndexOrThrow(
                                COLUMN_WIND
                            )
                        ),

                    unit =
                        it.getString(
                            it.getColumnIndexOrThrow(
                                COLUMN_UNIT
                            )
                        ),

                    savedAt =
                        it.getString(
                            it.getColumnIndexOrThrow(
                                COLUMN_SAVED_AT
                            )
                        )
                )
            }
        }

        return null
    }

    fun updateWeather(
        weather: WeatherRecord
    ): Int {

        val db = writableDatabase

        val values = ContentValues().apply {

            put(
                COLUMN_LOCATION,
                weather.location
            )

            put(
                COLUMN_TEMPERATURE,
                weather.temperature
            )

            put(
                COLUMN_CONDITION,
                weather.condition
            )

            put(
                COLUMN_HUMIDITY,
                weather.humidity
            )

            put(
                COLUMN_WIND,
                weather.windSpeed
            )

            put(
                COLUMN_UNIT,
                weather.unit
            )

            put(
                COLUMN_SAVED_AT,
                weather.savedAt
            )
        }

        return db.update(

            TABLE_WEATHER,

            values,

            "$COLUMN_ID = ?",

            arrayOf(
                weather.id.toString()
            )
        )
    }

    fun deleteWeather(
        id: Int
    ): Int {

        val db = writableDatabase

        return db.delete(

            TABLE_WEATHER,

            "$COLUMN_ID = ?",

            arrayOf(id.toString())
        )
    }
}