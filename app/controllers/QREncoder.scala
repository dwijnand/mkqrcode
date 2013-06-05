package controllers

import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.{Encoder, QRCode}
import com.google.zxing.common.BitMatrix

import scala.collection.JavaConverters._

object QREncoder {
  def encode(contents: String, hints: Map[EncodeHintType, _] = Map()) = {
    require(contents.length != 0, "Found empty contents")

    val ecLevelAny = hints.getOrElse(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L)
    val ecLevel = ecLevelAny.asInstanceOf[ErrorCorrectionLevel]

    Encoder.encode(contents, ecLevel, hints.asInstanceOf[Map[EncodeHintType, Any]].asJava)
  }

  def render(qrCode: QRCode, width: Int, height: Int, hints: Map[EncodeHintType, _] = Map()) = {
    require(width > 0 || height > 0, "Requested dimensions are too small: " + width + 'x' + height)
    val input = qrCode.getMatrix
    require(input != null)

    val quietZoneAny = hints.getOrElse(EncodeHintType.MARGIN, 4)
    val quietZone = quietZoneAny.asInstanceOf[Int]

    val inputWidth = input.getWidth
    val inputHeight = input.getHeight
    val qrWidth = inputWidth + (quietZone << 1)
    val qrHeight = inputHeight + (quietZone << 1)
    val outputWidth = Math.max(width, qrWidth)
    val outputHeight = Math.max(height, qrHeight)

    val multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight)
    val leftPadding = (outputWidth - (inputWidth * multiple)) / 2
    val topPadding = (outputHeight - (inputHeight * multiple)) / 2

    val output = new BitMatrix(outputWidth, outputHeight)

    var inputY = 0
    var outputY = topPadding
    while (inputY < inputHeight) {
      var inputX = 0
      var outputX = leftPadding
      while (inputX < inputWidth) {
        if (input.get(inputX, inputY) == 1) {
          output.setRegion(outputX, outputY, multiple, multiple)
        }
        inputX += 1
        outputX += multiple
      }
      inputY += 1
      outputY += multiple
    }

    output
  }
}
