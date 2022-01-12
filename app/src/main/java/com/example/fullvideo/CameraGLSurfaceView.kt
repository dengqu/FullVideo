package com.example.fullvideo

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

/**
 * Time:2022/1/12 2:53 下午
 * Author:dengqu
 * Description:
 */
class CameraGLSurfaceView : GLSurfaceView {
    private var mTextureID = -1

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        //设置opengl es的版本为2.0
        setEGLContextClientVersion(2)
        // 设置当前GLSurfaceView绑定的Renderer
        setRenderer(CameraSurfaceRenderer())
        // 设置渲染的模式
        renderMode = RENDERMODE_WHEN_DIRTY
    }
}