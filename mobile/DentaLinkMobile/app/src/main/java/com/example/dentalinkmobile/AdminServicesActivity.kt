package com.example.dentalinkmobile

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.model.ServiceDto
import com.example.dentalinkmobile.model.ServiceRequest
import kotlinx.coroutines.launch

class AdminServicesActivity : AppCompatActivity() {

    private var serviceList = listOf<ServiceDto>()
    private lateinit var lvServices: ListView
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_services)

        lvServices = findViewById(R.id.lvAdminServices)
        tvEmpty    = findViewById(R.id.tvAdminServicesEmpty)

        findViewById<Button>(R.id.btnAddService).setOnClickListener {
            showServiceDialog(null)
        }

        loadServices()
    }

    private fun loadServices() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getServices()
                if (response.isSuccessful) {
                    serviceList = response.body()?.data ?: emptyList()
                    renderList()
                } else {
                    Toast.makeText(this@AdminServicesActivity, "Failed to load services", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminServicesActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderList() {
        if (serviceList.isEmpty()) {
            tvEmpty.visibility    = View.VISIBLE
            lvServices.visibility = View.GONE
            return
        }
        tvEmpty.visibility    = View.GONE
        lvServices.visibility = View.VISIBLE

        val labels = serviceList.map { s ->
            "${s.name}\nP${String.format("%.2f", s.price)}\n${s.description ?: ""}"
        }
        lvServices.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, labels)

        lvServices.setOnItemClickListener { _, _, position, _ ->
            showItemOptions(serviceList[position])
        }
    }

    private fun showItemOptions(service: ServiceDto) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this)
            .setTitle(service.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showServiceDialog(service)
                    1 -> confirmDelete(service)
                }
            }
            .show()
    }

    private fun showServiceDialog(existing: ServiceDto?) {
        val view = layoutInflater.inflate(R.layout.dialog_service, null)
        val etName        = view.findViewById<EditText>(R.id.etServiceName)
        val etDescription = view.findViewById<EditText>(R.id.etServiceDescription)
        val etPrice       = view.findViewById<EditText>(R.id.etServicePrice)

        if (existing != null) {
            etName.setText(existing.name)
            etDescription.setText(existing.description ?: "")
            etPrice.setText(existing.price.toString())
        }

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Add Service" else "Edit Service")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val name  = etName.text.toString().trim()
                val desc  = etDescription.text.toString().trim()
                val price = etPrice.text.toString().toDoubleOrNull()

                if (name.isEmpty() || price == null) {
                    Toast.makeText(this, "Name and valid price are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val request = ServiceRequest(name, desc, price)
                if (existing == null) createService(request) else updateService(existing.id, request)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createService(request: ServiceRequest) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.createService(request)
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminServicesActivity, "Service created", Toast.LENGTH_SHORT).show()
                    loadServices()
                } else {
                    Toast.makeText(this@AdminServicesActivity, "Failed to create service (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminServicesActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateService(id: Long, request: ServiceRequest) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.updateService(id, request)
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminServicesActivity, "Service updated", Toast.LENGTH_SHORT).show()
                    loadServices()
                } else {
                    Toast.makeText(this@AdminServicesActivity, "Failed to update service (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminServicesActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDelete(service: ServiceDto) {
        AlertDialog.Builder(this)
            .setTitle("Delete Service")
            .setMessage("Delete \"${service.name}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteService(service.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteService(id: Long) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteService(id)
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminServicesActivity, "Service deleted", Toast.LENGTH_SHORT).show()
                    loadServices()
                } else {
                    Toast.makeText(this@AdminServicesActivity, "Failed to delete service (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminServicesActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}