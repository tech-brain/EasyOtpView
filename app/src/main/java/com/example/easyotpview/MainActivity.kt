package com.example.easyotpview

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class MainActivity : AppCompatActivity() {

    lateinit var btnLine : AppCompatButton
    lateinit var btnRectangle : AppCompatButton
    lateinit var btnCustom : AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLine = findViewById(R.id.btnLine)
        btnRectangle = findViewById(R.id.btnRectangle)
        btnCustom = findViewById(R.id.btnCustom)

        btnLine.setOnClickListener {
            startActivity(Intent(this, LineOtpActivity::class.java))
        }


        btnRectangle.setOnClickListener {
            startActivity(Intent(this, RectangleOtpViewActivity::class.java))
        }


        btnCustom.setOnClickListener {
            startActivity(Intent(this, CustomOtpViewActivity::class.java))
        }

    }
}