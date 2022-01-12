package com.example.fullvideo

/**
 * Time:2022/1/12 5:03 下午
 * Author:dengqu
 * Description:
 */
class FullFrameRect {
    fun createTextureObject(): Int {
        return mProgram?.createTextureObject() ?: -1
    }

    private var mProgram: Texture2dProgram? = null

    constructor(program: Texture2dProgram) {
        mProgram = program
    }
}