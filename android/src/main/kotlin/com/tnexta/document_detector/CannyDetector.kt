package com.tnexta.document_detector

import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.core.Size

object CannyDetector {
  fun getEdges(image: Mat): Mat {
    var gray : Mat = Mat()
    Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY)
    var blur : Mat = Mat()
    Imgproc.GaussianBlur(gray, blur, Size(3.0, 3.0), 1.4, 1.4)
    var sobel : Mat = Mat()
    Imgproc.Sobel(blur, sobel, 1, 1, 1)
    // var edged : Mat = Mat()
    // Imgproc.Canny(dst, edged, 75.0, 200.0)
    return sobel
  }
}