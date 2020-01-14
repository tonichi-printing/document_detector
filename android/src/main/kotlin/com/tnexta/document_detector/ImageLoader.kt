package com.tnexta.document_detector

import org.opencv.videoio.VideoCapture
import org.opencv.core.Mat

object ImageLoader {
  fun load(imagePath: String): Mat {
    val capture = VideoCapture(imagePath)
    var image : Mat = Mat()
    val isCaptured : Boolean = capture.read(image)
    if (isCaptured == false) {
      throw IllegalArgumentException("Cannot Read Image")
    }
    return image
  }
}