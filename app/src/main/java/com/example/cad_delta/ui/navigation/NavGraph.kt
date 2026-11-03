package com.example.cad_delta.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cad_delta.ui.screens.AddEditCustomerScreen
import com.example.cad_delta.ui.screens.AddEditDeviceScreen
import com.example.cad_delta.ui.screens.CustomerDetailScreen
import com.example.cad_delta.ui.screens.CustomerListScreen
import com.example.cad_delta.ui.screens.DeliveredCustomerListScreen
import com.example.cad_delta.ui.viewmodel.CustomerFormViewModel
import com.example.cad_delta.ui.viewmodel.DeviceFormViewModel
import com.example.cad_delta.ui.viewmodel.MainViewModel

object NavRoutes {
    const val CUSTOMER_LIST = "customer_list"
    const val DELIVERED_CUSTOMER_LIST = "delivered_customer_list"
    const val ADD_EDIT_CUSTOMER = "add_edit_customer/{customerId}"
    const val CUSTOMER_DETAIL = "customer_detail/{customerId}"
    const val ADD_EDIT_DEVICE = "add_edit_device/{customerId}/{deviceId}"

    fun addEditCustomer(customerId: Long = -1L) = "add_edit_customer/$customerId"
    fun customerDetail(customerId: Long) = "customer_detail/$customerId"
    fun addEditDevice(customerId: Long, deviceId: Long = -1L) = "add_edit_device/$customerId/$deviceId"
}

@Composable
fun NavGraph(
    mainViewModel: MainViewModel,
    customerFormViewModel: CustomerFormViewModel,
    deviceFormViewModel: DeviceFormViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.CUSTOMER_LIST,
        modifier = modifier
    ) {
        composable(NavRoutes.CUSTOMER_LIST) {
            CustomerListScreen(
                viewModel = mainViewModel,
                onNavigateToAddCustomer = {
                    navController.navigate(NavRoutes.addEditCustomer(-1L))
                },
                onNavigateToCustomerDetail = { customerId ->
                    navController.navigate(NavRoutes.customerDetail(customerId))
                },
                onNavigateToDeliveryList = {
                    mainViewModel.clearOsFilter()
                    mainViewModel.clearDateFilter()
                    mainViewModel.clearDeliveryDateFilter()
                    navController.navigate(NavRoutes.DELIVERED_CUSTOMER_LIST)
                }
            )
        }

        composable(NavRoutes.DELIVERED_CUSTOMER_LIST) {
            DeliveredCustomerListScreen(
                viewModel = mainViewModel,
                onNavigateToAddCustomer = { navController.navigate(NavRoutes.addEditCustomer(-1L)) },
                onNavigateToCustomerDetail = { customerId ->
                    navController.navigate(NavRoutes.customerDetail(customerId))
                },
                onNavigateToEntryList = {
                    mainViewModel.clearDeliveryDateFilter()
                    mainViewModel.clearOsFilter()
                    mainViewModel.clearDateFilter()
                    navController.navigate(NavRoutes.CUSTOMER_LIST)
                }
            )
        }

        composable(
            route = NavRoutes.ADD_EDIT_CUSTOMER,
            arguments = listOf(
                navArgument("customerId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: -1L
            AddEditCustomerScreen(
                customerId = customerId,
                viewModel = customerFormViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.CUSTOMER_DETAIL,
            arguments = listOf(
                navArgument("customerId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: -1L
            CustomerDetailScreen(
                customerId = customerId,
                viewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditCustomer = { custId ->
                    navController.navigate(NavRoutes.addEditCustomer(custId))
                },
                onNavigateToAddDevice = { custId ->
                    navController.navigate(NavRoutes.addEditDevice(custId, -1L))
                },
                onNavigateToEditDevice = { custId, devId ->
                    navController.navigate(NavRoutes.addEditDevice(custId, devId))
                }
            )
        }

        composable(
            route = NavRoutes.ADD_EDIT_DEVICE,
            arguments = listOf(
                navArgument("customerId") { type = NavType.LongType },
                navArgument("deviceId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: -1L
            val deviceId = backStackEntry.arguments?.getLong("deviceId") ?: -1L
            AddEditDeviceScreen(
                customerId = customerId,
                deviceId = deviceId,
                viewModel = deviceFormViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
