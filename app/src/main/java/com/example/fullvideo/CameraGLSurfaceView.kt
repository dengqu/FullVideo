package com.example.fullvideo

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log

/**
 * Time:2022/1/12 2:53 下午
 * Author:dengqu
 * Description:
 */
class CameraGLSurfaceView : GLSurfaceView, SurfaceTexture.OnFrameAvailableListener {
    private val TAG = "CameraGLSurfaceView"
    private var mTextureID = -1
    private var mCamera: Camera? = null
    private var mCameraSurfaceRenderer: CameraSurfaceRenderer? = null

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        //设置opengl es的版本为2.0
        setEGLContextClientVersion(2)
        // 设置当前GLSurfaceView绑定的Renderer
        mCameraSurfaceRenderer = CameraSurfaceRenderer(this)
        setRenderer(mCameraSurfaceRenderer)
        // 设置渲染的模式
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun handleSetSurfaceTexture(surfaceTexture: SurfaceTexture?) {
        Handler(Looper.getMainLooper()).post {
            surfaceTexture?.setOnFrameAvailableListener(this)
            mCamera?.setPreviewTexture(surfaceTexture)
            mCamera?.startPreview()
        }
    }

    fun setCamera(camera: Camera?) {
        mCamera = camera
    }

    fun setCameraPreviewSize(mCameraPreviewWidth: Int, mCameraPreviewHeight: Int) {
        mCameraSurfaceRenderer?.setCameraPreviewSize(mCameraPreviewWidth, mCameraPreviewHeight)
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        Log.d(TAG, "onFrameAvailable")
        requestRender()
    }
}