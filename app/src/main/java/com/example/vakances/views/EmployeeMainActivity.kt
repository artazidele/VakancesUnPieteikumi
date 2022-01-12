package com.example.vakances.views

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vakances.MainActivity
import com.example.vakances.R
import com.example.vakances.databinding.ActivityEmployeeMainBinding
import com.example.vakances.databinding.ActivityMainBinding
import com.example.vakances.model.Application
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.net.HttpURLConnection
import java.net.URL

class EmployeeMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmployeeMainBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var email: String
    private val storage = Firebase.storage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        email = intent.getStringExtra("email").toString()
        val query = db.collection("Application")
            .whereEqualTo("email", email)
            .orderBy("vacancy_title")
        val options =
            FirestoreRecyclerOptions.Builder<Application>().setQuery(query, Application::class.java)
                .setLifecycleOwner(this).build()
        val adapter = object : FirestoreRecyclerAdapter<Application, ApplicationViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
                val view = LayoutInflater.from(this@EmployeeMainActivity)
                    .inflate(R.layout.employee_row, parent, false)
                return ApplicationViewHolder(view)
            }

            override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int, model: Application) {
                holder.itemView.findViewById<TextView>(R.id.vacancy_title).text = model.vacancy_title
                holder.itemView.setOnClickListener {
                    showMoreAboutApplication(model, this@EmployeeMainActivity)
                }
            }
        }
        binding.applicationRv.adapter = adapter
        binding.applicationRv.layoutManager = LinearLayoutManager(this)
    }

    public fun showMoreAboutApplication(application: Application, context: Context) {
        val dialogView =
            LayoutInflater.from(context).inflate(R.layout.more_about_application, null)
        val builder = AlertDialog.Builder(context)
            .setView(dialogView)
        val alertDialog = builder.show()
        dialogView.findViewById<TextView>(R.id.application_name).text = "Vārds: " + application.name
        dialogView.findViewById<TextView>(R.id.application_surname).text = "Uzvārds: " + application.surname
        dialogView.findViewById<TextView>(R.id.application_address).text = "Adrese: " + application.address
        dialogView.findViewById<TextView>(R.id.application_phone).text = "Telefona numurs: " + application.phone
        dialogView.findViewById<TextView>(R.id.application_email).text = "E-pasts: " + application.email
        dialogView.findViewById<TextView>(R.id.application_motivation_letter).text = "Motivācijas vēstule: \n" + application.motivation_letter
        dialogView.findViewById<TextView>(R.id.application_personal_code).text = "Personas kods: " + application.personal_code
        dialogView.findViewById<TextView>(R.id.application_vacancy_title).text = "Vakance: " + application.vacancy_title
        dialogView.findViewById<Button>(R.id.close_application_button).setOnClickListener {
            alertDialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.show_cv).setOnClickListener{
            showCV(application)
        }
    }

    private fun showCV(application: Application) {
        val ref = storage.reference
        ref.child(application.id).downloadUrl.addOnSuccessListener { uri ->
            openUrl(uri)
        }
    }

    private fun openUrl(uri: Uri) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri!!.toString()))
        startActivity(browserIntent)
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
            R.id.my_profile -> toMyProfile(this)
        }
        return super.onOptionsItemSelected(item)
    }

    public fun toAllApplications(context: Context) {
        val intent = Intent(context!!, AllVacanciesActivity::class.java)
        intent.putExtra("email", email)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    public fun toMyProfile(context: Context) {
        val intent = Intent(context!!, EmployeeProfileActivity::class.java)
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

    public fun toProfileActivity(context: Context) {
        val intent = Intent(context!!, EmployeeProfileActivity::class.java)
        intent.putExtra("email", email)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
