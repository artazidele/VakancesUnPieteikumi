package com.example.vakances.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vakances.MainActivity
import com.example.vakances.R
import com.example.vakances.databinding.ActivityAllVacanciesBinding
import com.example.vakances.model.Application
import com.example.vakances.model.Employee
import com.example.vakances.model.Vacancy
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.util.*

import com.google.android.gms.tasks.OnCompleteListener

import com.google.firebase.storage.FirebaseStorage

import android.app.ProgressDialog
import android.widget.*
import java.lang.Exception


class AllVacanciesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAllVacanciesBinding
    private val db = FirebaseFirestore.getInstance()
    private val storage = Firebase.storage
    private lateinit var email: String
    private lateinit var fileTextView: TextView
    private lateinit var applicationId: String
    val pdf: Int = 0
    lateinit var uri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllVacanciesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        email = intent.getStringExtra("email").toString()
        val query = db.collection("Vacancy")
            .orderBy("title")
        val options =
            FirestoreRecyclerOptions.Builder<Vacancy>().setQuery(query, Vacancy::class.java)
                .setLifecycleOwner(this).build()
        val adapter = object : FirestoreRecyclerAdapter<Vacancy, VacancyViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VacancyViewHolder {
                val view = LayoutInflater.from(this@AllVacanciesActivity)
                    .inflate(R.layout.all_vacancy_row, parent, false)
                return VacancyViewHolder(view)
            }

            override fun onBindViewHolder(
                holder: VacancyViewHolder,
                position: Int,
                model: Vacancy
            ) {
                holder.itemView.findViewById<TextView>(R.id.vacancy_title).text = model.title
                holder.itemView.findViewById<TextView>(R.id.vacancy_salary).text = model.salary
                holder.itemView.findViewById<Button>(R.id.check_button).setOnClickListener {
                    makeApplication(model, email, this@AllVacanciesActivity)
                }
            }
        }
        binding.vacancyRv.adapter = adapter
        binding.vacancyRv.layoutManager = LinearLayoutManager(this)
    }

    private fun makeApplication(vacancy: Vacancy, email: String, context: Context) {
        db.collection("Employee")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val employee = document.toObject<Employee>()
                    makeApplicationData(vacancy, employee, context)
                    break
                }
            }
    }

    private fun makeApplicationData(vacancy: Vacancy, employee: Employee, context: Context) {
        val dialogView =
            LayoutInflater.from(context).inflate(R.layout.make_application, null)
        val builder = AlertDialog.Builder(context)
            .setView(dialogView)
        val alertDialog = builder.show()
        val uuid = UUID.randomUUID()
        val id = uuid.toString()
        applicationId = id
        fileTextView = dialogView.findViewById<TextView>(R.id.file_path_tv)
        dialogView.findViewById<Button>(R.id.add_cv_button).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            startActivityForResult(intent, pdf)
        }

        dialogView.findViewById<Button>(R.id.add_application_button).setOnClickListener {
            var motivationLetter = dialogView.findViewById<EditText>(R.id.letter_et).text.toString()
            val application = Application(
                id,
                employee.id,
                employee.name,
                employee.surname,
                employee.personal_code,
                employee.email,
                employee.phone,
                employee.address,
                motivationLetter,
                vacancy.title,
                vacancy.id
            )
            dialogView.findViewById<TextView>(R.id.file_path_tv).visibility = View.INVISIBLE
            val fileName = dialogView.findViewById<TextView>(R.id.file_path_tv).text.toString()
            if (fileName != "") {
                addCV(application)
                alertDialog.dismiss()
            } else {
                SignUpActivity().showErrorDialog(
                    "",
                    "Lūdzu, pievienojiet CV!",
                    this
                )
            }
        }
        dialogView.findViewById<Button>(R.id.cancel).setOnClickListener {
            alertDialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == pdf) {
                uri = data!!.data!!
                fileTextView.text = uri.toString()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.employee_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sign_out -> signOut(this)
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

    public fun signOut(context: Context) {
        Firebase.auth.signOut()
        val intent = Intent(context!!, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun addCV(application: Application) {
        val storageRef = storage.reference
        val documentRef = storageRef.child(applicationId)
        documentRef.putFile(uri!!)
            .addOnSuccessListener {
                addApplication(application)
            }
    }

    private fun addApplication(application: Application) {
        db.collection("Application").document(application.id)
            .set(application)
            .addOnSuccessListener {
                SignUpActivity().showErrorDialog(
                    "",
                    "Pieteikums saglabāts.",
                    this
                )
            }

    }
}
