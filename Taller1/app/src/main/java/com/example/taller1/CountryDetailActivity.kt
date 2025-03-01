package com.example.taller1

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.taller1.databinding.ActivityCountryDetailBinding

class CountryDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCountryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCountryDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val capital = intent.getStringExtra("capital")
        val nombre_pais = intent.getStringExtra("nombre_pais")
        val nombre_pais_int = intent.getStringExtra("nombre_pais_int")
        val sigla = intent.getStringExtra("sigla")
        val siglalw = sigla.toString().lowercase()

        binding.textViewName.text = nombre_pais
        binding.textViewNameEng.text = nombre_pais_int
        binding.textViewSiglas.text = sigla
        binding.textViewCapital.text = "Capital: $capital"
        binding.imageViewFlag.setImageResource(getFlagResId(this, siglalw))
    }
    fun getFlagResId(context: Context, countryCode: String): Int {
        return try {
            val field = R.drawable::class.java.getField("flag_${countryCode}")
            field.getInt(null)
        } catch (e: Exception) {
            R.drawable.flag_co // Use a default image if not found
        }
    }


}