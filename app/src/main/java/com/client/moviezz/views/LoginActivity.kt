package com.client.moviezz.views

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.chaos.view.PinView

import com.client.moviezz.R
import com.client.moviezz.viewmodel.MovieViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var edtSdt: EditText
    private lateinit var pinView: PinView
    private lateinit var tvResendOtp: TextView
    private lateinit var btnLogin: Button
    private lateinit var tvPhoneError: TextView
    private lateinit var tvQuayLai: TextView
    private lateinit var tvOtpError: TextView
    private lateinit var viewModel: MovieViewModel
    private lateinit var llsdt: LinearLayout
    private lateinit var tvNotiSdt: TextView
    private var count: Int = 0

    private fun isValidPhoneNumber(phone: String): Boolean {
        // Cho phép nhập số điện thoại có dấu + ở đầu
        val cleanPhone = if (phone.startsWith("+")) {
            phone.substring(1)
        } else {
            phone
        }
        return cleanPhone.isNotEmpty() && cleanPhone.all { it.isDigit() }
    }

    private fun isValidOtp(otp: String): Boolean {
        // Kiểm tra OTP 6 số
        val otpRegex = "^[0-9]{6}$"
        return otp.matches(otpRegex.toRegex())
    }

    private fun formatPhoneNumber(phone: String): String {
        // Nếu số điện thoại đã có dấu + ở đầu, giữ nguyên
        // Nếu không có dấu +, thêm +67 vào đầu
        return if (phone.startsWith("+")) {
            phone
        } else {
            "+67$phone"
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        anhXa()
        viewModel = ViewModelProvider(this)[MovieViewModel::class.java]
        edtSdt.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val phoneNumber = s.toString().trim()
                if (phoneNumber.isEmpty()) {
                    btnLogin.isEnabled = false
                    btnLogin.setBackgroundResource(R.drawable.bg_btn_login_disabled)
                    tvPhoneError.visibility = View.GONE
                } else {
                    if (isValidPhoneNumber(phoneNumber)) {
                        btnLogin.isEnabled = true
                        btnLogin.setBackgroundResource(R.drawable.bg_btn_login)
                        tvPhoneError.visibility = View.GONE
                    } else {
                        btnLogin.isEnabled = false
                        btnLogin.setBackgroundResource(R.drawable.bg_btn_login_disabled)
                        tvPhoneError.visibility = View.VISIBLE
                        tvPhoneError.text = "Vui lòng chỉ nhập số"
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        pinView.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val otp = s.toString()
                if (otp.length == 6) {
                    btnLogin.isEnabled = true
                    btnLogin.setBackgroundResource(R.drawable.bg_btn_login)
                } else {
                    btnLogin.isEnabled = false
                    btnLogin.setBackgroundResource(R.drawable.bg_btn_login_disabled)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        tvResendOtp.setOnClickListener {
            val sdt = edtSdt.text.toString()
            if (sdt.isNotEmpty()) {
                if (isValidPhoneNumber(sdt)) {
                    tvNotiSdt.text = "A OTP has been sent to $sdt"
                    val fullPhoneNumber = formatPhoneNumber(sdt)
                    viewModel.fetchOTP(fullPhoneNumber)
                    tvPhoneError.visibility = View.GONE
                } else {
                    tvPhoneError.visibility = View.VISIBLE
                    tvPhoneError.text = "Vui lòng chỉ nhập số"
                }
            }
        }

        btnLogin.setOnClickListener {
            if (count == 0) {
                val phoneNumber = edtSdt.text.toString()
                if (isValidPhoneNumber(phoneNumber)) {
                    btnLogin.text = "Continue"
                    tvQuayLai.visibility = View.VISIBLE
                    pinView.visibility = View.VISIBLE
                    llsdt.visibility = View.GONE
                    tvPhoneError.visibility = View.GONE
                    tvResendOtp.visibility = View.VISIBLE
                    tvNotiSdt.visibility = View.VISIBLE
                    tvNotiSdt.text = "A OTP has been sent to $phoneNumber"
                    val fullPhoneNumber = formatPhoneNumber(phoneNumber)
                    viewModel.fetchOTP(fullPhoneNumber)
                    // Disable button sau khi gửi OTP thành công
                    btnLogin.isEnabled = false
                    btnLogin.setBackgroundResource(R.drawable.bg_btn_login_disabled)
                } else {
                    tvPhoneError.visibility = View.VISIBLE
                    tvPhoneError.text = "Vui lòng chỉ nhập số"
                }
            } else {
                val msisdn = edtSdt.text.toString()
                val otp = pinView.text.toString()
                Log.e("hoho", "pinview: "+ otp)
                if (msisdn.isNotEmpty() && otp.isNotEmpty()) {
                    if (isValidOtp(otp)) {
                        val fullPhoneNumber = formatPhoneNumber(msisdn)
                        viewModel.fetchToken(fullPhoneNumber, otp)
                        tvOtpError.visibility = View.GONE
                        // Không disable button khi gửi OTP để cho phép click nhiều lần
                    } else {
                        tvOtpError.visibility = View.VISIBLE
                        tvOtpError.text = "Mã OTP không hợp lệ"
                    }
                }
            }
            count++
        }
        tvQuayLai.setOnClickListener {
            pinView.text = null
            count = 0
            tvQuayLai.visibility = View.GONE
            pinView.visibility = View.GONE
            llsdt.visibility = View.VISIBLE
            tvPhoneError.visibility = View.GONE
            tvResendOtp.visibility = View.GONE
            tvNotiSdt.visibility = View.GONE
            btnLogin.isEnabled = false
            btnLogin.setBackgroundResource(R.drawable.bg_btn_login_disabled)
            btnLogin.text = "Login by OTP"
        }

        observeViewModel()
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uuid.collectLatest { uuid ->
                uuid?.let {
                    Toast.makeText(this@LoginActivity, "OTP sent. UUID: $it", Toast.LENGTH_SHORT)
                        .show()
                    tvOtpError.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.token.collectLatest { token ->
                token?.let {
                    Toast.makeText(
                        this@LoginActivity,
                        "Login successful. Token: $it",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.error.collectLatest { error ->
                error?.let {
                    Toast.makeText(this@LoginActivity, "Error: $it", Toast.LENGTH_SHORT).show()
                    if (count == 0) {
                        tvPhoneError.visibility = View.VISIBLE
                        tvPhoneError.text = "Không thể gửi OTP. Vui lòng thử lại"
                    } else {
                        tvOtpError.visibility = View.VISIBLE
                        tvOtpError.text = "Mã OTP không đúng. Vui lòng thử lại"
                    }
                }
            }
        }
    }

    private fun anhXa() {
        edtSdt = findViewById(R.id.edt_sdt)
        tvResendOtp = findViewById(R.id.tv_resend_otp)
        btnLogin = findViewById(R.id.btn_login)
        tvPhoneError = findViewById(R.id.tv_phone_error)
        tvOtpError = findViewById(R.id.tv_otp_error)
        pinView = findViewById(R.id.pin_view)
        llsdt = findViewById(R.id.ll_sdt)
        tvNotiSdt = findViewById(R.id.tv_noti_sdt)
        tvQuayLai = findViewById(R.id.tv_quay_lai)
    }
}
