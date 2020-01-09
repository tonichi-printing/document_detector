package com.tnexta.document_detector

import org.bytedeco.javacpp.Loader
import org.bytedeco.opencv.global.opencv_dnn

object ImageLoader {
  fun load(imagePath: String): String {
    // Loader.load(opencv_java)
    val ver : String = opencv_dnn.OPENCV_DNN_API_VERSION.toString()
    return ver
    // return imagePath
  }
}