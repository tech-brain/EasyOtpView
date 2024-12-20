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

class CustomOtpViewActivity : AppCompatActivity() {

    lateinit var customOtpView : EasyOtpView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_otp_view)
        customOtpView = findViewById(R.id.customOtpView)

        customOtpView.setOtpCompletionListener(object : EasyOtpViewCompleteListener {
            override fun otpCompleteListener(view: View, otp: String?) {
                Toast.makeText(
                    this@CustomOtpViewActivity,
                    "Entered OTP $otp",
                    Toast.LENGTH_LONG
                ).show()
            }

        })
    }
}