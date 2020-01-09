import 'package:flutter/material.dart';

class DisplayScreen extends StatelessWidget {
  final String documentCoordinates;

  DisplayScreen({this.documentCoordinates});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
          icon: Icon(Icons.close),
          onPressed: () {
            Navigator.of(context).pop();
          },
        ),
      ),
      body: Center(
        child: Text('Document Quadrilateral Coordinates: ' + documentCoordinates + '\n'),
      ),
    );
  }
}
