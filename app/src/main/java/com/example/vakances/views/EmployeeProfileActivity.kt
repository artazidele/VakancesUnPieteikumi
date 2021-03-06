package com.example.vakances.views

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.vakances.MainActivity
import com.example.vakances.R
import com.example.vakances.databinding.ActivityAllVacanciesBinding
import com.example.vakances.databinding.ActivityEmployeeMainBinding
import com.example.vakances.databinding.ActivityEmployeeProfileBinding
import com.example.vakances.model.Employee
import com.example.vakances.model.Manager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class EmployeeProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmployeeProfileBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var email: String
    private lateinit var nameTextView: TextView
    private lateinit var surnameTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var pkTextView: TextView
    private lateinit var nameEditText: EditText
    private lateinit var surnameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var pkEditText: EditText
    private lateinit var id: String
    private var editTextHiden = true
    private lateinit var editProfileButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        email = intent.getStringExtra("email").toString()
        nameTextView = binding.nameTv
        surnameTextView = binding.surnameTv
        phoneTextView = binding.phoneTv
        addressTextView = binding.addressTv
        pkTextView = binding.pkTv
        nameEditText = binding.nameEt
        surnameEditText = binding.surnameEt
        phoneEditText = binding.phoneEt
        addressEditText = binding.addressEt
        pkEditText = binding.pkEt
        setEmployeeInfo(email)
        editProfileButton = binding.editProfileButton
        editProfileButton.setOnClickListener {
            if (editTextHiden == true) {
                editEmployee()
                editTextHiden = false
                editProfileButton.text = "Saglab??t izmai??as"
            } else {
                saveChanges()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.employee_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sign_out -> signOut(this)
            R.id.all_vacancies -> toAllApplications(this)
            R.id.my_applications -> toMyApplications(this)
        }
        return super.onOptionsItemSelected(item)
    }

    public fun toMyApplications(context: Context) {
        val intent = Intent(context!!, EmployeeMainActivity::class.java)
        intent.putExtra("email", email)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    public fun toAllApplications(context: Context) {
        val intent = Intent(context!!, AllVacanciesActivity::class.java)
        intent.putExtra("email", email)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    public fun signOut(context: Context) {
        Firebase.auth.signOut()
        val intent = Intent(context!!, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun setEmployeeInfo(email: String) {
        db.collection("Employee")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val employee = document.toObject<Employee>()
                    id = employee.id
                    nameTextView.text = employee.name
                    surnameTextView.text = employee.surname
                    phoneTextView.text = employee.phone
                    addressTextView.text = employee.address
                    pkTextView.text = employee.personal_code
                    break
                }
            }
    }

    private fun editEmployee() {
        nameEditText.setText(nameTextView.text.toString())
        surnameEditText.setText(surnameTextView.text.toString())
        phoneEditText.setText(phoneTextView.text.toString())
        addressEditText.setText(addressTextView.text.toString())
        pkEditText.setText(pkTextView.text.toString())
        nameEditText.visibility = View.VISIBLE
        surnameEditText.visibility = View.VISIBLE
        phoneEditText.visibility = View.VISIBLE
        addressEditText.visibility = View.VISIBLE
        pkEditText.visibility = View.VISIBLE
        nameTextView.visibility = View.INVISIBLE
        surnameTextView.visibility = View.INVISIBLE
        phoneTextView.visibility = View.INVISIBLE
        addressTextView.visibility = View.INVISIBLE
        pkTextView.visibility = View.INVISIBLE
    }

    private fun saveChanges() {
        if (pkEditText.text.toString().length == 12 && phoneEditText.text.toString().length == 8 && nameEditText.text.toString() != "" && surnameEditText.text.toString() != "" && addressEditText.text.toString() != "") {
            db.collection("Employee").document(id)
                .update(
                    mapOf(
                        "name" to nameEditText.text.toString(),
                        "surname" to surnameEditText.text.toString(),
                        "phone" to phoneEditText.text.toString(),
                        "address" to addressEditText.text.toString(),
                        "personal_code" to pkEditText.text.toString()
                    )
                )
                .addOnSuccessListener {
                    nameTextView.text = nameEditText.text.toString()
                    surnameTextView.text = surnameEditText.text.toString()
                    phoneTextView.text = phoneEditText.text.toString()
                    addressTextView.text = addressEditText.text.toString()
                    pkTextView.text = pkEditText.text.toString()
                    nameEditText.visibility = View.INVISIBLE
                    surnameEditText.visibility = View.INVISIBLE
                    phoneEditText.visibility = View.INVISIBLE
                    addressEditText.visibility = View.INVISIBLE
                    pkEditText.visibility = View.INVISIBLE
                    nameTextView.visibility = View.VISIBLE
                    surnameTextView.visibility = View.VISIBLE
                    phoneTextView.visibility = View.VISIBLE
                    addressTextView.visibility = View.VISIBLE
                    pkTextView.visibility = View.VISIBLE
                    editTextHiden = true
                    editProfileButton.text = "Labot profilu"
                }
        }
    }
}
