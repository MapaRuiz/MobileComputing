package com.example.taller2

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.taller2.databinding.ActivityCameraBinding
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var photoUri: Uri? = null

    // Permisos dinámicos según versión Android
    private val cameraPermission = Manifest.permission.CAMERA
    private val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE

    // Launchers para resultados
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && photoUri != null) {
            loadAndDisplayImage()
            Toast.makeText(this, "Imagen guardada en galería", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { loadAndScaleImage(it) }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        when {
            permissions[cameraPermission] == true -> launchCamera()
            permissions[galleryPermission] == true -> galleryLauncher.launch("image/*")
            else -> showPermissionDeniedWarning()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cameraButton.setOnClickListener { checkCameraPermission() }
        binding.galleryButton.setOnClickListener { checkGalleryPermission() }
    }

    // --- Manejo de Permisos ---
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            permissionLauncher.launch(arrayOf(cameraPermission, galleryPermission))
        }
    }

    private fun checkGalleryPermission() {
        if (ContextCompat.checkSelfPermission(this, galleryPermission) == PackageManager.PERMISSION_GRANTED) {
            galleryLauncher.launch("image/*")
        } else {
            permissionLauncher.launch(arrayOf(galleryPermission))
        }
    }

    // --- Lógica de Cámara ---
    private fun launchCamera() {
        photoUri = createImageUri()
        photoUri?.let { cameraLauncher.launch(it) } ?: showUriError()
    }

    private fun createImageUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    // --- Procesamiento de Imágenes ---
    private fun loadAndDisplayImage() {
        photoUri?.let { uri ->
            val bitmap = decodeSampledBitmapFromUri(uri, 600, 600)
            binding.imageView.setImageBitmap(bitmap)
        }
    }

    private fun loadAndScaleImage(uri: Uri) {
        try {
            val bitmap = decodeSampledBitmapFromUri(uri, 600, 600)
            binding.imageView.setImageBitmap(bitmap)
        } catch (e: IOException) {
            Toast.makeText(this, "Error al cargar imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun decodeSampledBitmapFromUri(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        return contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.Options().run {
                inJustDecodeBounds = true
                BitmapFactory.decodeStream(stream, null, this)
                inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
                inJustDecodeBounds = false
                BitmapFactory.decodeStream(contentResolver.openInputStream(uri), null, this)
            }
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    // --- Manejo de Errores ---
    private fun showUriError() {
        Toast.makeText(this, "Error al crear URI para guardar la imagen", Toast.LENGTH_LONG).show()
    }

    private fun showPermissionDeniedWarning() {
        AlertDialog.Builder(this)
            .setTitle("Permisos requeridos")
            .setMessage("Debe conceder los permisos en ajustes del dispositivo (Xiaomi: Ajustes > Apps > Su app > Permisos)")
            .setPositiveButton("Abrir ajustes") { _, _ ->
                startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                })
            }
            .show()
    }
}