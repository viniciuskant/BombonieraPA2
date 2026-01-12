package com.bomboniere.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Configuração para os botões
        val buttonPersonalizados = findViewById<Button>(R.id.button_personalizados)
        val buttonCriaAdesivosLotes = findViewById<Button>(R.id.button_cria_adesivos_lotes)
        val buttonExibeLotes = findViewById<Button>(R.id.button_exibe_lotes)
        val buttonBuscaLotes = findViewById<Button>(R.id.button_busca_lotes)
        val buttonPlanilhas = findViewById<Button>(R.id.button_planilhas)
        val buttonConfig = findViewById<Button>(R.id.config)

        buttonConfig.setOnClickListener {
            val intent = Intent(this, Config::class.java)
            startActivity(intent)
        }

        buttonPersonalizados.setOnClickListener {
            val intent = Intent(this, AdesivosPersonalizados::class.java)
            startActivity(intent)
        }

        buttonCriaAdesivosLotes.setOnClickListener {
            val intent = Intent(this, AdesivosLotes::class.java)
            startActivity(intent)
        }

        buttonExibeLotes.setOnClickListener {
            val intent = Intent(this, ExibeLotesAntigos::class.java)
            startActivity(intent)
        }

        buttonBuscaLotes.setOnClickListener {
            val intent = Intent(this, ConsultaLotesAntigos::class.java)
            startActivity(intent)
        }

        buttonPlanilhas.setOnClickListener {
            val intent = Intent(this, Planilhas::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
