package controllers

import com.google.common.io.BaseEncoding
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.ByteMatrix
import com.google.zxing.qrcode.encoder.{Encoder, QRCode}

import scala.collection.JavaConverters._

import java.awt.image.BufferedImage
import java.awt.Image
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

object QREncoder {

  def encode(contents: String, hints: Map[EncodeHintType, _] = Map()) = {
    require(contents.length != 0, "contents can't be empty")

    val ecLevelAny = hints.getOrElse(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L)
    val ecLevel = ecLevelAny.asInstanceOf[ErrorCorrectionLevel]

    Encoder.encode(contents, ecLevel, hints.asInstanceOf[Map[EncodeHintType, Any]].asJava)
  }

  def render(qrCode: QRCode, hints: Map[EncodeHintType, _], size: Int): String = {
    val input = qrCode.getMatrix
    require(input != null)
    require(input.getWidth == input.getHeight)

    val quietZoneAny = hints.getOrElse(EncodeHintType.MARGIN, 4)
    val quietZone = quietZoneAny.asInstanceOf[Int]

    val bitMatrix = toBitMatrix(input, quietZone = quietZone)

    val bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)
    val scaledImage = bufferedImage.getScaledInstance(size, size, Image.SCALE_FAST)

    val bi = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null), BufferedImage.TYPE_BYTE_BINARY)

    val g = bi.createGraphics()
    g.drawImage(scaledImage, 0, 0, null)
    g.dispose()

    val byteArrayOutputStream = new ByteArrayOutputStream()
    ImageIO.write(bi, "png", byteArrayOutputStream)
    val bytes = byteArrayOutputStream.toByteArray

    BaseEncoding.base64().encode(bytes)
  }

  private[this] def toBitMatrix(input: ByteMatrix, quietZone: Int) = {
    val inputSize = input.getWidth
    val outputSize = inputSize + quietZone * 2
    val output = new BitMatrix(outputSize, outputSize)

    var inputY = 0
    var outputY = quietZone
    while (inputY < inputSize) {
      var inputX = 0
      var outputX = quietZone
      while (inputX < inputSize) {
        if (input.get(inputX, inputY) == 1) {
          output.setRegion(outputX, outputY, 1, 1)
        }
        inputX += 1
        outputX += 1
      }
      inputY += 1
      outputY += 1
    }

    output
  }
}
