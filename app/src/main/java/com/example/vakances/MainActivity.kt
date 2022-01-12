package com.example.vakances

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.vakances.databinding.ActivityMainBinding
import com.example.vakances.databinding.ActivitySignUpBinding
import com.example.vakances.model.Manager
import com.example.vakances.views.EmployeeMainActivity
import com.example.vakances.views.ManagerMainActivity
import com.example.vakances.views.SignUpActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        binding.logInButton.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val password = binding.passwordEt.text.toString()
            if (email == "" || password == "") {
                SignUpActivity().showErrorDialog(
                    "",
                    "Lūdzu, ievadiet gan e-pastu, gan paroli!",
                    this
                )
            } else {
                logIn(email, password, this)
            }
        }
        binding.toSignUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun logIn(email: String, password: String, context: Context) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    logInAs(email, context)
                } else {
                    val message = task.exception.toString()
                    Log.v(TAG, message)
                    SignUpActivity().showErrorDialog(
                        "",
                        "Lūdzu, pārliecinieties, ka ievadījāt pareizu e-pastu un paroli!",
                        this
                    )
                }
            }
    }

    private fun logInAs(email: String, context: Context){
        db.collection("Manager")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.count() == 1) {
                    var id = ""
                    for (document in documents) {
                        val manager = document.toObject<Manager>()
                        id = manager.id
                    }
                    val intent = Intent(context!!, ManagerMainActivity::class.java)
                    intent.putExtra("id", id)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        db.collection("Employee")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.count() == 1) {
                    val intent = Intent(context!!, EmployeeMainActivity::class.java)
                    intent.putExtra("email", email)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
    }
}
