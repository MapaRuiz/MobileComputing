package com.example.taller1

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

        val name = intent.getStringExtra("name")
        val capital = intent.getStringExtra("capital")
        val flag = intent.getStringExtra("flag")
        val flagResId = resources.getIdentifier(flag, "drawable", packageName)
        val flagDrawable = ResourcesCompat.getDrawable(resources, flagResId, theme)

        binding.textViewName.text = name
        binding.textViewCapital.text = "Information: $capital"
        binding.imageViewFlag.setImageDrawable(flagDrawable)
    }
}