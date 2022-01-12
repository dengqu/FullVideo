package com.example.fullvideo

import android.Manifest
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.Surface
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.callback.RequestCallback
import java.lang.RuntimeException

class MainActivity : AppCompatActivity() {
    private val TAG = ""
    private var mCamera: Camera? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        requestPermissions()
    }

    override fun onPause() {
        super.onPause()
        releaseCamera()
    }

    private fun requestPermissions() {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
            ).request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    if (mCamera == null) {
                        openCamera(720, 1280)
                    }
                    Toast.makeText(this, "All permissions are granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "These permissions are denied: $deniedList", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun releaseCamera() {
        if (mCamera != null) {
            mCamera!!.stopPreview()
            mCamera!!.release()
            mCamera = null
            Log.d(TAG, "releaseCamera -- done")
        }
    }

    private fun openCamera(desiredWidth: Int, desiredHeight: Int) {
        if (mCamera != null) {
            throw RuntimeException("camera already initialized")
        }
        val info = CameraInfo()

        // Try to find a front-facing camera (e.g. for videoconferencing).
        val numCameras = Camera.getNumberOfCameras()
        for (i in 0 until numCameras) {
            Camera.getCameraInfo(i, info)
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                mCamera = Camera.open(i)
                break
            }
        }
        if (mCamera == null) {
            Log.d(TAG, "No front-facing camera found; opening default")
            mCamera = Camera.open() // opens first back-facing camera
        }
        if (mCamera == null) {
            throw RuntimeException("Unable to open camera")
        }
        val parms: Camera.Parameters = mCamera!!.parameters
        CameraUtils.choosePreviewSize(parms, desiredWidth, desiredHeight)

        // Give the camera a hint that we're recording video.  This can have a big
        // impact on frame rate.
        parms.setRecordingHint(true)

        // leave the frame rate set to default
        mCamera!!.setParameters(parms)
        val fpsRange = IntArray(2)
        val mCameraPreviewSize = parms.previewSize
        parms.getPreviewFpsRange(fpsRange)
        var previewFacts = mCameraPreviewSize.width.toString() + "x" + mCameraPreviewSize.height
        previewFacts += if (fpsRange[0] == fpsRange[1]) {
            " @" + fpsRange[0] / 1000.0 + "fps"
        } else {
            " @[" + fpsRange[0] / 1000.0 +
                " - " + fpsRange[1] / 1000.0 + "] fps"
        }
    }
}