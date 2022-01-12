package com.example.fullvideo

import android.hardware.Camera
import android.util.Log

/**
 * Time:2022/1/12 5:42 下午
 * Author:dengqu
 * Description:
 */
object CameraUtils {

    private val TAG = "CameraUtils"

    /**
     * Attempts to find a preview size that matches the provided width and height (which
     * specify the dimensions of the encoded video).  If it fails to find a match it just
     * uses the default preview size for video.
     *
     *
     * TODO: should do a best-fit match, e.g.
     * https://github.com/commonsguy/cwac-camera/blob/master/camera/src/com/commonsware/cwac/camera/CameraUtils.java
     */
    fun choosePreviewSize(parms: Camera.Parameters, width: Int, height: Int) {
        // We should make sure that the requested MPEG size is less than the preferred
        // size, and has the same aspect ratio.
        val ppsfv = parms.preferredPreviewSizeForVideo
        if (ppsfv != null) {
            Log.d(
                TAG, "Camera preferred preview size for video is " +
                    ppsfv.width + "x" + ppsfv.height
            )
        }

        //for (Camera.Size size : parms.getSupportedPreviewSizes()) {
        //    Log.d(TAG, "supported: " + size.width + "x" + size.height);
        //}
        for (size in parms.supportedPreviewSizes) {
            Log.d(
                TAG, "choosePreviewSize size " +
                    size.width + "x" + size.height
            )
            if (size.width == width && size.height == height) {
                parms.setPreviewSize(width, height)
                return
            }
        }
        Log.w(TAG, "Unable to set preview size to " + width + "x" + height)
        if (ppsfv != null) {
            parms.setPreviewSize(ppsfv.width, ppsfv.height)
        }
        // else use whatever the default size is
    }
}