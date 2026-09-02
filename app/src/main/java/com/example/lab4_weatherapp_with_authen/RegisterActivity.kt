package com.example.lab4_weatherapp_with_authen

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnCreateAccount: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        etEmail = findViewById(R.id.etRegisterEmail)
        etPassword = findViewById(R.id.etRegisterPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)

        btnCreateAccount = findViewById(R.id.btnCreateAccount)

        progressBar =
            findViewById(R.id.registerProgressBar)

        btnCreateAccount.setOnClickListener {

            registerUser()
        }
    }

    private fun registerUser() {

        val email =
            etEmail.text.toString().trim()

        val password =
            etPassword.text.toString().trim()

        val confirmPassword =
            etConfirmPassword.text.toString().trim()

        if (email.isEmpty()) {

            etEmail.error = "Email is required"
            return
        }

        if (password.length < 6) {

            etPassword.error =
                "Password must contain at least 6 characters"

            return
        }

        if (password != confirmPassword) {

            etConfirmPassword.error =
                "Passwords do not match"

            return
        }

        progressBar.visibility = View.VISIBLE

        btnCreateAccount.isEnabled = false

        auth.createUserWithEmailAndPassword(
            email,
            password
        ).addOnCompleteListener { task ->

            progressBar.visibility = View.GONE

            btnCreateAccount.isEnabled = true

            if (task.isSuccessful) {

                Toast.makeText(
                    this,
                    "Account created. You can now login.",
                    Toast.LENGTH_LONG
                ).show()

                auth.signOut()

                finish()

            } else {

                Toast.makeText(
                    this,
                    task.exception?.message
                        ?: "Registration failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}