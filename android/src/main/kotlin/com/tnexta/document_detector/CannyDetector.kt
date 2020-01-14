package com.tnexta.document_detector

import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

object CannyDetector {
  fun getEdges(image: Mat): Mat {
    var dst : Mat = Mat()
    Imgproc.cvtColor(image, dst, Imgproc.COLOR_BGR2GRAY)
    var edged : Mat = Mat()
    Imgproc.Canny(dst, edged, 75.0, 200.0)
    return edged
  }
}