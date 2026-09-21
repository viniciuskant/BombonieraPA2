package com.bomboniere.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bomboniere.app.conversao.ConversaoActivity
import com.bomboniere.app.insumos.InsumosActivity
import com.bomboniere.app.receitas.ReceitasActivity
import com.bomboniere.app.databinding.ActivityCalcCustosBinding

class CalcCustos : AppCompatActivity() {

    private lateinit var binding: ActivityCalcCustosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

        binding = ActivityCalcCustosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.buttonCustoInsumos.setOnClickListener {
            startActivity(Intent(this, InsumosActivity::class.java))
        }

        binding.buttonTabelaConversao.setOnClickListener {
            startActivity(Intent(this, ConversaoActivity::class.java))
        }

        binding.buttonReceitas.setOnClickListener {
            startActivity(Intent(this, ReceitasActivity::class.java))
        }
    }
}