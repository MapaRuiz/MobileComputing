package com.example.taller1

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.taller1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Datos en el spinner
        val opciones = listOf("Français", "Español", "English", "Italiano", "Русский")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter

        binding.btnTic.setOnClickListener {
            Toast.makeText(applicationContext, "Dirigiendose a TicTacToe", Toast.LENGTH_LONG).show()
            val i1 = Intent(this, TictactoeActivity::class.java)
            startActivity(i1)
        }
        binding.btnRand.setOnClickListener {
            Toast.makeText(applicationContext, "Dirigiendose a Random", Toast.LENGTH_LONG).show()
            val Idioma = binding.spinner.selectedItem.toString()
            val i2 = Intent(this, RandomActivity::class.java)
            i2.putExtra("Idioma", Idioma)
            startActivity(i2)
        }
        binding.btnCountries.setOnClickListener {
            Toast.makeText(applicationContext, "Dirigiendose a Countries", Toast.LENGTH_LONG).show()
            val i3 = Intent(this, CountriesActivity::class.java)
            startActivity(i3)
        }

    }
}