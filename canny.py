import numpy as np
import cv2 as cv
import argparse

parser = argparse.ArgumentParser(
        description='This sample shows how to define custom OpenCV deep learning layers in Python. '
                    'Canny edge detection is used.')
parser.add_argument('--input', help='Path to image or video. Skip to capture frames from camera')
args = parser.parse_args()

cap = cv.VideoCapture(args.input)
hasFrame, image = cap.read()


#######################################
# Refer: https://stackoverflow.com/a/44752405/6402452
def removeShadows(image):
  rgb_planes = cv.split(image)

  result_norm_planes = []
  for plane in rgb_planes:
    dilated_img = cv.dilate(plane, np.ones((7,7), np.uint8))
    bg_img = cv.medianBlur(dilated_img, 21)
    diff_img = 255 - cv.absdiff(plane, bg_img)
    norm_img = cv.normalize(diff_img,None, alpha=0, beta=255, norm_type=cv.NORM_MINMAX, dtype=cv.CV_8UC1)
    result_norm_planes.append(norm_img)

  result_norm = cv.merge(result_norm_planes)
  return result_norm

#######################################

dist = removeShadows(image)
# _, dist = cv.threshold(dist, 230, 0, cv.THRESH_TRUNC)
# cv.normalize(dist, dist, alpha=0, beta=255, norm_type=cv.NORM_MINMAX, dtype=cv.CV_8UC1)
dist = cv.cvtColor(dist, cv.COLOR_BGR2GRAY)
# Do not threshold the image first as the shadows will hide that area
dist = cv.GaussianBlur(dist, (3, 3), 1.4, 1.4)
# dist = cv.Canny(dist, 75, 200)
# _, dist = cv.threshold(dist, 0, 255, cv.THRESH_BINARY | cv.THRESH_OTSU)
# cv.normalize(dist, dist, alpha=0, beta=1.0, norm_type=cv.NORM_MINMAX, dtype=cv.CV_32F)
# dist = cv.distanceTransform(dist, cv.DIST_L2, 3)
# _, dist = cv.threshold(dist, 0.4, 1, cv.THRESH_BINARY)
# kernel1 = np.ones((3,3), dtype=np.uint8)
# dist = cv.dilate(dist, kernel1)
cv.imwrite('op-' + args.input, dist)
# _, contours = cv.findContours(dist.astype('uint8'), cv.RETR_EXTERNAL, cv.CHAIN_APPROX_SIMPLE)
# # Create the marker image for the watershed algorithm
# markers = np.zeros(dist.shape, dtype=np.int32)
# # Draw the foreground markers
# for i in range(len(contours)):
#     cv.drawContours(markers, contours, i, (i+1), -1)
# # Draw the background marker
# cv.circle(markers, (5,5), 3, (255,255,255), -1)
# cv.imshow('Markers', markers*10000)
