package com.example.panchify.vistas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.panchify.R
import com.example.panchify.preferences.SessionManager

class Stats : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        sessionManager = SessionManager(this)

        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_stats
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(android.content.Intent(this, Home::class.java))
                    true
                }
                R.id.nav_songs -> {
                    startActivity(android.content.Intent(this, Songs::class.java))
                    true
                }
                R.id.nav_stats -> {
                    true
                }
                R.id.nav_comments -> {
                    startActivity(android.content.Intent(this, Comments::class.java))
                    true
                }
                R.id.nav_friends -> {
                    startActivity(android.content.Intent(this, Friends::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
