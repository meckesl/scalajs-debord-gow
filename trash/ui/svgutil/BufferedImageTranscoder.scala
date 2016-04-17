package com.lms.gow.ui.svgutil

import java.awt.image.BufferedImage

import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.ImageTranscoder

class BufferedImageTranscoder extends ImageTranscoder {

  var img: BufferedImage = _

  @Override
  def createImage(width: Int, height: Int): BufferedImage = {
    new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
  }

  @Override
  def writeImage(img: BufferedImage, to: TranscoderOutput) {
    this.img = img;
  }

  def getBufferedImage: BufferedImage = img

}
