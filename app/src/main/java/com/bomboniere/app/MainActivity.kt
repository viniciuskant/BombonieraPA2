package com.bomboniere.app

import com.bomboniere.app.Lotes.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // se não tenho a permissão de notificação, pesso ela
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),  // aqui posso passar uma lista de permissões que quero ter, tudo de uma única vez
                101)
        }

        // habilita as notificações
        LoteNotificationManager.createNotificationChannel(this)
        LoteNotificationManager.scheduleDailyCheck(this)


        // findViewById<>() busca no ContentView definido anteriormente os comdonentes com esse id,
        // mas tenho que definir o tipo dele e para isso preciso importar cada um deles usando "import android.widget.NOME"
        val buttonPersonalizados = findViewById<Button>(R.id.button_personalizados)
        val buttonCriaAdesivosLotes = findViewById<Button>(R.id.button_cria_adesivos_lotes)
        val buttonConsultaLotes = findViewById<Button>(R.id.button_consulta_lotes)
//        val buttonConfig = findViewById<Button>(R.id.config)
        val buttonCalcCustos = findViewById<Button>(R.id.button_calc_custos)


//        buttonConfig.setOnClickListener {
//            val intent = Intent(this, Config::class.java)
//            startActivity(intent)
//        }

        buttonPersonalizados.setOnClickListener {
            val intent = Intent(this, AdesivosPersonalizados::class.java)
            startActivity(intent)
        }

        buttonCriaAdesivosLotes.setOnClickListener {
            val intent = Intent(this, AdesivosLotes::class.java)
            startActivity(intent)
        }

        buttonConsultaLotes.setOnClickListener {
            val intent = Intent(this, ConsultaLotes::class.java)
            startActivity(intent)
        }
        buttonCalcCustos.setOnClickListener {
            val intent = Intent(this, CalcCustos::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
