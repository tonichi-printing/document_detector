import CoreGraphics
import Flutter
import UIKit
import Vision

public class SwiftDocumentDetectorPlugin: NSObject, FlutterPlugin {
  enum ProcessingError: Error {
    case cantCropImage
    case cantSave
    case cantCreateFilter
  }

  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "document_detector", binaryMessenger: registrar.messenger())
    let instance = SwiftDocumentDetectorPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    guard call.method == "detectDocument" else {
      return result(FlutterMethodNotImplemented)
    }

    // Get Path
    let args = call.arguments as! [String: Any]
    let imagePath = args["imagePath"] as? String ?? ""
    if imagePath.isEmpty {
      return result("noImagePathProvided")
    }
    if #available(iOS 11.0, *) {
      return detectCard(result: result, imagePath: imagePath)
    } else {
      return result("iOS not Supported")
    }
  }

  @available(iOS 11.0, *)
  private func detectCard(result: @escaping FlutterResult, imagePath: String) {
    let url = URL(fileURLWithPath: imagePath)
    let imageRequestHandler = VNImageRequestHandler(url: url, options: [:])

    let rectDetectRequest = VNDetectRectanglesRequest()
    // Customize & configure the request to detect only certain rectangles.
    rectDetectRequest.maximumObservations = 1 // Vision currently supports up to 16.
    rectDetectRequest.minimumConfidence = 0.6 // Be confident.
    rectDetectRequest.maximumAspectRatio = 1 // height / width

    do {
      try imageRequestHandler.perform([rectDetectRequest])
    } catch let error as NSError {
      return result("Failed to perform image request: \(error)")
    }

    guard let rects = rectDetectRequest.results as? [VNRectangleObservation] else { return result("failed") }
    guard let rect = rects.first else { return result("Failed to get Rectangle") }

    do {
      let croppedImage = try cropAsPerRect(rect: rect, url: url)
      try saveImage(image: croppedImage, url: url)
      result(imagePath)
    } catch { return result("Couldn't Crop or Save") }
  }

  private func cropAsPerRect(rect: VNRectangleObservation, url: URL) throws -> UIImage {
    guard let filter = CIFilter(name: "CIPerspectiveCorrection") else {
      throw ProcessingError.cantCreateFilter
    }

    let data = try! Data(contentsOf: url)
    let image = UIImage(data: data)!
    let width = image.size.width
    let height = image.size.height

    var transform: CGAffineTransform
    if UIApplication.shared.statusBarOrientation.isLandscape {
      transform = CGAffineTransform.identity
        .scaledBy(x: -1, y: 1)
        .translatedBy(x: -width, y: 0)
        .scaledBy(x: width, y: height)
    } else {
      transform = CGAffineTransform.identity
        .scaledBy(x: 1, y: -1)
        .translatedBy(x: 0, y: -height)
        .scaledBy(x: width, y: height)
    }

    let topLeft = rect.topLeft.applying(transform)
    let topRight = rect.topRight.applying(transform)
    let bottomLeft = rect.bottomLeft.applying(transform)
    let bottomRight = rect.bottomRight.applying(transform)

    filter.setValue(CIVector(cgPoint: topLeft), forKey: "inputTopLeft")
    filter.setValue(CIVector(cgPoint: topRight), forKey: "inputTopRight")
    filter.setValue(CIVector(cgPoint: bottomLeft), forKey: "inputBottomLeft")
    filter.setValue(CIVector(cgPoint: bottomRight), forKey: "inputBottomRight")

    let ciImage = CIImage(image: image)!.oriented(.up)
    filter.setValue(ciImage, forKey: kCIInputImageKey)
    guard let perspectiveImage: CIImage = filter.value(forKey: kCIOutputImageKey) as? CIImage else { throw ProcessingError.cantCropImage }

    print([topLeft.x, topLeft.y, bottomRight.x, bottomRight.y], separator: ", ", terminator: ";")
    return UIImage(ciImage: perspectiveImage, scale: -1.0, orientation: .leftMirrored)
  }

  private func saveImage(image: UIImage, url: URL) throws {
    guard let data = image.pngData() else { throw ProcessingError.cantSave }
    try data.write(to: url)
  }
}
