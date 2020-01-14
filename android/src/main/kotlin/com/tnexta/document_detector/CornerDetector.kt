package com.tnexta.document_detector

import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Size
import org.opencv.core.Point


object CornerDetector {
  fun getCornerCoordinates(image: Mat): Corners {
    val contours = findContours(image)
    val corners = getCorners(contours, image.size())
    return corners
  }

  private fun findContours(image: Mat) : ArrayList<MatOfPoint> {
    var contours = ArrayList<MatOfPoint>()
    val hierarchy = Mat()
    Imgproc.threshold(image, image, 20.0, 255.0, Imgproc.THRESH_TRIANGLE)
    Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
    contours.sortByDescending { p: MatOfPoint -> Imgproc.contourArea(p) }
    return contours
  }

  private fun getCorners(contours: ArrayList<MatOfPoint>, size: Size): Corners {
    val indexTo: Int
    when (contours.size) {
      in 0..5 -> indexTo = contours.size - 1
      else -> indexTo = 4
    }
    for (index in 0..contours.size) {
      if (index in 0..indexTo) {
        val c2f = MatOfPoint2f(*contours[index].toArray())
        val peri = Imgproc.arcLength(c2f, true)
        val approx = MatOfPoint2f()
        Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true)
        val points = approx.toArray().asList()
        // select biggest 4 angles polygon
        if (points.size == 4) {
          val foundPoints = sortPoints(points)
          return Corners(foundPoints, size)
        }
      } else {
        throw Exception("No contours found")
      }
    }
    throw Exception("No contours found")
  }

  private fun sortPoints(points: List<Point>): List<Point> {
    val p0 = points.minBy { point -> point.x + point.y } ?: Point()
    val p1 = points.maxBy { point -> point.x - point.y } ?: Point()
    val p2 = points.maxBy { point -> point.x + point.y } ?: Point()
    val p3 = points.minBy { point -> point.x - point.y } ?: Point()

    return listOf(p0, p1, p2, p3)
  }
}