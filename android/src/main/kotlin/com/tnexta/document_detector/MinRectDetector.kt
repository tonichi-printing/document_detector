package com.tnexta.document_detector

import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.core.Size
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Scalar
import org.opencv.core.Point
import org.opencv.imgproc.Moments
import org.opencv.core.RotatedRect
import org.opencv.core.MatOfInt


object MinRectDetector {
  fun getRect(edgedImage: Mat, origImage: Mat) : Array<Point> {
    var contours : MutableList<MatOfPoint> = getRelevantContours(edgedImage)
    contours = mergeContours(contours)
    val hull : MutableList<MatOfPoint> = hullify(contours)
    // Imgproc.drawContours(origImage, hull, -1, Scalar(0.0, 0.0, 255.0), 2)
    // return origImage
    val hull2f : MutableList<MatOfPoint2f> = convertToPoint2f(hull)
    val rectangle : RotatedRect = Imgproc.minAreaRect(hull2f[0])
    var vertices : Array<Point> = Array<Point>(4) { Point() }
    rectangle.points(vertices)
    return vertices
  }

  private fun getRelevantContours(image: Mat) : MutableList<MatOfPoint> {
    var contours : MutableList<MatOfPoint> = mutableListOf()
    var hierarchy : Mat = Mat()
    Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)
    val contourComparator = ContourComparator()
    contours.sortWith(contourComparator.reversed())
    val image_area : Double = (image.size()).area()
    contours = filterContoursBySize(contours, image_area)
    contours = filterContoursByDistance(contours, image)
    return contours
  }

  private fun filterContoursBySize(contours : MutableList<MatOfPoint>, image_area : Double): MutableList<MatOfPoint> {
    val nContours : MutableList<MatOfPoint> = mutableListOf()
    for (ci in contours) {
      val contourArea : Double = Imgproc.contourArea(ci)
      if (contourArea > 10000.0 && 0.9 * image_area > contourArea) { // @TODO Reconsider Min Value
        nContours.add(ci)
      }
    }
    return nContours
  }

  private fun filterContoursByDistance(contours : MutableList<MatOfPoint>, image: Mat): MutableList<MatOfPoint> {
    val centers : MutableList<Array<Int>> = mutableListOf()
    val nContours : MutableList<MatOfPoint> = mutableListOf()
    for (ci in contours) {
      val M : Moments = Imgproc.moments(ci)
      val cX : Int = (M.get_m10() / M.get_m00()).toInt()
      val cY : Int = (M.get_m01() / M.get_m00()).toInt()
      val center = arrayOf(cX, cY)
      if (centers.size == 0 || isWithinAcceptableDistance(center, centers, image)) {
        nContours.add(ci)
        centers.add(center)
      }
    }
    return nContours
  }

  private fun isWithinAcceptableDistance(center : Array<Int>, centers : MutableList<Array<Int>>, image: Mat): Boolean {
    val size : Size = image.size()
    val image_diag : Double = kotlin.math.sqrt(size.height * size.height + size.width * size.width)
    for (c in centers) {
      val dx : Int = c[0] - center[0]
      val dy : Int = c[1] - center[1]
      val D : Double = kotlin.math.sqrt((dx*dx + dy*dy).toDouble())
      if (0.3 * image_diag > D) { // @TODO Reconsider Max Value
        return true
      }
    }
    return false
  }

  private fun mergeContours(contours : MutableList<MatOfPoint>): MutableList<MatOfPoint> {
    val pointsList : MutableList<Point> = mutableListOf()
    for (ci in contours) {
      for (point in ci.toArray()) {
        pointsList.add(point)
      }
    }
    val combinedContour : MatOfPoint = MatOfPoint()
    combinedContour.fromList(pointsList)
    val mergedContours : MutableList<MatOfPoint> = mutableListOf(combinedContour)
    return mergedContours
  }

  private fun hullify(contours : MutableList<MatOfPoint>): MutableList<MatOfPoint> {
    val nContours : MutableList<MatOfPoint> = mutableListOf()
    var hull : MatOfInt = MatOfInt()
    var hullPoints : MatOfPoint;
    for (ci in contours) {
      Imgproc.convexHull(ci, hull)
      hullPoints = convertIndicesToPoint(ci, hull)
      nContours.add(hullPoints)
    }
    return nContours
  }

  private fun convertIndicesToPoint(contour : MatOfPoint, hull : MatOfInt) : MatOfPoint {
    val arrIndex : IntArray = hull.toArray();
    val arrContour : Array<Point> = contour.toArray()
    val arrPoints : Array<Point> = Array(arrIndex.size) {Point()}

    val last = arrIndex.size - 1
    for (i in 0..last) {
      arrPoints[i] = arrContour[arrIndex[i]]
    }

    var hullOfPoints : MatOfPoint = MatOfPoint();
    hullOfPoints.fromArray(*arrPoints);
    return hullOfPoints;
  }

  private fun convertToPoint2f(hull : MutableList<MatOfPoint>) :  MutableList<MatOfPoint2f> {
    val newHulls : MutableList<MatOfPoint2f> = mutableListOf()
    var newPoint : MatOfPoint2f
    for (point in hull) {
      newPoint = MatOfPoint2f(*point.toArray());
      newHulls.add(newPoint);
    }
    return newHulls
  }
}