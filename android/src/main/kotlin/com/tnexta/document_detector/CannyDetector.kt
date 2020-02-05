package com.tnexta.document_detector

import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.core.Size
import org.opencv.core.CvType
import org.opencv.core.Core
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Scalar
import org.opencv.core.Point
import org.opencv.imgproc.Moments
import org.opencv.core.RotatedRect
import org.opencv.core.MatOfInt

object CannyDetector {
  fun getEdges(image: Mat): Mat {
    val processedImage : Mat = processImageForContourDetection(image)
    val contours : MutableList<MatOfPoint> = getRelevantContours(processedImage)
    val hull : MutableList<MatOfPoint2f> = hullify(contours)
    val rectangle : RotatedRect = Imgproc.minAreaRect(hull[0])
    var vertices : Array<Point> = arrayOf()
    rectangle.points(vertices)

    for (i in 1..4) {
      // line(image, vertices[i], vertices[(i+1)%4], Scalar(0,255,0), 2);
      Imgproc.line(image, vertices[i], vertices[(i+1)%4], Scalar(0.0, 0.0, 255.0), 2);
    }
    return image
  }

  private fun processImageForContourDetection(image: Mat) : Mat {
    var dst : Mat = Mat()

    Imgproc.cvtColor(image, dst, Imgproc.COLOR_BGR2GRAY)
    var noShadow : Mat = removeShadows(dst);
    Imgproc.GaussianBlur(noShadow, dst, Size(7.0, 7.0), 1.4, 1.4)
    Imgproc.GaussianBlur(dst, dst, Size(9.0, 9.0), 1.4, 1.4)
    Imgproc.adaptiveThreshold(dst, dst, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2.0)
    // dst = cv.Canny(thresh, 50, 200, None, 3)
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
    Core.absdiff(image, dst, dst)
    Core.multiply(dst, Scalar(-1.0), dst)
    Core.add(dst, Scalar(255.0), dst);
    Core.normalize(dst, dst, 0.0, 255.0, Core.NORM_MINMAX, CvType.CV_8UC1)
    Imgproc.threshold(dst, dst, 230.0, 0.0, Imgproc.THRESH_TRUNC)
    Core.normalize(dst, dst, 0.0, 255.0, Core.NORM_MINMAX, CvType.CV_8UC1)
    return dst
  }

  private fun getRelevantContours(image: Mat) : MutableList<MatOfPoint> {
    var contours : MutableList<MatOfPoint> = mutableListOf()
    var hierarchy : Mat = Mat()
    Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)
    val contourComparator = ContourComparator()
    contours.sortWith(contourComparator.reversed())
    val image_area : Double = image.size().area()
    contours = filterContoursBySize(contours, image_area)
    contours = filterContoursByDistance(contours, image)
    contours = mergeContours(contours)
    return contours
  }

  private fun filterContoursBySize(contours : MutableList<MatOfPoint>, image_area : Double): MutableList<MatOfPoint> {
    val nContours : MutableList<MatOfPoint> = mutableListOf()
    for (ci in contours) {
      val contourArea : Double = Imgproc.contourArea(ci)
      if (contourArea > 10000.0 && 0.9 * image_area > contourArea) {
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
      if (0.3 * image_diag > D) {
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
    // def mergeContours(contours):
    //   contour = np.vstack(contours[i] for i in range(len(contours)))
    //   return [contour]
  }

  private fun hullify(contours : MutableList<MatOfPoint>): MutableList<MatOfPoint2f> {
    val nContours : MutableList<MatOfPoint2f> = mutableListOf()
    var hull : MatOfInt = MatOfInt()
    var hullPoints : MatOfPoint2f;
    for (ci in contours) {
      Imgproc.convexHull(ci, hull)
      hullPoints = convertIndicesToPoint2f(ci, hull)
      nContours.add(hullPoints)
    }
    return nContours
  }

  private fun convertIndicesToPoint2f(contour : MatOfPoint, hull : MatOfInt) : MatOfPoint2f {
    val arrIndex : IntArray = hull.toArray();
    val arrContour : Array<Point> = contour.toArray()
    val arrPoints : Array<Point> = Array(arrIndex.size) {Point()}

    for (i in 0..arrIndex.size-1) {
      arrPoints[i] = arrContour[arrIndex[i]]
    }

    var hullOfPoints : MatOfPoint2f = MatOfPoint2f();
    hullOfPoints.fromArray(*arrPoints);
    return hullOfPoints;
  }
}