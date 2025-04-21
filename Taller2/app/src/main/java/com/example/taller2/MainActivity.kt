package com.example.taller2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.taller2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater);val view = binding.root;setContentView(view)

        binding.btnContacts.setOnClickListener {
            val i1 = Intent(this, ContactsActivity::class.java)
            startActivity(i1)
        }
        binding.btnCamera.setOnClickListener {
            val i2 = Intent(this, CameraActivity::class.java)
            startActivity(i2)
        }
        binding.btnMaps.setOnClickListener {
            val i3 = Intent(this, MapsActivity::class.java)
            startActivity(i3)
        }
    }
}