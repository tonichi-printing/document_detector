import 'package:flutter/material.dart';
import 'package:camera/camera.dart';
import 'package:document_detector_example/take_picture_screen.dart';

class CameraScreen extends StatefulWidget {
  static const routeName = 'camera';

  @override
  _CameraScreenState createState() => _CameraScreenState();
}

class _CameraScreenState extends State<CameraScreen> {
  @override
  void initState() {
    super.initState();

    // Obtain a list of the available cameras on the device.
    _cameras = availableCameras();
  }

  Future _cameras;

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: _cameras,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.done) {
          if (snapshot.data.isEmpty) {
            return Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: <Widget>[
                Icon(
                  Icons.error_outline,
                  color: Colors.red,
                  size: 60,
                ),
                Padding(
                  padding: const EdgeInsets.all(16),
                  child: Text('Error: No Camera Found'),
                )
              ],
            );
          }
          final firstCamera = snapshot.data.first;
          return TakePictureScreen(
            camera: firstCamera,
          );
        } else {
          // Otherwise, display a loading indicator.
          return Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: <Widget>[
                CircularProgressIndicator(),
              ],
            );
        }
      },
    );
  }
}
