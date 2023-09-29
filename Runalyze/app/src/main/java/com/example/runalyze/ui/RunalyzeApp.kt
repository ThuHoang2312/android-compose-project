package com.example.runalyze.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.runalyze.BottomNavItem
import com.example.runalyze.components.BottomNavigationMenu
import com.example.runalyze.ui.screen.Activity
import com.example.runalyze.ui.screen.Home
import com.example.runalyze.ui.screen.Profile
import com.example.runalyze.ui.screen.TrainingScreen
import com.example.runalyze.ui.screen.AddGoalView
import com.example.runalyze.ui.screen.RunningPlanScreen
import com.example.runalyze.viewmodel.GoalViewModel

@Composable
fun RunalyzeApp(goalViewModel: GoalViewModel){
    val scrollState = rememberScrollState()
    val navController = rememberNavController()
    MainScreen(
        navHostController = navController,
        scrollState = scrollState,
        goalViewModel = goalViewModel)
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navHostController: NavHostController, scrollState: ScrollState, goalViewModel: GoalViewModel){
    Scaffold(
        bottomBar = {
            BottomNavigationMenu(navController = navHostController)
        },
    ) {
        Navigation(navController = navHostController, scrollState = scrollState, goalViewModel = goalViewModel)
    }
}

@Composable
fun Navigation(
    navController: NavHostController,
    scrollState: ScrollState,
    goalViewModel: GoalViewModel){
    NavHost(navController = navController, startDestination = "Home"){
        bottomNavigation(navController = navController)
        composable("home"){
            Home(navController = navController)
        }
        composable("training"){
            TrainingScreen(navController = navController)
        }
        composable("goal"){
            AddGoalView(goalViewModel, navController)
        }
        composable("plan"){
            RunningPlanScreen(navController = navController)
        }
    }
}

fun NavGraphBuilder.bottomNavigation(navController: NavController){
    composable(BottomNavItem.Home.route){
        Home(navController = navController)
    }
    composable(BottomNavItem.Training.route){
        RunningPlanScreen(navController = navController)
    }
    composable(BottomNavItem.Activity.route){
        Activity()
    }
    composable(BottomNavItem.Profile.route){
        Profile()
    }
}