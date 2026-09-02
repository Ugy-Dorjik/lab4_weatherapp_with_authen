package com.example.lab4_weatherapp_with_authen.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lab4_weatherapp_with_authen.R
import com.example.lab4_weatherapp_with_authen.database.WeatherRecord
import java.util.Locale

class WeatherAdapter(
    private var records: List<WeatherRecord>,
    private val onItemClick: (WeatherRecord) -> Unit
) : RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>() {

    class WeatherViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val location: TextView =
            itemView.findViewById(R.id.tvItemLocation)

        val temperature: TextView =
            itemView.findViewById(R.id.tvItemTemperature)

        val condition: TextView =
            itemView.findViewById(R.id.tvItemCondition)

        val humidity: TextView =
            itemView.findViewById(R.id.tvItemHumidity)

        val wind: TextView =
            itemView.findViewById(R.id.tvItemWind)

        val date: TextView =
            itemView.findViewById(R.id.tvItemDate)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WeatherViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_weather_record,
                parent,
                false
            )

        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: WeatherViewHolder,
        position: Int
    ) {

        val record = records[position]

        holder.location.text =
            record.location

        holder.temperature.text =
            String.format(
                Locale.getDefault(),
                "Temperature: %.1f %s",
                record.temperature,
                record.unit
            )

        holder.condition.text =
            "Condition: ${record.condition}"

        holder.humidity.text =
            "Humidity: ${record.humidity}%"

        holder.wind.text =
            String.format(
                Locale.getDefault(),
                "Wind Speed: %.1f km/h",
                record.windSpeed
            )

        holder.date.text =
            "Saved: ${record.savedAt}"

        holder.itemView.setOnClickListener {
            onItemClick(record)
        }
    }

    override fun getItemCount(): Int {
        return records.size
    }

    fun updateData(newRecords: List<WeatherRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }
}