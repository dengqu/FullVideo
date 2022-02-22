package com.example.fullvideo

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.fullvideo.GlUtil.loadShader
import kotlinx.android.synthetic.main.activity_draw_triangle.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Time:2022/2/18 10:40 上午
 * Author:dengqu
 * Description:
 */
class DrawTriangleActivity : AppCompatActivity() {
    private val TAG = "DrawTriangleActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw_triangle)
        // 设置opengl es版本为2.0
        mGLSurfaceView?.setEGLContextClientVersion(2)
        // 设置渲染Render
        mGLSurfaceView?.setRenderer(MyGLSurfaceViewRender())
        //设置渲染模式
        // RENDERMODE_CONTINUOUSLY：默认的模式，定时进行刷新模式
        // RENDERMODE_WHEN_DIRTY:数据发生变化时渲染
        mGLSurfaceView?.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    inner class MyGLSurfaceViewRender : GLSurfaceView.Renderer {
        //定点着色器
        private val VERSOURCE = "attribute vec4 vPosition;\n" +
            " void main() {\n" +
            "  gl_Position = vPosition;\n" +
            " }"

        //片段着色器
        private val FRAGMENTSOURCE = "precision mediump float;\n" +
            " uniform vec4 vColor;\n" +
            " void main() {\n" +
            "  gl_FragColor = vColor;\n" +
            " }"

        // 三角形定点坐标
        private val TRIANGLECOORDS = floatArrayOf(
            0.5f, 0.5f, 0.0f,  // top
            -0.5f, -0.5f, 0.0f,  // bottom left
            0.5f, -0.5f, 0.0f // bottom right
        )

        // 颜色数据:白色
        private val COLOR = floatArrayOf(
            1.0f, 1.0f, 1.0f, 1.0f
        )

        private var mProgram = 0
        private var vertexBuffer: FloatBuffer? = null
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            Log.d(TAG, "onSurfaceCreated------")
            // 设置背景为灰色
            GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
            // 申请底层空间
            val bb = ByteBuffer.allocateDirect(TRIANGLECOORDS.size * 4)
            bb.order(ByteOrder.nativeOrder())
            // 将坐标数据转换FloatBuffer，用以传入给OpenGL ES程序
            vertexBuffer = bb.asFloatBuffer()
            vertexBuffer?.put(TRIANGLECOORDS)
            vertexBuffer?.position(0)

            //加载定点着色器
            val vertexShader: Int = loadShader(
                GLES20.GL_VERTEX_SHADER,
                VERSOURCE
            )
            //加载片段着色器
            val fragmentShader: Int = loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                FRAGMENTSOURCE
            )

            //创建一个空的OpenGLES程序
            mProgram = GLES20.glCreateProgram()
            //将顶点着色器加入到程序
            GLES20.glAttachShader(mProgram, vertexShader)
            //将片元着色器加入到程序中
            GLES20.glAttachShader(mProgram, fragmentShader)
            //连接到着色器程序
            GLES20.glLinkProgram(mProgram)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            Log.d(TAG, "onSurfaceChanged gl:$gl, width:$width,height:$height")
            GLES20.glViewport(0, 0, width, height)
        }

        override fun onDrawFrame(gl: GL10?) {
            Log.d(TAG, "onDrawFrame gl:$gl")
            // 将程序加入到OpenGLES2.0环境
            GLES20.glUseProgram(mProgram)

            // 获取顶点着色器的vPosition成员句柄
            val mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
            // 启用三角形顶点的句柄
            GLES20.glEnableVertexAttribArray(mPositionHandle)
            //准备三角形的坐标数据
            GLES20.glVertexAttribPointer(
                mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer
            )
            //获取片元着色器的vColor成员的句柄
            val mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
            //设置绘制三角形的颜色
            GLES20.glUniform4fv(mColorHandle, 1, COLOR, 0)
            //绘制三角形
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
            //禁止顶点数组的句柄
            GLES20.glDisableVertexAttribArray(mPositionHandle)
        }
    }
}