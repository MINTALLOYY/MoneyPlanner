package com.vibhu.moneyplanner

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.vibhu.moneyplanner.databinding.FragmentCameraReceiptBinding
import okio.IOException
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CameraReceiptFragment : Fragment(){

    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private var _binding: FragmentCameraReceiptBinding? = null
    private val binding get() = _binding!!
    private val CAMERA_PERMISSION_REQUEST = 100
    private lateinit var photoUri: Uri
    private lateinit var currentPhotoPath: String // Where the picture is going to be stored
    private lateinit var expenseCategoryID: UUID

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraReceiptBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("CameraFragment", "onViewCreated")

        val expenseCategoryIDStr = arguments?.getString("category_id")
        if(expenseCategoryIDStr != null) {
            expenseCategoryID = UUID.fromString(expenseCategoryIDStr)
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == RESULT_OK){
                try {
                    Log.d("PhotoUri", photoUri.toString())
                    Log.d("PhotoPath", currentPhotoPath)
                    sendPicture()
                } catch(e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error processing image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Camera action cancelled or failed", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize the permission launcher - this replaces onRequestPermissionsResult
        requestCameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
                // You may want to navigate back or show additional UI here
            }
        }

        // Check for camera permission and request if needed
        checkCameraPermissionAndOpen()
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission", "Permission Granted")
            openCamera()
        } else {
            Log.d("Permission", "Requesting permission")
            // This launches the permission request and handles the result in the registered callback
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        Log.d("CameraIntent", takePictureIntent.toString())

        val photoFile: File? = try {
            createImageFile() // Create a temporary file
        } catch (ex: IOException) {
            Log.e("CameraFragment", "Error creating image file", ex)
            Toast.makeText(requireContext(), "Could not create image file", Toast.LENGTH_SHORT).show()
            null
        }

        photoFile?.also {
            try {
                photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.vibhu.moneyplanner.fileprovider",
                    it
                )
                Log.d("PhotoUri", photoUri.toString())
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                Log.d("CameraLaunch", "Launching camera with URI: $photoUri")
                takePictureLauncher.launch(takePictureIntent)
            } catch (e: Exception) {
                Log.e("CameraFragment", "Error launching camera", e)
                Toast.makeText(requireContext(), "Error launching camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Throws(java.io.IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if(storageDir != null)  Log.d("DirectoryLocation", storageDir.path)

        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun sendPicture() {
        val bundle = Bundle()
        bundle.putString("filePath", currentPhotoPath)
        bundle.putString("category_id", expenseCategoryID.toString())

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val receiptScannerFragment = ReceiptScannerFragment()
        receiptScannerFragment.arguments = bundle

        fragmentTransaction.replace(R.id.fragment_container, receiptScannerFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

}