package com.amtron.ferryticket.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amtron.ferryticket.R
import com.amtron.ferryticket.databinding.ActivityScannerBinding
import com.amtron.ferryticket.model.Booking
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.coroutines.DelicateCoroutinesApi
import java.io.IOException

@DelicateCoroutinesApi
class ScannerActivity : AppCompatActivity() {
	private lateinit var binding: ActivityScannerBinding
	private lateinit var cameraSource: CameraSource
	private lateinit var barcodeDetector: BarcodeDetector
	private val requestCodeCameraPermission = 1001
	private var scannedValue = ""

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityScannerBinding.inflate(layoutInflater)
		setContentView(binding.root)

		if (ContextCompat.checkSelfPermission(
				this@ScannerActivity, android.Manifest.permission.CAMERA
			) != PackageManager.PERMISSION_GRANTED
		) {
			askForCameraPermission()
		} else {
			setupControls()
		}
	}

	private fun setupControls() {
		barcodeDetector =
			BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()

		cameraSource =
			CameraSource.Builder(this, barcodeDetector).setRequestedPreviewSize(1920, 1080)
				.setAutoFocusEnabled(true) //you should add this feature
				.build()

		binding.cameraSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
			@SuppressLint("MissingPermission")
			override fun surfaceCreated(holder: SurfaceHolder) {
				try {
					//Start preview after 1s delay
					cameraSource.start(holder)
				} catch (e: IOException) {
					e.printStackTrace()
				}
			}

			@SuppressLint("MissingPermission")
			override fun surfaceChanged(
				holder: SurfaceHolder, format: Int, width: Int, height: Int
			) {
				try {
					cameraSource.start(holder)
				} catch (e: IOException) {
					e.printStackTrace()
				}
			}

			override fun surfaceDestroyed(holder: SurfaceHolder) {
				cameraSource.stop()
			}
		})

		barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
			override fun release() {
				Log.d("Response", "Value: $scannedValue")
			}

			override fun receiveDetections(detections: Detector.Detections<Barcode>) {
				val barcodes = detections.detectedItems
				if (barcodes.size() == 1) {
					binding.scanningProgressBar.visibility = View.VISIBLE
					scannedValue = barcodes.valueAt(0).rawValue
					barcodeDetector.release()
					if (scannedValue.isNotEmpty()) {
						val bundle = Bundle()
						val i = Intent(this@ScannerActivity, BookActivity::class.java)
						bundle.putString("scanResult", scannedValue)
						i.putExtras(bundle)
						startActivity(i)
					}
				} else {
					Log.d("RESPONSE", "NOT FOUND")
				}
			}
		})
	}

	private fun askForCameraPermission() {
		ActivityCompat.requestPermissions(
			this@ScannerActivity,
			arrayOf(android.Manifest.permission.CAMERA),
			requestCodeCameraPermission
		)
	}

	override fun onRequestPermissionsResult(
		requestCode: Int, permissions: Array<out String>, grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				setupControls()
			} else {
				Toast.makeText(this@ScannerActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
			}
		}
	}
}