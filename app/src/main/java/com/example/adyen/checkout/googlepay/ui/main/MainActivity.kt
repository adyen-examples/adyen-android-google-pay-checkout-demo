package com.example.adyen.checkout.googlepay.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.adyen.checkout.googlepay.databinding.ActivityMainBinding
import com.example.adyen.checkout.googlepay.ui.googlepay.GooglePayActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonStartGooglePay.setOnClickListener {
            startActivity(Intent(this, GooglePayActivity::class.java))
        }
    }
}
