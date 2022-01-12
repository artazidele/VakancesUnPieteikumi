package com.example.vakances.views

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vakances.MainActivity
import com.example.vakances.R
import com.example.vakances.databinding.ActivityManagerMainBinding
import com.example.vakances.model.Application
import com.example.vakances.model.Vacancy
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.*

class ManagerMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManagerMainBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var managerId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagerMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        managerId = intent.getStringExtra("id").toString()
        val query = db.collection("Vacancy")
            .whereEqualTo("managerId", managerId)
            .orderBy("title")
        val options =
            FirestoreRecyclerOptions.Builder<Vacancy>().setQuery(query, Vacancy::class.java)
                .setLifecycleOwner(this).build()
        val adapter = object : FirestoreRecyclerAdapter<Vacancy, VacancyViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VacancyViewHolder {
                val view = LayoutInflater.from(this@ManagerMainActivity)
                    .inflate(R.layout.vacancy_row, parent, false)
                return VacancyViewHolder(view)
            }
            override fun onBindViewHolder(holder: VacancyViewHolder, position: Int, model: Vacancy) {
                holder.itemView.findViewById<TextView>(R.id.vacancy_title).text = model.title
                holder.itemView.findViewById<TextView>(R.id.vacancy_salary).text = model.salary
                holder.itemView.setOnClickListener {
                    showVacancyApplications(model, this@ManagerMainActivity)
                }
            }
        }
        binding.vacancyRv.adapter = adapter
        binding.vacancyRv.layoutManager = LinearLayoutManager(this)
    }

    private fun showVacancyApplications(vacancy: Vacancy, context: Context) {
        val dialogView =
            LayoutInflater.from(context).inflate(R.layout.one_vacancy_applications, null)
        val builder = AlertDialog.Builder(context)
            .setView(dialogView)
        val alertDialog = builder.show()
        val query = db.collection("Application")
            .whereEqualTo("vacancy_id", vacancy.id)
            .orderBy("surname")
        val options =
            FirestoreRecyclerOptions.Builder<Application>().setQuery(query, Application::class.java)
                .setLifecycleOwner(this).build()
        val adapter = object : FirestoreRecyclerAdapter<Application, ApplicationViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.employee_row, parent, false)
                return ApplicationViewHolder(view)
            }

            override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int, model: Application) {
                holder.itemView.findViewById<TextView>(R.id.vacancy_title).text = model.name + " " + model.surname
                holder.itemView.setOnClickListener {
                    EmployeeMainActivity().showMoreAboutApplication(model, context)
                }
            }
        }
        dialogView.findViewById<RecyclerView>(R.id.vacancy_rv).adapter = adapter
        dialogView.findViewById<RecyclerView>(R.id.vacancy_rv).layoutManager = LinearLayoutManager(this)
        dialogView.findViewById<Button>(R.id.close_window_button).setOnClickListener {
            alertDialog.dismiss()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.manager_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sign_out -> signOut(this)
            R.id.new_vacancy -> addVacancy(this, managerId)
            R.id.my_profile -> toMyProfile(this)
        }
        return super.onOptionsItemSelected(item)
    }

    public fun toMyProfile(context: Context) {
        val intent = Intent(context!!, ManagerProfileActivity::class.java)
        intent.putExtra("id", managerId)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    public fun addVacancy(context: Context, managerId: String) {
        val dialogView =
            LayoutInflater.from(context).inflate(R.layout.add_vacancy, null)
        val builder = AlertDialog.Builder(context)
            .setView(dialogView)
        val alertDialog = builder.show()
        dialogView.findViewById<Button>(R.id.save_vacancy_button).setOnClickListener {
            val title = dialogView.findViewById<EditText>(R.id.title_et).text.toString()
            val salary = dialogView.findViewById<EditText>(R.id.salary_et).text.toString()
            val uuid = UUID.randomUUID()
            val id = uuid.toString()
            val vacancy = Vacancy(id, title, managerId, salary)
            db.collection("Vacancy").document(id).set(vacancy)
            alertDialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.close_button).setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun signOut(context: Context) {
        Firebase.auth.signOut()
        val intent = Intent(context!!, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
