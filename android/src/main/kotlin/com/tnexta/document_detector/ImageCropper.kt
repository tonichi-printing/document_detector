package com.tnexta.document_detector

import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect

object ImageCropper {
  fun crop(rectVer : Array<Point>, image: Mat): Mat {
            // rect is the RotatedRect (I got it from a contour...)
            RotatedRect rect;
            // matrices we'll use
            Mat M, rotated, cropped;
            // get angle and size from the bounding box
            float angle = rect.angle;
            Size rect_size = rect.size;
            // thanks to http://felix.abecassis.me/2011/10/opencv-rotation-deskewing/
            if (rect.angle < -45.) {
                angle += 90.0;
                swap(rect_size.width, rect_size.height);
            }
            // get the rotation matrix
            M = getRotationMatrix2D(rect.center, angle, 1.0);
            // perform the affine transformation
            warpAffine(src, rotated, M, src.size(), INTER_CUBIC);
            // crop the resulting image
            getRectSubPix(rotated, rect_size, rect.center, cropped);
    val rect = Rect(rectVer[0].x, rectVer[0].y , (rectVer[3].x - rectVer[0].x + 1), (rectVer[3].y - p1.y+1))
    return image
  }
}