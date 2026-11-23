package com.anudeepananth.projects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.anudeepananth.projects.design.PageScaffold
import com.anudeepananth.projects.design.ProjectsSiteTheme
import com.anudeepananth.projects.design.TopBarLink
import com.anudeepananth.projects.home.NotFoundScreen
import com.anudeepananth.projects.platform.openAddressInCurrentBrowserTab
import com.anudeepananth.projects.navigation.AppRoute
import com.anudeepananth.projects.navigation.AppRoutePaths
import com.anudeepananth.projects.navigation.BrowserPathRouter
import com.anudeepananth.projects.ocrolus.screens.OcrolusAssignmentScreen
import com.anudeepananth.projects.ocrolus.screens.OcrolusCompletedRunsScreen
import com.anudeepananth.projects.ocrolus.screens.OcrolusRunResultScreen

/** Root of projects.anudeepananth.com: applies the theme, reads the address bar, and shows the matching screen. */
@Composable
fun App() {
    val browserPathRouter = remember {
        BrowserPathRouter()
    }
    val currentRoute = AppRoute.fromBrowserAddress(browserPathRouter.currentAddress)
    val navigateToPath = browserPathRouter::navigateToPath

    ProjectsSiteTheme {
        PageScaffold(
            siteName = "Anudeep projects",
            compactSiteName = "Projects",
            onSiteNameClick = {
                openAddressInCurrentBrowserTab(AppRoutePaths.MAIN_SITE_ADDRESS)
            },
            topBarLinks = listOf(
                TopBarLink(
                    label = "Assignment",
                    compactLabel = "Assignment",
                    isCurrentPage = currentRoute == AppRoute.OcrolusAssignment,
                    onClick = {
                        navigateToPath(AppRoutePaths.OCROLUS_ASSIGNMENT)
                    },
                ),
                TopBarLink(
                    label = "Completed runs",
                    compactLabel = "Runs",
                    isCurrentPage = currentRoute == AppRoute.OcrolusCompletedRuns || currentRoute is AppRoute.OcrolusRunResult,
                    onClick = {
                        navigateToPath(AppRoutePaths.OCROLUS_COMPLETED_RUNS)
                    },
                ),
            ),
            scrollResetKey = browserPathRouter.currentAddress.substringBefore('?'),
            footerText = "Built by Anudeep Ananth with Kotlin, Compose Multiplatform and Ktor. The customer data shown is synthetic.",
        ) {
            when (currentRoute) {
                AppRoute.Home -> LaunchedEffect(Unit) {
                    openAddressInCurrentBrowserTab(AppRoutePaths.MAIN_SITE_ADDRESS)
                }

                AppRoute.OcrolusAssignment -> OcrolusAssignmentScreen(navigateToPath)
                AppRoute.OcrolusCompletedRuns -> OcrolusCompletedRunsScreen(navigateToPath)
                is AppRoute.OcrolusRunResult -> OcrolusRunResultScreen(
                    runId = currentRoute.runId,
                    selectedPartNumber = currentRoute.partNumber,
                    onNavigateToPath = navigateToPath,
                    onReplaceCurrentPathWith = browserPathRouter::replaceCurrentPathWith,
                    onBackToAllRuns = {
                        browserPathRouter.goBackWithinSiteOrOpen(AppRoutePaths.OCROLUS_COMPLETED_RUNS)
                    },
                )

                AppRoute.NotFound -> NotFoundScreen(navigateToPath)
            }
        }
    }
}
