package com.tnexta.document_detector

import org.opencv.core.Mat
import org.opencv.dnn.Dnn.blobFromImage
import org.opencv.dnn.Dnn.readNetFromCaffe
import org.opencv.dnn.Net
import org.bytedeco.opencv.opencv_dnn.LayerFactory
import java.nio.file.Paths


object HedDetector {
  fun hed(image: Mat): Mat {
    val net : Net = this.initializeHedNet()
    val input : Mat = blobFromImage(image)
    net.setInput(input)
    var output : Mat = net.forward()
    output = this.processOutput(output)
    return output
  }

  fun initializeHedNet() : Net {
    LayerFactory.registerLayer("Crop", CropLayer())
    var net : Net = readNetFromCaffe("deploy.prototxt.txt", "/hed_pretrained_bsds.caffemodel")
    return net
  }

  fun processOutput(output : Mat) : Mat {
    // out = out[0, 0]
    // out = cv.resize(out, (frame.shape[1], frame.shape[0]))
    // out = 255 * out
    // out = out.astype(np.uint8)
    // out=cv.cvtColor(out,cv.COLOR_GRAY2BGR)
    // con=np.concatenate((frame,out),axis=1)
    return output
  }
}