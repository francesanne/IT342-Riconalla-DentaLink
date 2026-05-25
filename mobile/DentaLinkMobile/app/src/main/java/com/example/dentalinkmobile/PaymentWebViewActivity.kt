package com.example.dentalinkmobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class PaymentWebViewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CHECKOUT_URL   = "checkout_url"
        const val EXTRA_APPOINTMENT_ID = "appointment_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_webview)

        val toolbar  = findViewById<MaterialToolbar>(R.id.toolbar)
        val webView  = findViewById<WebView>(R.id.wvPayment)
        val progress = findViewById<ProgressBar>(R.id.progressPayment)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        val checkoutUrl = intent.getStringExtra(EXTRA_CHECKOUT_URL)
        if (checkoutUrl.isNullOrBlank()) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        webView.settings.apply {
            javaScriptEnabled    = true
            domStorageEnabled    = true
            loadWithOverviewMode = true
            useWideViewPort      = true
            setSupportMultipleWindows(true)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return handleUrl(request.url.toString())
            }

            @Suppress("OVERRIDE_DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return handleUrl(url)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progress.progress   = newProgress
                progress.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
            }

            override fun onCreateWindow(
                view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?
            ): Boolean {
                val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
                val tempView = WebView(view.context)
                tempView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(tv: WebView, request: WebResourceRequest): Boolean {
                        handleUrl(request.url.toString())
                        tv.destroy()
                        return true
                    }
                    @Suppress("OVERRIDE_DEPRECATION")
                    override fun shouldOverrideUrlLoading(tv: WebView, url: String): Boolean {
                        handleUrl(url)
                        tv.destroy()
                        return true
                    }
                }
                transport.webView = tempView
                resultMsg.sendToTarget()
                return true
            }
        }

        webView.loadUrl(checkoutUrl)
    }

    private fun handleUrl(url: String): Boolean {
        if (url.contains("paymongo.com")) return false
        if (url.contains("payment/success") || url.contains("payment-success")) {
            val appointmentId = try {
                Uri.parse(url).getQueryParameter("appointmentId")?.toLong() ?: -1L
            } catch (e: Exception) { -1L }
            val data = Intent().apply { putExtra(EXTRA_APPOINTMENT_ID, appointmentId) }
            setResult(RESULT_OK, data)
        } else {
            setResult(RESULT_CANCELED)
        }
        finish()
        return true
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }
}