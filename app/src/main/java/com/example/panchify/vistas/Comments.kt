package com.example.panchify.vistas

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.panchify.R

class Comments : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_comments)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
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
        bottomNavigationView.menu.findItem(R.id.nav_comments)?.isChecked = true
    }
}
