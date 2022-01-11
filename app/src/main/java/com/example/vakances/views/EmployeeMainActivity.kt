package com.example.vakances.views

import android.content.Context
import android.content.Intent
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class EmployeeMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmployeeMainBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var email: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        email = intent.getStringExtra("email").toString()
        val query = db.collection("Applications")
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
//        dialogView.findViewById<TextView>(R.id.title_text_view).text = cake.title
//        dialogView.findViewById<TextView>(R.id.description_text_view).text = cake.description
//        dialogView.findViewById<TextView>(R.id.ingredients_text_view).text = cake.ingredients
        dialogView.findViewById<Button>(R.id.close_application_button).setOnClickListener {
            alertDialog.dismiss()
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
        }
        return super.onOptionsItemSelected(item)
    }

    public fun toAllApplications(context: Context) {
        val intent = Intent(context!!, MainActivity::class.java)
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
}