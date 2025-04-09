package com.vibhu.moneyplanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import coil.load
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraReceiptActivity : AppCompatActivity() {

    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private val CAMERA_PERMISSION_REQUEST = 100
    private lateinit var photoUri: Uri
    private lateinit var currentPhotoPath: String // Store the path receipt use

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val frameLayout = findViewById<FrameLayout>(R.id.fragment_container)
            val inflater = LayoutInflater.from(this)
            val cameraReceiptView = inflater.inflate(R.layout.activity_camera_reciept_scanner, null)
            frameLayout.addView(cameraReceiptView)
            if (result.resultCode == RESULT_OK) {
                try {
                    val imageView = findViewById<ImageView>(R.id.receipt)
                    Log.d("CameraActivity", "Photo URI: $photoUri")
                    imageView?.load(photoUri)

                    Log.d("CameraActivity", "Photo URI: $photoUri")
                    sendPhotoUriToReceiptScannerFragment(photoUri) // Send photoUri to ReceiptScannerFragment


                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Camera action cancelled or failed", Toast.LENGTH_SHORT).show()
            }
        }

        // Check for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
        }

    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //if (takePictureIntent.resolveActivity(packageManager) != null) {
        val photoFile: File? = try {
            createImageFile() // Create a temporary file
        } catch (ex: IOException) {
            null
        }
        photoFile?.also {
            photoUri = FileProvider.getUriForFile(this, "com.vibhu.moneyplanner.fileprovider", it)
            Log.d("PhotoUri",photoUri.toString())
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            takePictureLauncher.launch(takePictureIntent)
        //} else {
        //    Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
        //}
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if(storageDir != null)  Log.d("DirectoryLocation", storageDir.path)

        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                finish() // Or handle the denial appropriately
            }
        }
    }

    fun sendPhotoUriToReceiptScannerFragment(photoUri: Uri) {
        val bundle = Bundle()
        bundle.putParcelable("photoUri", photoUri)

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val receiptScannerFragment = ReceiptScannerFragment()
        receiptScannerFragment.arguments = bundle

        fragmentTransaction.replace(R.id.fragment_container, receiptScannerFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}