package com.tnexta.document_detector

import org.bytedeco.opencv.opencv_dnn.LayerFactory.Constructor
import org.bytedeco.opencv.opencv_dnn.MatShapeVector
import org.bytedeco.javacpp.IntPointer
import org.bytedeco.opencv.opencv_core.MatVector
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Range

class CropLayer : Constructor() {
  var xstart : Int = 0;
  var xend : Int = 0;
  var ystart : Int = 0;
  var yend : Int = 0;

  fun constructor() {
    this.xstart = 0
    this.xend = 0
    this.ystart = 0
    this.yend = 0
  }

  fun getMemoryShapes(inputs : MatShapeVector, requiredOutputs : Int, outputs : MatShapeVector, internals : MatShapeVector) : Boolean {
    val inputShape : IntPointer = inputs[0]
    val targetShape : IntPointer = inputs[1]

    val batchSize : Int = inputShape[0]
    val numChannels : Int = inputShape[1]

    val height : Int = targetShape[2]
    val width : Int = targetShape[3]

    this.ystart = (inputShape[2] - targetShape[2]) // 2
    this.xstart = (inputShape[3] - targetShape[3]) // 2
    this.yend = this.ystart + height
    this.xend = this.xstart + width

    val info : IntPointer = IntPointer(batchSize, numChannels, height, width)
    outputs.clear()
    outputs.put(info)

    return true
    // return [[batchSize, numChannels, height, width]]
  }

  fun forward(inputs : MatVector, outputs : MatVector, internals : MatVector) {
    val first : Mat = inputs[0]
    print((first.dims()).toString())

    // val B : Mat = first.col(1)
    // val G : Mat = first.col(2)
    // val R : Mat = first.col(3)
    // val newR = R[Range(this.ystart, this.yend), Range(this.xstart, this.xend)]
    // Mat(
    //   3, first[0], first[1], first[this.ystart:this.yend, this.xstart:this.xend]
    // )
    // val output : Mat = first(Range::all(), Range::all(), this.ystart:this.yend, this.xstart:this.xend)
    // val output : Mat = Mat();
    outputs.clear()
    outputs.put(first)
  }
  //     class CropLayer(object):
  //     def __init__(self, params, blobs):
  //         self.xstart = 0
  //         self.xend = 0
  //         self.ystart = 0
  //         self.yend = 0

  //     # Our layer receives two inputs. We need to crop the first input blob
  //     # to match a shape of the second one (keeping batch size and number of channels)
  //     def getMemoryShapes(self, inputs):
  //         inputShape, targetShape = inputs[0], inputs[1]
  //         batchSize, numChannels = inputShape[0], inputShape[1]
  //         height, width = targetShape[2], targetShape[3]

  //         self.ystart = (inputShape[2] - targetShape[2]) // 2
  //         self.xstart = (inputShape[3] - targetShape[3]) // 2
  //         self.yend = self.ystart + height
  //         self.xend = self.xstart + width

  //         return [[batchSize, numChannels, height, width]]

  //     def forward(self, inputs):
  //         return [inputs[0][:,:,self.ystart:self.yend,self.xstart:self.xend]]
}