package com.example.fullvideo

import android.opengl.Matrix

/**
 * Time:2022/1/12 5:03 下午
 * Author:dengqu
 * Description:
 */
class FullFrameRect {
    private val mRectDrawable: Drawable2d = Drawable2d(Drawable2d.Prefab.FULL_RECTANGLE)

    companion object {
        /** Identity matrix for general use.  Don't modify or life will get weird.  */
        var IDENTITY_MATRIX: FloatArray

        init {
            IDENTITY_MATRIX = FloatArray(16)
            Matrix.setIdentityM(IDENTITY_MATRIX, 0)
        }
    }

    fun createTextureObject(): Int {
        return mProgram?.createTextureObject() ?: -1
    }

    fun getProgram(): Texture2dProgram? {
        return mProgram
    }

    private var mProgram: Texture2dProgram? = null

    constructor(program: Texture2dProgram) {
        mProgram = program
    }


    /**
     * Changes the program.  The previous program will be released.
     *
     *
     * The appropriate EGL context must be current.
     */
    fun changeProgram(program: Texture2dProgram) {
        mProgram?.release()
        mProgram = program
    }

    /**
     * Draws a viewport-filling rect, texturing it with the specified texture object.
     */
    fun drawFrame(textureId: Int, texMatrix: FloatArray?) {
        // Use the identity matrix for MVP so our 2x2 FULL_RECTANGLE covers the viewport.
        mProgram?.draw(
            IDENTITY_MATRIX, mRectDrawable.getVertexArray(), 0,
            mRectDrawable.getVertexCount(), mRectDrawable.getCoordsPerVertex(),
            mRectDrawable.getVertexStride(),
            texMatrix, mRectDrawable.getTexCoordArray(), textureId,
            mRectDrawable.getTexCoordStride()
        )
    }
}