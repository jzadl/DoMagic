package com.domagic.ui

sealed class Screen(val route: String) {
    object Intro      : Screen("intro")
    object Connect    : Screen("connect")
    object DeviceInfo : Screen("device_info")
    object Patch      : Screen("patch")
    object Flash      : Screen("flash")
    object Community  : Screen("community")
    object TestMode   : Screen("test_mode")
}
