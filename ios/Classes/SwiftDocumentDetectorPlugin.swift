import Flutter
import UIKit
import Vision

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

    // Get Path
    let args = call.arguments as! [String: Any]
    let imagePath = args["imagePath"] as? String ?? ""
    if imagePath.isEmpty {
      return result("noImagePathProvided")
    }

    let url = URL(fileURLWithPath: imagePath)
        if #available(iOS 11.0, *) {
            return detectCard(url: url, result: result)
        } else {
            return result("iOS not Supported")
        }

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

    @available(iOS 11.0, *)
    func detectCard(url: URL, result: @escaping FlutterResult) {
        let imageRequestHandler = VNImageRequestHandler(url: url,
        options: [:])

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
    
        guard let rects = rectDetectRequest.results as? [VNRectangleObservation] else { return result("failed1") }
        print("size of numbers is : \(rects.count)")
        guard let rect = rects.first else{return result("failed2")}
        return result("suceesssss")
        return result(rect.boundingBox)
    }
}
