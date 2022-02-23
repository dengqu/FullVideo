package com.example.fullvideo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_draw_triangle?.setOnClickListener(this)
        btn_camera_preview?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        v?.id?.let {
            when (it) {
                R.id.btn_draw_triangle -> {
                    startActivity(Intent(this, DrawTriangleActivity::class.java))
                }
                R.id.btn_camera_preview -> {
                    startActivity(Intent(this, CameraPreviewActivity::class.java))
                }
            }
        }
    }
}