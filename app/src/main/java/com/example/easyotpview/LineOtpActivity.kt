package com.example.easyotpview

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.easyotpview.easyotpview.EasyOtpView
import com.easyotpview.listeners.EasyOtpViewCompleteListener

class LineOtpActivity : AppCompatActivity() {

    lateinit var otpView : EasyOtpView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_line_otp)
        otpView = findViewById(R.id.otpView)

        otpView.setOtpCompletionListener(object : EasyOtpViewCompleteListener{
            override fun otpCompleteListener(view: View, otp: String?) {
                Toast.makeText(
                    this@LineOtpActivity,
                    "Entered OTP $otp",
                    Toast.LENGTH_LONG
                ).show()
            }

        })
    }
}