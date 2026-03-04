package com.example.notebook

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.notebook.databinding.ActivityWebAppBinding

class WebAppActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebAppBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                } else {
                    finish()
                }
            }
        })

        binding.webView.apply {
            webViewClient = WebViewClient()
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    binding.webProgress.progress = newProgress
                    binding.webProgress.alpha = if (newProgress >= 98) 0f else 1f
                }
            }
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.defaultTextEncodingName = "utf-8"
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.builtInZoomControls = false
            settings.displayZoomControls = false
            isVerticalScrollBarEnabled = false
            setBackgroundColor(Color.TRANSPARENT)
            loadUrl("file:///android_asset/web/index.html")
        }
    }
}
