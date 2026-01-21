package com.example.panchify

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.panchify.preferences.SessionManager
import com.example.panchify.vistas.Home
import com.example.panchify.vistas.Login

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            // Ya hay sesión → Home
            startActivity(Intent(this, Home::class.java))

        } else {
            // No hay sesión → Login
            startActivity(Intent(this, Login::class.java))
        }

        finish()

    }
}