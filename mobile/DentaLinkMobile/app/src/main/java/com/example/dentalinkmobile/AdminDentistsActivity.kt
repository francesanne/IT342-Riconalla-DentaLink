package com.example.dentalinkmobile

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.features.dentists.model.DentistDto
import com.example.dentalinkmobile.features.payments.model.DentistRequest
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class AdminDentistsActivity : AppCompatActivity() {

    private var dentistList = listOf<DentistDto>()
    private lateinit var lvDentists: ListView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dentists)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        lvDentists  = findViewById(R.id.lvAdminDentists)
        tvEmpty     = findViewById(R.id.tvAdminDentistsEmpty)
        progressBar = findViewById(R.id.progressBar)

        findViewById<Button>(R.id.btnAddDentist).setOnClickListener {
            showDentistDialog(null)
        }
    }

    override fun onResume() {
        super.onResume()
        loadDentists()
    }

    private fun loadDentists() {
        progressBar.visibility = View.VISIBLE
        lvDentists.visibility  = View.GONE
        tvEmpty.visibility     = View.GONE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getDentists()
                if (response.isSuccessful) {
                    dentistList = response.body()?.data ?: emptyList()
                    renderList()
                } else {
                    Snackbar.make(this@AdminDentistsActivity.findViewById(android.R.id.content), "Failed to load dentists", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(this@AdminDentistsActivity.findViewById(android.R.id.content), "Network error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun renderList() {
        if (dentistList.isEmpty()) {
            tvEmpty.visibility    = View.VISIBLE
            lvDentists.visibility = View.GONE
            return
        }
        tvEmpty.visibility    = View.GONE
        lvDentists.visibility = View.VISIBLE

        lvDentists.adapter = AdminDentistAdapter(this, dentistList)
        lvDentists.setOnItemClickListener { _, _, position, _ ->
            showItemOptions(dentistList[position])
        }
    }

    private fun showItemOptions(dentist: DentistDto) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this)
            .setTitle(dentist.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showDentistDialog(dentist)
                    1 -> confirmDelete(dentist)
                }
            }
            .show()
    }

    private fun showDentistDialog(existing: DentistDto?) {
        val view       = layoutInflater.inflate(R.layout.dialog_dentist, null)
        val etName     = view.findViewById<EditText>(R.id.etDentistName)
        val etSpec     = view.findViewById<EditText>(R.id.etDentistSpecialization)
        val rgStatus   = view.findViewById<RadioGroup>(R.id.rgDentistStatus)
        val rbActive   = view.findViewById<RadioButton>(R.id.rbActive)
        val rbInactive = view.findViewById<RadioButton>(R.id.rbInactive)

        if (existing != null) {
            etName.setText(existing.name)
            etSpec.setText(existing.specialization ?: "")
            if (existing.status == "INACTIVE") rbInactive.isChecked = true else rbActive.isChecked = true
        } else {
            rbActive.isChecked = true
        }

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Add Dentist" else "Edit Dentist")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val name   = etName.text.toString().trim()
                val spec   = etSpec.text.toString().trim()
                val status = if (rgStatus.checkedRadioButtonId == R.id.rbInactive) "INACTIVE" else "ACTIVE"

                if (name.isEmpty()) {
                    Snackbar.make(findViewById(android.R.id.content), "Name is required", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val request = DentistRequest(name, spec, status)
                if (existing == null) createDentist(request) else updateDentist(existing.id, request)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createDentist(request: DentistRequest) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.createDentist(request)
                if (response.isSuccessful) {
                    Snackbar.make(this@AdminDentistsActivity.findViewById(android.R.id.content), "Dentist added", Snackbar.LENGTH_SHORT).show()
                    loadDentists()
                } else {
                    Snackbar.make(this@AdminDentistsActivity.findViewById(android.R.id.content), "Failed to add dentist (${response.code()})", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(this@AdminDentistsActivity.findViewById(android.R.id.content), "Network error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateDentist(id: Long, request: DentistRequest) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.updateDentist(id, request)
                if (response.isSuccessful) {
                    Snackbar.make(this@AdminDentistsActivity.findViewById(android.R.id.content), "Dentist updated", Snackbar.LENGTH_SHORT).show()
                    loadDentists()
                } else {
                    Snackbar.make(this@AdminDentistsActivity.findViewById(android.R.id.content), "Failed to update dentist (${response.code()})", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(this@AdminDentistsActivity.findViewById(android.R.id.content), "Network error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDelete(dentist: DentistDto) {
        AlertDialog.Builder(this)
            .setTitle("Delete Dentist")
            .setMessage("Delete \"${dentist.name}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteDentist(dentist.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteDentist(id: Long) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteDentist(id)
                if (response.isSuccessful) {
                    Snackbar.make(this@AdminDentistsActivity.findViewById(android.R.id.content), "Dentist deleted", Snackbar.LENGTH_SHORT).show()
                    loadDentists()
                } else {
                    Snackbar.make(this@AdminDentistsActivity.findViewById(android.R.id.content), "Failed to delete dentist (${response.code()})", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Snackbar.make(this@AdminDentistsActivity.findViewById(android.R.id.content), "Network error: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}

private class AdminDentistAdapter(
    context: Context,
    private val items: List<DentistDto>
) : ArrayAdapter<DentistDto>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_admin_dentist, parent, false)

        val item = items[position]

        view.findViewById<TextView>(R.id.tvDentistInitial).text = item.name.firstOrNull()?.uppercase() ?: "?"
        view.findViewById<TextView>(R.id.tvDentistName).text   = item.name
        view.findViewById<TextView>(R.id.tvDentistSpec).text   = item.specialization ?: ""

        val tvStatus = view.findViewById<TextView>(R.id.tvDentistStatus)
        tvStatus.text = item.status
        when (item.status) {
            "ACTIVE" -> {
                tvStatus.setBackgroundResource(R.drawable.bg_badge_completed)
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.badge_completed_text))
            }
            else -> {
                tvStatus.setBackgroundResource(R.drawable.bg_badge_cancelled)
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.badge_cancelled_text))
            }
        }

        return view
    }
}