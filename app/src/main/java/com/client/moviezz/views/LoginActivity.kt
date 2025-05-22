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
import com.client.moviezz.databinding.ActivityLoginBinding
import com.client.moviezz.viewmodel.MovieViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: MovieViewModel
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
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[MovieViewModel::class.java]
        binding.edtSdt.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val phoneNumber = s.toString().trim()
                if (phoneNumber.isEmpty()) {
                    binding.btnLogin.isEnabled = false
                    binding.btnLogin.setBackgroundResource(R.drawable.bg_btn_login_disabled)
                    binding.tvPhoneError.visibility = View.GONE
                } else {
                    if (isValidPhoneNumber(phoneNumber)) {
                        binding.btnLogin.isEnabled = true
                        binding.btnLogin.setBackgroundResource(R.drawable.bg_btn_login)
                        binding.tvPhoneError.visibility = View.GONE
                    } else {
                        binding.btnLogin.isEnabled = false
                        binding.btnLogin.setBackgroundResource(R.drawable.bg_btn_login_disabled)
                        binding.tvPhoneError.visibility = View.VISIBLE
                        binding.tvPhoneError.text = "Vui lòng chỉ nhập số"
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.pinView.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val otp = s.toString()
                if (otp.length == 6) {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.setBackgroundResource(R.drawable.bg_btn_login)
                } else {
                    binding.btnLogin.isEnabled = false
                    binding.btnLogin.setBackgroundResource(R.drawable.bg_btn_login_disabled)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.tvResendOtp.setOnClickListener {
            val sdt = binding.edtSdt.text.toString()
            if (sdt.isNotEmpty()) {
                if (isValidPhoneNumber(sdt)) {
                    binding.tvNotiSdt.text = "A OTP has been sent to $sdt"
                    val fullPhoneNumber = formatPhoneNumber(sdt)
                    viewModel.fetchOTP(fullPhoneNumber)
                    binding.tvPhoneError.visibility = View.GONE
                } else {
                    binding.tvPhoneError.visibility = View.VISIBLE
                    binding.tvPhoneError.text = "Vui lòng chỉ nhập số"
                }
            }
        }

        binding.btnLogin.setOnClickListener {
            if (count == 0) {
                val phoneNumber = binding.edtSdt.text.toString()
                if (isValidPhoneNumber(phoneNumber)) {
                    binding.btnLogin.text = "Continue"
                    binding.tvQuayLai.visibility = View.VISIBLE
                    binding.pinView.visibility = View.VISIBLE
                    binding.llSdt.visibility = View.GONE
                    binding.tvPhoneError.visibility = View.GONE
                    binding.tvResendOtp.visibility = View.VISIBLE
                    binding.tvNotiSdt.visibility = View.VISIBLE
                    binding.tvNotiSdt.text = "A OTP has been sent to $phoneNumber"
                    val fullPhoneNumber = formatPhoneNumber(phoneNumber)
                    viewModel.fetchOTP(fullPhoneNumber)
                    // Disable button sau khi gửi OTP thành công
                    binding.btnLogin.isEnabled = false
                    binding.btnLogin.setBackgroundResource(R.drawable.bg_btn_login_disabled)
                } else {
                    binding.tvPhoneError.visibility = View.VISIBLE
                    binding.tvPhoneError.text = "Vui lòng chỉ nhập số"
                }
            } else {
                val msisdn = binding.edtSdt.text.toString()
                val otp = binding.pinView.text.toString()
                Log.e("hoho", "pinview: "+ otp)
                if (msisdn.isNotEmpty() && otp.isNotEmpty()) {
                    if (isValidOtp(otp)) {
                        val fullPhoneNumber = formatPhoneNumber(msisdn)
                        viewModel.fetchToken(fullPhoneNumber, otp)
                        binding.tvOtpError.visibility = View.GONE
                        // Không disable button khi gửi OTP để cho phép click nhiều lần
                    } else {
                        binding.tvOtpError.visibility = View.VISIBLE
                        binding.tvOtpError.text = "Mã OTP không hợp lệ"
                    }
                }
            }
            count++
        }
        binding.tvQuayLai.setOnClickListener {
            binding.pinView.text = null
            count = 0
            binding.tvQuayLai.visibility = View.GONE
            binding.pinView.visibility = View.GONE
            binding.llSdt.visibility = View.VISIBLE
            binding.tvPhoneError.visibility = View.GONE
            binding.tvResendOtp.visibility = View.GONE
            binding.tvNotiSdt.visibility = View.GONE
            binding.btnLogin.isEnabled = false
            binding.btnLogin.setBackgroundResource(R.drawable.bg_btn_login_disabled)
            binding.btnLogin.text = "Login by OTP"
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
                    binding.tvOtpError.visibility = View.GONE
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
                        binding.tvPhoneError.visibility = View.VISIBLE
                        binding.tvPhoneError.text = "Không thể gửi OTP. Vui lòng thử lại"
                    } else {
                        binding.tvOtpError.visibility = View.VISIBLE
                        binding.tvOtpError.text = "Mã OTP không đúng. Vui lòng thử lại"
                    }
                }
            }
        }
    }
}
