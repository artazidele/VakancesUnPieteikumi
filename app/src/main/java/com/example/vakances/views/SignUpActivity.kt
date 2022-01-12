package com.example.vakances.views

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import com.example.vakances.MainActivity
import com.example.vakances.R
import com.example.vakances.databinding.ActivitySignUpBinding
import com.example.vakances.model.Employee
import com.example.vakances.model.Manager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.*

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding
    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        binding.toLogInButton.setOnClickListener {
            toLogIn(this)
        }
        binding.signUpButton.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val password = binding.passwordEt.text.toString()
            val passwordSecond = binding.passwordSecondEt.text.toString()
            val uuid = UUID.randomUUID()
            val id = uuid.toString()
            val name = binding.nameEt.text.toString()
            val surname = binding.surnameEt.text.toString()
            val personal_code = binding.personalCodeEt.text.toString()
            val phone = binding.phoneEt.text.toString()
            val address = binding.addressEt.text.toString()
            var allFieldsCorrect = true
            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
            if (email == "" && !email.matches(emailPattern.toRegex())) {
                allFieldsCorrect = false
                showErrorDialog(
                    "E-pasts",
                    "Ludzu, ievadiet korektu e-pastu!",
                    this
                )
            }
            if (name == "") {
                allFieldsCorrect = false
                showErrorDialog(
                    "Vārds",
                    "Ludzu, ievadiet vārdu!",
                    this
                )
            }
            if (surname == "") {
                allFieldsCorrect = false
                showErrorDialog(
                    "Uzvārds",
                    "Ludzu, ievadiet uzvārdu!",
                    this
                )
            }
            if (phone == "" || phone.length != 8) {
                allFieldsCorrect = false
                showErrorDialog(
                    "Telefona numurs",
                    "Ludzu, ievadiet pareizu telefona numuru bez valsts koda!",
                    this
                )
            }
            if (address == "") {
                allFieldsCorrect = false
                showErrorDialog(
                    "Adrese",
                    "Ludzu, ievadiet adresi!",
                    this
                )
            }
            if (personal_code == "" || personal_code.length != 12) {
                allFieldsCorrect = false
                showErrorDialog(
                    "Personas kods",
                    "Ludzu, ievadiet pareizu personas kodu!",
                    this
                )
            }
            if (password == "" || passwordSecond == "" || password != passwordSecond || password.length < 8) {
                allFieldsCorrect = false
                showErrorDialog(
                    "Parole",
                    "Ludzu, ievadiet divas vismaz 8 simbolu garas un vienādas paroles!",
                    this
                )
            }
            if (allFieldsCorrect == true) {
                if (binding.employeeRb.isChecked == true) {
                    val user = Employee(id, name, surname, personal_code, email, phone, address)
                    addEmployee(email, password, user, this)
                } else if (binding.managerRb.isChecked == true) {
                    val user = Manager(id, name, surname, personal_code, email, phone, address)
                    addManager(email, password, user, this)
                } else {
                    showErrorDialog(
                        "Darba devējs/Darbinieks",
                        "Ludzu, norādiet, vai esat darbinieks vai darba devējs!",
                        this
                    )
                }
            }
        }
    }

    public fun showErrorDialog(title: String, message: String, context: Context) {
        val dialogView =
            LayoutInflater.from(context).inflate(R.layout.error_dialog, null)
        val builder = AlertDialog.Builder(context)
            .setView(dialogView)
        val alertDialog = builder.show()
        dialogView.findViewById<TextView>(R.id.title_tv).text = title
        dialogView.findViewById<TextView>(R.id.message_tv).text = message
        dialogView.findViewById<Button>(R.id.understand_button).setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun addManager(email: String, password: String, user: Manager, context: Context) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                Log.v(TAG, password)
                if (task.isSuccessful) {
                    addManagerToDataBase(user, context)
                }
            }
    }

    private fun addEmployee(email: String, password: String, user: Employee, context: Context) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    addEmployeeToDataBase(user, context)
                }
            }
    }

    private fun addEmployeeToDataBase(employee: Employee, context: Context) {
        db.collection("Employee").document(employee.id)
            .set(employee)
            .addOnSuccessListener {
                toLogIn(context)
            }
    }

    private fun addManagerToDataBase(manager: Manager, context: Context) {
        db.collection("Manager").document(manager.id)
            .set(manager)
            .addOnSuccessListener {
                toLogIn(context)
            }
    }

    private fun toLogIn(context: Context) {
        val intent = Intent(context!!, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
