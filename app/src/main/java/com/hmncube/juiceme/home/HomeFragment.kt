package com.hmncube.juiceme.home

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.hmncube.juiceme.R
import com.hmncube.juiceme.UserFeedback
import com.hmncube.juiceme.ViewModelFactory
import com.hmncube.juiceme.data.AppDatabase
import com.hmncube.juiceme.data.CardNumber
import com.hmncube.juiceme.databinding.FragmentHomeBinding
import com.hmncube.juiceme.useCases.PreferencesUseCase
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException

@SuppressWarnings("TooManyFunctions")
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

    private var pickMedia : ActivityResultLauncher<PickVisualMediaRequest>? = null

    private var ussdCodeLength = 0

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
                    displayMessage( R.string.permission_not_granted)
                }
            }

            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.CALL_PHONE
                )
            ).apply {
                if (VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                }
            }
        }
        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                toggleProgressBar(true)
                extractText(
                    savedUri = uri,
                    isInternalPicture = true
                )
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelFactory(AppDatabase.getDatabase(requireContext())).create(HomeViewModel::class.java)
        toggleProgressBar(false)
        viewBinding.dialBtn.isClickable = false

        val preferencesUseCases = PreferencesUseCase(requireContext())
        ussdCodeLength = preferencesUseCases.getRechargeCardLength()
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
                displayMessage(R.string.camera_no_permission)
            }
        }
        viewBinding.dialBtn.setOnClickListener {
            if (grantedCallPermission) {
                dialNumber(codePrefix, extractedNumber, viewBinding.root, requireContext())
            } else {
                displayMessage(R.string.call_no_permission)
            }
        }

        viewBinding.filePickerBtn.setOnClickListener {
            pickMedia?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onResume() {
        super.onResume()
        if (grantedCameraPermission) {
            startCamera()
        }
    }

    private fun displayMessage(message: Int) {
        UserFeedback().displayFeedback(requireView(), message, UserFeedback.LENGTH_SHORT)
    }

    private fun displayMessage(message: String) {
        UserFeedback().displayFeedback(requireView(), message, UserFeedback.LENGTH_SHORT)
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
            if(VERSION.SDK_INT > Build.VERSION_CODES.P) {
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

    private fun extractText(savedUri: Uri?, isInternalPicture: Boolean = false) {
        extractedNumber = "No numbers found!"
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
                        if(blockText.isDigitsOnly() && blockText.count() == ussdCodeLength) {
                            extractedNumber = blockText
                            if (storeHistory) {
                                viewModel.saveCardNumber(
                                    CardNumber(
                                        uid = 0,
                                        number = blockText,
                                        date = Calendar.getInstance().timeInMillis
                                    )
                                )
                            }
                            if (dialDirect && grantedCallPermission) {
                                dialNumber(codePrefix, extractedNumber, viewBinding.root, requireContext())
                            }else if (dialDirect){
                                displayMessage(R.string.call_no_permission)
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
            Log.e(TAG, "extractText: Error", e)
            displayMessage(String.format(resources.getString(R.string.error_extracting_text), e.message))
            viewBinding.cameraBtn.isClickable = false
        }

        if (!isInternalPicture) {
            deleteGeneratedImage(savedUri!!)
        }
    }

    private fun deleteGeneratedImage(savedUri: Uri) {
        if (automaticallyDelete) {
            val fileDelete = File(savedUri.path!!)
            fileDelete.delete(requireContext())
        }
    }

    private fun File.delete(context: Context) {
        var selectionArgs = arrayOf(this.absolutePath)
        val contentResolver = context.contentResolver
        val where: String?
        val filesUri: Uri?
        if (VERSION.SDK_INT >= VERSION_CODE_29) {
            filesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            where = MediaStore.Images.Media._ID + "=?"
            selectionArgs = arrayOf(this.name)
        } else {
            where = MediaStore.MediaColumns.DATA + "=?"
            filesUri = MediaStore.Files.getContentUri("external")
        }

        contentResolver.delete(filesUri!!, where, selectionArgs)
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
                    if (this::viewBinding.isInitialized) {
                        it.setSurfaceProvider(viewBinding.resultIv.surfaceProvider)
                    }
                }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: RejectedExecutionException) {
                displayMessage(R.string.camera_initialisation_failed)
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
        if (this::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
    }

    companion object {
        private const val TAG = "JuiceMe"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val VERSION_CODE_29 = 29

        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.CALL_PHONE,
            ).apply {
                if (VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()

        fun dialNumber(codePrefix: String, extractedNumber: String, view: View, context: Context) {
            if (extractedNumber == "No numbers found!") {
                UserFeedback().displayFeedback(view, R.string.cannot_recharge, UserFeedback.LENGTH_SHORT)
                return
            }

            if (codePrefix.isEmpty()) {
                UserFeedback().displayFeedback(view, R.string.network_not_in_list, UserFeedback.LENGTH_SHORT)
                return
            }
            val dialIntent = Intent(Intent.ACTION_CALL)
            val str = Uri.encode("$codePrefix${extractedNumber}${Uri.decode("%23")}")
            dialIntent.data = Uri.parse("tel:$str")
            context.startActivity(dialIntent)
        }

    }
}
