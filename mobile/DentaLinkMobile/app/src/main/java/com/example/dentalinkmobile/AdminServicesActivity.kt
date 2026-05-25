package com.example.dentalinkmobile

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dentalinkmobile.api.RetrofitClient
import com.example.dentalinkmobile.features.services.model.ServiceDto
import com.example.dentalinkmobile.features.payments.model.ServiceRequest
import com.example.dentalinkmobile.utils.ImageLoader
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class AdminServicesActivity : AppCompatActivity() {

    private var serviceList = listOf<ServiceDto>()
    private lateinit var lvServices: ListView
    private lateinit var tvEmpty: TextView

    private var pendingServiceImageUri: Uri? = null
    private var onImagePickedCallback: ((Uri) -> Unit)? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingServiceImageUri = uri
            onImagePickedCallback?.invoke(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_services)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        lvServices = findViewById(R.id.lvAdminServices)
        tvEmpty    = findViewById(R.id.tvAdminServicesEmpty)

        findViewById<Button>(R.id.btnAddService).setOnClickListener {
            showServiceDialog(null)
        }
    }

    override fun onResume() {
        super.onResume()
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

        lvServices.adapter = AdminServiceAdapter(this, serviceList, lifecycleScope)
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
        pendingServiceImageUri = null

        val view = layoutInflater.inflate(R.layout.dialog_service, null)
        val flImagePicker  = view.findViewById<android.widget.FrameLayout>(R.id.flServiceImagePicker)
        val ivImagePreview = view.findViewById<ImageView>(R.id.ivServiceImagePreview)
        val llImageHint    = view.findViewById<android.widget.LinearLayout>(R.id.llServiceImageHint)
        val etName         = view.findViewById<EditText>(R.id.etServiceName)
        val etDescription  = view.findViewById<EditText>(R.id.etServiceDescription)
        val etPrice        = view.findViewById<EditText>(R.id.etServicePrice)

        if (existing != null) {
            etName.setText(existing.name)
            etDescription.setText(existing.description ?: "")
            etPrice.setText(existing.price.toString())
            if (!existing.imageUrl.isNullOrBlank()) {
                lifecycleScope.launch {
                    val bitmap = ImageLoader.fetchBitmap(existing.imageUrl)
                    if (bitmap != null) {
                        ivImagePreview.setImageBitmap(bitmap)
                        ivImagePreview.visibility = View.VISIBLE
                        llImageHint.visibility    = View.GONE
                    }
                }
            }
        }

        onImagePickedCallback = { uri ->
            showLocalPreview(uri, ivImagePreview, llImageHint)
        }

        flImagePicker.setOnClickListener {
            pickImageLauncher.launch("image/*")
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
            .setNegativeButton("Cancel") { _, _ ->
                pendingServiceImageUri = null
                onImagePickedCallback = null
            }
            .show()
    }

    private fun showLocalPreview(uri: Uri, ivPreview: ImageView, llHint: android.widget.LinearLayout) {
        try {
            val stream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(stream)
            stream?.close()
            if (bitmap != null) {
                ivPreview.setImageBitmap(bitmap)
                ivPreview.visibility = View.VISIBLE
                llHint.visibility    = View.GONE
            }
        } catch (e: Exception) {
            // silently ignore — upload can still proceed
        }
    }

    private fun createService(request: ServiceRequest) {
        val imageUri = pendingServiceImageUri
        pendingServiceImageUri = null
        onImagePickedCallback  = null

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.createService(request)
                if (response.isSuccessful) {
                    val newService = response.body()?.data
                    Toast.makeText(this@AdminServicesActivity, "Service created", Toast.LENGTH_SHORT).show()
                    // Upload image first (suspend), THEN reload so the image URL is already saved
                    if (newService != null && imageUri != null) {
                        uploadServiceImage(newService.id, imageUri)
                    }
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
        val imageUri = pendingServiceImageUri
        pendingServiceImageUri = null
        onImagePickedCallback  = null

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.updateService(id, request)
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminServicesActivity, "Service updated", Toast.LENGTH_SHORT).show()
                    // Upload image first (suspend), THEN reload
                    if (imageUri != null) {
                        uploadServiceImage(id, imageUri)
                    }
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

    // suspend — runs inline inside the caller's coroutine so loadServices() waits for it
    private suspend fun uploadServiceImage(serviceId: Long, uri: Uri) {
        try {
            val mimeType    = contentResolver.getType(uri) ?: "image/jpeg"
            val bytes       = contentResolver.openInputStream(uri)?.readBytes() ?: return
            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part        = MultipartBody.Part.createFormData("file", "service.jpg", requestBody)

            val response = RetrofitClient.apiService.uploadServiceImage(serviceId, part)
            if (!response.isSuccessful) {
                Toast.makeText(this@AdminServicesActivity, "Image upload failed (${response.code()})", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this@AdminServicesActivity, "Image upload error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

private class AdminServiceAdapter(
    context: Context,
    private val items: List<ServiceDto>,
    private val scope: CoroutineScope
) : ArrayAdapter<ServiceDto>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_admin_service, parent, false)

        val item    = items[position]
        val ivImage = view.findViewById<ImageView>(R.id.ivServiceImage)

        view.findViewById<TextView>(R.id.tvServiceName).text        = item.name
        view.findViewById<TextView>(R.id.tvServiceDescription).text = item.description ?: ""
        view.findViewById<TextView>(R.id.tvServicePrice).text       = "P${String.format("%.2f", item.price)}"

        ivImage.setImageResource(android.R.drawable.ic_menu_gallery)
        if (!item.imageUrl.isNullOrBlank()) {
            scope.launch {
                ImageLoader.loadInto(item.imageUrl, ivImage)
            }
        }

        return view
    }
}