package com.anudeepananth.projects.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.anudeepananth.projects.platform.goBackOneEntryInBrowserHistory
import com.anudeepananth.projects.platform.listenForBrowserBackAndForwardNavigation
import com.anudeepananth.projects.platform.pushPathOntoBrowserHistory
import com.anudeepananth.projects.platform.readBrowserLocationPath
import com.anudeepananth.projects.platform.readBrowserLocationQuery
import com.anudeepananth.projects.platform.readSiteHistoryIndexOfCurrentEntry
import com.anudeepananth.projects.platform.replaceCurrentPathInBrowserHistory

/**
 * Keeps the page shown by the app and the browser's address bar in step, so refresh, back, forward
 * and shared links all work. Screens read [currentAddress] (path plus any `?query`); links call [navigateToPath].
 */
class BrowserPathRouter {
    var currentAddress by mutableStateOf(readBrowserLocationPath() + readBrowserLocationQuery())
        private set

    private var currentSiteHistoryIndex by mutableIntStateOf(readSiteHistoryIndexOfCurrentEntry())

    /** True when the page before this one in the browser history is also a page of this site. */
    val canGoBackWithinSite: Boolean
        get() = currentSiteHistoryIndex > 0

    init {
        listenForBrowserBackAndForwardNavigation { newBrowserAddress ->
            currentAddress = newBrowserAddress
            currentSiteHistoryIndex = readSiteHistoryIndexOfCurrentEntry()
        }
    }

    /** Shows the page at [address] and adds it to the browser history, unless it is already showing. */
    fun navigateToPath(address: String) {
        if (address == currentAddress) return
        currentSiteHistoryIndex++
        pushPathOntoBrowserHistory(address, currentSiteHistoryIndex)
        currentAddress = address
    }

    /** Shows the page at [address] in place of the current one, so the "back" button skips the current one. */
    fun replaceCurrentPathWith(address: String) {
        replaceCurrentPathInBrowserHistory(address, currentSiteHistoryIndex)
        currentAddress = address
    }

    /** Goes back one page in the browser history when that page is on this site; otherwise opens [fallbackAddress]. */
    fun goBackWithinSiteOrOpen(fallbackAddress: String) {
        if (canGoBackWithinSite) goBackOneEntryInBrowserHistory() else navigateToPath(fallbackAddress)
    }
}
