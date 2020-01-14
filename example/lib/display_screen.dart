import 'package:flutter/material.dart';
import 'dart:io';

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
        child: Image.file(File(documentCoordinates)),
      ),
    );
  }
}
