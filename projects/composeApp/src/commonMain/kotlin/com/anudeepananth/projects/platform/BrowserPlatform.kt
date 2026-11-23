package com.anudeepananth.projects.platform

import io.ktor.client.HttpClient

/** The path part of the address in the browser's address bar, such as `/ocrolus/runs`. */
expect fun readBrowserLocationPath(): String

/** The scheme, host and port the site was loaded from, such as `https://projects.anudeepananth.com`. */
expect fun readBrowserOriginAddress(): String

/** The full address of the current page, for "Copy link" buttons. */
expect fun readBrowserPageAddress(): String

/** The query part of the address, such as `?part=2`, or an empty string when there is none. */
expect fun readBrowserLocationQuery(): String

/**
 * Changes the address bar to [path] without reloading the page and adds it to the back/forward history.
 * [siteHistoryIndex] counts how many in-site pages lie behind this one, so the app knows whether "back" stays on the site.
 */
expect fun pushPathOntoBrowserHistory(path: String, siteHistoryIndex: Int)

/** Changes the address bar to [path] without adding a history entry, so "back" skips the replaced page. */
expect fun replaceCurrentPathInBrowserHistory(path: String, siteHistoryIndex: Int)

/** The in-site history index stored with the current history entry, or 0 for a page opened directly. */
expect fun readSiteHistoryIndexOfCurrentEntry(): Int

/** Asks the browser to go back one page, exactly like its back button. */
expect fun goBackOneEntryInBrowserHistory()

/** Calls [onBrowserPathChanged] with the new path whenever the visitor presses the browser's back or forward button. */
expect fun listenForBrowserBackAndForwardNavigation(onBrowserPathChanged: (String) -> Unit)

/** Puts [text] on the visitor's clipboard so they can paste it elsewhere. */
expect fun copyTextToBrowserClipboard(text: String)

/** Leaves this web app and loads [address] in the same tab, for links to other sites. */
expect fun openAddressInCurrentBrowserTab(address: String)

/** Formats a moment in time as a date and time in the visitor's own language and time zone. */
expect fun formatEpochMillisecondsAsViewerLocalDateAndTime(epochMilliseconds: Long): String

/** The current time as milliseconds since 1970, read from the browser's clock. */
expect fun readCurrentEpochMillisecondsFromBrowserClock(): Long

/** Builds the client used for every request to the server; it uses the browser's own networking. */
expect fun createBrowserHttpClient(): HttpClient

/** A live connection opened by [openServerSentEventStream]; call [close] to stop listening. */
interface ServerSentEventStreamConnection {
    fun close()
}

/**
 * Opens a one-way live stream from the server at [address] using the browser's built-in `EventSource`.
 * [onEventData] receives the text of each event; [onConnectionError] is called if the stream breaks or ends.
 */
expect fun openServerSentEventStream(
    address: String,
    onEventData: (String) -> Unit,
    onConnectionError: () -> Unit,
): ServerSentEventStreamConnection
