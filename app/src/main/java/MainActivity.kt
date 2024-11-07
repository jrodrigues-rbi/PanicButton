package com.uebrasil.panicbuttonapp

import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import services.MainService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Remover o título
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        // Definir a atividade em tela cheia
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_main)

        // Ocultar a barra de ação (ActionBar)
        supportActionBar?.hide()

        val serviceIntent = Intent(this, MainService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}
