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

class RectangleOtpViewActivity : AppCompatActivity() {

    lateinit var otpView : EasyOtpView
    lateinit var customOtpView : EasyOtpView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rectangle_otp_view)

        otpView = findViewById(R.id.otpView)
        customOtpView = findViewById(R.id.customOtpView)

        otpView.setOtpCompletionListener(object : EasyOtpViewCompleteListener {
            override fun otpCompleteListener(view: View, otp: String?) {
                Toast.makeText(
                    this@RectangleOtpViewActivity,
                    "Entered OTP $otp",
                    Toast.LENGTH_LONG
                ).show()
            }

        })


        customOtpView.setOtpCompletionListener(object : EasyOtpViewCompleteListener {
            override fun otpCompleteListener(view: View, otp: String?) {
                Toast.makeText(
                    this@RectangleOtpViewActivity,
                    "Entered OTP $otp",
                    Toast.LENGTH_LONG
                ).show()
            }

        })

    }
}