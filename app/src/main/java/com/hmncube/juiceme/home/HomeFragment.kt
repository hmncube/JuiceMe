package com.hmncube.juiceme.home

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color.argb
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.hmncube.juiceme.R
import com.hmncube.juiceme.ViewModelFactory
import com.hmncube.juiceme.data.AppDatabase
import com.hmncube.juiceme.data.CardNumber
import com.hmncube.juiceme.databinding.FragmentHomeBinding
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.effet.RippleEffect
import com.takusemba.spotlight.shape.Circle
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentHomeBinding.inflate(inflater)
        return viewBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelFactory(AppDatabase.getDatabase(requireContext())).create(HomeViewModel::class.java)
        toggleProgressBar(false)
        viewBinding.dialBtn.isClickable = false

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        storeHistory = sharedPreferences.getBoolean("store_history", false)
        dialDirect = sharedPreferences.getBoolean("dial_action", false)
        automaticallyDelete = sharedPreferences.getBoolean("images", true)

        if (dialDirect) {
            viewBinding.dialBtn.visibility = View.GONE
        }

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        viewBinding.cameraBtn.setOnClickListener { takePhoto() }
        viewBinding.dialBtn.setOnClickListener { dialNumber(extractedNumber, viewBinding.root, requireContext()) }

        cameraExecutor = Executors.newSingleThreadExecutor()

        setUpTutorial()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(requireContext(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }
        }
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
            val result = recognizer.process(image)
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
                            if (dialDirect){
                                dialNumber(extractedNumber, viewBinding.root, requireContext())
                            }
                        }
                    }
                    viewBinding.resultTv.text = extractedNumber
                }
                .addOnFailureListener { e ->
                    toggleProgressBar(false)
                    Log.e(TAG, "extractText: Error", e)
                    Toast.makeText(requireContext(), "There was an error extracting the numbers : ${e.message}", Toast.LENGTH_SHORT).show()
                    viewBinding.cameraBtn.isClickable = true
                }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "extractText: Error", e)
            Toast.makeText(requireContext(), "There was an error extracting the numbers : ${e.message}", Toast.LENGTH_SHORT).show()
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
        val picturePath: String = cursor.getString(columnIndex) // returns null
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
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.resultIv.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun setUpTutorial() {
        val firstRoot = FrameLayout(requireContext())
        val firstLayout = layoutInflater.inflate(R.layout.layout_target, firstRoot)
        val firstTarget = Target.Builder()
            .setAnchor(view?.findViewById<View>(R.id.cameraBtn)!!)
            .setShape(Circle(100f))
            //.setEffect(RippleEffect(100f, 200f, argb(30, 124, 255, 90)))
            .setOverlay(firstLayout)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    Log.d(TAG, "onStarted: first target is started")
                }
                override fun onEnded() {
                    Log.d(TAG, "onStarted: first target ends")
                }
            })
            .build()
        /*val firstTarget = setUpTutorialTarget(
            overlay = firstLayout,
            anchorView = viewBinding.resultIv
        )*/

        val secondRoot = FrameLayout(requireContext())
        val secondLayout = layoutInflater.inflate(R.layout.layout_target, secondRoot)
        val secondTarget = setUpTutorialTarget(
            overlay = secondLayout,
            anchorView = viewBinding.cameraBtn
        )

        val thirdRoot = FrameLayout(requireContext())
        val thirdLayout = layoutInflater.inflate(R.layout.layout_target, thirdRoot)
        val thirdTarget = setUpTutorialTarget(
            overlay = thirdLayout,
            anchorView = viewBinding.resultTv
        )

        val fourthRoot = FrameLayout(requireContext())
        val fourthLayout = layoutInflater.inflate(R.layout.layout_target, thirdRoot)
        val fourthTarget = setUpTutorialTarget(
            overlay = fourthRoot,
            anchorView = viewBinding.resultTv
        )

        val targets = listOf(
            firstTarget,
            secondTarget,
            thirdTarget,
            fourthTarget
        )

        // create spotlight
        val spotlight = Spotlight.Builder(requireActivity())
            .setTargets(targets)
            .setBackgroundColorRes(R.color.teal)
            .setDuration(1000L)
            .setAnimation(DecelerateInterpolator(2f))
            .setOnSpotlightListener(object : OnSpotlightListener {
                override fun onStarted() {
                    Log.d(TAG, "onStarted: spotlight")
                }
                override fun onEnded() {
                    Log.d(TAG, "onEnded: onEnded")
                }
            })
            .build()

        spotlight.start()

        val nextTarget = View.OnClickListener { spotlight.next() }

        val closeSpotlight = View.OnClickListener { spotlight.finish() }

        firstLayout.findViewById<View>(R.id.close_target).setOnClickListener(nextTarget)
        secondLayout.findViewById<View>(R.id.close_target).setOnClickListener(nextTarget)
        thirdLayout.findViewById<View>(R.id.close_target).setOnClickListener(nextTarget)
        fourthLayout.findViewById<View>(R.id.close_target).setOnClickListener(nextTarget)

        firstLayout.findViewById<View>(R.id.close_spotlight).setOnClickListener(closeSpotlight)
        secondLayout.findViewById<View>(R.id.close_spotlight).setOnClickListener(closeSpotlight)
        thirdLayout.findViewById<View>(R.id.close_spotlight).setOnClickListener(closeSpotlight)
        fourthLayout.findViewById<View>(R.id.close_spotlight).setOnClickListener(closeSpotlight)
    }

    private fun setUpTutorialTarget(overlay: View, anchorView: View) : Target {
        return Target.Builder()
            .setAnchor(anchorView)
            .setShape(Circle(100f))
            .setEffect(RippleEffect(100f, 200f, argb(30, 124, 255, 90)))
            .setOverlay(overlay)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    Log.d(TAG, "onStarted: first target is started")
                }
                override fun onEnded() {
                    Log.d(TAG, "onStarted: first target ends")
                }
            })
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "JuiceMe"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.CALL_PHONE,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()

        fun dialNumber(extractedNumber: String, view: View, context: Context) {
            if (extractedNumber == "No numbers found!") {
                Snackbar.make(view, "Cannot recharge with invalid number", Snackbar.LENGTH_SHORT).show()
                return
            }
            val dialIntent = Intent(Intent.ACTION_CALL)
            val str = Uri.encode("*121*${extractedNumber}${Uri.decode("%23")}")
            dialIntent.data = Uri.parse("tel:$str")
            context.startActivity(dialIntent)
        }

    }
}