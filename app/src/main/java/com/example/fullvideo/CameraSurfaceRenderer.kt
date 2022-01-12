package com.example.fullvideo

import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Time:2022/1/12 4:47 下午
 * Author:dengqu
 * Description:
 */
class CameraSurfaceRenderer : GLSurfaceView.Renderer {
    private val TAG = "CameraSurfaceRenderer"

    private lateinit var mFullScreen: FullFrameRect
    private var mSurfaceTexture: SurfaceTexture? = null

    private var mTextureId: Int = -1

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        mFullScreen = FullFrameRect(Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT))
        mTextureId = mFullScreen.createTextureObject()

        // Create a SurfaceTexture, with an external texture, in this EGL context.  We don't
        // have a Looper in this thread -- GLSurfaceView doesn't create one -- so the frame
        // available messages will arrive on the main thread.
        mSurfaceTexture = SurfaceTexture(mTextureId)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    }

    override fun onDrawFrame(gl: GL10?) {
    }
}