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
    rectDetectRequest.maximumObservations = 8 // Vision currently supports up to 16.
    rectDetectRequest.minimumConfidence = 0.6 // Be confident.
    rectDetectRequest.minimumAspectRatio = 0.5 // height / width

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
    let topLeft = CGPoint(x: rect.topLeft.x * width, y: rect.topLeft.y * height)
    let topRight = CGPoint(x: rect.topRight.x * width, y: rect.topRight.y * height)
    let bottomLeft = CGPoint(x: rect.bottomLeft.x * width, y: rect.bottomLeft.y * height)
    let bottomRight = CGPoint(x: rect.bottomRight.x * width, y: rect.bottomRight.y * height)

    filter.setValue(CIVector(cgPoint: topLeft), forKey: "inputTopLeft")
    filter.setValue(CIVector(cgPoint: topRight), forKey: "inputTopRight")
    filter.setValue(CIVector(cgPoint: bottomLeft), forKey: "inputBottomLeft")
    filter.setValue(CIVector(cgPoint: bottomRight), forKey: "inputBottomRight")

    let ciImage = CIImage(image: image)!.oriented(.up)
    filter.setValue(ciImage, forKey: kCIInputImageKey)
    guard let perspectiveImage: CIImage = filter.value(forKey: kCIOutputImageKey) as? CIImage else { throw ProcessingError.cantCropImage
    }
//    let data = try! Data(contentsOf: url)
//    let image = UIImage(data: data)!
//    guard let croppedCGImage: CGImage = image.cgImage?.cropping(to: CGRect(x: rect.minX * image.size.width, y: rect.minY * image.size.height, width: rect.width * image.size.width, height: rect.height * image.size.height))
//    else {
//      throw ProcessingError.cantCropImage
//    }
//    print([rect.minX, rect.minY, rect.width, rect.height], separator: ", ", terminator: ";")
    return UIImage(ciImage: perspectiveImage)
  }

  private func saveImage(image: UIImage, url: URL) throws {
    guard let data = image.pngData() else { throw ProcessingError.cantSave }
    try data.write(to: url)
  }
}
