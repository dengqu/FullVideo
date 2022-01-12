package com.example.fullvideo

import android.opengl.GLES20
import android.util.Log
import java.lang.RuntimeException

/**
 * Time:2022/1/12 5:02 下午
 * Author:dengqu
 * Description:
 */
object GlUtil {
    private val TAG = "GlUtil"

    /**
     * Checks to see if a GLES error has been raised.
     */
    fun checkGlError(op: String) {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            val msg = op + ": glError 0x" + Integer.toHexString(error)
            Log.e(TAG, msg)
            throw RuntimeException(msg)
        }
    }
}