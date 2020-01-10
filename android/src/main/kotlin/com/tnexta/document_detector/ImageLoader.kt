package com.tnexta.document_detector

import org.bytedeco.javacpp.Loader
import org.bytedeco.opencv.global.opencv_dnn
import org.bytedeco.opencv.opencv_videoio.VideoCapture
import org.bytedeco.opencv.opencv_core.Mat

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