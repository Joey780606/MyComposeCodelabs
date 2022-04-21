/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.compose.rally

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.compose.rally.data.UserData
import com.example.compose.rally.ui.accounts.AccountsBody
import com.example.compose.rally.ui.accounts.SingleAccountBody
import com.example.compose.rally.ui.bills.BillsBody
import com.example.compose.rally.ui.components.RallyTabRow
import com.example.compose.rally.ui.overview.OverviewBody
import com.example.compose.rally.ui.theme.RallyTheme

/**
 * This Activity recreates part of the Rally Material Study from
 * https://material.io/design/material-studies/rally.html
 */

val accountsName = RallyScreen.Accounts.name    //Ch4 Step 1

class RallyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RallyApp()
        }
    }
}

@Composable
fun RallyApp() {
    RallyTheme {
        val allScreens = RallyScreen.values().toList()

        //var currentScreen by rememberSaveable { mutableStateOf(RallyScreen.Overview) }
        // Ch3,這樣改變,表示收縮(collapsing)和擴展選取的items是不會工作.  Navigation為你抓住back stack,且能與目前的back stack entry 以State提供給你.

        val navController = rememberNavController() //Ch3 Create NavController
        val backStackEntry = navController.currentBackStackEntryAsState()   //Ch3 join backstackEngry
        val currentScreen = RallyScreen.fromRoute(
            backStackEntry.value?.destination?.route
        )
        Scaffold(
            topBar = {
                RallyTabRow(
                    allScreens = allScreens,
                    //onTabSelected = { screen -> currentScreen = screen },
                    onTabSelected = { screen -> navController.navigate(screen.name) },  //Ch3 modify
                    currentScreen = currentScreen
                )
            }
        ) { innerPadding ->
            //Box(Modifier.padding(innerPadding)) {
            NavHost(    // Ch3, Use NavHost
                navController = navController,
                startDestination = RallyScreen.Overview.name,
                modifier = Modifier.padding(innerPadding)
            ) {
//                composable(RallyScreen.Overview.name) {     //重要, composable 的用法
//                    Text(text = RallyScreen.Overview.name)
//                }
//                composable(RallyScreen.Accounts.name) {
//                    Text(RallyScreen.Accounts.name)
//                }
//                composable(RallyScreen.Bills.name) {
//                    Text(RallyScreen.Bills.name)
//                }

                composable( //Ch4 Step 1, Step 2: move to here and adjust content
                    route = "$accountsName/{name}",
                    arguments = listOf(
                        navArgument("name") {
                            // Make argument type safe
                            type = NavType.StringType
                        },
                    ),
                    deepLinks = listOf(navDeepLink {    //Ch5 Step 2, add navDeepLink
                        uriPattern = "rally://$accountsName/{name}"
                    })
                ) { entry -> // 在 NavBackStackEntry 裡的變數,看 "name"
                    val accountName = entry.arguments?.getString("name")
                    val account = UserData.getAccount(accountName)
                    SingleAccountBody(account = account)
                }

                composable(RallyScreen.Overview.name) { //Ch3 modify
                    OverviewBody(
                        onClickSeeAllAccounts = { navController.navigate(RallyScreen.Accounts.name) },
                        onClickSeeAllBills = { navController.navigate(RallyScreen.Bills.name) },
                        // Ch4 Step 3
                        onAccountClick = { name ->
                            navigateToSingleAccount(navController, name)
                        }
                    )
                }
                composable(RallyScreen.Accounts.name) {
                    AccountsBody(accounts = UserData.accounts) { name ->
                        // Ch4 Step 3
                        navigateToSingleAccount(
                            navController = navController,
                            accountName = name
                        )
                    }
                }
                composable(RallyScreen.Bills.name) {
                    BillsBody(bills = UserData.bills)
                }
            }
        }
    }
}

private fun navigateToSingleAccount(
    navController: NavHostController,
    accountName: String
) {
    navController.navigate("${RallyScreen.Accounts.name}/$accountName")
}