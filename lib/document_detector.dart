import 'dart:async';

import 'package:flutter/services.dart';

class DocumentDetector {
  static const MethodChannel _channel =
      const MethodChannel('document_detector');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
