package com.mecagoentodo.huertohogar_v2.view

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Inicio")
    object Account : Screen("account", "Cuenta")
    object Profile : Screen("profile", "Perfil")
    object Cart : Screen("cart", "Carrito")
    object Checkout : Screen("checkout", "Finalizar Compra")
}