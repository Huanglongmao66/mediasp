package com.mpvp.platform

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.content.pm.PackageManager

/**
 * Android权限状态
 */
data class PermissionState(
    val granted: Boolean = false,
    val shouldShowRationale: Boolean = false
)

/**
 * 记住权限状态
 *
 * 用于在Compose中请求和跟踪权限状态
 *
 * @param permission 权限名称
 * @return 权限状态
 */
@Composable
fun rememberPermissionState(permission: String): PermissionState {
    val context = LocalContext.current
    var permissionState by remember { mutableStateOf(PermissionState()) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionState = permissionState.copy(granted = granted)
    }

    LaunchedEffect(permission) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

        permissionState = PermissionState(
            granted = granted,
            shouldShowRationale = false
        )
    }

    return permissionState.copy(
        shouldShowRationale = false
    )
}

/**
 * Android存储权限申请封装
 *
 * 处理Android不同版本的存储权限申请
 * - Android 13+ (API 33+): READ_MEDIA_VIDEO
 * - Android 12- (API 32-): READ_EXTERNAL_STORAGE
 */
@Composable
fun rememberStoragePermission(): Pair<PermissionState, () -> Unit> {
    val context = LocalContext.current
    val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var permissionState by remember { mutableStateOf(PermissionState()) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionState = permissionState.copy(granted = granted)
    }

    LaunchedEffect(permission) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

        permissionState = PermissionState(granted = granted)
    }

    val requestPermission = {
        launcher.launch(permission)
    }

    return Pair(permissionState, requestPermission)
}

/**
 * Android网络权限申请封装
 */
@Composable
fun rememberNetworkPermission(): Pair<PermissionState, () -> Unit> {
    val context = LocalContext.current
    var permissionState by remember { mutableStateOf(PermissionState()) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionState = permissionState.copy(granted = granted)
    }

    LaunchedEffect(Unit) {
        // 网络权限是普通权限，无需运行时申请
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.INTERNET
        ) == PackageManager.PERMISSION_GRANTED

        permissionState = PermissionState(granted = granted)
    }

    val requestPermission = {
        // 网络权限是普通权限，自动授予
    }

    return Pair(permissionState, requestPermission)
}
