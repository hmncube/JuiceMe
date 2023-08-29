package com.hmncube.juiceme.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.hmncube.juiceme.R
import com.hmncube.juiceme.theme.JuiceMeTheme
import java.util.concurrent.RejectedExecutionException

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    /*Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(text = "Home", textAlign = TextAlign.Center)
            },
        )
    }
    ) {
    }*/
    HomeContent()
}

@Composable
private fun HomeContent() {
    Column {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            val height = this.maxHeight
            CameraPreview()
            Text(
                text = stringResource(id = R.string.results),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 72.dp),
                textAlign = TextAlign.Center,
                color = Color.White,
            )

            Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                Column(
                    modifier = Modifier
                        .padding(end = 32.dp, top = 64.dp, bottom = 64.dp)
                        .height(height / 3) //or test with 3
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.End
                ) {
                    ActionButton(
                        onClick = { },
                        icon = R.drawable.ic_camera,
                        description = stringResource(R.string.camera_btn_description)
                    )

                    ActionButton(
                        icon = R.drawable.ic_phone,
                        description = stringResource(R.string.phone_btn_description),
                        onClick = {}
                    )

                    ActionButton(
                        icon = R.drawable.ic_file_open,
                        description = stringResource(R.string.file_picker_btn_description),
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    @DrawableRes icon: Int,
    description: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = { onClick() },
        modifier = Modifier
            .then(Modifier.size(50.dp))
            .border(1.dp, Color.Green, shape = CircleShape)
            .background(color = Color.Green, shape = CircleShape)
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = description
        )
    }
}

@Composable
private fun CameraPreview() {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch (exc: RejectedExecutionException) {
                    //displayMessage(R.string.camera_initialisation_failed)
                    Log.e("MainActivity", "Use case binding failed", exc)
                }
            }, executor)
            previewView
        },
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
    )

}

// previews
@Composable
@androidx.compose.ui.tooling.preview.Preview
fun HomeContentPreview() {
    JuiceMeTheme {
        Text(text = "Ha")
    }
}