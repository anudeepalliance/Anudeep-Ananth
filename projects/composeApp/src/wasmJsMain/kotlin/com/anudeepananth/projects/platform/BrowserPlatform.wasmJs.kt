@file:OptIn(ExperimentalWasmJsInterop::class)

package com.anudeepananth.projects.platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

private fun readLocationPathnameFromWindow(): String = js("window.location.pathname")

private fun readLocationOriginFromWindow(): String = js("window.location.origin")

private fun readLocationHrefFromWindow(): String = js("window.location.href")

private fun assignWindowLocation(address: String): Unit = js("window.location.assign(address)")

private fun readLocationSearchFromWindow(): String = js("window.location.search")

private fun callHistoryPushState(path: String, siteHistoryIndex: Int): Unit =
    js("window.history.pushState({ siteHistoryIndex: siteHistoryIndex }, '', path)")

private fun callHistoryReplaceState(path: String, siteHistoryIndex: Int): Unit =
    js("window.history.replaceState({ siteHistoryIndex: siteHistoryIndex }, '', path)")

private fun readHistoryStateSiteIndex(): Int =
    js("((window.history.state && window.history.state.siteHistoryIndex) || 0)")

private fun callHistoryBack(): Unit = js("window.history.back()")

private fun addWindowPopStateListener(onPopState: () -> Unit): Unit =
    js("window.addEventListener('popstate', () => onPopState())")

private fun writeTextToNavigatorClipboard(text: String): Unit =
    js("(navigator.clipboard ? navigator.clipboard.writeText(text).catch(() => {}) : undefined)")

private fun formatDateWithBrowserLocale(epochMilliseconds: Double): String =
    js("new Date(epochMilliseconds).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })")

private fun readDateNow(): Double = js("Date.now()")

/** The browser's built-in class for receiving a live stream of server-sent events. */
private external class EventSource(url: String) : JsAny {
    var onmessage: ((EventSourceMessage) -> Unit)?
    var onerror: ((JsAny) -> Unit)?
    fun close()
}

/** One event delivered by [EventSource]; [data] is the event's text. */
private external interface EventSourceMessage : JsAny {
    val data: JsString
}

actual fun readBrowserLocationPath(): String = readLocationPathnameFromWindow()

actual fun readBrowserOriginAddress(): String = readLocationOriginFromWindow()

actual fun readBrowserPageAddress(): String = readLocationHrefFromWindow()

actual fun readBrowserLocationQuery(): String = readLocationSearchFromWindow()

actual fun pushPathOntoBrowserHistory(path: String, siteHistoryIndex: Int) {
    callHistoryPushState(path, siteHistoryIndex)
}

actual fun replaceCurrentPathInBrowserHistory(path: String, siteHistoryIndex: Int) {
    callHistoryReplaceState(path, siteHistoryIndex)
}

actual fun readSiteHistoryIndexOfCurrentEntry(): Int = readHistoryStateSiteIndex()

actual fun goBackOneEntryInBrowserHistory() {
    callHistoryBack()
}

actual fun listenForBrowserBackAndForwardNavigation(onBrowserPathChanged: (String) -> Unit) {
    addWindowPopStateListener {
        onBrowserPathChanged(readLocationPathnameFromWindow() + readLocationSearchFromWindow())
    }
}

actual fun openAddressInCurrentBrowserTab(address: String) {
    assignWindowLocation(address)
}

actual fun copyTextToBrowserClipboard(text: String) {
    writeTextToNavigatorClipboard(text)
}

actual fun formatEpochMillisecondsAsViewerLocalDateAndTime(epochMilliseconds: Long): String =
    formatDateWithBrowserLocale(epochMilliseconds.toDouble())

actual fun readCurrentEpochMillisecondsFromBrowserClock(): Long = readDateNow().toLong()

actual fun createBrowserHttpClient(): HttpClient = HttpClient(Js) {
    expectSuccess = false
}

actual fun openServerSentEventStream(
    address: String,
    onEventData: (String) -> Unit,
    onConnectionError: () -> Unit,
): ServerSentEventStreamConnection {
    val eventSource = EventSource(address)
    eventSource.onmessage = { eventSourceMessage ->
        onEventData(eventSourceMessage.data.toString())
    }
    eventSource.onerror = {
        onConnectionError()
    }
    return object : ServerSentEventStreamConnection {
        override fun close() {
            eventSource.close()
        }
    }
}
