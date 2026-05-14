package com.example.panchify.vistas

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.panchify.R
import com.example.panchify.adapters.FriendsAdapter
import com.example.panchify.adapters.UserSearchAdapter
import com.example.panchify.db.AmistadDao
import com.example.panchify.modelos.FriendItem
import com.example.panchify.preferences.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomnavigation.BottomNavigationView

class Friends : AppCompatActivity() {
    private lateinit var listaAmigos: RecyclerView
    private lateinit var txtEmptyFriends: TextView
    private lateinit var botonInvitar: MaterialButton
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_friends)
        sessionManager = SessionManager(this)
        listaAmigos = findViewById(R.id.listaAmigos)
        txtEmptyFriends = findViewById(R.id.txtEmptyFriends)
        botonInvitar = findViewById(R.id.botonInvitar)
        listaAmigos.layoutManager = LinearLayoutManager(this)

        botonInvitar.setOnClickListener {
            mostrarDialogoInvitar()
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_friends
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

        this.cargarIconoPerfil()
        cargarAmigos()
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.menu.findItem(R.id.nav_friends)?.isChecked = true
        cargarAmigos()
    }

    private fun cargarAmigos() {
        val idUsuario = sessionManager.getUserId()
        if (idUsuario == null) {
            txtEmptyFriends.visibility = View.VISIBLE
            txtEmptyFriends.text = "No se encontro tu usuario"
            return
        }

        Thread {
            val items = AmistadDao.obtenerAmigosYSolicitudes(idUsuario)
            runOnUiThread {
                txtEmptyFriends.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                txtEmptyFriends.text = "Aun no tienes amigos ni solicitudes"
                listaAmigos.adapter = FriendsAdapter(
                    items = items,
                    onFriendClick = { item -> abrirDetalleAmigo(item) },
                    onAccept = { item -> responderSolicitud(item.idSolicitud, true) },
                    onReject = { item -> responderSolicitud(item.idSolicitud, false) }
                )
            }
        }.start()
    }

    private fun responderSolicitud(idSolicitud: Int?, aceptar: Boolean) {
        val idUsuario = sessionManager.getUserId() ?: return
        if (idSolicitud == null) return

        Thread {
            val ok = AmistadDao.responderSolicitud(idSolicitud, idUsuario, aceptar)
            runOnUiThread {
                Toast.makeText(
                    this,
                    if (ok && aceptar) "Solicitud aceptada" else if (ok) "Solicitud rechazada" else "No se pudo responder",
                    Toast.LENGTH_SHORT
                ).show()
                cargarAmigos()
            }
        }.start()
    }

    private fun mostrarDialogoInvitar() {
        val idUsuario = sessionManager.getUserId()
        if (idUsuario == null) {
            Toast.makeText(this, "No se encontro tu usuario", Toast.LENGTH_SHORT).show()
            return
        }

        val input = EditText(this).apply {
            hint = "Buscar usuario"
            setSingleLine(true)
            setPadding(32, 12, 32, 12)
        }

        val recycler = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@Friends)
            minimumHeight = (260 * resources.displayMetrics.density).toInt()
        }

        val empty = TextView(this).apply {
            text = "Cargando usuarios..."
            setPadding(32, 20, 32, 20)
            textAlignment = View.TEXT_ALIGNMENT_CENTER
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 12, 32, 0)
            addView(input)
            addView(empty)
            addView(recycler)
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Invitar amigo")
            .setView(container)
            .setNegativeButton("Cancelar", null)
            .create()

        val adapter = UserSearchAdapter(emptyList()) { user ->
            dialog.dismiss()
            enviarSolicitud(user)
        }
        recycler.adapter = adapter

        dialog.setOnShowListener {
            cargarUsuariosParaInvitar(idUsuario, adapter, empty, input)
        }
        dialog.show()
    }

    private fun abrirDetalleAmigo(item: com.example.panchify.modelos.FriendItem) {
        val intent = Intent(this, FriendDetail::class.java).apply {
            putExtra(FriendDetail.EXTRA_USER_ID, item.idUsuario)
            putExtra(FriendDetail.EXTRA_USER_NAME, item.nombreUsuario ?: item.spotifyId ?: "Amigo")
            putExtra(FriendDetail.EXTRA_USER_IMAGE, item.imagenPerfil)
        }
        startActivity(intent)
    }

    private fun cargarUsuariosParaInvitar(
        idUsuario: Int,
        adapter: UserSearchAdapter,
        empty: TextView,
        input: EditText
    ) {
        Thread {
            val usuarios = AmistadDao.obtenerUsuariosParaInvitar(idUsuario)
            runOnUiThread {
                fun filtrar(texto: String) {
                    val filtro = texto.trim().lowercase()
                    val filtrados = if (filtro.isEmpty()) {
                        usuarios
                    } else {
                        usuarios.filter { user ->
                            listOfNotNull(user.nombreUsuario, user.email, user.spotifyId)
                                .any { it.lowercase().contains(filtro) }
                        }
                    }
                    adapter.submitList(filtrados)
                    empty.visibility = if (filtrados.isEmpty()) View.VISIBLE else View.GONE
                    empty.text = if (usuarios.isEmpty()) "No hay usuarios disponibles" else "No se encontraron usuarios"
                }

                input.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        filtrar(s?.toString().orEmpty())
                    }
                    override fun afterTextChanged(s: Editable?) {}
                })
                filtrar("")
            }
        }.start()
    }

    private fun enviarSolicitud(usuario: FriendItem) {
        if (usuario.estado == "aceptada" || usuario.estado == "pendiente") {
            Toast.makeText(
                this,
                if (usuario.estado == "aceptada") "Ese usuario ya es tu amigo" else "Ya hay una solicitud pendiente",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val idUsuario = sessionManager.getUserId()
        if (idUsuario == null) {
            Toast.makeText(this, "No se encontro tu usuario", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            val ok = AmistadDao.enviarSolicitudAUsuario(idUsuario, usuario.idUsuario)
            runOnUiThread {
                Toast.makeText(
                    this,
                    if (ok) "Solicitud enviada" else "No se encontro ese usuario o ya existe solicitud",
                    Toast.LENGTH_SHORT
                ).show()
                cargarAmigos()
            }
        }.start()
    }
}
