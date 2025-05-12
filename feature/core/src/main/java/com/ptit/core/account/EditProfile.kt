package com.ptit.core.account

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.MaxWidthRow
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.rememberState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.safeCollectFlow
import com.ptit.domain.utils.onError
import com.ptit.domain.utils.onLoading
import com.ptit.domain.utils.onSuccess

@Composable
fun EditProfile(
    backStackEntry: NavBackStackEntry,
    onBack: () -> Unit,
) {
    LocalBottomNavigationVisibility.current.value = false

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val viewModel = hiltViewModel<AccountViewModel>(backStackEntry)
    val uiModel = viewModel.updateProfileUiModel.collectAsStateWithLifecycle()

    val isShowProgressBar = rememberState { false }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                viewModel.onAvatarChanged(it)
            }
        }
    )

    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.updateProfileState) {
            it
                .onLoading {
                    isShowProgressBar.value = true
                }
                .onSuccess {
                    isShowProgressBar.value = false
                    onBack()
                }
                .onError {
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        "Cập nhật thông tin thất bại",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    MaxSizeColumn(
        modifier = Modifier
            .background(color = colorResource(com.ptit.common.R.color.colorSystem_background_level_0))
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        MaxWidthRow(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = colorResource(id = com.ptit.common.R.color.colorSystem_heading_button)
                )
            }

            Text(
                text = "Cập nhật thông tin",
                style = CustomTypography.TextBold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = colorResource(id = com.ptit.common.R.color.colorSystem_heading_button)
            )

            // Empty space to balance the back button
            Spacer(modifier = Modifier.size(48.dp))
        }


    }

}