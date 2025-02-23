package com.example.taller1

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
        if(Idioma.equals("Français")){ binding.textView.text = "Bonjour"}
        else if (Idioma.equals("Español")){ binding.textView.text = "Hola"}
        else if (Idioma.equals("English")){ binding.textView.text = "Hi"}
        else if (Idioma.equals("Italiano")){ binding.textView.text = "Ciao"}
        else if (Idioma.equals("Русский")){ binding.textView.text = "Здравствуйте"}
    }
}