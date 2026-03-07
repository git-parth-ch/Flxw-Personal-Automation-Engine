package com.flxw.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.*
import androidx.navigation.compose.*
import com.flxw.ui.screens.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Rules : Screen("rules", "Rules", Icons.Default.List)
    object Logs : Screen("logs", "Logs", Icons.Default.DateRange)
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)

    // Non-tab screens
    object RuleDetail : Screen("rule_detail/{ruleId}", "Detail", Icons.Default.Info) {
        fun createRoute(ruleId: String) = "rule_detail/$ruleId"
    }
    object RuleWizard : Screen("rule_wizard?ruleId={ruleId}", "Create Rule", Icons.Default.Add) {
        fun createRoute(ruleId: String? = null) =
            if (ruleId != null) "rule_wizard?ruleId=$ruleId" else "rule_wizard?ruleId="
    }
}

val bottomNavItems = listOf(Screen.Rules, Screen.Dashboard, Screen.Logs)

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = bottomNavItems.any { currentRoute?.startsWith(it.route) == true }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Rules.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Rules.route) {
                RuleListScreen(
                    onCreateRule = { navController.navigate(Screen.RuleWizard.createRoute()) },
                    onRuleClick = { ruleId -> navController.navigate(Screen.RuleDetail.createRoute(ruleId)) }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onViewLogs = { navController.navigate(Screen.Logs.route) }
                )
            }

            composable(Screen.Logs.route) {
                LogViewerScreen()
            }

            composable(
                route = Screen.RuleDetail.route,
                arguments = listOf(navArgument("ruleId") { type = NavType.StringType })
            ) { backStackEntry ->
                val ruleId = backStackEntry.arguments?.getString("ruleId") ?: return@composable
                RuleDetailScreen(
                    ruleId = ruleId,
                    onEdit = { navController.navigate(Screen.RuleWizard.createRoute(ruleId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.RuleWizard.route,
                arguments = listOf(
                    navArgument("ruleId") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val ruleId = backStackEntry.arguments?.getString("ruleId")
                    ?.takeIf { it.isNotBlank() }
                RuleWizardScreen(
                    editRuleId = ruleId,
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}