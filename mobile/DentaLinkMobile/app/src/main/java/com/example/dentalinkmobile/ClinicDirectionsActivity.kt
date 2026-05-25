package com.example.dentalinkmobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class ClinicDirectionsActivity : AppCompatActivity() {

    companion object {
        private const val CLINIC_LAT = 10.24738412405074
        private const val CLINIC_LNG = 123.8000086426953
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinic_directions)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val webView = findViewById<WebView>(R.id.wvClinicMap)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()

        // Google Maps embed now requires an actual <iframe> — load HTML wrapper
        val embedUrl = "https://maps.google.com/maps?q=$CLINIC_LAT,$CLINIC_LNG&z=16&output=embed"
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    * { margin: 0; padding: 0; }
                    html, body, iframe { width: 100%; height: 100%; border: 0; }
                </style>
            </head>
            <body>
                <iframe src="$embedUrl" allowfullscreen loading="lazy"></iframe>
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL("https://maps.google.com", html, "text/html", "UTF-8", null)

        findViewById<Button>(R.id.btnOpenMaps).setOnClickListener {
            val geoUri = Uri.parse(
                "geo:$CLINIC_LAT,$CLINIC_LNG?q=$CLINIC_LAT,$CLINIC_LNG(DentaLink+Dental+Clinic)"
            )
            val mapsIntent = Intent(Intent.ACTION_VIEW, geoUri)
            if (mapsIntent.resolveActivity(packageManager) != null) {
                startActivity(mapsIntent)
            } else {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps?q=$CLINIC_LAT,$CLINIC_LNG")
                    )
                )
            }
        }
    }
}