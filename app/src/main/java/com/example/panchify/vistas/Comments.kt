package com.example.panchify.vistas

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.panchify.R
import com.example.panchify.api.RetrofitClient
import com.example.panchify.modelos.ComentarioRequest
import com.example.panchify.modelos.ComentarioResponse
import com.example.panchify.preferences.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Comments : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ComentariosAdapter
    private lateinit var campoComentario: TextInputEditText
    private lateinit var btnSend: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

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

        // Configurar RecyclerView
        recycler = findViewById(R.id.listaComentarios)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = ComentariosAdapter(emptyList())
        recycler.adapter = adapter

        // Campo de texto y botón enviar
        campoComentario = findViewById(R.id.campoComentarios)
        btnSend = findViewById(R.id.btnSend)

        btnSend.setOnClickListener {
            enviarComentario()
        }

        cargarIconoPerfil()
        cargarComentarios()
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.menu.findItem(R.id.nav_comments)?.isChecked = true
    }

    /**
     * Carga los comentarios usando JDBC de Java en hilo secundario
     */
    private fun cargarComentarios() {
        Thread {
            val comentarios = com.example.panchify.db.ComentarioDao.listarComentarios()
            
            runOnUiThread {
                adapter = ComentariosAdapter(comentarios)
                recycler.adapter = adapter

                if (comentarios.isEmpty()) {
                    Toast.makeText(this@Comments, "No hay comentarios aún. ¡Sé el primero!", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    /**
     * Envía un nuevo comentario al backend
     */
    private fun enviarComentario() {
        val texto = campoComentario.text?.toString()?.trim() ?: ""
        if (texto.isEmpty()) {
            Toast.makeText(this, "Escribe algo primero", Toast.LENGTH_SHORT).show()
            return
        }

        val sessionManager = SessionManager(this)
        val idUsuario = sessionManager.getUserId()
        if (idUsuario == null) {
            Toast.makeText(this, "Error: usuario no registrado", Toast.LENGTH_SHORT).show()
            return
        }

        btnSend.isEnabled = false

        // Insertar comentario usando JDBC en hilo secundario
        Thread {
            val resultado = com.example.panchify.db.ComentarioDao.crearComentario(
                idUsuario,
                "general",
                texto,
                null,
                "General"
            )
            
            runOnUiThread {
                btnSend.isEnabled = true
                if (resultado != null) {
                    campoComentario.text?.clear()
                    cargarComentarios() // Recargar lista
                    Toast.makeText(this@Comments, "Comentario publicado 🎵", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@Comments, "Error al publicar", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
