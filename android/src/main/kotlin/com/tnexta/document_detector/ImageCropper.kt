package com.tnexta.document_detector

import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.RotatedRect
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

object ImageCropper {
  fun crop(rect : RotatedRect, image: Mat): Mat {
    // Refer: https://answers.opencv.org/question/497/extract-a-rotatedrect-area/?answer=518#post-id-518

    var angle : Double = rect.angle
    val rectSize : Size = rect.size

    if (-45.0 > angle) {
      angle += 90.0
      // Refer: https://stackoverflow.com/a/45377921/6402452
      rectSize.width = rectSize.height.also { rectSize.height = rectSize.width }
    }

    var M : Mat = Imgproc.getRotationMatrix2D(rect.center, angle, 1.0)
    var rotated : Mat = Mat()
    Imgproc.warpAffine(image, rotated, M, image.size(), Imgproc.INTER_CUBIC)

    var cropped : Mat = Mat()
    Imgproc.getRectSubPix(rotated, rectSize, rect.center, cropped)

    return cropped
  }
}