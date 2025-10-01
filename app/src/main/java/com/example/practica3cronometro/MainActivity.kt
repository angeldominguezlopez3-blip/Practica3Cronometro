package com.example.practica3cronometro


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var tvTimer: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnReset: Button
    private lateinit var btnLap: Button
    private lateinit var listViewLaps: ListView

    private lateinit var adaptadorLaps: ArrayAdapter<String>
    private val listaLaps = mutableListOf<String>()

    private var handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var startTime = 0L
    private var elapsedTime = 0L
    private var lapNumber = 1

    private val updateTimer = object : Runnable {
        override fun run() {
            if (isRunning) {
                val currentTime = System.currentTimeMillis()
                elapsedTime = currentTime - startTime
                updateTimerDisplay()
                handler.postDelayed(this, 10) // Actualizar cada 10ms para milisegundos
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        inicializarVistas()
        configurarAdaptador()
        configurarEventos()
        actualizarEstadoBotones()
    }

    private fun inicializarVistas() {
        tvTimer = findViewById(R.id.tvTimer)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        btnReset = findViewById(R.id.btnReset)
        btnLap = findViewById(R.id.btnLap)
        listViewLaps = findViewById(R.id.listViewLaps)
    }

    private fun configurarAdaptador() {
        adaptadorLaps = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            listaLaps
        )
        listViewLaps.adapter = adaptadorLaps
    }

    private fun configurarEventos() {
        // Evento del botón Inicio
        btnStart.setOnClickListener {
            iniciarCronometro()
        }

        // Evento del botón Stop
        btnStop.setOnClickListener {
            detenerCronometro()
        }

        // Evento del botón Reset
        btnReset.setOnClickListener {
            resetearCronometro()
        }

        // Evento del botón Lap
        btnLap.setOnClickListener {
            agregarVuelta()
        }

        // Evento de clic en items de la lista de vueltas
        listViewLaps.setOnItemClickListener { parent, view, position, id ->
            val vueltaSeleccionada = listaLaps[position]
            Toast.makeText(
                this,
                "Vuelta: $vueltaSeleccionada",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Evento de clic largo para eliminar vuelta
        listViewLaps.setOnItemLongClickListener { parent, view, position, id ->
            eliminarVuelta(position)
            true // Indica que el evento fue manejado
        }
    }

    private fun iniciarCronometro() {
        if (!isRunning) {
            startTime = System.currentTimeMillis() - elapsedTime
            isRunning = true
            handler.post(updateTimer)
            actualizarEstadoBotones()
        }
    }

    private fun detenerCronometro() {
        if (isRunning) {
            isRunning = false
            handler.removeCallbacks(updateTimer)
            actualizarEstadoBotones()
        }
    }

    private fun resetearCronometro() {
        isRunning = false
        handler.removeCallbacks(updateTimer)
        elapsedTime = 0L
        lapNumber = 1
        listaLaps.clear()
        adaptadorLaps.notifyDataSetChanged()
        updateTimerDisplay()
        actualizarEstadoBotones()
    }

    private fun agregarVuelta() {
        if (isRunning) {
            val tiempoVuelta = formatTiempo(elapsedTime)
            listaLaps.add("Vuelta $lapNumber: $tiempoVuelta")
            adaptadorLaps.notifyDataSetChanged()
            lapNumber++

            // Hacer scroll automático a la última vuelta
            listViewLaps.smoothScrollToPosition(listaLaps.size - 1)
        } else {
            Toast.makeText(this, "El cronómetro debe estar en ejecución", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eliminarVuelta(position: Int) {
        if (position < listaLaps.size) {
            val vueltaEliminada = listaLaps[position]
            listaLaps.removeAt(position)
            adaptadorLaps.notifyDataSetChanged()
            Toast.makeText(
                this,
                "Eliminado: $vueltaEliminada",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateTimerDisplay() {
        tvTimer.text = formatTiempo(elapsedTime)
    }

    private fun formatTiempo(millis: Long): String {
        val minutos = (millis / 60000) % 60
        val segundos = (millis / 1000) % 60
        val milisegundos = millis % 1000

        return String.format("%02d:%02d:%03d", minutos, segundos, milisegundos)
    }

    private fun actualizarEstadoBotones() {
        btnStart.isEnabled = !isRunning
        btnStop.isEnabled = isRunning
        btnLap.isEnabled = isRunning
        btnReset.isEnabled = !isRunning || elapsedTime > 0
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimer)
    }
}