import 'dart:async';

import 'package:flutter/services.dart';

class DocumentDetector {
  static const MethodChannel _channel =
      const MethodChannel('document_detector');

  static Future<String> detect(String imagePath) async {
    final String documentCooridnates = await _channel.invokeMethod('detectDocument', <String, String>{
      'imagePath': imagePath,
    });
    return documentCooridnates;
  }
}
