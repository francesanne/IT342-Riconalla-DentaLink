package com.example.dentalinkmobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.model.ServiceDto
import kotlinx.coroutines.launch

class ServicesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services)

        val lvServices  = findViewById<ListView>(R.id.lvServices)
        val tvEmpty     = findViewById<TextView>(R.id.tvServicesEmpty)
        val progressBar = findViewById<ProgressBar>(R.id.progressServices)

        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getServices()
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val services = response.body()?.data ?: emptyList()

                    if (services.isEmpty()) {
                        tvEmpty.visibility    = View.VISIBLE
                        lvServices.visibility = View.GONE
                    } else {
                        tvEmpty.visibility    = View.GONE
                        lvServices.visibility = View.VISIBLE
                        lvServices.adapter    = ServiceAdapter(this@ServicesActivity, services)
                        lvServices.setOnItemClickListener { _, _, position, _ ->
                            openBooking(services[position])
                        }
                    }
                } else {
                    Toast.makeText(this@ServicesActivity, "Failed to load services", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ServicesActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openBooking(service: ServiceDto) {
        val intent = Intent(this, BookingActivity::class.java).apply {
            putExtra("SERVICE_ID",    service.id)
            putExtra("SERVICE_NAME",  service.name)
            putExtra("SERVICE_PRICE", service.price)
        }
        startActivity(intent)
    }
}

private class ServiceAdapter(
    context: Context,
    private val items: List<ServiceDto>
) : ArrayAdapter<ServiceDto>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_service, parent, false)

        val item = items[position]
        view.findViewById<TextView>(R.id.tvServiceName).text        = item.name
        view.findViewById<TextView>(R.id.tvServiceDescription).text = item.description ?: ""
        view.findViewById<TextView>(R.id.tvServicePrice).text       = "P${String.format("%.2f", item.price)}"
        return view
    }
}