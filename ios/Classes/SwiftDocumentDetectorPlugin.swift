import Flutter
import UIKit

public class SwiftDocumentDetectorPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "document_detector", binaryMessenger: registrar.messenger())
    let instance = SwiftDocumentDetectorPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    guard call.method == "detectDocument" else {
      return result(FlutterMethodNotImplemented)
    }
    var imagePath : String = "";
    if let args = call.arguments as? [String: Any] {
      imagePath = args["imagePath"] as? String ?? ""
    }
    // if (imagePath == '') {
    //   return result("noImagePathProvided")
    // }
    return result(imagePath)

    // let image    = UIImage(contentsOfFile: imageURL.path)
    // let imageRequestHandler = VNImageRequestHandler(cgImage: image,
    //                                                 orientation: orientation,
    //                                                 options: [:])
    // lazy var rectangleDetectionRequest: VNDetectRectanglesRequest = {
    //   let rectDetectRequest = VNDetectRectanglesRequest(completionHandler: self.handleDetectedRectangles)
    //   // Customize & configure the request to detect only certain rectangles.
    //   rectDetectRequest.maximumObservations = 8 // Vision currently supports up to 16.
    //   rectDetectRequest.minimumConfidence = 0.6 // Be confident.
    //   rectDetectRequest.minimumAspectRatio = 0.5 // height / width
    //   return rectDetectRequest
    // }()

    // do {
    //   try imageRequestHandler.perform(requests)
    // } catch let error as NSError {
    //   print("Failed to perform image request: \(error)")
    //   self.presentAlert("Image Request Failed", error: error)
    //   return
    // }

    // guard let drawLayer = self.pathLayer,
    //     let results = request?.results as? [VNFaceObservation] else {
    //         return
    // }
    // self.draw(faces: results, onImageWithBounds: drawLayer.bounds)
    // drawLayer.setNeedsDisplay()

    // result("iOS " + UIDevice.current.systemVersion)
    // return
  }
}
