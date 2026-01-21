package com.example.panchify.vistas

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.panchify.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, Home::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_songs -> {
                    val intent = Intent(this, Songs::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_stats -> {
                    // acción search
                    val intent = Intent(this, Stats::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_comments -> {
                    // acción library
                    val intent = Intent(this, Comments::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_friends -> {
                    // acción profile
                    val intent = Intent(this, Friends::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

    }
}
