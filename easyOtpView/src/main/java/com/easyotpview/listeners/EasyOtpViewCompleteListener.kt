package com.easyotpview.listeners

import android.view.View

/**
 *
 * @author Tech Brain
 * All copy write are reserved
 *
 * This interface is used to define a callback for when the OTP (One-Time Password) input is complete.
 * The `otpCompleteListener` method will be called when the user has finished entering the OTP.
 *
 */
interface EasyOtpViewCompleteListener {

    /**
     * Callback method to notify that the OTP input is complete.
     *
     * @param view The view where the OTP input was completed (e.g., the OTP field or button).
     * @param otp The OTP entered by the user, or null if no OTP was entered.
     */
    fun otpCompleteListener(view : View, otp: String?)
}