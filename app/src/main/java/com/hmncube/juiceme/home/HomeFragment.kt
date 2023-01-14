package com.hmncube.juiceme.home

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.hmncube.juiceme.R
import com.hmncube.juiceme.ViewModelFactory
import com.hmncube.juiceme.data.AppDatabase
import com.hmncube.juiceme.data.CardNumber
import com.hmncube.juiceme.databinding.FragmentHomeBinding
import com.hmncube.juiceme.use_cases.PreferencesUseCase
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var viewBinding: FragmentHomeBinding
    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService
    private var extractedNumber = "No numbers found!"
    private var storeHistory = false
    private var dialDirect = false
    private var automaticallyDelete = false
    private var codePrefix = ""

    private var grantedCallPermission = true
    private var grantedCameraPermission = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentHomeBinding.inflate(inflater)
        return viewBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            val permissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { isGranted ->
                grantedCallPermission = isGranted[Manifest.permission.CALL_PHONE]!!
                grantedCameraPermission = isGranted[Manifest.permission.CAMERA]!!
                if (isGranted.all { !it.value }) {
                    displayMessage( R.string.permission_not_granted, Snackbar.LENGTH_LONG)
                }
            }

            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.CALL_PHONE
                )
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelFactory(AppDatabase.getDatabase(requireContext())).create(HomeViewModel::class.java)
        toggleProgressBar(false)
        viewBinding.dialBtn.isClickable = false

        val preferencesUseCases = PreferencesUseCase(requireContext())
        storeHistory = preferencesUseCases.getStoreHistory()
        dialDirect = preferencesUseCases.getDirectDial()
        automaticallyDelete = preferencesUseCases.getAutomaticallyDelete()
        codePrefix = preferencesUseCases.getUssdCode() ?: ""

        if (dialDirect) {
            viewBinding.dialBtn.visibility = View.GONE
        }
        viewBinding.cameraBtn.setOnClickListener {
            if (grantedCameraPermission) {
                takePhoto()
            } else {
                displayMessage(R.string.camera_no_permission, Snackbar.LENGTH_SHORT)
            }
        }
        viewBinding.dialBtn.setOnClickListener {
            if (grantedCallPermission) {
                dialNumber(codePrefix, extractedNumber, viewBinding.root, requireContext())
            } else {
                displayMessage(R.string.call_no_permission, Snackbar.LENGTH_SHORT)
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onResume() {
        super.onResume()
        if (grantedCameraPermission) {
            startCamera()
        }
    }
    private fun displayMessage(message: Int, duration: Int) {
        Snackbar.make(requireView(), message, duration).show()
    }

    private fun displayMessage(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun takePhoto() {
        viewBinding.cameraBtn.isClickable = false
        viewBinding.dialBtn.isClickable = false
        toggleProgressBar(true)

        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault())
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/JuiceMe-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    viewBinding.cameraBtn.isClickable = true
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Log.d(TAG, msg)
                    extractText(output.savedUri)
                }
            }
        )
    }

    private fun extractText(savedUri: Uri?) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image: InputImage
        try {
            image = InputImage.fromFilePath(requireContext(), savedUri!!)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    toggleProgressBar(false)
                    viewBinding.cameraBtn.isClickable = true
                    viewBinding.dialBtn.isClickable = true
                    val blocks = visionText.textBlocks
                    for (block in blocks) {
                        val blockText = block.text.replace(" ", "")
                        if(blockText.isDigitsOnly() && blockText.count() == 17) {
                            extractedNumber = blockText
                            if (storeHistory) {
                                viewModel.saveCardNumber(
                                    CardNumber(
                                        0,
                                        blockText,
                                        Calendar.getInstance().timeInMillis
                                    )
                                )
                            }
                            if (dialDirect && grantedCallPermission) {
                                dialNumber(codePrefix, extractedNumber, viewBinding.root, requireContext())
                            }else if (dialDirect){
                                displayMessage(R.string.call_no_permission, Snackbar.LENGTH_SHORT)
                            }
                        }
                    }
                    viewBinding.resultTv.text = extractedNumber
                }
                .addOnFailureListener { e ->
                    toggleProgressBar(false)
                    Log.e(TAG, "extractText: Failure", e)
                    displayMessage(String.format(resources.getString(R.string.error_extracting_text), e.message))
                    viewBinding.cameraBtn.isClickable = true
                }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "extractText: Error", e)
            displayMessage(String.format(resources.getString(R.string.error_extracting_text), e.message))
            viewBinding.cameraBtn.isClickable = false
        }
        deleteGeneratedImage(savedUri!!)
    }

    private fun deleteGeneratedImage(savedUri: Uri) {
        if (automaticallyDelete) {
            val filePath = getFilePath(savedUri)
            val fileDelete = File(filePath)
            if (fileDelete.exists()) {
                try {
                    fileDelete.delete()
                    Log.d(TAG, "deleteGeneratedImage: deleted file at $filePath with uri $savedUri")
                } catch (e: Exception) {
                    Log.e(TAG, "deleteGeneratedImage: failed deleting at $filePath ${e.message}")
                }
            }
        }
    }

    private fun getFilePath(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor = requireContext().contentResolver.query(uri, projection, null, null, null)!!
        cursor.moveToFirst()
        val columnIndex: Int = cursor.getColumnIndex(projection[0])
        val picturePath: String = cursor.getString(columnIndex)
        cursor.close()
        return picturePath
    }

    private fun toggleProgressBar(isVisible : Boolean) {
        if (isVisible) {
            viewBinding.progressBar.visibility = View.VISIBLE
        } else {
            viewBinding.progressBar.visibility = View.GONE
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.resultIv.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                displayMessage(R.string.camera_initialisation_failed, Snackbar.LENGTH_SHORT)
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "JuiceMe"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.CALL_PHONE,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()

        fun dialNumber(codePrefix: String, extractedNumber: String, view: View, context: Context) {
            if (extractedNumber == "No numbers found!") {
                Snackbar.make(view, "Cannot recharge with invalid number", Snackbar.LENGTH_SHORT).show()
                return
            }

            if (codePrefix.isEmpty()) {
                Snackbar.make(view, R.string.network_not_in_list, Snackbar.LENGTH_SHORT).show()
                return
            }
            val dialIntent = Intent(Intent.ACTION_CALL)
            val str = Uri.encode("$codePrefix${extractedNumber}")
            dialIntent.data = Uri.parse("tel:$str")
            context.startActivity(dialIntent)
        }

    }
}