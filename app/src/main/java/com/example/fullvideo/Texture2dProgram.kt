package com.example.fullvideo

import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Log
import java.nio.FloatBuffer

/**
 * Time:2022/1/12 4:52 下午
 * Author:dengqu
 * Description:
 */
class Texture2dProgram {
    private val TAG = "Texture2dProgram"
    private var mProgramType = ProgramType.TEXTURE_2D
    private var mTextureTarget = -1

    private var mTexOffset: FloatArray? = null
    private var mProgramHandle = 0
    private var muMVPMatrixLoc = 0
    private var muTexMatrixLoc = 0
    private var maPositionLoc = 0
    private var maTextureCoordLoc = 0
    private var muKernelLoc = 0
    private var mColorAdjust = 0f
    private var muTexOffsetLoc = 0
    private var muColorAdjustLoc = 0
    val KERNEL_SIZE = 9
    private val mKernel = FloatArray(KERNEL_SIZE)


    // Simple vertex shader, used for all programs.
    private val VERTEX_SHADER =
        "uniform mat4 uMVPMatrix;\n" +
            "uniform mat4 uTexMatrix;\n" +
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main() {\n" +
            "    gl_Position = uMVPMatrix * aPosition;\n" +
            "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
            "}\n"


    // Simple fragment shader for use with "normal" 2D textures.
    private val FRAGMENT_SHADER_2D =
        "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform sampler2D sTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}\n"

    // Simple fragment shader for use with external 2D textures (e.g. what we get from
    // SurfaceTexture).
    private val FRAGMENT_SHADER_EXT =
        "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}\n"

    enum class ProgramType {
        TEXTURE_2D, TEXTURE_EXT, TEXTURE_EXT_BW, TEXTURE_EXT_FILT
    }

    constructor(programType: ProgramType) {
        mProgramType = programType
        when (programType) {
            ProgramType.TEXTURE_2D -> {
                mTextureTarget = GLES20.GL_TEXTURE_2D
                mProgramHandle =
                    GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_2D)
            }
            ProgramType.TEXTURE_EXT -> {
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES
                mProgramHandle =
                    GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT)
            }
            else -> {
                throw RuntimeException("Unhandled type $programType")
            }
        }


        // get locations of attributes and uniforms
        maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition")
        GlUtil.checkLocation(maPositionLoc, "aPosition")


        maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord")
        GlUtil.checkLocation(maTextureCoordLoc, "aTextureCoord")

        muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix")
        GlUtil.checkLocation(muMVPMatrixLoc, "uMVPMatrix")

        muTexMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexMatrix")
        GlUtil.checkLocation(muTexMatrixLoc, "uTexMatrix")

