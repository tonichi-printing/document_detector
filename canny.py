import numpy as np
import cv2 as cv
import argparse

parser = argparse.ArgumentParser(
        description='This file attempts to detect edges of a document in the given image. '
                    'Canny edge detection is used.')
parser.add_argument('--input', help='Path to image. Compulsory!')
args = parser.parse_args()

img = cv.imread(args.input)
cv.imshow('orig', img)
cv.waitKey()

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


def remShad(img):
  dilated_img = cv.dilate(img, np.ones((7,7), np.uint8))
  bg_img = cv.medianBlur(dilated_img, 21)
  diff_img = 255 - cv.absdiff(img, bg_img)
  norm_img = cv.normalize(diff_img, None, alpha=0, beta=255, norm_type=cv.NORM_MINMAX, dtype=cv.CV_8UC1)
  _, thr_img = cv.threshold(norm_img, 230, 0, cv.THRESH_TRUNC)
  # thr_img = cv.adaptiveThreshold(norm_img,255,cv.ADAPTIVE_THRESH_GAUSSIAN_C,cv.THRESH_BINARY,11,2)
  result_img = cv.normalize(thr_img, None, alpha=0, beta=255, norm_type=cv.NORM_MINMAX, dtype=cv.CV_8UC1)
  return result_img

gray = cv.cvtColor(img,cv.COLOR_BGR2GRAY)

gray = remShad(gray)
cv.imshow('remshad', cv.cvtColor(gray, cv.COLOR_GRAY2RGB))
cv.waitKey()


# gray = cv.equalizeHist(gray)
# cv.imshow('eqhist', gray)
# cv.waitKey()

gray = cv.GaussianBlur(gray, (7, 7), 0)
invGamma = 1.0 / 0.3
table = np.array([((i / 255.0) ** invGamma) * 255
for i in np.arange(0, 256)]).astype("uint8")

# apply gamma correction using the lookup table
# gray = cv.LUT(gray, table)
cv.imshow('lut', gray)
cv.waitKey()

gray = cv.GaussianBlur(gray, (9, 9), 0)

ret,thresh1 = cv.threshold(gray,0,255,cv.THRESH_BINARY+cv.THRESH_OTSU)
cv.imshow('otsu', thresh1)
cv.waitKey()

thresh = cv.adaptiveThreshold(gray,255,cv.ADAPTIVE_THRESH_GAUSSIAN_C,cv.THRESH_BINARY,11,2)
cv.imshow('adap', thresh)
cv.waitKey()

# dst = cv.GaussianBlur(thresh, (9, 9), 0)
# cv.imshow('blur', dst)
# cv.waitKey()

dst = cv.Canny(thresh, 50, 200, None, 3)
cv.imshow('canny', dst)
cv.waitKey()

dst = cv.dilate(dst, np.ones((13,13), np.uint8))
cv.imshow('dilated', dst)
cv.waitKey()

# lines = cv.HoughLines(dst, 1, np.pi / 180, 150, None, 0, 0)
# ht = cv.cvtColor(dst, cv.COLOR_GRAY2BGR)
# if lines is not None:
#   for i in range(0, len(lines)):
#     rho = lines[i][0][0]
#     theta = lines[i][0][1]
#     a = math.cos(theta)
#     b = math.sin(theta)
#     x0 = a * rho
#     y0 = b * rho
#     pt1 = (int(x0 + 1000*(-b)), int(y0 + 1000*(a)))
#     pt2 = (int(x0 - 1000*(-b)), int(y0 - 1000*(a)))
#     cv.line(ht, pt1, pt2, (0,0,255), 3, cv.LINE_AA)

# cv.imshow('result', ht)
# cv.waitKey()


contours, hierarchy = cv.findContours(dst, cv.RETR_TREE, cv.CHAIN_APPROX_SIMPLE)

def filterContoursBySize(contours, img_area):
  nContours = []
  for index in range(len(contours)):
    ci = contours[index]
    contourArea = cv.contourArea(ci)
    if contourArea > 10000 and contourArea < 0.9 * img_area:
      nContours.append(ci)
  return nContours

def isWithinAcceptableDistance(center, centers, shape):
  img_diag = np.sqrt(shape[0]**2 + shape[1]**2)
  for c in centers:
    dx = c[0] - center[0]
    dy = c[1] - center[1]
    D = np.sqrt(dx*dx + dy*dy)
    if D < 0.3 * img_diag:
      return True
  return False

def filterContoursByDistance(contours, shape):
  centers=[]
  nContours = []
  for index in range(len(contours)):
    ci = contours[index]
    M = cv.moments(ci)
    cX = int(M['m10'] /M['m00'])
    cY = int(M['m01'] /M['m00'])
    center = [cX, cY]
    if len(centers) == 0 or isWithinAcceptableDistance(center, centers, shape):
      nContours.append(ci)
      centers.append(center)
  return nContours

def mergeContours(contours):
  contour = np.vstack(contours[i] for i in range(len(contours)))
  return [contour]


def hullify(contours):
  nContours = []
  for index in range(len(contours)):
    ci = contours[index]
    hull = cv.convexHull(ci)
    nContours.append(hull)
  return nContours

contours = sorted(contours, key=cv.contourArea, reverse=True)
img_area = dst.shape[0]*dst.shape[1]
contours = filterContoursBySize(contours, img_area)
contours = filterContoursByDistance(contours, dst.shape)
contours = mergeContours(contours)
hull = hullify(contours)


image = cv.drawContours(img.copy(), hull, -1, (0,0,255),3)
image = cv.drawContours(image, contours, -1, (255,0,0),3)

rect = cv.minAreaRect(hull[0])
box = cv.boxPoints(rect)
box = np.int0(box)
image = cv.drawContours(image, [box], 0, (0,255,0), 2)

cv.imshow('Result', image)
cv.waitKey()
