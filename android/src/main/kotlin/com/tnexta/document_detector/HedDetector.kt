package com.tnexta.document_detector

import org.opencv.core.Mat
import org.opencv.dnn.Dnn.blobFromImage
import org.opencv.dnn.Dnn.readNetFromCaffe
import org.opencv.dnn.Net
import org.bytedeco.opencv.opencv_dnn.LayerFactory
import java.nio.file.Paths
import io.flutter.plugin.common.PluginRegistry.Registrar

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
    val prototext = "dnn/deploy.prototxt.txt";
    val model = "dnn/hed_pretrained_bsds.caffemodel";
    var net : Net = readNetFromCaffe(prototext, model)
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