@file:OptIn(ExperimentalWasmJsInterop::class)

package com.anudeepananth.projects

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.configureWebResources

private fun removeHtmlLoadingSplashFromPage(): Unit =
    js("(document.getElementById('loading-splash') ? document.getElementById('loading-splash').remove() : undefined)")

/**
 * Browser entry point: mounts the Compose app onto the page body, then removes the HTML loading splash.
 * Resource files (the fonts) are fetched from the site root, because a relative address would break on
 * deep links such as `/ocrolus/runs/abc`, where the server answers unknown paths with the page itself.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class)
fun main() {
    configureWebResources {
        resourcePathMapping { resourcePath ->
            "/$resourcePath"
        }
    }
    ComposeViewport {
        App()
    }
    removeHtmlLoadingSplashFromPage()
}
