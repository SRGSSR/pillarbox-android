/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.webview

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Web view showcase
 * A show case to integrate Pillarbox web inside a WebView.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewShowcase() {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                settings.javaScriptEnabled = true
                webChromeClient = DrmReadyWebChromeClient()
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.setSupportZoom(false)
                settings.setSupportMultipleWindows(true)
            }
        },
        update = { webView ->
            // DRM content can be played only inside a secure context. To do so we create ab iframe with a correct base url.
            webView.loadDataWithBaseURL(
                "https://srgssr.github.io/pillarbox-web",
                "<iframe src=\"https://srgssr.github.io/pillarbox-web\" width=\"100%\" " +
                    "height=\"100%\" allow=\"encrypted-media;\"></iframe>",
                "text/html",
                "UTF-8",
                null
            )
            // No secure context, so DRM doesn't work.
            // webView.loadUrl("https://srgssr.github.io/pillarbox-web/")
        }
    )
}

/**
 * Drm ready web chrome client
 *
 * https://amokranechentir.hashnode.dev/android-play-drm-protected-content-inside-a-webview
 */
private class DrmReadyWebChromeClient : WebChromeClient() {
    override fun onPermissionRequest(request: PermissionRequest?) {
        val resources = request?.resources
        resources?.forEach { resource ->
            if (PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID == resource) {
                request.grant(resources)
                return
            }
        }
        super.onPermissionRequest(request)
    }
}
