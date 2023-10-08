package com.example.runalyze.ui

import RunScreen
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.runalyze.BottomNavItem
import com.example.runalyze.service.RunningPlan
import com.example.runalyze.ui.components.BottomNavigationMenu
import com.example.runalyze.ui.screen.Activity
import com.example.runalyze.ui.screen.AddGoalView
import com.example.runalyze.ui.screen.Home
import com.example.runalyze.ui.screen.runningPlan.RunningPlanDetailView
import com.example.runalyze.ui.screen.runningPlan.RunningPlanListScreen
import com.example.runalyze.viewmodel.ActivityViewModel
import com.example.runalyze.viewmodel.GoalViewModel
import com.example.runalyze.viewmodel.RunViewModel
import com.example.runalyze.viewmodel.RunningPlanViewModel

@Composable

fun RunalyzeApp(
    goalViewModel: GoalViewModel,
    activityViewModel: ActivityViewModel,
    runViewModel: RunViewModel
) {
    val scrollState = rememberScrollState()
    val navController = rememberNavController()

    //activityViewModel.addDummyData()

    MainScreen(
        navHostController = navController,
        scrollState = scrollState,
        goalViewModel = goalViewModel,
        activityViewModel = activityViewModel,
        runViewModel = runViewModel
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    navHostController: NavHostController,
    scrollState: ScrollState,
    goalViewModel: GoalViewModel,
    activityViewModel: ActivityViewModel,
    runViewModel: RunViewModel
) {
    var showBottomBar by remember { mutableStateOf(true) }
    val navBackStackEntry by navHostController.currentBackStackEntryAsState()
    showBottomBar = when (navBackStackEntry?.destination?.route) {
        "training" -> false
        else -> true
    }

    Scaffold(
        bottomBar = {
            if(showBottomBar)
            BottomNavigationMenu(navController = navHostController)
        }
    )
    { innerPadding ->
        Box(
            modifier = Modifier.padding(
                PaddingValues(
                    0.dp,
                    0.dp,
                    0.dp,
                    innerPadding.calculateBottomPadding()
                )
            )
        ) {
            Navigation(
                navController = navHostController,
                scrollState = scrollState,
                goalViewModel = goalViewModel,
                activityViewModel = activityViewModel,
                runViewModel = runViewModel
            )
        }
    }
}

@Composable
fun Navigation(
    navController: NavHostController,
    scrollState: ScrollState,
    goalViewModel: GoalViewModel,
    activityViewModel: ActivityViewModel,
    runViewModel: RunViewModel
) {
    val runningPlanViewModel  = RunningPlanViewModel()
    runningPlanViewModel.getRunningPlanList()
    val runningPlanList: List<RunningPlan> by runningPlanViewModel.runningPlanList.observeAsState(
        mutableListOf()
    )

    NavHost(navController = navController, startDestination = "Home") {
        bottomNavigation(navController = navController, activityViewModel, runningPlanList)
        composable("home") {
            Home(navController = navController)
        }
        composable("training") {
            RunScreen(navController = navController, viewModel = runViewModel)
        }
        composable("goal") {
            AddGoalView(goalViewModel, navController)
        }
        composable("plan") {

            RunningPlanListScreen(runningPlanList, navController = navController)
        }
        composable(
            "runningPlanDetail/{runningPlanId}",
            arguments = listOf(navArgument("runningPlanId") { type = NavType.IntType })
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getInt("runningPlanId")
            Log.d("RunAlyze", "aaaa navigateId - ${planId.toString()}")
            RunningPlanDetailView(
                backStackEntry.arguments?.getInt("runningPlanId") ?: 1,
                runningPlanList,
                navController
            )
        }
    }
}

fun NavGraphBuilder.bottomNavigation(
    navController: NavController,
    activityViewModel: ActivityViewModel,
    runningPlanList: List<RunningPlan>
) {
    composable(BottomNavItem.Home.route) {
        Home(navController = navController)
    }
    composable(BottomNavItem.Planning.route) {
        RunningPlanListScreen(runningPlanList, navController = navController)
    }
    composable(BottomNavItem.Statistic.route) {
        Activity(activityViewModel, navController)
    }
}