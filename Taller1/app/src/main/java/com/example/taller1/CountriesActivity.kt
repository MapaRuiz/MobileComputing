package com.example.taller1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.taller1.databinding.ActivityCountriesBinding
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset

class CountriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCountriesBinding
    private lateinit var countryList: ArrayList<Country>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCountriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        countryList = loadCountriesFromJson()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, countryList.map { it.nombre_pais })
        binding.listView.adapter = adapter

        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val selectedCountry = countryList[position]
            val intent = Intent(this, CountryDetailActivity::class.java).apply {
                putExtra("capital", selectedCountry.capital)
                putExtra("nombre_pais", selectedCountry.nombre_pais)
                putExtra("nombre_pais_int", selectedCountry.nombre_pais_int)
                putExtra("sigla", selectedCountry.sigla)
            }
            startActivity(intent)
        }
    }

    private fun loadCountriesFromJson(): ArrayList<Country> {
        val countryList = ArrayList<Country>()
        try {
            val inputStream = assets.open("paises.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            val json = String(buffer, Charset.forName("UTF-8"))
            val jsonObject = JSONObject(json)
            val jsonArray = jsonObject.getJSONArray("paises")


            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val country = Country(
                    capital = jsonObject.getString("capital"),
                    nombre_pais = jsonObject.getString("nombre_pais"),
                    nombre_pais_int = jsonObject.getString("nombre_pais_int"),
                    sigla = jsonObject.getString("sigla")
                )
                countryList.add(country)
            }
        } catch (e: IOException) {
            Log.e("JSON Error", "Error reading JSON file", e)
        }
        return countryList
    }
}

data class Country(val capital: String, val nombre_pais: String, val nombre_pais_int: String, val sigla: String)