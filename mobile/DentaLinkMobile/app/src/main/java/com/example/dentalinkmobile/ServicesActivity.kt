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
import com.example.dentalinkmobile.features.services.model.ServiceDto
import com.example.dentalinkmobile.utils.ImageLoader
import com.example.dentalinkmobile.utils.formatPeso
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ServicesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services)

        val toolbar     = findViewById<MaterialToolbar>(R.id.toolbar)
        val lvServices  = findViewById<ListView>(R.id.lvServices)
        val tvEmpty     = findViewById<TextView>(R.id.tvServicesEmpty)
        val progressBar = findViewById<ProgressBar>(R.id.progressServices)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

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
                        lvServices.adapter    = ServiceAdapter(this@ServicesActivity, services, lifecycleScope)
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
    private val items: List<ServiceDto>,
    private val scope: CoroutineScope
) : ArrayAdapter<ServiceDto>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_service, parent, false)

        val item    = items[position]
        val ivImage = view.findViewById<ImageView>(R.id.ivServiceImage)

        view.findViewById<TextView>(R.id.tvServiceName).text        = item.name
        view.findViewById<TextView>(R.id.tvServiceDescription).text = item.description ?: ""
        view.findViewById<TextView>(R.id.tvServicePrice).text       = formatPeso(item.price)

        // Reset to placeholder before (re)loading — prevents stale images during recycling
        ivImage.setImageResource(android.R.drawable.ic_menu_gallery)

        if (!item.imageUrl.isNullOrBlank()) {
            scope.launch {
                ImageLoader.loadInto(item.imageUrl, ivImage)
            }
        }

        return view
    }
}