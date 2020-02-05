package com.tnexta.document_detector

import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.core.Size
import org.opencv.core.CvType
import org.opencv.core.Core
import org.opencv.core.Scalar

object CannyDetector {
  fun getEdges(image: Mat): Mat {
    val processedImage : Mat = processImageForContourDetection(image)
    return processedImage
  }

  private fun processImageForContourDetection(image: Mat) : Mat {
    var dst : Mat = Mat()

    Imgproc.cvtColor(image, dst, Imgproc.COLOR_BGR2GRAY)
    dst = removeShadows(dst)
    Imgproc.GaussianBlur(dst, dst, Size(7.0, 7.0), 1.4, 1.4)
    Imgproc.GaussianBlur(dst, dst, Size(9.0, 9.0), 1.4, 1.4)
    Imgproc.adaptiveThreshold(dst, dst, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2.0)
    Imgproc.Canny(dst, dst, 50.0, 200.0)
    Imgproc.dilate(dst, dst, Mat.ones(Size(13.0, 13.0), CvType.CV_8U))

    return dst
  }

  private fun removeShadows(image: Mat): Mat {
    var dst : Mat = Mat()

    var kernel : Mat = Mat.ones(Size(7.0, 7.0), CvType.CV_8U)
    Imgproc.dilate(image, dst, kernel)
    Imgproc.medianBlur(dst, dst, 21)

    // diff_img = 255 - cv.absdiff(img, bg_img)
    var subMat : Mat = Mat.ones(image.size(), CvType.CV_8U)
    Core.multiply(subMat, Scalar(255.0), subMat)
    Core.absdiff(image, dst, dst)
    Core.subtract(subMat, dst, dst)

    Core.normalize(dst, dst, 0.0, 255.0, Core.NORM_MINMAX, CvType.CV_8UC1)
    Imgproc.threshold(dst, dst, 230.0, 0.0, Imgproc.THRESH_TRUNC)
    Core.normalize(dst, dst, 0.0, 255.0, Core.NORM_MINMAX, CvType.CV_8UC1)

    return dst
  }
}