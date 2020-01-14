package com.tnexta.document_detector

import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

import org.bytedeco.javacpp.Loader
import org.opencv.core.Mat

/** DocumentDetectorPlugin */
public class DocumentDetectorPlugin: FlutterPlugin, MethodCallHandler {
  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    val channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "document_detector")
    channel.setMethodCallHandler(DocumentDetectorPlugin());
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "document_detector")
      channel.setMethodCallHandler(DocumentDetectorPlugin())
    }
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "detectDocument") {
      Loader.load(org.bytedeco.opencv.opencv_java::class.java)

      detectDocument(call, result)
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
  }

  fun detectDocument(@NonNull call: MethodCall, @NonNull result: Result) {
    var imagePath : String? = call.argument("imagePath")
    if (imagePath == null) {
      result.error("No image path provided", null, null)
    }
    // result.success(org.opencv.core.Core.getBuildInformation());
    // Reference: https://github.com/legolas123/cv-tricks.com/blob/master/OpenCV/Edge_detection/edge.py
    try {
      val image : Mat = ImageLoader.load(imagePath.toString());
      result.success((image.empty()).toString())
  } catch (e: IllegalArgumentException) {
      result.error(e.message, null, null)
    }
    // result.success((image.dims()).toString())
    // val documentCoordinates = CannyDetector.hed(image)
    // result.success((documentCoordinates.dims()).toString())
  }
}
