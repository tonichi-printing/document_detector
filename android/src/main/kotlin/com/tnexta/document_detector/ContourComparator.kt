package com.tnexta.document_detector

import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc

class ContourComparator: Comparator<MatOfPoint>{
  override fun compare(o1: MatOfPoint, o2: MatOfPoint): Int {
    return Imgproc.contourArea(o1).compareTo(Imgproc.contourArea(o2))
  }
}