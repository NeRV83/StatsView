package ru.netology.statsview

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.statsview.ui.StatsView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val statsView = findViewById<StatsView>(R.id.statsView)
        val switch = findViewById<SwitchCompat>(R.id.animationSwitch)

        statsView.data = listOf(500F, 500F, 500F, 500F)

        switch.setOnCheckedChangeListener { _, isChecked ->
            statsView.setAnimationType(isSequential = isChecked)
        }

    }
}