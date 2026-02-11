package com.dtaiot.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Bitmap
import android.view.View
import android.widget.ProgressBar
import android.os.Build
import android.widget.LinearLayout
import android.widget.Button
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError
import android.webkit.WebResourceResponse
import android.net.ConnectivityManager
import android.net.NetworkCapabilities





class MainActivity : AppCompatActivity() {

    lateinit var webView: WebView

    lateinit var progressBar: ProgressBar
    lateinit var errorLayout: LinearLayout   // add this line
    lateinit var retryButton: Button
    


    private fun isInternetAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val info = cm.activeNetworkInfo
            info != null && info.isConnected
        }
    }



    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.importantForAutofill =
                View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }


        webView = findViewById(R.id.webView)
        // 🚫 Disable Autofill only for WebView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        }

        // 🔒 Disable password save / autofill
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        errorLayout = findViewById<LinearLayout>(R.id.errorLayout)
        retryButton = findViewById<Button>(R.id.retryButton)

        retryButton.setOnClickListener {
            if (isInternetAvailable()) {
                errorLayout.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                webView.visibility = View.INVISIBLE

                webView.loadUrl("about:blank") // 🔑 prevents error flash

                webView.postDelayed({
                    webView.loadUrl("https://dtaiotweb.pythonanywhere.com")
                }, 300)
            }
        }



        @Suppress("DEPRECATION")
        webView.settings.setSaveFormData(false)

        // Hide WebView identity
        webView.settings.userAgentString =
            webView.settings.userAgentString.replace("wv", "")

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        webView.settings.allowFileAccess = false
        webView.settings.allowContentAccess = false


//        webView.webViewClient = WebViewClient()

        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progressBar.visibility = View.VISIBLE
                errorLayout.visibility = View.GONE
                webView.visibility = View.INVISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {

                progressBar.visibility = View.GONE
                webView.visibility = View.VISIBLE

            }

            // 🔴 THIS IS THE IMPORTANT PART
            @Suppress("DEPRECATION")
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            )
            {
                showCustomError(view)
            }

            // ✅ For Android 6.0+ (API 23+)
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    showCustomError(view)
                }
            }
            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                if (request?.isForMainFrame == true) {
                    showCustomError(view)
                }
            }



        }

        // 🔗 YOUR DJANGO URL
        if (isInternetAvailable()) {
            webView.loadUrl("https://dtaiotweb.pythonanywhere.com")
        } else {
            showCustomError(null)
        }
    }

    private fun showCustomError(view: WebView?) {
        view?.stopLoading()
        webView.visibility = View.GONE
        progressBar.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
    }
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
