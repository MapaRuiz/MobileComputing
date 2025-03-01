package com.example.taller1

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.taller1.databinding.ActivityRandomBinding

class RandomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRandomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRandomBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val Idioma = intent.getStringExtra("Idioma")
        if(Idioma.equals("Frances")){ binding.textView.text = getString(R.string.saludo_FR)}
        else if (Idioma.equals("Español")){ binding.textView.text = getString(R.string.saludo_ES)}
        else if (Idioma.equals("Ingles")){ binding.textView.text = getString(R.string.saludo_EN)}
        else if (Idioma.equals("Italiano")){ binding.textView.text = getString(R.string.saludo_IT)}
        else if (Idioma.equals("Aleman")){ binding.textView.text = getString(R.string.saludo_GER)}
    }
}