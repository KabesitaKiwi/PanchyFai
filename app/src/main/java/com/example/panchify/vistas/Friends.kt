package com.example.panchify.vistas

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.panchify.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class Friends : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_friends)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_friends
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, Home::class.java))
                    true
                }
                R.id.nav_songs -> {
                    true
                }
                R.id.nav_stats -> {
                    startActivity(Intent(this, Stats::class.java))
                    true
                }
                R.id.nav_comments -> {
                    startActivity(Intent(this, Comments::class.java))
                    true
                }
                R.id.nav_friends -> {
                    startActivity(Intent(this, Friends::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
