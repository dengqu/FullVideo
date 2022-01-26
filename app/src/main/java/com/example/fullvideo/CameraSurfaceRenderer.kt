package com.example.fullvideo

import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.util.Log
import java.lang.RuntimeException
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Time:2022/1/12 4:47 下午
 * Author:dengqu
 * Description:
 */
class CameraSurfaceRenderer : GLSurfaceView.Renderer {
    private var mIncomingSizeUpdated: Boolean = false
    private var mIncomingHeight: Int = 0
    private var mIncomingWidth: Int = 0
    private val TAG = "CameraSurfaceRenderer"

    private lateinit var mFullScreen: FullFrameRect
    private var mSurfaceTexture: SurfaceTexture? = null

    private var mTextureId: Int = -1

    private var mCameraGLSurfaceView: CameraGLSurfaceView? = null

    private val mSTMatrix = FloatArray(16)

    private var mCurrentFilter = 0
    private var mNewFilter = 0

    // Camera filters; must match up with cameraFilterNames in strings.xml
    val FILTER_NONE = 0
    val FILTER_BLACK_WHITE = 1
    val FILTER_BLUR = 2
    val FILTER_SHARPEN = 3
    val FILTER_EDGE_DETECT = 4
    val FILTER_EMBOSS = 5

    constructor(cameraGLSurfaceView: CameraGLSurfaceView) {
        mCameraGLSurfaceView = cameraGLSurfaceView

        // We could preserve the old filter mode, but currently not bothering.

        // We could preserve the old filter mode, but currently not bothering.
        mCurrentFilter = -1
        mNewFilter = 0
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        mFullScreen = FullFrameRect(Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT))
        mTextureId = mFullScreen.createTextureObject()

        // Create a SurfaceTexture, with an external texture, in this EGL context.  We don't
        // have a Looper in this thread -- GLSurfaceView doesn't create one -- so the frame
        // available messages will arrive on the main thread.
        mSurfaceTexture = SurfaceTexture(mTextureId)
        mCameraGLSurfaceView?.handleSetSurfaceTexture(mSurfaceTexture)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    }

    override fun onDrawFrame(gl: GL10?) {
        Log.d(TAG, "onDrawFrame tex=$mTextureId")
        var showBox = false

        // Latch the latest frame.  If there isn't anything new, we'll just re-use whatever
        // was there before.

        // Latch the latest frame.  If there isn't anything new, we'll just re-use whatever
        // was there before.
        mSurfaceTexture?.updateTexImage()

        if (mIncomingWidth <= 0 || mIncomingHeight <= 0) {
            // Texture size isn't set yet.  This is only used for the filters, but to be
            // safe we can just skip drawing while we wait for the various races to resolve.
            // (This seems to happen if you toggle the screen off/on with power button.)
            Log.i(TAG, "Drawing before incoming texture size set; skipping")
            return
        }

        // Update the filter, if necessary.
        // Update the filter, if necessary.
        if (mCurrentFilter != mNewFilter) {
            updateFilter()
        }

        if (mIncomingSizeUpdated) {
            mFullScreen.getProgram()?.setTexSize(mIncomingWidth, mIncomingHeight)
            mIncomingSizeUpdated = false
        }
        // Draw the video frame.
        mSurfaceTexture?.getTransformMatrix(mSTMatrix)

        Log.d(TAG, "mSTMatrix = $mSTMatrix")
        mFullScreen?.drawFrame(mTextureId, mSTMatrix)
    }

    fun setCameraPreviewSize(width: Int, height: Int) {
        Log.d(TAG, "setCameraPreviewSize")
        mIncomingWidth = width
        mIncomingHeight = height
        mIncomingSizeUpdated = true
    }

    /**
     * Updates the filter program.
     */
    fun updateFilter() {
        val programType: Texture2dProgram.ProgramType
        var kernel: FloatArray? = null
        var colorAdj = 0.0f
        Log.d(TAG, "Updating filter to $mNewFilter")
        when (mNewFilter) {
            FILTER_NONE -> programType =
                Texture2dProgram.ProgramType.TEXTURE_EXT
            FILTER_BLACK_WHITE ->                 // (In a previous version the TEXTURE_EXT_BW variant was enabled by a flag called
                // ROSE_COLORED_GLASSES, because the shader set the red channel to the B&W color
                // and green/blue to zero.)
                programType = Texture2dProgram.ProgramType.TEXTURE_EXT_BW
            FILTER_BLUR -> {
                programType = Texture2dProgram.ProgramType.TEXTURE_EXT_FILT
                kernel = floatArrayOf(
                    1f / 16f, 2f / 16f, 1f / 16f,
                    2f / 16f, 4f / 16f, 2f / 16f,
                    1f / 16f, 2f / 16f, 1f / 16f
                )
            }
            FILTER_SHARPEN -> {
                programType = Texture2dProgram.ProgramType.TEXTURE_EXT_FILT
                kernel = floatArrayOf(
                    0f, -1f, 0f,
                    -1f, 5f, -1f,
                    0f, -1f, 0f
                )
            }
            FILTER_EDGE_DETECT -> {
                programType = Texture2dProgram.ProgramType.TEXTURE_EXT_FILT
                kernel = floatArrayOf(
                    -1f, -1f, -1f,
                    -1f, 8f, -1f,
                    -1f, -1f, -1f
                )
            }
            FILTER_EMBOSS -> {
                programType = Texture2dProgram.ProgramType.TEXTURE_EXT_FILT
                kernel = floatArrayOf(
                    2f, 0f, 0f,
                    0f, -1f, 0f,
                    0f, 0f, -1f
                )
                colorAdj = 0.5f
            }
            else -> throw RuntimeException("Unknown filter mode $mNewFilter")
        }

        // Do we need a whole new program?  (We want to avoid doing this if we don't have
        // too -- compiling a program could be expensive.)
        if (programType !== mFullScreen.getProgram()!!.getProgramType()) {
            mFullScreen.changeProgram(Texture2dProgram(programType))
            // If we created a new program, we need to initialize the texture width/height.
            mIncomingSizeUpdated = true
        }

        // Update the filter kernel (if any).
        if (kernel != null) {
            mFullScreen.getProgram()!!.setKernel(kernel, colorAdj)
        }
        mCurrentFilter = mNewFilter
    }
}