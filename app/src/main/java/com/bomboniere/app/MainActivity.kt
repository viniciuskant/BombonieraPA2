package com.bomboniere.app

import com.bomboniere.app.Lotes.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.jar.Manifest
import android.os.Build
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        LoteNotificationManager.createNotificationChannel(this)
        LoteNotificationManager.scheduleDailyCheck(this)

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
