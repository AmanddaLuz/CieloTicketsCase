package br.com.amandaluz.cielotickets.feature.receipt.view

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix

class QrCodeBitmapRenderer {
    fun render(content: String, size: Int): Bitmap? =
        if (content.isBlank() || size <= 0) {
            null
        } else {
            encode(content, size)?.toBitmap(size)
        }

    private fun encode(content: String, size: Int): BitMatrix? =
        try {
            MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size,
            )
        } catch (_: WriterException) {
            null
        }

    private fun BitMatrix.toBitmap(size: Int): Bitmap {
        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            for (x in 0 until size) {
                pixels[(y * size) + x] = if (this[x, y]) {
                    Color.BLACK
                } else {
                    Color.WHITE
                }
            }
        }
        return Bitmap.createBitmap(
            pixels,
            size,
            size,
            Bitmap.Config.RGB_565,
        )
    }
}