        muKernelLoc = GLES20.glGetUniformLocation(mProgramHandle, "uKernel")
        if (muKernelLoc < 0) {
            // no kernel in this one
            muKernelLoc = -1
            muTexOffsetLoc = -1
            muColorAdjustLoc = -1
        } else {
            // has kernel, must also have tex offset and color adj
            muTexOffsetLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexOffset")
            GlUtil.checkLocation(muTexOffsetLoc, "uTexOffset")
            muColorAdjustLoc = GLES20.glGetUniformLocation(mProgramHandle, "uColorAdjust")
            GlUtil.checkLocation(muColorAdjustLoc, "uColorAdjust")

            // initialize default values
            setKernel(floatArrayOf(0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f), 0f)
            setTexSize(256, 256)
        }
    }


    /**
     * Configures the convolution filter values.
     *
     * @param values Normalized filter values; must be KERNEL_SIZE elements.
     */
    fun setKernel(values: FloatArray, colorAdj: Float) {
        require(values.size == KERNEL_SIZE) {
            "Kernel size is " + values.size +
                " vs. " + KERNEL_SIZE
        }
        System.arraycopy(values, 0, mKernel, 0, KERNEL_SIZE)
        mColorAdjust = colorAdj
        //Log.d(TAG, "filt kernel: " + Arrays.toString(mKernel) + ", adj=" + colorAdj);
    }

    /**
     * Creates a texture object suitable for use with this program.
     *
     *
     * On exit, the texture will be bound.
     */
    fun createTextureObject(): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        GlUtil.checkGlError("glGenTextures")
        val texId = textures[0]
        GLES20.glBindTexture(mTextureTarget, texId)
        GlUtil.checkGlError("glBindTexture $texId")
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GlUtil.checkGlError("glTexParameter")
        return texId
    }

    fun setTexSize(width: Int, height: Int) {
        val rw = 1.0f / width
        val rh = 1.0f / height

        // Don't need to create a new array here, but it's syntactically convenient.

        // Don't need to create a new array here, but it's syntactically convenient.
        mTexOffset = floatArrayOf(
            -rw, -rh, 0f, -rh, rw, -rh,
            -rw, 0f, 0f, 0f, rw, 0f,
            -rw, rh, 0f, rh, rw, rh
        )
        //Log.d(TAG, "filt size: " + width + "x" + height + ": " + Arrays.toString(mTexOffset));
    }

    /**
     * Issues the draw call.  Does the full setup on every call.
     *
     * @param mvpMatrix The 4x4 projection matrix.
     * @param vertexBuffer Buffer with vertex position data.
     * @param firstVertex Index of first vertex to use in vertexBuffer.
     * @param vertexCount Number of vertices in vertexBuffer.
     * @param coordsPerVertex The number of coordinates per vertex (e.g. x,y is 2).
     * @param vertexStride Width, in bytes, of the position data for each vertex (often
     * vertexCount * sizeof(float)).
     * @param texMatrix A 4x4 transformation matrix for texture coords.  (Primarily intended
     * for use with SurfaceTexture.)
     * @param texBuffer Buffer with vertex texture data.
     * @param texStride Width, in bytes, of the texture data for each vertex.
     */
    fun draw(
        mvpMatrix: FloatArray?, vertexBuffer: FloatBuffer?, firstVertex: Int,
        vertexCount: Int, coordsPerVertex: Int, vertexStride: Int,
        texMatrix: FloatArray?, texBuffer: FloatBuffer?, textureId: Int, texStride: Int
    ) {
        GlUtil.checkGlError("draw start")

        // Select the program.
        GLES20.glUseProgram(mProgramHandle)
        GlUtil.checkGlError("glUseProgram")

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(mTextureTarget, textureId)

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mvpMatrix, 0)
        GlUtil.checkGlError("glUniformMatrix4fv")

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0)
        GlUtil.checkGlError("glUniformMatrix4fv")

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maPositionLoc)
        GlUtil.checkGlError("glEnableVertexAttribArray")

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(
            maPositionLoc, coordsPerVertex,
            GLES20.GL_FLOAT, false, vertexStride, vertexBuffer
        )
        GlUtil.checkGlError("glVertexAttribPointer")

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc)
        GlUtil.checkGlError("glEnableVertexAttribArray")

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(
            maTextureCoordLoc, 2,
            GLES20.GL_FLOAT, false, texStride, texBuffer
        )
        GlUtil.checkGlError("glVertexAttribPointer")

        // Populate the convolution kernel, if present.
        if (muKernelLoc >= 0) {
            GLES20.glUniform1fv(muKernelLoc, KERNEL_SIZE, mKernel, 0)
            GLES20.glUniform2fv(muTexOffsetLoc, KERNEL_SIZE, mTexOffset, 0)
            GLES20.glUniform1f(muColorAdjustLoc, mColorAdjust)
        }

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount)
        GlUtil.checkGlError("glDrawArrays")

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(maPositionLoc)
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc)
        GLES20.glBindTexture(mTextureTarget, 0)
        GLES20.glUseProgram(0)
    }

    /**
     * Returns the program type.
     */
    fun getProgramType(): ProgramType? {
        return mProgramType
    }

    fun release() {
        Log.d(TAG, "deleting program $mProgramHandle")
        GLES20.glDeleteProgram(mProgramHandle)
        mProgramHandle = -1
    }
}