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
            val targetClass = when (menuItem.itemId) {
                R.id.nav_home -> if (this !is Home) Home::class.java else null
                R.id.nav_songs -> if (this !is Songs) Songs::class.java else null
                R.id.nav_stats -> if (this !is Stats) Stats::class.java else null
                R.id.nav_comments -> if (this !is Comments) Comments::class.java else null
                R.id.nav_friends -> if (this !is Friends) Friends::class.java else null
                else -> null
            }
            if (targetClass != null) {
                val targetIntent = android.content.Intent(this, targetClass)
                targetIntent.flags = android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
                startActivity(targetIntent)
                overridePendingTransition(0, 0)
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.menu.findItem(R.id.nav_stats)?.isChecked = true
    }
}
